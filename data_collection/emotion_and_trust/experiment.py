import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '../..'))
from motion.generative_phrase import GenerativePhrase

from pythonosc import osc_server, dispatcher, udp_client
from subprocess import Popen, PIPE
import threading
import time
import random
import os.path as op
import pickle
from copy import deepcopy
import pygame.mixer as mixer


class EmotionTrustExperiment:
    def __init__(self, group=True):
        # Running the Node.js web server
        command_string = "node web/index.js"
        self.node_server = Popen(command_string.split(' '), stdout=PIPE, bufsize=1, universal_newlines=True)

        # Wait until the server is running to continue
        for line in self.node_server.stdout.readline():
            if line == "GO":
                print("Server running.")
                break

        # Open the webpage
        command_string = "chromium-browser http://localhost:8009"
        self.browser = Popen(command_string.split(' '))

        # Dispatches received messages to callbacks
        self.dispatcher = dispatcher.Dispatcher()

        self.dispatcher.map("/get_trial", self.on_get_trial)
        self.dispatcher.map("/trial_result", self.on_trial_result)

        # Server for listening for OSC messages
        self.local_address = "127.0.0.1"
        self.local_port = 6003
        self.osc_server = osc_server.ThreadingOSCUDPServer((self.local_address, self.local_port), self.dispatcher)

        # Listen (in a separate thread as to not block)
        threading.Thread(target=self.osc_server.serve_forever).start()

        # Client for sending OSC messages
        self.remote_address = "127.0.0.1"
        self.remote_port = 6002
        self.osc_client = udp_client.SimpleUDPClient(self.remote_address, self.remote_port)

        self.group = group

        if self.group:
            self.group_name = "shimi"
            self.audio_path = op.join("audio", self.group_name)
        else:
            self.group_name = "text"
            self.audio_path = op.join("audio", self.group_name)

        self.quadrant_names = ['happy', 'calm', 'sad', 'angry']

        self.trials = ["audio_only", "random_movement", "generated_movement"]

        self.audio_only_files = ["happy_1", "happy_2", "sad_1", "sad_2", "angry_1", "angry_2", "calm_1", "calm_2"]
        self.random_movement_audio_files = ["happy_1", "happy_2", "sad_1", "sad_2", "angry_1", "angry_2", "calm_1",
                                            "calm_2"]
        self.random_movement_no_audio = ["happy_1", "happy_2", "sad_1", "sad_2", "angry_1", "angry_2", "calm_1",
                                         "calm_2"]
        self.generated_movement_audio_files = ["happy_1", "happy_2", "sad_1", "sad_2", "angry_1", "angry_2", "calm_1",
                                               "calm_2"]
        self.generated_movement_no_audio = ["happy_1", "happy_2", "sad_1", "sad_2", "angry_1", "angry_2", "calm_1",
                                            "calm_2"]

        self.current_trial = None
        self.results = {
            "group": self.group_name,
            "results": []
        }

        self.generator = GenerativePhrase()

        mixer.init()

    def on_get_trial(self, _):
        if not self.trials:
            self.complete()

        stimulus = None

        while stimulus is None:
            if not self.trials:
                self.complete()
                return

            trial_type = random.choice(self.trials)
            if trial_type == "audio_only":
                with_audio = True
            else:
                with_audio = random.choice([True, False])

            if trial_type == "audio_only":
                if len(self.audio_only_files) > 0:
                    stimulus = random.choice(self.audio_only_files)
                    self.audio_only_files.remove(stimulus)
                    continue
                else:
                    self.trials.remove("audio_only")
                    continue
            elif trial_type == "random_movement":
                if with_audio:
                    if len(self.random_movement_audio_files) > 0:
                        stimulus = random.choice(self.random_movement_audio_files)
                        self.random_movement_audio_files.remove(stimulus)
                else:
                    if len(self.random_movement_no_audio) > 0:
                        stimulus = random.choice(self.random_movement_no_audio)
                        self.random_movement_no_audio.remove(stimulus)

                if stimulus is None:
                    self.trials.remove("random_movement")
                    continue
            else:
                if with_audio:
                    if len(self.generated_movement_audio_files) > 0:
                        stimulus = random.choice(self.generated_movement_audio_files)
                        self.generated_movement_audio_files.remove(stimulus)
                else:
                    if len(self.generated_movement_no_audio) > 0:
                        stimulus = random.choice(self.generated_movement_no_audio)
                        self.generated_movement_no_audio.remove(stimulus)

                if stimulus is None:
                    self.trials.remove("generated_movement")
                    continue

        ground_truth = stimulus.split("_")[0]

        self.current_trial = {
            "trial_type": trial_type,
            "with_audio": with_audio,
            "ground_truth": ground_truth,
            "reported": None
        }

        base_filename = op.join(self.audio_path, stimulus)

        # Actually run the trial
        if trial_type == "audio_only":
            mixer.music.load(base_filename + ".wav")
            mixer.music.play()
            self.osc_client.send_message("/trial_started", [])
            print("Running trial %s for stimulus %s, with_audio %s" % (trial_type, stimulus, str(with_audio)))
            self.wait_for_playback()  # Blocks until complete
        else:
            valence, arousal = self.get_valence_arousal_from_quadrant(ground_truth)
            if with_audio:
                if trial_type == "random_movement":
                    self.osc_client.send_message("/trial_started", [])
                    print("Running trial %s for stimulus %s, with_audio %s" %
                          (trial_type, stimulus, str(with_audio)))
                    self.generator.generate(base_filename + ".mid", valence, arousal, wav_path=base_filename + ".wav",
                                            random_movement=True, seed=stimulus)
                else:
                    self.osc_client.send_message("/trial_started", [])
                    print("Running trial %s for stimulus %s, with_audio %s" %
                          (trial_type, stimulus, str(with_audio)))
                    self.generator.generate(base_filename + ".mid", valence, arousal, wav_path=base_filename + ".wav")
            else:
                if trial_type == "random_movement":
                    self.osc_client.send_message("/trial_started", [])
                    print("Running trial %s for stimulus %s, with_audio %s" %
                          (trial_type, stimulus, str(with_audio)))
                    self.generator.generate(base_filename + ".mid", valence, arousal, mute=True, random_movement=True,
                                            seed=stimulus)
                else:
                    self.osc_client.send_message("/trial_started", [])
                    print("Running trial %s for stimulus %s, with_audio %s" %
                          (trial_type, stimulus, str(with_audio)))
                    self.generator.generate(base_filename + ".mid", valence, arousal, mute=True)

        self.osc_client.send_message("/trial_ended", [])

    def on_trial_result(self, _, reported_id):
        reported = self.quadrant_names[int(reported_id)]
        self.current_trial["reported"] = reported
        self.results["results"].append(deepcopy(self.current_trial))
        self.current_trial = None

    def complete(self):
        self.osc_client.send_message("/complete", [])
        output_name = op.join("results", time.strftime("%m%d%y_%H%M%S.p"))
        pickle.dump(self.results, open(output_name, "wb"))
        print("Completed trials, opening survey.")
        command_string = "chromium-browser https://gatech.co1.qualtrics.com/jfe/form/SV_b288WeOXsbcKAYZ"
        self.browser = Popen(command_string.split(' '))

    def wait_for_playback(self):
        while mixer.music.get_busy():
            continue

    def get_valence_arousal_from_quadrant(self, quadrant):
        if quadrant == "happy":
            return 0.5, 0.5
        elif quadrant == "calm":
            return 0.5, -0.5
        elif quadrant == "sad":
            return -0.5, -0.5
        else:
            return -0.5, 0.5


if __name__ == '__main__':
    e = EmotionTrustExperiment()
