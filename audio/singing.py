import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))

from audio.audio_analysis import Sample
import numpy as np
import os.path as op
from subprocess import Popen, PIPE
import soundfile as sf
import pickle
from pyo import *
import time


class MelodyExtraction:
    def __init__(self, path):
        self.path = path
        self.sound_data, self.sr = sf.read(self.path, always_2d=True)
        self.length_samples = self.sound_data.shape[0]
        self.length_seconds = self.length_samples * (1 / self.length_samples)
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
        command_string = "python3.5 " + op.join(self.deep_learning_path,
                                                "VocalMelodyExtraction.py") + " --input_file " + self.abs_path + \
                         " -m " + self.deep_learning_model_path + " --output_file " + "deep_learning_" + self.name

        process = Popen(command_string.split(' '), stdout=PIPE, bufsize=1, universal_newlines=True)

        # Wait until the process is finished  to continue
        for line in process.stdout.readline():
            if line == "FINISHED":
                print("Deep learning melody extraction complete.")
                break

        deep_learning_data = np.loadtxt("deep_learning_" + self.name + ".txt")
        self.deep_learning_data = deep_learning_data[:, 1]
        num_points = self.deep_learning_data.shape[0]
        self.deep_learning_timestamps = [(i / num_points) * self.length_seconds for i in range(num_points)]

    def melodia_extraction(self):
        command_string = "exagear -- " + op.join(self.run_melodia_path,
                                                 "run_melodia.sh") + " " + self.abs_path + " " + self.name

        input("Please run the following in a new shell, and press enter when it is done:\n%s\n" % command_string)

        melodia_data = pickle.load(open("melodia_" + self.name + ".p", "rb"))
        self.melodia_data = melodia_data["frequencies"]
        self.melodia_timestamps = melodia_data["timestamps"]


class Singing:
    def __init__(self, path, extraction_type, duplex=False):
        self.path = path
        self.server = Server()
        pa_list_devices()

        # Mac testing
        self.server.setInputDevice(0)
        self.server.setOutputDevice(1)
        # if self.duplex:
        #     self.server = Server(sr=16000, ichnls=4)
        #     self.server.setInOutDevice(2)
        # else:
        #     self.server = Server(sr=16000, duplex=0)
        #     self.server.setOutputDevice(2)
        # self.server.deactivateMidi()
        # self.server.boot().start()

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

        self.speeds = []
        current_start = -1
        current_end = -1
        for freq, timestamp in zip(self.melody_data, self.melody_timestamps):
            if freq > 0 and current_start < 0:
                current_start = timestamp
            if freq <= 0 and current_start > 0:
                current_end = timestamp
                speed = (current_end - current_start) / self.length_seconds
                self.speeds.append(speed)
                current_start = -1
                current_end = -1
        if current_start > 0 and current_end < 0:  # catch melody that doesn't end with silence
            current_end = self.melody_timestamps[-1]
            speed = (current_end - current_start) / self.length_seconds
            self.speeds.append(speed)

        self.frequency_table = DataTable(init=self.melody_data)
        self.frequency_read = TableRead(self.frequency_table, freq=1 / self.length_seconds)

        self.start_vocal_thresh = Thresh(self.frequency_read, threshold=0.001, dir=0)
        self.end_vocal_thresh = Thresh(self.frequency_read, threshold=0.001, dir=1)

        self.start_vocal_func = TrigFunc(self.start_vocal_thresh, self.start_vocal)
        self.end_vocal_func = TrigFunc(self.end_vocal_thresh, self.end_vocal)

        self.vocal_path = "shimi_vocalization.wav"
        self.shimi_sample = Sample(self.vocal_path, trans_value=self.frequency_read)
        self.speed_index = 0

        self.song_sample = Sample(self.path)

        print("Waiting for PV Analysis to be done...")
        for i in range(10):
            print("%d..." % (10 - i))
            time.sleep(1.0)

    def start_vocal(self):
        self.shimi_sample.set_speed(self.speeds[self.speed_index])
        self.shimi_sample.play()
        self.speed_index = (self.speed_index + 1) % len(self.speeds)

    def end_vocal(self):
        self.shimi_sample.stop()

    def sing(self):
        self.frequency_read.play()
        self.song_sample.play()


if __name__ == '__main__':
    s = Singing("casey_jones.wav", "melodia")
    s.sing()
