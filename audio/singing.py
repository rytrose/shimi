import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))

from audio.audio_analysis import Sample
import numpy as np
import os.path as op
from subprocess import Popen, PIPE
import soundfile as sf
import pickle


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

if __name__ == '__main__':
    me = MelodyExtraction('casey_jones.wav')
    me.deep_learning_extraction()
    me.melodia_extraction()
    print(me.melodia_data, me.melodia_timestamps)
