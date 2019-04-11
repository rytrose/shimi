import sys
import os
import io
from ctypes import *
import speech_recognition as sr
from pythonosc import udp_client
import argparse
import glob
import time
import numpy as np
import fileinput
import threading


# Used to catch and drop alsa warnings
def py_error_handler(filename, line, function, err, fmt):
    pass


def silence_alsa():
    ERROR_HANDLER_FUNC = CFUNCTYPE(
        None, c_char_p, c_int, c_char_p, c_int, c_char_p)
    c_error_handler = ERROR_HANDLER_FUNC(py_error_handler)
    asound = cdll.LoadLibrary('libasound.so')
    asound.snd_lib_error_set_handler(c_error_handler)


class SpeechRecognizerServer:
    """Runs speech recognition processes.
    
    N.B. This requires the uses of a fork of Uberi/speech_recognition found here: https://github.com/rytrose/speech_recognition
    The fork allows for a callback function when hot word is detected, which is very useful for Shimi acknowledgement. It also has minor
    performance improvements.

    """

    def __init__(self, respeaker=True, mic_index=2, snowboy_configuration=None):
        """Connects to Shimi's microphone and establishes OSC endpoint.
            respeaker (bool, optional): Defaults to True. Determines whether to look for the ReSpeaker microphone inside of Shimi.
            mic_index (int, optional): Defaults to 2. Specifies the input device index to use.
            snowboy_configuration (tuple, optional): Defaults to None. The first argument should be the location of the Snowboy library, and the second should be a list of model files.
        """

        self.snowboy_configuration = snowboy_configuration
        if self.snowboy_configuration:
            self.snowboy_configuration = (
                snowboy_configuration[0], snowboy_configuration[1], self.on_hotword)

        # Look for ReSpeaker
        mic_names = sr.Microphone.list_microphone_names()

        for index, name in enumerate(mic_names):
            if len(name) > 8 and name[:9] == "ReSpeaker":
                mic_index = index
            print("Microphone with name \"{1}\" found for `Microphone(device_index={0})`".format(
                index, name))

        self.r = sr.Recognizer()

        if respeaker is True:
            if mic_index == -1:
                raise Exception("Unable to find ReSpeaker mic array.")
            self.m = sr.Microphone(device_index=mic_index)
        else:
            self.m = sr.Microphone(device_index=mic_index)

        self.calibrate()

        self.ip = "127.0.0.1"
        self.send_port = 5700
        self.client = udp_client.SimpleUDPClient(self.ip, self.send_port)

    def run(self):
        with self.m as source:
            while True:
                line = input()
                split = line.split(',')
                timeout = int(split[0])
                phrase_time_limit = int(split[1])
                self.listen_for_phrase(timeout, phrase_time_limit, source)

    def on_hotword(self):
        self.client.send_message("/on_hotword", True)

    def calibrate(self):
        with self.m as source:
            self.r.adjust_for_ambient_noise(source)

    def listen_for_phrase(self, timeout, phrase_time_limit, source):
        """Waits for hotword (if config provided) and phrase.
        
        Args:
            timeout (float): Time in seconds to listen for before returning nothing if no voice is heard.
            phrase_time_limit (float): Time in seconds to record a phrase once voice is heard before returning.
            source (speech_recognition.Microphone): Input resource.
        """

        if timeout == -1:
            timeout = None
        if phrase_time_limit == -1:
            phrase_time_limit = None

        try:
            t = time.time()
            if phrase_time_limit is not None:
                if timeout is not None:
                    if self.snowboy_configuration:
                        print(
                            "Running snowboy with listen timeout and phrase time limit.")
                        sys.stdout.flush()
                        audio = self.r.listen(source, snowboy_configuration=self.snowboy_configuration,
                                              timeout=timeout, phrase_time_limit=phrase_time_limit)
                    else:
                        print(
                            "Running no hot word with listen timeout and phrase time limit.")
                        sys.stdout.flush()
                        audio = self.r.listen(
                            source, timeout=timeout, phrase_time_limit=phrase_time_limit)
                else:
                    if self.snowboy_configuration:
                        print("Running snowboy with phrase time limit.")
                        sys.stdout.flush()
                        audio = self.r.listen(source, snowboy_configuration=self.snowboy_configuration,
                                              phrase_time_limit=phrase_time_limit)
                    else:
                        print("Running no hot word with phrase time limit.")
                        sys.stdout.flush()
                        audio = self.r.listen(
                            source, phrase_time_limit=phrase_time_limit)
            else:
                if self.snowboy_configuration:
                    print("Running snowboy with no timeouts.")
                    sys.stdout.flush()
                    audio = self.r.listen(
                        source, snowboy_configuration=self.snowboy_configuration)
                else:
                    print("Running no hot word with no timeouts.")
                    sys.stdout.flush()
                    audio = self.r.listen(source)
            print("Time to collect phrase: %f" % (time.time() - t))
            sys.stdout.flush()
        except sr.WaitTimeoutError:
            self.client.send_message(
                "/phrase_error", "Phrase listening timed out.")
            return
        except Exception as e:
            self.client.send_message("/phrase_error", str(e))
            return

        try:
            # Heard phrase
            self.client.send_message("/heard_phrase", True)

            # Convert audio to np array (emulating librosa.core.load)
            raw_data_bytes = audio.get_raw_data()
            # audio_data = np.fromstring(raw_data_bytes, dtype=np.int16) / 32768

            filename = "audio_data.dat"
            with open(filename, "wb") as f:
                f.write(raw_data_bytes)

            # Speech-to-text
            value = self.r.recognize_google(audio)

            # we need some special handling here to correctly print unicode characters to standard output
            # this version of Python uses bytes for strings (Python 2)
            if str is bytes:
                phrase = u"{}".format(value).encode("utf-8")
            # this version of Python uses unicode for strings (Python 3+)
            else:
                phrase = "{}".format(value)

            self.client.send_message(
                "/phrase", [filename, audio.sample_rate, phrase])
            return

        except sr.UnknownValueError:
            self.client.send_message(
                "/speech_to_text_error", "Oops! Didn't catch that")
            return
        except sr.RequestError as e:
            self.client.send_message("/speech_to_text_error",
                                     "Uh oh! Couldn't request results from speech recognition service; {0}".format(e))
            return


if __name__ == "__main__":
    silence_alsa()
    parser = argparse.ArgumentParser()
    parser.add_argument("--respeaker", type=bool, default=True)
    parser.add_argument("--mic_index", default=2)
    parser.add_argument("--hotword", type=bool, default=False)
    parser.add_argument("--snowboy_dir", default="wakeword")
    parser.add_argument("--snowboy_model_files", action='append', default=[])
    args = parser.parse_args()

    if not args.snowboy_model_files:
        args.snowboy_model_files = glob.glob(
            "wakeword/resources/models/*.pmdl")

    snowboy_configuration = None
    if args.hotword:
        snowboy_configuration = (
            args.snowboy_dir, args.snowboy_model_files, None)

    server = SpeechRecognizerServer(respeaker=args.respeaker, mic_index=args.mic_index,
                                    snowboy_configuration=snowboy_configuration)

    server.run()
