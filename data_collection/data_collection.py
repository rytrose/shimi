import time
import os
import subprocess
from pythonosc import udp_client
from utils.utils import countdown

IP = '127.0.0.1'
PORT = 8002
VIDEO_PATH = "/Users/rytrose/Cloud/GTCMT/shimi/data_collection/video_gestures"
SUBJECT_NUMBER = 0
GESTURE_NUMBER = 1
CAMERA_ANGLE = 2


class DataCollector:
    def __init__(self, openMax=True):
        # Set up OSC sender
        self.client = udp_client.SimpleUDPClient(IP, PORT)

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

    def record_new_gesture(self, subject_number, gesture_number, camera_angle, duration=2.0, wait=3.0):
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

    def play_gesture(self, subject_number, getsure_number, camera_angle):
        filename = None

        print("Finding video...")
        for f in os.listdir(VIDEO_PATH):
            split = f.split("_")
            if len(split) > 3 \
                    and int(split[SUBJECT_NUMBER]) == subject_number \
                    and int(split[GESTURE_NUMBER]) == getsure_number \
                    and int(split[CAMERA_ANGLE]) == camera_angle:
                filename = f

        if not filename:
            print("Could not find a video with subject {0}, gesture {1}, and camera angle {2}.".format(subject_number,
                                                                                                       getsure_number,
                                                                                                       camera_angle))
            return

        # Playback the video
        print("Playing {}...".format(filename))
        filepath = os.path.join(VIDEO_PATH, filename)
        self.client.send_message("/play", filepath)


    def turn_on_camera(self):
        # Turns off the camera
        self.client.send_message("/cameraOn", [])

    def turn_off_camera(self):
        # Turns off the camera
        self.client.send_message("/cameraOff", [])
