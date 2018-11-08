from matt.SpeechRecognizer import SpeechRecognizer
from matt.PorcupineReader import PorcupineReader
from pypot.utils import StoppableThread
from shimi import Shimi
from audio.audio_demos import play_opera, play_outkast
from motion.move import No
import inspect

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

]


class WakeWord(StoppableThread):
    def __init__(self, shimi=None, on_wake=None, on_phrase=None, respeaker=False):
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
        self.on_wake = on_wake
        self.on_phrase = on_phrase
        self.porcupine = PorcupineReader()
        self.speech_recognizer = SpeechRecognizer(respeaker=respeaker)

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def setup(self):
        print("Please be quiet for microphone calibration...")
        self.speech_recognizer.calibrate()
        print("...finished.")

    def run(self):
        while not self.should_stop():
            # Blocks until key word "hey Shimi" is spoken
            print("Listening for key word...")
            res = self.porcupine.waitForKeyword()

            # Cancelled
            if res is False:
                break

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
                continue

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
                                #
                                if self.is_thread(phrase_callback["callback"]):
                                    phrase_callback["callback"](self.shimi, phrase=phrase).start()
                                else:
                                    phrase_callback["callback"](phrase)
                            except Exception as e:
                                print("Callback failed:", e)
                                break

                            # Only call the first trigger
                            break

                if not found_callback:
                    print("Heard \"%s\", but didn't have any function to pass it to." % phrase)

        # Reinitialize porcupine
        self.porcupine = PorcupineReader(input_device_index=1)
        print("No longer listening.")

    def stop(self, wait=True):
        self.porcupine.stop = True
        super().stop(wait)

    def is_thread(self, obj):
        try:
            if "StoppableThread" in [cls.__name__ for cls in inspect.getmro(obj)]:
                return True
            else:
                return False
        except:
            return False
