import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))

from utils.utils import get_bit
from audio.melody_extraction import MelodyExtraction
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
from librosa.effects import time_stretch, pitch_shift
import time
import random
import glob
import argparse


class Sample:
    def __init__(self, path, balance=None):
        PVSIZE = 1024
        PVOLAPS = 4

        self.path = path
        self.info = sndinfo(path)
        self.NUM_FRAMES = self.info[0]
        self.LENGTH = self.info[1]
        self.SR = self.info[2]
        self.snd_player = SfPlayer(self.path)
        self.pv_analysis = PVAnal(self.snd_player, size=PVSIZE, overlaps=PVOLAPS)
        self.pointer = Phasor(freq=1.0 / self.LENGTH)
        self.trans_value = SigTo(1, time=0.005)
        self.pv_buffer = PVBuffer(self.pv_analysis, self.pointer, pitch=self.trans_value, length=self.LENGTH)
        self.adsr = Adsr(attack=0.005, release=0.05)

        if balance is None:
            self.pv_synth = PVSynth(self.pv_buffer, mul=self.adsr)
            self.output = self.pv_synth
        else:
            self.pv_synth = PVSynth(self.pv_buffer)
            self.output = Balance(self.pv_synth, balance, mul=self.adsr)

    def play(self):
        self.adsr.play()
        self.output.out()

    def stop(self):
        self.adsr.stop()
        CallAfter(lambda: self.output.stop(), self.adsr.release)

    def set_transposition(self, val):
        self.trans_value.setValue(val)

    def set_speed(self, val):
        self.pointer.setFreq(val)

    def set_phase(self, val):
        self.pointer.setPhase(val)


class Singing:
    def __init__(self, init_pyo=False, duplex=False, resource_path="/home/nvidia/shimi/audio"):
        self.duplex = duplex
        self.vocal_paths = glob.glob(op.join(resource_path, "audio_files", "shimi_vocalizations", "*"))
        self.shimi_vocal_path = op.join(resource_path, "audio_files", "shimi_vocalization.wav")
        self.resource_path = resource_path
        self.playing = False

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

    def audio_initialize(self, audio_path, extraction_type="cnn", extraction_file=None):
        self.path = audio_path
        self.song_length_in_samples, self.song_length_in_seconds, self.song_sr, _, _, _ = sndinfo(self.path)
        self.song_length_in_samples = int(self.song_length_in_samples)
        self.song_sr = int(self.song_sr)
        self.song_vol = 0.1
        self.song_sample = SfPlayer(self.path, mul=self.song_vol)
        self.vocal_filter = Biquadx(self.song_sample * (1 / self.song_vol), freq=800, q=0.75, type=2)

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
                melody_timestamps = [(i / num_points) * self.melody_extraction.length_seconds for i in
                                     range(num_points)]
                self.melody_data, self.melody_timestamps, self.melody_extraction.notes = self.melody_extraction.process_data(
                    melody_data, melody_timestamps)
            else:
                self.melody_extraction.deep_learning_extraction()
                self.melody_data = self.melody_extraction.deep_learning_data
                self.melody_timestamps = self.melody_extraction.deep_learning_timestamps

        self.length_seconds = self.melody_extraction.length_seconds
        self.shimi_sample = Sample(self.shimi_vocal_path, balance=self.vocal_filter)
        self.num_utterances = 4
        self.frequency_timestep = self.melody_timestamps[1] - self.melody_timestamps[0]
        self.speed_index = 0
        self.speeds = []

        for note in self.melody_extraction.notes:
            speed = 1 / (self.num_utterances * (((note['end'] - note['start']) * self.frequency_timestep) + (
                    2 * self.shimi_sample.adsr.release)))
            self.speeds.append(float(speed))

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
            self.shimi_sample.stop()
            self.song_sample.stop()
            self.playing = False

        self.frequency_index = (self.frequency_index + 1) % len(self.melody_data)

    def start_vocal(self):
        self.shimi_sample.set_speed(self.speeds[self.speed_index])
        self.shimi_sample.set_phase((1 / self.num_utterances) * random.randint(0, 3))
        self.shimi_sample.play()
        self.speed_index = (self.speed_index + 1) % len(self.speeds)

    def end_vocal(self):
        self.shimi_sample.stop()

    def sing_audio(self, audio_path, extraction_type, extraction_file=None):
        self.audio_initialize(audio_path, extraction_type=extraction_type, extraction_file=extraction_file)
        self.frequency_setter.play()
        self.song_sample.out(delay=0.01)

        self.playing = True
        while self.playing:
            pass

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
