import sys
import os

# Add this dir to path
sys.path.insert(1, os.path.join(sys.path[0], '.'))

from matt.SpeechRecognizer import SpeechRecognizer
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
    },
    {
        "triggers": ["play outcast", "play outkast"],
        "callback": play_outkast
    },
    {
        "triggers": ["justin bieber"],
        "callback": No
    },
    {
        "triggers": ["test speak", "say something"],
        "callback": generate_phrase,
        "args": ("audio/test.mid",)
    }
]


class WakeWord(StoppableThread):
    def __init__(self, shimi=None, model="wakeword/resources/models/Hey-Shimi2.pmdl", on_wake=None, on_phrase=None,
                 respeaker=False):
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
        phrase_generator = GenerativePhrase(shimi=shimi)

        self.on_wake = on_wake
        self.on_phrase = on_phrase
        self.snowboy = snowboydecoder.HotwordDetector(model, sensitivity=0.5)
        self.hotword_detection = False
        self.respeaker = respeaker
        self.speech_recognizer = SpeechRecognizer(respeaker=self.respeaker)

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def setup(self):
        print("Please be quiet for microphone calibration...")
        self.speech_recognizer.calibrate()
        print("...finished.")

    def run(self):
        print("Listening for hotword...")
        self.start_hotword_detection()

        while not self.should_stop():
            paused = False
            while self.should_pause():
                paused = True
                print("Pausing hotword detection.")
                self.hotword_detection = False

            if paused:
                paused = False
                print("Resuming hotword detection.")
                self.hotword_detection = True

        self.snowboy.terminate()

    def start_hotword_detection(self):
        self.hotword_detection = True
        self.snowboy.start(detected_callback=self.on_hotword,
               interrupt_check=self.hotword_status,
               sleep_time=0.03)

    def stop_hotword_detection(self):
        self.hotword_detection = False

    def hotword_status(self):
        return not self.hotword_detection

    def on_hotword(self):
        # Call wake function if it exists
        # Handle thread
        thread = None
        is_thread = False
        if self.on_wake:
            is_thread = self.is_thread(self.on_wake)
            if is_thread:
                thread = self.on_wake(self.shimi)
                thread.start()
            else:
                self.on_wake(self.shimi)

        # Start to listen for a phrase
        phrase = self.speech_recognizer.listenForPhrase()

        # Phrase listening done, stop on_wake if thread
        if is_thread:
            # Stop thread
            thread.stop()

            # Wait for it to be done if it isn't
            if thread._thread.is_alive():
                thread.join()

        if phrase is None:
            # TODO: handle nothing said/heard
            return

        # Make phrase lowercase
        phrase = phrase.lower()

        # Pass the phrase to callback
        if self.on_phrase:
            self.on_phrase(phrase)
        else:
            # Check callbacks for trigger words
            found_callback = False
            for phrase_callback in PHRASE_CALLBACKS:
                for trigger in phrase_callback["triggers"]:
                    if trigger in phrase:
                        found_callback = True
                        print("Calling:", phrase_callback["callback"])
                        try:
                            if self.is_thread(phrase_callback["callback"]):
                                phrase_callback["callback"](self.shimi, phrase=phrase).start()
                            else:
                                # HACK FOR DEMO
                                if "args" in phrase_callback:
                                    phrase_callback["callback"](self.shimi, phrase, *phrase_callback["args"])
                                else:
                                    phrase_callback["callback"](self.shimi)
                        except Exception as e:
                            print("Callback failed:", e)
                            break

                        # Only call the first trigger
                        break

            if not found_callback:
                print("Heard \"%s\", but didn't have any function to pass it to." % phrase)

    def is_thread(self, obj):
        try:
            if "StoppableThread" in [cls.__name__ for cls in inspect.getmro(obj)]:
                return True
            else:
                return False
        except:
            return False
