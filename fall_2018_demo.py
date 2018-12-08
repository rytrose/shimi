from shimi import Shimi
from motion.move import Alert
from wakeword.wakeword_activation import WakeWord
from shimiAudio.demo import audio_response_demo
from ctypes import *
import pygame.mixer as mixer


# Used to catch and drop alsa warnings
def py_error_handler(filename, line, function, err, fmt):
    pass


# Used to take a phrase, give to generation code, and start gesture playback
def dialogue(shimi, phrase, audio_data):
    filename = audio_response_demo(phrase, audio_data[0], audio_data[1])

    print("Playing Richard file.")
    mixer.music.load(filename)
    mixer.music.play()


"""
Silence the myriad warnings from alsa...
"""
ERROR_HANDLER_FUNC = CFUNCTYPE(None, c_char_p, c_int, c_char_p, c_int, c_char_p)
c_error_handler = ERROR_HANDLER_FUNC(py_error_handler)
asound = cdll.LoadLibrary('libasound.so')
asound.snd_lib_error_set_handler(c_error_handler)

# Set up pygame
mixer.init()

try:
    # Make Shimi object
    shimi = Shimi()

    # Set up wakeword
    wakeword = WakeWord(shimi=shimi, on_wake=Alert, on_phrase=dialogue, respeaker=True)

    wakeword.start()

    while True:
        # Continue listening until keyboard interrupt
        pass

except KeyboardInterrupt as e:
    print("Exiting...", e)
    shimi.initial_position()
    shimi.disable_torque()
