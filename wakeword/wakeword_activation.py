import os
import sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))
import time
from wakeword.speech_recognizer import SpeechRecognizer, SpeechRecognizerClient
from pypot.utils import StoppableThread
from shimi import Shimi
from audio.audio_demos import play_opera, play_outkast
from motion.move import No
from motion.generative_phrase import GenerativePhrase
import inspect
import random
import wakeword.snowboydecoder as snowboydecoder


# N.B. All callbacks should take one argument, an instance of Shimi, and accept arbitrary keyword arguments
PHRASE_CALLBACKS = [
    {
        "triggers": ["play opera", "sing opera"],
        "callback": play_opera,
    }
]


class WakeWordClient:
    """Provides interface for the wakeword detection process."""

    def __init__(self, shimi=None, models=["wakeword/resources/models/Hey-Shimi-Ryan1.pmdl"],
                 phrase_callbacks=PHRASE_CALLBACKS,
                 default_callback=None,
                 on_wake=None, on_phrase=None, on_phrase_heard=None,
                 respeaker=False, posenet=False, manual_wake=False):
        """Intializes callbacks and starts wakeword process.
            shimi (Shimi, optional): An instance of the Shimi motor controller class.
            models (list, optional): Defaults to ["wakeword/resources/models/Hey-Shimi-Ryan1.pmdl"]. List of paths to wakeword model files.
            phrase_callbacks (dict, optional): Defaults to PHRASE_CALLBACKS. A dict of objects containing an array of trigger words and a callback function to run on trigger word detection.
            default_callback (function, optional): Defaults to None. A function to call if no trigger word is found.
            on_wake (function, optional): Defaults to None. A function to call when the wake word is heard.
            on_phrase (function, optional): Defaults to None. A function to call when a phrase has been processed into text.
            on_phrase_heard (function, optional): Defaults to None. A function to call when a phrase has been heard, but before it has been processed.
            respeaker (bool, optional): Defaults to False. Determines whether to look for the ReSpeaker microphone inside of Shimi.
            posenet (bool, optional): Defaults to False. Determines whether or not to start the PoseNet process.
            manual_wake (bool, optional): Defaults to False. Determines whether to bypass microphone input and wake Shimi with just keyboard input.
        """
        if shimi is not None:
            self.shimi = shimi
        else:
            self.shimi = Shimi()

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
        """Runs the speech recognition client, or prompts for keyboard input."""
        if self.manual_wake:
            print("Press enter to wake Shimi.")
            input()
            self.on_wake_wrapper()

        # Start the speech recognition client
        self.speech_recognizer_client.listen()

    def on_wake_wrapper(self):
        """Wraps on_wake function, allowing StoppableThread objects to be used as on_wake."""
        # Call wake function if it exists
        # Handle thread
        if self.on_wake:
            if self.on_wake_is_thread:
                self.on_wake_thread = self.on_wake(self.shimi)
                self.on_wake_thread.start()
            else:
                self.on_wake(self.shimi)

    def on_phrase_heard_wrapper(self):
        """Wraps on_phrase_heard function, allowing StoppableThread objects to be used as on_phrase_heard."""
        if self.on_phrase_heard:
            self.on_phrase_heard()

        if self.on_wake_is_thread:
            self.stop_on_wake_thread()

    def on_phrase_wrapper(self, audio_data, sample_rate, phrase):
        """Wraps on_phrase function, allowing allowing StoppableThread objects to be used as on_phrase.

        Also receives and passes along input audio data.

        Args:
            audio_data (list): Raw PCM input audio data.
            sample_rate (int): Sample rate of recorded input audio.
            phrase (str): Text transcription of audio input.
        """
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
                print(
                    "Heard \"%s\", but didn't have any function to pass it to. Calling default." % phrase)
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
        """Stops the on_wake_thread StoppableThread."""
        # Stop thread
        self.on_wake_thread.stop()

        # Wait for it to be done if it isn't
        if self.on_wake_thread._thread.is_alive():
            self.on_wake_thread.join()

    def is_thread(self, obj):
        """Helper to check if an object is a StoppableThread."""
        try:
            if "StoppableThread" in [cls.__name__ for cls in inspect.getmro(obj)]:
                return True
            else:
                return False
        except:
            return False
