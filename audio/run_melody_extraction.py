import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))

from audio.singing import MelodyExtraction
import argparse

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--input_path", "-i", required=True, type=str)
    args = parser.parse_args()

    m = MelodyExtraction(args.input_path)
    m.deep_learning_extraction()
    m.melodia_extraction()
