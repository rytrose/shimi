"""Runs both types of melody extraction on Shimi."""

import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))

from melody_exctraction import MelodyExtraction
import argparse

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--input_path", "-i", required=True, type=str)
    args = parser.parse_args()

    m = MelodyExtraction(args.input_path)
    m.deep_learning_extraction(process=False)
    m.melodia_extraction(process=False)
