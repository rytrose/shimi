import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))

from utils.utils import get_bit
import matplotlib

matplotlib.use("TkAgg")
import matplotlib.pyplot as plt
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
    def __init__(self, path, mul=1, balance=None):
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
        self.adsr = Adsr(attack=0.02, mul=mul)
        self.pv_synth = PVSynth(self.trans_object, mul=self.adsr)
        if balance is None:
            self.output = self.pv_synth
        else:
            self.output = Balance(self.pv_synth, balance, mul=self.adsr)

    def play(self):
        self.speed_object.reset()
        self.adsr.play()
        self.output.out()

    def stop(self):
        self.adsr.stop()
        # self.output.stop()

    def set_transposition(self, val):
        self.trans_value.setValue(val)

    def set_speed(self, val):
        self.speed_table.replace([(0, val), (512, val)])


class MelodyExtraction:
    def __init__(self, path, resource_path="/home/nvidia/shimi/audio"):
        self.resource_path = resource_path
        self.path = path
        self.sound_data, self.sr = sf.read(self.path, always_2d=True)
        self.tempo = estimate_tempo(self.sound_data[:, 1], self.sr)[0] / 60
        self.length_samples = self.sound_data.shape[0]
        self.length_seconds = self.length_samples * (1 / self.sr)
        self.abs_path = op.abspath(self.path)
        self.name = "_".join(self.abs_path.split('/')[-1].split('.')[:-1])

        self.deep_learning_path = "/media/nvidia/disk2/Vocal-Melody-Extraction"
        self.deep_learning_model_path = op.join(self.deep_learning_path, "pretrained_models", "Seg")
        self.deep_learning_data = None
        self.deep_learning_timestamps = None

        self.run_melodia_path = self.resource_path
        self.melodia_data = None
        self.melodia_timestamps = None

    def deep_learning_extraction(self, process=True):
        if not op.exists(op.join(self.resource_path, "cnn_outputs", "cnn_" + self.name + ".txt")):
            command_string = "python3.5 " + op.join(self.deep_learning_path,
                                                    "VocalMelodyExtraction.py") + " --input_file " + self.abs_path + \
                             " -m " + self.deep_learning_model_path + " --output_file " + op.join(self.resource_path,
                                                                                                  "cnn_outputs",
                                                                                                  "cnn_") + self.name \
                             + " --jetson"

            process = Popen(command_string.split(' '), stdout=PIPE, bufsize=1, universal_newlines=True)

            # Wait until the process is finished  to continue
            for line in process.stdout.readline():
                if line == "FINISHED":
                    print("Deep learning melody extraction complete.")
                    break

        deep_learning_data = np.loadtxt(op.join(self.resource_path, "cnn_outputs", "cnn_" + self.name + ".txt"))
        self.deep_learning_data = deep_learning_data[:, 1]
        np.place(self.deep_learning_data, self.deep_learning_data <= 0, 0)
        num_points = self.deep_learning_data.shape[0]
        self.deep_learning_timestamps = [(i / num_points) * self.length_seconds for i in range(num_points)]
        if process:
            self.deep_learning_data, self.deep_learning_timestamps, self.notes = self.process_data(
                self.deep_learning_data,
                self.deep_learning_timestamps)

    def melodia_extraction(self, process=True):
        if not op.exists(op.join("melodia_outputs", "melodia_" + self.name + ".p")):
            command_string = "exagear -- " + op.join(self.run_melodia_path,
                                                     "run_melodia.sh") + " " + self.abs_path + " " + self.name

            input("Please run the following in a new shell, and press enter when it is done:\n%s\n" % command_string)

        melodia_data = pickle.load(
            open(op.join(self.resource_path, "melodia_outputs", "melodia_" + self.name + ".p"), "rb"))
        self.melodia_data = melodia_data["frequencies"]
        self.melodia_timestamps = melodia_data["timestamps"]

        if process:
            self.melodia_data, self.melodia_timestamps, self.notes = self.process_data(self.melodia_data,
                                                                                       self.melodia_timestamps)

    def process_data(self, melody_data, timestamps, fix_octaves=True, smooth_false_negatives=True,
                     remove_false_positives=True, remove_spikes=True):
        data_len = len(melody_data)
        timestep = timestamps[1] - timestamps[0]
        np.place(melody_data, melody_data <= 0, 0)  # Replace no VAD with 0
        np.place(melody_data, melody_data < 70, 0)  # Remove unrealistic low frequencies
        np.place(melody_data, melody_data > 750, 0)  # Remove unrealistic high frequencies

        data_without_zeros = []  # get all the non-zero values to use for interpolation later
        timestamps_without_zeros = []

        for freq, timestamp in zip(melody_data, timestamps):
            if freq > 0:
                data_without_zeros.append(freq)
                timestamps_without_zeros.append(timestamp)

        if smooth_false_negatives:
            vad_smoothing_delta = 2 * timestep

            timestamps_to_interpolate = []
            indicies_to_interpolate = []
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
                            indicies_to_interpolate.append(t)
                start_idx = end_idx

            print("Filled in %d false negatives." % num_false_negatives)
            print("Interpolating %d points." % len(timestamps_to_interpolate))

            interpolated_values = np.interp(timestamps_to_interpolate, timestamps_without_zeros, data_without_zeros)
            for ind, val in zip(indicies_to_interpolate, interpolated_values):
                melody_data[ind] = val

        if remove_false_positives:
            num_false_positives = 0
            points_removed = 0

            false_positves_delta = 4 * timestep
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

        if remove_spikes:
            num_spikes = 0

            spike_tolerance = 1.5
            start_idx = 0
            end_idx = 0
            timestamps_to_interpolate = []
            indicies_to_interpolate = []
            while start_idx < data_len:  # look at both sides (if possible) of a point to check for spike
                while start_idx < data_len and melody_data[start_idx] <= 0:
                    start_idx += 1
                    end_idx += 1
                while end_idx < data_len and melody_data[end_idx] > 0:
                    end_idx += 1
                for i in range(start_idx, end_idx):
                    if i == start_idx:
                        forward_spike_check = (melody_data[i] / melody_data[i + 1]) > spike_tolerance or (
                                melody_data[i] / melody_data[i + 1]) < (1 / spike_tolerance)

                        if forward_spike_check:
                            num_spikes += 1
                            melody_data[i] = 0
                    elif i == end_idx - 1:
                        prev_spike_check = (melody_data[i] / melody_data[i - 1]) > spike_tolerance or (
                                melody_data[i] / melody_data[i - 1]) < (1 / spike_tolerance)
                        if prev_spike_check:
                            num_spikes += 1
                            melody_data[i] = 0
                    else:
                        prev_spike_check = (melody_data[i] / melody_data[i - 1]) > spike_tolerance or (
                                melody_data[i] / melody_data[i - 1]) < (1 / spike_tolerance)
                        forward_spike_check = (melody_data[i] / melody_data[i + 1]) > spike_tolerance or (
                                melody_data[i] / melody_data[i + 1]) < (1 / spike_tolerance)

                        if prev_spike_check and forward_spike_check:
                            num_spikes += 1
                            try:
                                timestamps_without_zeros.pop(timestamps_without_zeros.index(timestamps[i]))
                                data_without_zeros.pop(data_without_zeros.index(melody_data[i]))
                            except:
                                print("Unable to pop spike at timestep %f." % timestamps[i])
                            timestamps_to_interpolate.append(timestamps[i])
                            indicies_to_interpolate.append(i)
                start_idx = end_idx

            interpolated_values = np.interp(timestamps_to_interpolate, timestamps_without_zeros, data_without_zeros)
            for ind, val in zip(indicies_to_interpolate, interpolated_values):
                melody_data[ind] = val

            print("Removed %d spikes." % num_spikes)

        if fix_octaves:
            notes = []
            start_idx = 0
            end_idx = 0

            while end_idx < data_len:  # create notes for reference
                while start_idx < data_len and melody_data[start_idx] <= 0:
                    start_idx += 1
                    end_idx += 1
                while end_idx < data_len and melody_data[end_idx] > 0:
                    end_idx += 1
                if start_idx < end_idx:
                    new_note = {
                        'start': start_idx,
                        'end': end_idx,
                        'avg': np.average(melody_data[start_idx:end_idx])
                    }
                    notes.append(new_note)
                start_idx = end_idx

            inter_note_look_dist = 10
            inter_note_tolerance = 1.5
            intra_note_look_dist = 4
            intra_note_tolerance = 2.2

            points_dropped = 0
            points_raised = 0
            notes_dropped = 0
            notes_raised = 0

            for note in notes:  # do inter-note fixing first
                note_start = note['start']
                note_end = note['end']
                for i in range(note_start, note_end):
                    back_avg = np.average(melody_data[max(note_start, i - inter_note_look_dist):i])
                    forward_avg = np.average(melody_data[i:min(note_end, i + inter_note_look_dist)])
                    surrounding_avg = (back_avg + forward_avg) * 0.5

                    if surrounding_avg == 0:
                        continue

                    octave_ratio = melody_data[i] / surrounding_avg

                    if octave_ratio > inter_note_tolerance:
                        while octave_ratio > inter_note_tolerance:
                            octave_ratio = octave_ratio / 2
                            melody_data[i] = melody_data[i] / 2
                        points_dropped += 1
                    elif octave_ratio < (1 / inter_note_tolerance):
                        while octave_ratio < (1 / inter_note_tolerance):
                            octave_ratio = octave_ratio * 2
                            melody_data[i] = melody_data[i] * 2
                        points_raised += 1

            for note_i, note in enumerate(notes):
                note_start = note['start']
                note_end = note['end']

                prev_avg = 0
                num_prev = 0
                for prev_note_i in range(max(0, note_i - intra_note_look_dist), note_i):
                    prev_note = notes[prev_note_i]
                    prev_note_start = prev_note['start']
                    prev_note_end = prev_note['end']
                    prev_avg += np.average(melody_data[prev_note_start:prev_note_end])
                    num_prev += 1

                if prev_avg == 0:
                    continue

                prev_avg = prev_avg / num_prev

                note_avg = np.average(melody_data[note_start:note_end])
                octave_ratio = note_avg / prev_avg

                if octave_ratio > intra_note_tolerance:
                    octave_closeness = abs(octave_ratio - 1)
                    while abs((octave_ratio / 2) - 1) < octave_closeness:
                        octave_closeness = abs((octave_ratio / 2) - 1)
                        octave_ratio = octave_ratio / 2
                        melody_data[note_start:note_end] = melody_data[note_start:note_end] / 2
                    notes_dropped += 1
                elif octave_ratio < (1 / intra_note_tolerance):
                    octave_closeness = abs(octave_ratio - 1)
                    while abs((octave_ratio * 2) - 1) < octave_closeness:
                        octave_closeness = abs((octave_ratio * 2) - 1)
                        octave_ratio = octave_ratio * 2
                        melody_data[note_start:note_end] = melody_data[note_start:note_end] * 2
                    notes_raised += 1

            print("Dropped %d points an octave." % points_dropped)
            print("Raised %d points an octave." % points_raised)
            print("Dropped %d notes an octave." % notes_dropped)
            print("Raised %d notes an octave." % notes_raised)

        notes = []
        start_idx = 0
        end_idx = 0

        while end_idx < data_len:  # create notes for reference
            while start_idx < data_len and melody_data[start_idx] <= 0:
                start_idx += 1
                end_idx += 1
            while end_idx < data_len and melody_data[end_idx] > 0:
                end_idx += 1
            if start_idx < end_idx:
                new_note = {
                    'start': start_idx,
                    'end': end_idx,
                    'avg': np.average(melody_data[start_idx:end_idx])
                }
                notes.append(new_note)
            start_idx = end_idx

        return melody_data, timestamps, notes

    def process_comparison(self, extraction_type):
        if extraction_type == "melodia":
            self.melodia_extraction(process=False)
            data = self.melodia_data
            np.place(data, data <= 0, 0)
            timestamps = self.melodia_timestamps
        else:
            self.deep_learning_extraction(process=False)
            data = self.deep_learning_data
            timestamps = self.deep_learning_timestamps

        np.place(data, data < 70, 0)  # Remove unrealistic low frequencies
        np.place(data, data > 750, 0)  # Remove unrealistic high frequencies

        for i in range(1, 16):
            label = ""
            fix_octaves = get_bit(i, 0)
            smooth_false_negatives = get_bit(i, 1)
            remove_false_positives = get_bit(i, 2)
            remove_spikes = get_bit(i, 3)

            if fix_octaves:
                label += "Fixed Octaves, "
            if smooth_false_negatives:
                label += "Smoothed False Negatives, "
            if remove_false_positives:
                label += "Removed False Positives, "
            if remove_spikes:
                label += "Removed Spikes, "

            label = label.rstrip(", ")

            print("--")
            processed_data, processed_timestamps, notes = self.process_data(copy.deepcopy(data),
                                                                            copy.deepcopy(timestamps),
                                                                            fix_octaves=fix_octaves,
                                                                            smooth_false_negatives=smooth_false_negatives,
                                                                            remove_false_positives=remove_false_positives,
                                                                            remove_spikes=remove_spikes)

            colors = np.repeat("blue", len(timestamps))
            colors[np.argwhere(data != processed_data)] = "red"

            print("Num different points: ", np.argwhere(data != processed_data).shape[0])

            fig, (ax1, ax2) = plt.subplots(nrows=2, sharex=True, sharey=True)
            fig.suptitle("%s (%s): Original vs. %s" % (self.name, extraction_type, label))
            fig.set_size_inches(18.5, 10.5)

            ax1.scatter(timestamps, data, s=1, color=colors)
            ax2.scatter(processed_timestamps, processed_data, s=1, color=colors)

            fig.subplots_adjust(hspace=0)
            plt.setp([a.get_xticklabels() for a in fig.axes[:-1]], visible=False)
            fig.savefig(op.join("plots", "%s_%s_%d_%d_%d_%d.png" % (
                extraction_type, self.name, fix_octaves, smooth_false_negatives, remove_false_positives,
                remove_spikes)), dpi=250)


class Singing:
    def __init__(self, init_pyo=False, duplex=False, resource_path="/home/nvidia/shimi/audio"):
        self.duplex = duplex
        self.vocal_paths = glob.glob(op.join(resource_path, "audio_files", "shimi_vocalizations", "*"))
        self.resource_path = resource_path

        if init_pyo:
            self.server = Server()
            pa_list_devices()

            # Mac testing
            #self.server = Server()
            if self.duplex:
                self.server = Server(sr=16000, ichnls=4)
                self.server.setInOutDevice(2)
            else:
                self.server = Server(sr=16000, duplex=0)
                self.server.setOutputDevice(2)
            self.server.deactivateMidi()
            self.server.boot().start()

    def audio_initialize(self, audio_path, extraction_type="cnn", extraction_file=None):
        self.path = audio_path
        self.song_length_in_samples, self.song_length_in_seconds, self.song_sr, _, _, _ = sndinfo(self.path)
        self.song_length_in_samples = int(self.song_length_in_samples)
        self.song_sr = int(self.song_sr)
        self.song_vol = 0.2
        self.song_sample = SfPlayer(self.path, mul=self.song_vol)
        self.vocal_filter = Biquadx(self.song_sample * (1 / self.song_vol), freq=800, q=0.75, type=2)
        self.shimi_audio_samples = []

        self.melody_extraction = MelodyExtraction(self.path, resource_path=self.resource_path)
        if extraction_type == "melodia":
            if extraction_file:
                melody_data = pickle.load(open(extraction_file, "rb"))
                self.melody_data, self.melody_timestamps, self.melody_extraction.notes = self.melody_extraction.process_data(
                    melody_data["frequencies"], melody_data["timestamps"])
            else:
                self.melody_extraction.melodia_extraction()
                self.melody_data = self.melody_extraction.melodia_data
                self.melody_timestamps = self.melody_extraction.melodia_timestamps
        else:
            if extraction_file:
                deep_learning_data = np.loadtxt(extraction_file)
                melody_data = deep_learning_data[:, 1]
                np.place(melody_data, melody_data <= 0, 0)
                num_points = melody_data.shape[0]
                melody_timestamps = [(i / num_points) * self.melody_extraction.length_seconds for i in range(num_points)]
                self.melody_data, self.melody_timestamps, self.melody_extraction.notes = self.melody_extraction.process_data(
                    melody_data, melody_timestamps)
            else:
                self.melody_extraction.deep_learning_extraction()
                self.melody_data = self.melody_extraction.deep_learning_data
                self.melody_timestamps = self.melody_extraction.deep_learning_timestamps

        self.length_seconds = self.melody_extraction.length_seconds

        for path in self.vocal_paths:
            shimi_sample = Sample(path, balance=self.vocal_filter)
            self.shimi_audio_samples.append(shimi_sample)

        self.shimi_sample = random.choice(self.shimi_audio_samples)

        self.frequency_timestep = self.melody_timestamps[1] - self.melody_timestamps[0]
        self.speed_index = 0
        self.speeds = []

        for note in self.melody_extraction.notes:
            speed = self.shimi_sample.LENGTH / ((note['end'] - note['start']) * self.frequency_timestep)
            self.speeds.append(speed)

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
        new_transposition = float(new_freq / 440)
        self.shimi_sample.set_transposition(new_transposition)

        if self.prev_freq <= 0 and new_freq > 0:
            self.start_vocal()
        elif self.prev_freq > 0 and new_freq <= 0:
            self.end_vocal()

        self.prev_freq = new_freq

        if self.frequency_index == len(self.melody_data) - 1:  # stop it because otherwise it will loop forever
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

    def sing_audio(self, audio_path, extraction_type, extraction_file=None):
        self.audio_initialize(audio_path, extraction_type=extraction_type, extraction_file=extraction_file)
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
    parser.add_argument("-r", "--resource_path", type=str, default=os.getcwd())
    args = parser.parse_args()
    s = Singing(init_pyo=args.pyo, resource_path=args.resource_path)
