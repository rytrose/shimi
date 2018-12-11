import sys
import os
sys.path.insert(1, os.path.join(sys.path[0], 'shimiAudio'))
from shimi import Shimi
from motion.move import Alert
from motion.generative_phrase import GenerativePhrase
from wakeword.wakeword_activation import WakeWord
from wakeword.doa import DOA
from demo import audio_response_demo
from ctypes import *
import pygame.mixer as mixer
import parselmouth as pm
import time


# Used to catch and drop alsa warnings
def py_error_handler(filename, line, function, err, fmt):
    pass


def silence_alsa():
    ERROR_HANDLER_FUNC = CFUNCTYPE(None, c_char_p, c_int, c_char_p, c_int, c_char_p)
    c_error_handler = ERROR_HANDLER_FUNC(py_error_handler)
    asound = cdll.LoadLibrary('libasound.so')
    asound.snd_lib_error_set_handler(c_error_handler)


def main():
    # Set up pygame
    mixer.init()

    try:
        # Make Shimi object
        shimi = Shimi()

        # Set up generative phrase motion
        phrase_generator = GenerativePhrase(shimi=shimi, posenet=False)

        # Use a closure to reference the phrase_generator
        # Used to take a phrase, give to generation code, and start gesture playback
        def dialogue(_, phrase, audio_data, doa_value):
            wav_filename, midi_filename, valence, arousal = audio_response_demo(phrase, audio_data[0], audio_data[1])
            midi_filename = "angry1.mid"
            print("Starting generation...valence: %f, arousal: %f" % (valence, arousal))
            phrase_generator.generate(midi_filename, valence, arousal, wav_path=wav_filename, doa_value=doa_value)

        # Set up wakeword
        wakeword = WakeWord(shimi=shimi, model="wakeword/resources/models/Hey-Shimi2.pmdl", on_wake=Alert,
                            on_phrase=dialogue, respeaker=True, use_doa=True, manual_wake=True)

        wakeword.start()

        while True:
            # Continue listening until keyboard interrupt
            pass

    except KeyboardInterrupt as e:
        print("Exiting...", e)
        # doa.stop()
        shimi.initial_position()
        shimi.disable_torque()


if __name__ == "__main__":
    silence_alsa()
    main()
