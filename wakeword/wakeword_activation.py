from matt.SpeechRecognizer import SpeechRecognizer
from matt.PorcupineReader import PorcupineReader
from pypot.utils import StoppableThread

class WakeWord(StoppableThread):
    def __init__(self, on_wake=None, on_phrase=None, respeaker=False):
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
            if self.on_wake:
                self.on_wake()

            # Start to listen for a phrase
            phrase = self.speech_recognizer.listenForPhrase()

            if phrase is None:
                # TODO: handle nothing said/heard
                pass

            # Pass the phrase to callback
            if self.on_phrase:
                self.on_phrase(phrase)
            else:
                print("Heard \"%s\", but didn't have any function to pass it to." % phrase)

        # Reinitialize porcupine
        self.porcupine = PorcupineReader(input_device_index=1)
        print("No longer listening.")

    def stop(self, wait=True):
        self.porcupine.stop = True
        super().stop(wait)