import sys
import os

sys.path.insert(1, os.path.join(sys.path[0], 'shimiAudio'))
from shimi import Shimi
from motion.move import Alert
from motion.generative_phrase import GenerativePhrase
from wakeword.wakeword_activation import WakeWordClient
from audio.audio_demos import play_opera
from wakeword.doa import DOA
from demo import audio_response_demo
from ctypes import *
import pygame.mixer as mixer
import parselmouth as pm
import time
import matplotlib.pyplot as plt
from PIL import Image
import glob
import random


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
        def dialogue(_, **kwargs):
            print(kwargs)
            print("Shimi heard: %s" % kwargs['phrase'])
            wav_filename, midi_filename, valence, arousal = audio_response_demo(kwargs['phrase'],
                                                                                kwargs['audio_data'][0],
                                                                                kwargs['audio_data'][1])

            # plt.title("Shimi Emotion")
            # plt.xlabel("Valence")
            # plt.ylabel("Arousal")
            # plt.xlim(-1.0, 1.0)
            # plt.ylim(-1.0, 1.0)
            # plt.scatter([valence], [arousal])
            # plt.xticks([0.0])
            # plt.yticks([0.0])
            # plt.grid(True)
            # print("VALENCE: %2f, AROUSAL: %.2f" % (valence, arousal))
            # plt.annotate("(%.2f, %.2f)" % (valence, arousal), xy=(valence, arousal - 0.05))
            #
            # plt.savefig("val_aro.png")
            # Image.open("val_aro.png").show()
            # plt.clf()

            phrase_generator.generate(midi_filename, valence, arousal, wav_path=wav_filename)

        def play_hey_jude(_, **kwargs):
            _, _, valence, arousal = audio_response_demo(kwargs['phrase'],
                                                         kwargs['audio_data'][0],
                                                         kwargs['audio_data'][1])

            phrase_generator.generate("heymidi.mid", valence, arousal, wav_path="heyjude.mp3")

        def heard():
            ping_file = "shimiAudio/audio/"
            ping_file += random.choice(["ping1.wav", "ping2.wav", "ping3.wav"])
            mixer.music.load(ping_file)
            mixer.music.play()

        phrase_callbacks = [
            {
                "triggers": ["play opera", "sing opera"],
                "callback": play_opera,
            },
            {
                "triggers": ["the beatles", "hey jude"],
                "callback": play_hey_jude
            }
        ]

        default_callback = {
            "callback": dialogue
        }

        # Set up wakeword
        model_files = glob.glob("wakeword/resources/models/*.pmdl")
        wakeword = WakeWordClient(shimi=shimi, models=model_files, on_wake=Alert, on_phrase_heard=heard,
                                  phrase_callbacks=phrase_callbacks, default_callback=default_callback, respeaker=True,
                                  manual_wake=False)

        wakeword.run()

    except KeyboardInterrupt as e:
        print("Exiting...", e)
        shimi.initial_position()
        shimi.disable_torque()


if __name__ == "__main__":
    silence_alsa()
    main()
