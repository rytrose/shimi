from pypot.utils import StoppableThread
from matt.SpeechRecognizer import *
from nltk.corpus import stopwords
from nltk.stem.snowball import EnglishStemmer
from audio.audio import play_opera, play_outkast
import time

class PlaySongDemo(StoppableThread):
    def __init__(self, shimi):
        self.shimi = shimi
        self.trigger_words = trigger_words = ["play", "bump", "listen", "hear"]
        self.stop_words = set(stopwords.words('english'))
        self.stop_words |= {"please", "shimi"}
        self.stemmer = EnglishStemmer()
        self.demos = {
            "opera": play_opera,
            "outkast": play_outkast
        }

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def setup(self):
        # Get the recognizer object
        self.recognizer = SpeechRecognizer(respeaker=True)

        # Calibrate for noise level
        print("Please be quiet for calibration...")
        self.recognizer.calibrate()
        print("...done calibrating.")

    def run(self):
        # Allow the demo to be stopped
        while not self.should_stop():
            # Allow the demo to be paused between queries
            while self.should_pause():
                pass

            # Get a phrase
            phrase = self.recognizer.listenForPhrase()

            # Demo song
            demo_song = self.get_demo_song(phrase)

            # If understood, start the correct demo
            if demo_song:
                # Play demo
                print("Playing {}...".format(demo_song))
                self.demos[demo_song](self.shimi)

    def get_demo_song(self, phrase):
        if not phrase:
            print("I don't understand: {}".format(phrase))
            return None

        phrase = phrase.lower()
        tokenized_phrase = phrase.split(" ")

        # Will hold the index of the trigger word
        start_index = None
        for i in range(len(tokenized_phrase)):
            word = tokenized_phrase[i]
            root_word = self.stemmer.stem(word)
            if root_word in self.trigger_words:
                start_index = i
                break

        # If not found, let the user know
        if start_index is None:
            print("I don't understand: {}".format(phrase))
            return None

        real_phrase = ""
        for i in range(start_index + 1, len(tokenized_phrase)):
            word = tokenized_phrase[i]
            if word in self.stop_words:
                # haven't found a real word after the listen word; keep going until we do
                if real_phrase == "":
                    continue

                # otherwise, we already have found our words, and now we want to ignore the stop words
                # so we break out of loop
                break
            else:
                real_phrase += word + " "

        song_phrase = real_phrase.strip()

        # If nothing was found, tell the user
        if song_phrase == "":
            print("I don't understand: {}".format(phrase))
            return None

        # Check for demo keywords
        if song_phrase in ["outcast", "outkast", "rap", "trap"]:
            return "outkast"
        elif song_phrase in ["opera", "classical", "mozart"]:
            return "opera"
        elif song_phrase in ["justin bieber", "justin", "bieber"]:
            return "bieber"
        else:
            print("I don't understand: {}".format(phrase))
            return None