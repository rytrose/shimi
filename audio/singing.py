from audio.audio_analysis import Sample
import numpy as np
import os.path as op
from subprocess import Popen, PIPE

class MelodyExtraction:
    def __init__(self, path):
        self.path = path

    def deep_learning_model(self):
        pass