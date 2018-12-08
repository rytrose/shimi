from matt.SpeechRecognizer import *
from shimiAudio.demo import audio_response_demo
import pygame.mixer as mixer
from ctypes import *

ERROR_HANDLER_FUNC = CFUNCTYPE(None, c_char_p, c_int, c_char_p, c_int, c_char_p)
def py_error_handler(filename, line, function, err, fmt):
  pass
c_error_handler = ERROR_HANDLER_FUNC(py_error_handler)
asound = cdll.LoadLibrary('libasound.so')
asound.snd_lib_error_set_handler(c_error_handler)

print("Setting up pygame...")
mixer.init()

sr = SpeechRecognizer()
print("Calibrating microphone...")
sr.calibrate()
print("...done.")

try:
    while True:
        print("Listening...")
        phrase, audio = sr.listenForPhrase(phrase_time_limit=4.0)

        print("Got phrase and audio.")
        if phrase is None:
            print("No phrase captured.")
            continue

        print("Giving to Richard...")
        # Hand to Richard
        filename = audio_response_demo(phrase, audio[0], audio[1])

        print("Playing Richard file.")
        mixer.music.load(filename)
        mixer.music.play()


except KeyboardInterrupt:
    print("Exiting.")
except Exception as e:
    print("Error", e)