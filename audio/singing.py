import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))

import pretty_midi as pm
import numpy as np
import os.path as op
from subprocess import Popen, PIPE
import soundfile as sf
import pickle
from pyo import *
from librosa.beat import tempo as estimate_tempo
from librosa.effects import time_stretch, pitch_shift
import time
import random
import glob
import argparse


class Sample:
    def __init__(self, path, mul=1):
        PVSIZE = 1024
        PVOLAPS = 4

        self.path = path
        self.info = sndinfo(path)
        self.NUM_FRAMES = self.info[0]
        self.LENGTH = self.info[1]
        self.SR = self.info[2]
        self.snd_player = SfPlayer(self.path)

        self.pv_analysis = PVAnal(self.snd_player, size=PVSIZE, overlaps=PVOLAPS)
        self.speed_table = LinTable([(0, 1), (512, 1)], size=512)
        self.speed_object = PVBufTabLoops(self.pv_analysis, self.speed_table, length=self.LENGTH)
        self.trans_value = SigTo(1, time=0.005)
        self.trans_object = PVTranspose(self.speed_object, transpo=self.trans_value)
        self.adsr = Adsr(mul=mul)
        self.pv_synth = PVSynth(self.trans_object, mul=self.adsr)

    def play(self):
        self.speed_object.reset()
        self.pv_synth.out()
        self.adsr.play()

    def stop(self):
        self.pv_synth.stop()

    def set_transposition(self, val):
        self.trans_value.setValue(val)

    def set_speed(self, val):
        self.speed_table.replace([(0, val), (512, val)])


class MelodyExtraction:
    def __init__(self, path):
        self.path = path
        self.sound_data, self.sr = sf.read(self.path, always_2d=True)
        self.tempo = estimate_tempo(self.sound_data[:, 1], self.sr)[0] / 60
        self.length_samples = self.sound_data.shape[0]
        self.length_seconds = self.length_samples * (1 / self.sr)
        self.abs_path = op.abspath(self.path)
        self.name = "_".join(self.abs_path.split('/')[-1].split('.')[:-1])

        self.deep_learning_path = "/media/nvidia/disk1/Vocal-Melody-Extraction"
        self.deep_learning_model_path = op.join(self.deep_learning_path, "pretrained_models", "Seg")
        self.deep_learning_data = None
        self.deep_learning_timestamps = None

        self.run_melodia_path = "/home/nvidia/shimi/audio"
        self.melodia_data = None
        self.melodia_timestamps = None

    def deep_learning_extraction(self):
        if not op.exists(op.join("cnn_outputs", "cnn_" + self.name + ".txt")):
            command_string = "python3.5 " + op.join(self.deep_learning_path,
                                                    "VocalMelodyExtraction.py") + " --input_file " + self.abs_path + \
                             " -m " + self.deep_learning_model_path + " --output_file " + "cnn_" + self.name

            process = Popen(command_string.split(' '), stdout=PIPE, bufsize=1, universal_newlines=True)

            # Wait until the process is finished  to continue
            for line in process.stdout.readline():
                if line == "FINISHED":
                    print("Deep learning melody extraction complete.")
                    break

            mv_command_string = "mv cnn_" + self.name + ".txt cnn_outputs/"
            Popen(mv_command_string.split(' '))
            time.sleep(0.1)

        deep_learning_data = np.loadtxt(op.join("cnn_outputs", "cnn_" + self.name + ".txt"))
        self.deep_learning_data = deep_learning_data[:, 1]
        np.place(self.deep_learning_data, self.deep_learning_data <= 0, 0)
        num_points = self.deep_learning_data.shape[0]
        self.deep_learning_timestamps = [(i / num_points) * self.length_seconds for i in range(num_points)]
        self.deep_learning_data, self.deep_learning_timestamps = self.process_data(self.deep_learning_data,
                                                                                   self.deep_learning_timestamps)

    def melodia_extraction(self):
        if not op.exists(op.join("melodia_outputs", "melodia_" + self.name + ".p")):
            command_string = "exagear -- " + op.join(self.run_melodia_path,
                                                     "run_melodia.sh") + " " + self.abs_path + " " + self.name

            input("Please run the following in a new shell, and press enter when it is done:\n%s\n" % command_string)

        melodia_data = pickle.load(open(op.join("melodia_outputs", "melodia_" + self.name + ".p"), "rb"))
        self.melodia_data = melodia_data["frequencies"]
        self.melodia_timestamps = melodia_data["timestamps"]
        self.melodia_data, self.melodia_timestamps = self.process_data(self.melodia_data, self.melodia_timestamps)

    def process_data(self, melody_data, timestamps, fix_octaves=True, smooth_false_negatives=True,
                     remove_false_positives=True):
        data_len = len(melody_data)
        np.place(melody_data, melody_data <= 0, 0)  # Replace no VAD with 0

        if fix_octaves:
            octave_look_back = self.tempo * 4  # look back a certain amount of time to judge octave
            difference_tolerance = 2.2  # distance from running freq avg to consider octave error
            start_idx = 0
            end_idx = (np.abs(timestamps - octave_look_back)).argmin()

            num_octaves_dropped = 0
            num_octaves_raised = 0

            while end_idx < data_len:
                buffer_average_freq = np.average(np.where(melody_data[start_idx:end_idx] > 0))
                octave_ratio = melody_data[end_idx] / buffer_average_freq

                if octave_ratio > 0:
                    if octave_ratio > difference_tolerance:  # drop octaves too high
                        while octave_ratio > difference_tolerance:
                            octave_ratio = octave_ratio / 2
                            melody_data[end_idx] = melody_data[end_idx] / 2
                        num_octaves_dropped += 1
                    elif octave_ratio < (1 / difference_tolerance):  # boost octaves too low
                        while octave_ratio < (1 / difference_tolerance):
                            octave_ratio = octave_ratio * 2
                            melody_data[end_idx] = melody_data[end_idx] * 2
                        num_octaves_raised += 1

                start_idx += 1
                end_idx += 1

            print("Dropped %d notes an octave." % num_octaves_dropped)
            print("Raised %d notes an octave." % num_octaves_raised)

        if smooth_false_negatives:
            data_without_zeros = []  # get all the non-zero values to use for interpolation later
            timestamps_without_zeros = []

            for freq, timestamp in zip(melody_data, timestamps):
                if freq > 0:
                    data_without_zeros.append(freq)
                    timestamps_without_zeros.append(timestamp)
            vad_smoothing_delta = self.tempo / 12

            timestamps_to_interpolate = []
            num_false_negatives = 0
            start_idx = 0
            end_idx = 0
            while melody_data[start_idx] < 0:  # move to first vocalization
                start_idx += 1
                end_idx += 1
            while start_idx < data_len:  # look for gaps in VAD that seem to be false negatives
                while start_idx < data_len and melody_data[start_idx] > 0:
                    start_idx += 1
                    end_idx += 1
                while end_idx < data_len and melody_data[end_idx] <= 0:  # current frequency is 0
                    end_idx += 1
                if end_idx < data_len:  # current frequency is not 0
                    if timestamps[end_idx] - timestamps[start_idx] < vad_smoothing_delta:
                        num_false_negatives += 1
                        for t in range(start_idx, end_idx):
                            timestamps_to_interpolate.append(timestamps[t])
                start_idx = end_idx

            print("Filled in %d false negatives." % num_false_negatives)
            print("Interpolating %d points." % len(timestamps_to_interpolate))

            interpolated_values = np.interp(timestamps_to_interpolate, timestamps_without_zeros, data_without_zeros)
            for start_idx, val in enumerate(interpolated_values):
                melody_data[np.where(timestamps == timestamps_to_interpolate[start_idx])[0]] = val

        if remove_false_positives:
            num_false_positives = 0
            points_removed = 0
            false_positves_delta = self.tempo / 10
            start_idx = 0
            end_idx = 0
            while start_idx < data_len:  # look for short detections that seem to be false positives
                while start_idx < data_len and melody_data[start_idx] <= 0:
                    start_idx += 1
                    end_idx += 1
                while end_idx < data_len and melody_data[end_idx] > 0:
                    end_idx += 1
                if end_idx < data_len:
                    if timestamps[end_idx] - timestamps[start_idx] < false_positves_delta:
                        num_false_positives += 1
                        for t in range(start_idx, end_idx):
                            melody_data[t] = 0
                            points_removed += 1
                start_idx = end_idx

            print("Removed %d false positives." % num_false_positives)
            print("Zeroed %d points." % points_removed)

        return melody_data, timestamps


class Singing:
    def __init__(self, init_pyo=False, duplex=False):
        self.duplex = duplex
        self.vocal_paths = glob.glob(op.join("audio_files", "shimi_vocalizations", "*"))

        if init_pyo:
            self.server = Server()
            pa_list_devices()

            # Mac testing
            # self.server = Server()
            if self.duplex:
                self.server = Server(sr=16000, ichnls=4)
                self.server.setInOutDevice(2)
            else:
                self.server = Server(sr=16000, duplex=0)
                self.server.setOutputDevice(2)
            self.server.deactivateMidi()
            self.server.boot().start()

    def audio_initialize(self, audio_path, extraction_type="cnn"):
        self.path = audio_path
        self.song_length_in_samples, self.song_length_in_seconds, self.song_sr, _, _, _ = sndinfo(self.path)
        self.song_length_in_samples = int(self.song_length_in_samples)
        self.song_sr = int(self.song_sr)
        self.song_sample = SfPlayer(self.path, mul=0.2)
        self.shimi_audio_samples = []

        self.melody_extraction = MelodyExtraction(self.path)
        if extraction_type == "melodia":
            self.melody_extraction.melodia_extraction()
            self.melody_data = self.melody_extraction.melodia_data
            self.melody_timestamps = self.melody_extraction.melodia_timestamps
        else:
            self.melody_extraction.deep_learning_extraction()
            self.melody_data = self.melody_extraction.deep_learning_data
            self.melody_timestamps = self.melody_extraction.deep_learning_timestamps

        self.length_seconds = self.melody_extraction.length_seconds

        for path in self.vocal_paths:
            shimi_sample = Sample(path)
            self.shimi_audio_samples.append(shimi_sample)

        self.shimi_sample = random.choice(self.shimi_audio_samples)

        self.speed_index = 0

        self.speeds = []
        current_start = -1
        current_end = -1
        for freq, timestamp in zip(self.melody_data, self.melody_timestamps):
            if freq > 0 and current_start < 0:
                current_start = timestamp
            if freq <= 0 and current_start > 0:
                current_end = timestamp
                speed = self.shimi_sample.LENGTH / (current_end - current_start)
                self.speeds.append(speed)
                current_start = -1
                current_end = -1
        if current_start > 0 and current_end < 0:  # catch melody that doesn't end with silence
            current_end = self.melody_timestamps[-1]
            speed = self.shimi_sample.LENGTH / (current_end - current_start)
            self.speeds.append(speed)

        self.frequency_timestep = self.melody_timestamps[1] - self.melody_timestamps[0]
        self.frequency_setter = Pattern(self.set_freq, time=float(self.frequency_timestep))
        self.frequency_index = 1
        self.prev_freq = self.melody_data[0]

        print("Waiting for PV Analysis to be done...")
        wait_time = 3
        for i in range(wait_time):
            print("%d..." % (wait_time - i))
            time.sleep(1.0)

    def set_freq(self):
        new_freq = self.melody_data[self.frequency_index]

        if new_freq != 0:  # clamp frequencies to appropriate range
            if new_freq < 150:
                while new_freq < 150:
                    new_freq = new_freq * 2
            elif new_freq > 600:
                while new_freq > 600:
                    new_freq = new_freq / 2

        new_transposition = float(new_freq / 440)
        self.shimi_sample.set_transposition(new_transposition)

        if self.prev_freq <= 0 and new_freq > 0:
            self.start_vocal()
        elif self.prev_freq > 0 and new_freq <= 0:
            self.end_vocal()

        self.prev_freq = new_freq

        if self.frequency_index == len(self.melody_data) - 1:  # Stop it because otherwise it will loop forever
            self.frequency_setter.stop()
            for s in self.shimi_audio_samples:
                s.stop()
            self.song_sample.stop()

        self.frequency_index = (self.frequency_index + 1) % len(self.melody_data)

    def start_vocal(self):
        self.shimi_sample.set_speed(self.speeds[self.speed_index])
        self.shimi_sample.play()
        self.speed_index = (self.speed_index + 1) % len(self.speeds)

    def end_vocal(self):
        self.shimi_sample.stop()
        self.shimi_sample = random.choice(self.shimi_audio_samples)

    def sing_audio(self, audio_path, extraction_type):
        self.audio_initialize(audio_path, extraction_type)
        self.frequency_setter.play()
        self.song_sample.out()

    def sing_midi(self, midi_path):
        self.name = "_".join(midi_path.split('/')[-1].split('.')[:-1])
        self.shimi_midi_samples = []
        midi_wav_path = op.join("midi_audio_files", self.name + "_midi.wav")
        if not op.exists(midi_wav_path):
            shimi_voice_sr = None
            shimi_sample_length_in_seconds = None
            for f in self.vocal_paths:
                y, shimi_voice_sr = sf.read(f, always_2d=True)
                shimi_sample_length_in_seconds = y.shape[0] * (1 / shimi_voice_sr)
                self.shimi_midi_samples.append(y[:, 0])

            pm_object = pm.PrettyMIDI(midi_path)
            notes = pm_object.instruments[0].notes
            length_in_samples = int(pm_object.get_end_time() * shimi_voice_sr)
            final_sample = np.zeros((length_in_samples, 2))

            for n in notes:
                start = n.start
                end = n.end
                length = end - start
                speed = shimi_sample_length_in_seconds / length
                stretched = time_stretch(random.choice(self.shimi_midi_samples), speed)
                stretched_and_shifted = pitch_shift(stretched, shimi_voice_sr, n_steps=n.pitch - 69)
                start_s = int(start * shimi_voice_sr)
                end_s = min(int(end * shimi_voice_sr), length_in_samples)

                if end_s - start_s > len(stretched_and_shifted):
                    end_s = start_s + len(stretched_and_shifted)

                # Make it evenly stereo
                samples = stretched_and_shifted[0:end_s - start_s]
                final_sample[start_s:end_s, 0] = samples
                final_sample[start_s:end_s, 1] = samples
            sf.write(midi_wav_path, final_sample, shimi_voice_sr)

        self.singing_sample = SfPlayer(midi_wav_path)
        self.singing_sample.out()


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-p", "--pyo", action="store_true", default=False)
    args = parser.parse_args()
    s = Singing(init_pyo=args.pyo)
