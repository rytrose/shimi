# Adapted from https://github.com/Uberi/speech_recognition/blob/19dc36eb6a6173b500e2cc8cf2161ea2fe8cb891/speech_recognition/__main__.py
import speech_recognition as sr
import os
import os.path as op
import datetime
import numpy as np
import json
import time
import threading
from subprocess import Popen, PIPE, DEVNULL

from pythonosc import dispatcher as osc_dispatcher
from pythonosc import osc_server
from pythonosc import udp_client


class SpeechRecognizer:
    def __init__(self, respeaker=False, mic_index=2, snowboy_configuration=None, google_cloud=False, sphinx=False):
        self.snowboy_configuration = snowboy_configuration
        self.google_cloud = google_cloud
        self.sphinx = sphinx

        # Look for ReSpeaker
        for index, name in enumerate(sr.Microphone.list_microphone_names()):
            if len(name) > 8 and name[:9] == "ReSpeaker":
                mic_index = index
            print("Microphone with name \"{1}\" found for `Microphone(device_index={0})`".format(index, name))

        self.r = sr.Recognizer()

        if respeaker is True:
            if mic_index == -1:
                raise Exception("Unable to find ReSpeaker mic array.")
            self.m = sr.Microphone(device_index=mic_index)
        else:
            self.m = sr.Microphone(device_index=mic_index)

    def calibrate(self):
        with self.m as source:
            self.r.adjust_for_ambient_noise(source)

    def listenForPhrase(self, timeout=None, phrase_time_limit=None, on_phrase=None, use_doa=False):
        with self.m as source:
            try:
                t = time.time()
                if phrase_time_limit is not None:
                    if timeout is not None:
                        if self.snowboy_configuration:
                            print("Running snowboy with listen timeout and phrase time limit.")
                            audio, doa_measures = self.r.listen(source,
                                                                snowboy_configuration=self.snowboy_configuration,
                                                                timeout=timeout, phrase_time_limit=phrase_time_limit,
                                                                use_doa=use_doa)
                        else:
                            print("Running no hot word with listen timeout and phrase time limit.")
                            audio, doa_measures = self.r.listen(source, timeout=timeout,
                                                                phrase_time_limit=phrase_time_limit, use_doa=use_doa)
                    else:
                        if self.snowboy_configuration:
                            print("Running snowboy with phrase time limit.")
                            audio, doa_measures = self.r.listen(source,
                                                                snowboy_configuration=self.snowboy_configuration,
                                                                phrase_time_limit=phrase_time_limit, use_doa=use_doa)
                        else:
                            print("Running no hot word with phrase time limit.")
                            audio, doa_measures = self.r.listen(source, phrase_time_limit=phrase_time_limit,
                                                                use_doa=use_doa)
                else:
                    if self.snowboy_configuration:
                        print("Running snowboy with no timeouts.")
                        audio, doa_measures = self.r.listen(source, snowboy_configuration=self.snowboy_configuration,
                                                            use_doa=use_doa)
                    else:
                        print("Running no hot word with no timeouts.")
                        audio, doa_measures = self.r.listen(source, use_doa=use_doa)
                print("Time to collect phrase: %f" % (time.time() - t))
            except sr.WaitTimeoutError:
                return None, None, None
        try:
            # Run this in new thread!
            if on_phrase:
                t = time.time()
                threading.Thread(target=on_phrase).start()
                print("Time for on_phrase() call: %f" % (time.time() - t))

            t = time.time()
            # convert audio to np array (emulating librosa.core.load)
            raw_data_bytes = audio.get_raw_data()
            audio_data = np.fromstring(raw_data_bytes, dtype=np.int16) / 32768

            save_input = True
            save_path = op.join(os.getcwd(), "audio", "input_recordings")
            if save_input:
                with open(op.join(save_path, '{:%m%d-%H%M%S}'.format(datetime.datetime.now()) + '.wav'), "wb") as f:
                    f.write(audio.get_wav_data())
            print("Time to save input audio: %f" % (time.time() - t))

            t = time.time()
            if self.sphinx:
                value = self.r.recognize_sphinx(audio)
            elif self.google_cloud:
                # hardcoded path for now
                credentials = json.dumps(
                    json.load(open("wakeword/resources/credentials/shimi-google-cloud-credentials.json", "r")))
                value = self.r.recognize_google_cloud(audio, credentials_json=credentials)
            else:
                value = self.r.recognize_google(audio)
            print("Time for speech-to-text: %f" % (time.time() - t))

            # Average doa
            doa_value = None
            if use_doa:
                doa_value = sum(doa_measures) / len(doa_measures)

            # we need some special handling here to correctly print unicode characters to standard output
            if str is bytes:  # this version of Python uses bytes for strings (Python 2)
                return u"{}".format(value).encode("utf-8"), (audio_data, audio.sample_rate), doa_value
            else:  # this version of Python uses unicode for strings (Python 3+)
                return "{}".format(value), (audio_data, audio.sample_rate), doa_value

        except sr.UnknownValueError:
            print("Oops! Didn't catch that")
            return None, None, None
        except sr.RequestError as e:
            print("Uh oh! Couldn't request results from speech recognition service; {0}".format(e))
            return None, None, None


class SpeechRecognizerClient:
    """
    Works with the SpeechRecognizerServer to handle callbacks and recognition.
    """

    def __init__(self, respeaker=True, mic_index=2, snowboy_configuration=None, on_phrase_heard=None, on_phrase=None):
        self.snowboy_configuration = snowboy_configuration
        self.on_phrase_heard = on_phrase_heard
        self.on_phrase = on_phrase

        self.ip = "127.0.0.1"
        self.send_port = 5701
        self.listen_port = 5700
        self.client = udp_client.SimpleUDPClient(self.ip, self.send_port)

        dispatcher = osc_dispatcher.Dispatcher()
        dispatcher.map("/on_hotword", self.on_hotword)
        dispatcher.map("/phrase_error", print)
        dispatcher.map("/speech_to_text_error", self.speech_to_text_error)
        dispatcher.map("/heard_phrase", self.heard_phrase)
        dispatcher.map("/phrase", self.phrase)

        server = osc_server.ThreadingOSCUDPServer((self.ip, self.listen_port), dispatcher)
        threading.Thread(target=server.serve_forever).start()

        command_string = "/usr/bin/python3.6 SpeechRecognizerServer.py --respeaker {0} --mic_index {1}".format(
            respeaker, mic_index)

        if self.snowboy_configuration:
            command_string += " --hotword True --snowboy_dir {0}".format(
                self.snowboy_configuration[0])

            for filename in self.snowboy_configuration[1]:
                command_string += " --snowboy_model_files " + filename

        print("Running the following command:\n%s" % command_string)

        self.server = Popen(
            command_string.split(' '),
            stdout=PIPE,
            stdin=PIPE,
            bufsize=1,
            universal_newlines=True)

        def read_output():
            for l in self.server.stdout:
                print(l, end='')

        threading.Thread(target=read_output).start()

    def on_hotword(self, _, __):
        self.snowboy_configuration[2]()

    def heard_phrase(self, _, __):
        self.on_phrase_heard()

    def phrase(self, _, filename, sample_rate, phrase):
        with open(filename, "rb") as f:
            audio_bytes = f.read()
        audio_data = np.frombuffer(audio_bytes, dtype=np.int16) / 32768
        self.on_phrase(audio_data, sample_rate, phrase)

    def speech_to_text_error(self, _, e):
        print(e)
        self.listen()

    def listen(self, timeout=-1, phrase_time_limit=5):
        self.server.stdin.write("%d,%d\n" % (timeout, phrase_time_limit))
        self.server.stdin.flush()
