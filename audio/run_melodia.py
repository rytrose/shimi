"""Runs melodia melody extraction on Shimi."""

import vamp
import soundfile as sf
import argparse
import numpy as np
import pickle

parser = argparse.ArgumentParser()
parser.add_argument('-i', '--input_filename', type=str)
parser.add_argument('-o', '--output_filename', type=str)
args = parser.parse_args()

audio, sr = sf.read(args.input_filename, always_2d=True)
audio = audio[:, 0]
data = vamp.collect(audio, sr, "mtg-melodia:melodia")
hop, frequencies = data['vector']

timestamps = 8 * 128 / 44100.0 + np.arange(len(frequencies)) * (128 / 44100.0)

pickle.dump({
    "frequencies": frequencies,
    "timestamps": timestamps
}, open("/home/nvidia/shimi/audio/melodia_outputs/melodia_" + args.output_filename + ".p", "wb+"))

print("FINISHED")
