import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))

import matplotlib
matplotlib.use("TkAgg")
from audio.singing import MelodyExtraction
import matplotlib.pyplot as plt
import pretty_midi as pm
import numpy as np
import time
import tables
import os.path as op
import json
import random
from subprocess import Popen


class LakhMidiAnalysis:
    def __init__(self, dataset_path):
        self.dataset_path = dataset_path
        self.match_scores = json.load(open(op.join(self.dataset_path, "match_scores.json")))

    def msd_id_to_dirs(self, msd_id):
        """Given an MSD ID, generate the path prefix.
        e.g. TRABCD12345678 -> A/B/C/TRABCD12345678"""
        return op.join(msd_id[2], msd_id[3], msd_id[4], msd_id)

    def msd_id_to_mp3(self, msd_id):
        """Given an MSD ID, return the path to the corresponding mp3"""
        return op.join(self.dataset_path, 'lmd_matched_mp3', self.msd_id_to_dirs(msd_id) + '.mp3')

    def msd_id_to_h5(self, msd_id):
        """Given an MSD ID, return the path to the corresponding h5"""
        return os.path.join(self.dataset_path, 'lmd_matched_h5', self.msd_id_to_dirs(msd_id) + '.h5')

    def get_midi_path(self, msd_id, midi_md5):
        """Given an MSD ID and MIDI MD5, return path to a MIDI file.
        kind should be one of 'matched' or 'aligned'. """
        return os.path.join(self.dataset_path, 'lmd_aligned', self.msd_id_to_dirs(msd_id), midi_md5 + '.mid')

    def compare_audio_and_midi(self, msd_id=None, extraction_type="cnn"):
        if msd_id is None:
            print("Choosing random Lakh MIDI example to analyze.")
            id_idx = random.randrange(len(self.match_scores.keys()))
            msd_id = list(self.match_scores.keys())[id_idx]

        with tables.open_file(self.msd_id_to_h5(msd_id)) as metadata:
            title = metadata.root.metadata.songs.cols.title[0]
            artist = metadata.root.metadata.songs.cols.artist_name[0]
            release = metadata.root.metadata.songs.cols.release[0]

            print('Analyzing "{}" by {} on "{}" [ID: {}]'.format(title, artist, release, msd_id))

            mp3_path = self.msd_id_to_mp3(msd_id)
            wav_path = ".".join(mp3_path.split(".")[:-1]) + ".wav"
            wav_filename = wav_path.split("/")[-1]

            if not op.exists(wav_path):
                command_string = "ffmpeg -i %s %s" % (mp3_path, wav_path)
                print("Running '%s'" % command_string)
                conversion = Popen(command_string.split(" "))
                conversion.wait()
                os.remove(mp3_path)

            melody_extractor = MelodyExtraction(wav_path, resource_path=os.getcwd())

            send_file_command = 'scp %s nvidia@weinberg-jetson-1.music.gatech.edu:~/shimi/audio/audio_files/%s' % (
                wav_path, wav_filename)
            shimi_command = 'python /home/nvidia/shimi/audio/run_melody_extraction.py -i /home/nvidia/shimi/audio/audio_files/%s' % wav_filename
            receive_files_command = r'scp nvidia@weinberg-jetson-1.music.gatech.edu:~/shimi/audio/\{cnn_outputs/cnn_%s.txt,melodia_outputs/melodia_%s.p\} ./' \
                                    % (msd_id, msd_id)

            input(
                "\nIf you haven't already, do the following:\non laptop run:\n%s\nthen on Shimi run:\n%s\nthen on laptop run:\n%s\n" % (
                    send_file_command, shimi_command, receive_files_command))

            if op.exists("melodia_%s.p" % msd_id):
                os.rename("melodia_%s.p" % msd_id, "melodia_outputs/melodia_%s.p" % msd_id)
            if op.exists("cnn_%s.txt" % msd_id):
                os.rename("cnn_%s.txt" % msd_id, "cnn_outputs/cnn_%s.txt" % msd_id)

            if extraction_type == "melodia":
                melody_extractor.melodia_extraction()
                extracted_melody_data = melody_extractor.melodia_data
                timestamps = melody_extractor.melodia_timestamps
            else:
                melody_extractor.deep_learning_extraction()
                extracted_melody_data = melody_extractor.deep_learning_data
                timestamps = melody_extractor.deep_learning_timestamps

            best_score = -1
            best_midi_md5 = None
            for midi_md5, score in self.match_scores[msd_id].items():
                if score > best_score:
                    best_score = score
                    best_midi_md5 = midi_md5

            print("Using file %s.mid [score: %f]" % (best_midi_md5, best_score))

            midi_object = pm.PrettyMIDI(self.get_midi_path(msd_id, best_midi_md5))

            print("MIDI File Voices:")
            for i, instrument in enumerate(midi_object.instruments):
                print("\t%d: %s" % (i, instrument.name))

            voice_idx = input("Please select a voice to use: ")
            voice_idx = int(voice_idx)

            piano_roll = midi_object.instruments[voice_idx].get_piano_roll(times=np.array(timestamps))
            midi_melody_data = []
            for i in range(piano_roll.shape[1]):
                note_vector = piano_roll[:,i]
                note_indices = np.argwhere(note_vector > 0.0)
                if len(note_indices) > 0:
                    midi_melody_data.append(pm.note_number_to_hz(note_indices[0]))
                else:
                    midi_melody_data.append(0)

            colors = np.repeat("blue", len(timestamps))
            colors[np.argwhere(extracted_melody_data != midi_melody_data)] = "red"

            print("Num different points: ", np.argwhere(extracted_melody_data != midi_melody_data).shape[0])

            fig, (ax1, ax2) = plt.subplots(nrows=2, sharex=True, sharey=True)
            fig.suptitle("[%s by %s] Audio Extracted vs. MIDI" % (title, artist))
            fig.set_size_inches(18.5, 10.5)

            ax1.scatter(timestamps, extracted_melody_data, s=1, color=colors)
            ax2.scatter(timestamps, midi_melody_data, s=1, color=colors)

            fig.subplots_adjust(hspace=0)
            plt.setp([a.get_xticklabels() for a in fig.axes[:-1]], visible=False)

            plt.show()


if __name__ == '__main__':
    lm = LakhMidiAnalysis('/Volumes/Ryan_Drive/Datasets/Lakh_MIDI')
