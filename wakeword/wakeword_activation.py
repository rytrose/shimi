import time
from matt.SpeechRecognizer import SpeechRecognizer, SpeechRecognizerClient
from pypot.utils import StoppableThread
from shimi import Shimi
from audio.audio_demos import play_opera, play_outkast
from motion.move import No
from motion.generative_phrase import GenerativePhrase
import inspect
import random
import wakeword.snowboydecoder as snowboydecoder

def generate_phrase(shimi, phrase, path):
    rand_valence = random.choice([-1, 1]) * random.random()
    rand_arousal = random.choice([-1, 1]) * random.random()
    print("VALENCE: %.4f, AROUSAL: %.4f" % (rand_valence, rand_arousal))
    phrase_generator.generate(path, rand_valence, rand_arousal)


# N.B. All callbacks should take one argument, an instance of Shimi
PHRASE_CALLBACKS = [
    {
        "triggers": ["play opera", "sing opera"],
        "callback": play_opera,
    }
]


class WakeWordClient:
    def __init__(self, shimi=None, models=["wakeword/resources/models/Hey-Shimi-Ryan1.pmdl"],
                 phrase_callbacks=PHRASE_CALLBACKS,
                 default_callback=None,
                 on_wake=None, on_phrase=None, on_phrase_heard=None,
                 respeaker=False, posenet=False, manual_wake=False):
        """
        Defines a threaded process to manage using the "hey Shimi" wakeword, and making appropriate callbacks.
        :param on_wake: a non-blocking function or StoppableThread to be called when the wakeword is heard
        :param on_phrase: a function to be called when a phrase is gathered after the wakeword
        :param respeaker: if True, will look for the ReSpeaker microphone
        """
        if shimi is not None:
            self.shimi = shimi
        else:
            self.shimi = Shimi()

        global phrase_generator
        phrase_generator = GenerativePhrase(shimi=shimi, posenet=posenet)

        self.on_wake = on_wake
        self.on_wake_is_thread = self.is_thread(self.on_wake)
        self.on_phrase = on_phrase
        self.on_phrase_heard = on_phrase_heard
        self.respeaker = respeaker
        self.phrase_callbacks = phrase_callbacks
        self.default_callback = default_callback
        self.manual_wake = manual_wake

        if self.manual_wake:
            self.speech_recognizer_client = SpeechRecognizerClient(respeaker=self.respeaker,
                                                                   on_phrase_heard=self.on_phrase_heard_wrapper,
                                                                   on_phrase=self.on_phrase_wrapper)
        else:
            self.speech_recognizer_client = SpeechRecognizerClient(respeaker=self.respeaker,
                                                                   on_phrase_heard=self.on_phrase_heard_wrapper,
                                                                   on_phrase=self.on_phrase_wrapper,
                                                                   snowboy_configuration=(
                                                                       'wakeword', models, self.on_wake_wrapper))

    def run(self):
        if self.manual_wake:
            print("Press enter to wake Shimi.")
            input()
            self.on_wake_wrapper()

        # Start the speech recognition client
        self.speech_recognizer_client.listen()

    def on_wake_wrapper(self):
        # Call wake function if it exists
        # Handle thread
        if self.on_wake:
            if self.on_wake_is_thread:
                self.on_wake_thread = self.on_wake(self.shimi)
                self.on_wake_thread.start()
            else:
                self.on_wake(self.shimi)

    def on_phrase_heard_wrapper(self):
        if self.on_phrase_heard:
            self.on_phrase_heard()

        if self.on_wake_is_thread:
            self.stop_on_wake_thread()

    def on_phrase_wrapper(self, audio_data, sample_rate, phrase):
        # Make phrase lowercase
        phrase = phrase.lower()

        # Check words
        split = phrase.split(" ")
        if split[0] == "hey":
            # Get rid of key word "Hey Shimi"
            phrase = " ".join(phrase.split(" ")[2:])

        if self.on_phrase is not None:
            self.on_phrase(self.shimi, phrase, (audio_data, sample_rate))
        else:
            # Check callbacks for trigger words
            found_callback = False
            for phrase_callback in self.phrase_callbacks:
                for trigger in phrase_callback["triggers"]:
                    if trigger in phrase:
                        found_callback = True
                        print("Calling:", phrase_callback["callback"])
                        try:
                            if self.is_thread(phrase_callback["callback"]):
                                running_callback = phrase_callback["callback"](self.shimi, phrase=phrase,
                                                            audio_data=(audio_data, sample_rate))
                                running_callback.start()
                                running_callback.join()
                            else:
                                if "args" in phrase_callback:
                                    phrase_callback["callback"](self.shimi, *phrase_callback["args"], phrase=phrase,
                                                                audio_data=(audio_data, sample_rate))
                                else:
                                    phrase_callback["callback"](self.shimi, phrase=phrase,
                                                                audio_data=(audio_data, sample_rate))
                        except Exception as e:
                            print("Callback failed:", e)
                            break

                        # Only call the first trigger
                        break

            if not found_callback:
                print("Heard \"%s\", but didn't have any function to pass it to. Calling default." % phrase)
                if not self.default_callback:
                    print("No default callback.")
                else:
                    try:
                        if "args" in self.default_callback:
                            self.default_callback["callback"](self.shimi, *default_callback["args"], phrase=phrase,
                                                              audio_data=(audio_data, sample_rate))
                        else:
                            self.default_callback["callback"](self.shimi, phrase=phrase,
                                                              audio_data=(audio_data, sample_rate))
                    except Exception as e:
                        print("Default callback failed.", e)

        # Listen again
        if self.manual_wake:
            print("Press enter to wake Shimi.")
            input()
            self.on_wake_wrapper()

        self.speech_recognizer_client.listen()

    def stop_on_wake_thread(self):
        # Stop thread
        self.on_wake_thread.stop()

        # Wait for it to be done if it isn't
        if self.on_wake_thread._thread.is_alive():
            self.on_wake_thread.join()

    def is_thread(self, obj):
        try:
            if "StoppableThread" in [cls.__name__ for cls in inspect.getmro(obj)]:
                return True
            else:
                return False
        except:
            return False


class WakeWord:
    def __init__(self, shimi=None, models=["wakeword/resources/models/Hey-Shimi-Ryan1.pmdl"],
                 phrase_callbacks=PHRASE_CALLBACKS,
                 default_callback=None,
                 on_wake=None, on_phrase=None,
                 respeaker=False, posenet=False, use_doa=False, manual_wake=False):
        """
        Defines a threaded process to manage using the "hey Shimi" wakeword, and making appropriate callbacks.
        :param on_wake: a non-blocking function or StoppableThread to be called when the wakeword is heard
        :param on_phrase: a function to be called when a phrase is gathered after the wakeword
        :param respeaker: if True, will look for the ReSpeaker microphone
        """
        if shimi is not None:
            self.shimi = shimi
        else:
            self.shimi = Shimi()

        global phrase_generator
        phrase_generator = GenerativePhrase(shimi=shimi, posenet=posenet)

        self.on_wake = on_wake
        self.on_wake_is_thread = self.is_thread(self.on_wake)
        self.on_phrase = on_phrase
        self.respeaker = respeaker
        self.use_doa = use_doa
        self.phrase_callbacks = phrase_callbacks
        self.default_callback = default_callback
        self.manual_wake = manual_wake
        if self.manual_wake:
            self.speech_recognizer = SpeechRecognizer(respeaker=self.respeaker)
        else:
            self.speech_recognizer = SpeechRecognizer(respeaker=self.respeaker,
                                                      snowboy_configuration=('wakeword', models, self.on_wake_word),
                                                      google_cloud=False,
                                                      sphinx=False)

        self.setup()

    def setup(self):
        print("Please be quiet for microphone calibration...")
        self.speech_recognizer.calibrate()
        print("...finished calibrating.")

    def run(self):
        while True:
            if self.manual_wake:
                print("Press enter to wake Shimi.")
                input()
                self.on_wake_word()

            if self.on_wake_is_thread:
                phrase, audio_data, doa_value = self.speech_recognizer.listenForPhrase(phrase_time_limit=5,
                                                                                       on_phrase=self.stop_on_wake_thread,
                                                                                       use_doa=self.use_doa)
            else:
                # Start to listen for a phrase
                phrase, audio_data, doa_value = self.speech_recognizer.listenForPhrase(phrase_time_limit=5,
                                                                                       use_doa=self.use_doa)

            # TODO: Handle no phrase
            if phrase is None:
                continue

            # Make phrase lowercase
            phrase = phrase.lower()

            # Check words
            split = phrase.split(" ")
            if split[0] == "hey":
                # Get rid of key word "Hey Shimi"
                phrase = " ".join(phrase.split(" ")[2:])

            print("Phrase said: %s" % phrase)

            # Pass the phrase to callback
            if self.on_phrase:
                t = time.time()
                self.on_phrase(self.shimi, phrase, audio_data, doa_value)
                print("Time for phrase processing call [e.g. dialogue()]: %f" % (time.time() - t))
            else:
                # Check callbacks for trigger words
                found_callback = False
                for phrase_callback in self.phrase_callbacks:
                    for trigger in phrase_callback["triggers"]:
                        if trigger in phrase:
                            found_callback = True
                            print("Calling:", phrase_callback["callback"])
                            try:
                                if self.is_thread(phrase_callback["callback"]):
                                    phrase_callback["callback"](self.shimi, phrase=phrase,
                                                                audio_data=audio_data).start()
                                else:
                                    # HACK FOR DEMO
                                    if "args" in phrase_callback:
                                        phrase_callback["callback"](self.shimi, *phrase_callback["args"], phrase=phrase,
                                                                    audio_data=audio_data)
                                    else:
                                        phrase_callback["callback"](self.shimi, phrase=phrase, audio_data=audio_data)
                            except Exception as e:
                                print("Callback failed:", e)
                                break

                            # Only call the first trigger
                            break

                if not found_callback:
                    print("Heard \"%s\", but didn't have any function to pass it to. Calling default." % phrase)
                    if not self.default_callback:
                        print("No default callback.")
                    else:
                        try:
                            default_callback["callback"](self.shimi, phrase, *default_callback["args"])
                        except Exception as e:
                            print("Default callback failed.", e)

    def stop_on_wake_thread(self):
        # Stop thread
        self.on_wake_thread.stop()

        # Wait for it to be done if it isn't
        if self.on_wake_thread._thread.is_alive():
            self.on_wake_thread.join()

    def on_wake_word(self):
        # Call wake function if it exists
        # Handle thread
        if self.on_wake:
            if self.on_wake_is_thread:
                self.on_wake_thread = self.on_wake(self.shimi)
                self.on_wake_thread.start()
            else:
                self.on_wake(self.shimi)

    def is_thread(self, obj):
        try:
            if "StoppableThread" in [cls.__name__ for cls in inspect.getmro(obj)]:
                return True
            else:
                return False
        except:
            return False
