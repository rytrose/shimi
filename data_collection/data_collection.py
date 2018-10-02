import time
import os
import subprocess
import threading
from pythonosc import udp_client
from pythonosc import dispatcher
from pythonosc import osc_server
from utils.utils import countdown
from motion.recorder import *

IP = '127.0.0.1'
PORT = 8002
DATA_COLLECTION_PATH = "/Users/rytrose/Cloud/GTCMT/shimi/data_collection"
VIDEO_PATH = DATA_COLLECTION_PATH + "/video_gestures"
SHIMI_PATH = DATA_COLLECTION_PATH + "/shimi_gestures"
SUBJECT_NUMBER = 0
GESTURE_NUMBER = 1
CAMERA_ANGLE = 2
MAX_TRIES = 100


class DataCollector:
    def __init__(self, openMax=True):
        # Set up OSC sender
        self.client = udp_client.SimpleUDPClient(IP, PORT)

        # Set up OSC receiver dispatcher
        self.dispatcher = dispatcher.Dispatcher()

        self.durations = []
        self.dispatcher.map("/duration", self.receive_duration)

        # Set up OSC receiver
        self.server = osc_server.ThreadingOSCUDPServer(("127.0.0.1", 8003), self.dispatcher)
        print("Listening to Max on {}".format(self.server.server_address))
        threading.Thread(target=self.server.serve_forever).start()

        # Open Max patch if needed
        if openMax:
            print("Opening Max...")
            self.max = subprocess.Popen(
                ["open", "-a", "/Applications/Max.app",
                 "/Users/rytrose/Cloud/GTCMT/shimi/data_collection/data_collection.maxpat"])

            # Wait for Max to open
            time.sleep(15)
            print("Make sure the camera windows are on the screen!")

        # Turn camera on
        self.client.send_message("/cameraOn", [])

        print("If the camera is not on, please run turn_on_camera().")

    def record_new_video_gesture(self, subject_number, gesture_number, camera_angle, duration=2.0, wait=3.0):
        # Record the time
        timestamp = time.strftime("%m%d%y_%H%M%S")

        # Specify the name and path
        filename = "{0}_{1}_{2}_{3}.mov".format(subject_number, gesture_number, camera_angle, timestamp)
        filepath = os.path.join(VIDEO_PATH, filename)

        # Count down to recording
        countdown(wait)

        # Make the recording
        print("Recording...")
        self.client.send_message("/record", filepath)
        time.sleep(duration)
        print("Finished recording!")

        # Stop the recording
        self.client.send_message("/stopRecord", [])

    def play_video_gesture(self, subject_number, gesture_number, camera_angle):
        # Get video filename
        filename = self.get_video_filename(subject_number, gesture_number, camera_angle)

        # Return if unable to find that gesture video
        if not filename:
            return

        # Playback the video
        print("Playing {}...".format(filename))
        filepath = os.path.join(VIDEO_PATH, filename)
        self.client.send_message("/play", filepath)

    def record_new_shimi_gesture(self, shimi, subject_number, gesture_number, camera_angle, wait=3.0):
        # Get appropriate video file
        filename = self.get_video_filename(subject_number, gesture_number, camera_angle)

        # Return if unable to find that gesture video
        if not filename:
            print("Must have an associated video gesture to record a gesture for Shimi.")
            return

        filepath = os.path.join(VIDEO_PATH, filename)

        # Get duration of video from Max
        self.request_duration(filepath)
        duration = self.get_duration()

        # Return if unable to get the length of video from Max
        if not duration:
            print("Unable to get the duration of the associated video gesture.")
            return

        # Put Shimi in initial position
        shimi.initial_position()

        # Create new recorder for neck and torso
        recorder = Recorder(shimi, [shimi.torso, shimi.neck_lr, shimi.neck_ud], duration, wait_time=0)

        # Countdown to record
        countdown(wait)

        # Start the video playback and signal Shimi recording
        self.client.send_message("/play", filepath)
        self.client.send_message("/recordShimi", [])

        # Start Shimi recording
        recorder.record()

        # Shimi is done recording, tell Max
        self.client.send_message("/stopShimi", [])

        return recorder

    def play_video_and_shimi(self, shimi, subject_number, gesture_number, camera_angle):
        # Get appropriate video file
        video_filename = self.get_video_filename(subject_number, gesture_number, camera_angle)

        # Return if unable to find that gesture video
        if not video_filename:
            print("Could not find video gesture to play.")
            return

        video_filepath = os.path.join(VIDEO_PATH, video_filename)

        # Get appropriate gesture recorder
        gesture_filename = self.get_shimi_gesture(subject_number, gesture_number)[:-2]
        recorder = load_recorder(shimi, gesture_filename, path=SHIMI_PATH)

        start_video = lambda: self.client.send_message("/play", video_filepath)

        # Start recorder playback, with callback to trigger video
        recorder.play(callback=start_video)

    def get_shimi_gesture(self, subject_number, gesture_number):
        print("Finding shimi gesture...")
        filename = None
        for f in os.listdir(SHIMI_PATH):
            split = f.split("_")
            if len(split) > 3 \
                    and int(split[SUBJECT_NUMBER]) == subject_number \
                    and int(split[GESTURE_NUMBER]) == gesture_number:
                filename = f

        if not filename:
            print("Could not find a gesture with subject {0} and gesture {1}.".format(subject_number,
                                                                                      gesture_number))
        return filename

    def get_video_filename(self, subject_number, gesture_number, camera_angle):
        print("Finding video...")
        filename = None
        for f in os.listdir(VIDEO_PATH):
            split = f.split("_")
            if len(split) > 3 \
                    and int(split[SUBJECT_NUMBER]) == subject_number \
                    and int(split[GESTURE_NUMBER]) == gesture_number \
                    and int(split[CAMERA_ANGLE]) == camera_angle:
                filename = f

        if not filename:
            print("Could not find a video with subject {0}, gesture {1}, and camera angle {2}.".format(subject_number,
                                                                                                       gesture_number,
                                                                                                       camera_angle))
        return filename

    def save_shimi_gesture(self, recorder, subject_number, gesture_number):
        # Record the time
        timestamp = time.strftime("%m%d%y_%H%M%S")

        # Specify the name
        filename = "{0}_{1}_{2}".format(subject_number, gesture_number, timestamp)

        # Save to correct folder
        recorder.save(filename, path=SHIMI_PATH)

    def request_duration(self, filepath):
        # Request duration for a video
        self.client.send_message("/getDuration", filepath)

    def receive_duration(self, address, duration):
        # Add duration to list
        self.durations.append(duration)

    def get_duration(self):
        duration = None
        tries = 0
        # Wait for Max to send a duration back
        while not duration and tries < MAX_TRIES:
            if len(self.durations) > 0:
                duration = self.durations.pop(0)
            time.sleep(0.05)
            tries += 1
        return duration

    def turn_on_camera(self):
        # Turns off the camera
        self.client.send_message("/cameraOn", [])

    def turn_off_camera(self):
        # Turns off the camera
        self.client.send_message("/cameraOff", [])
