import json
import math
import threading
import time
from pprint import pprint
from pythonosc import dispatcher as osc_dispatcher
from pythonosc import osc_server

positions = []
timestamps = []

class PoseNet:
    def __init__(self, shimi):
        # Reference to Shimi and motor controller
        self.shimi = shimi

        # Start time of recording
        self.recording_start = None

        # Length of recording
        self.recording_duration = None

        # Start listening for OSC from posenet
        dispatcher = osc_dispatcher.Dispatcher()

        # Listen for messages from PoseNet
        dispatcher.map('/posenet', self.posenet_receiver)

        server = osc_server.ThreadingOSCUDPServer(("localhost", 8000), dispatcher)
        server_thread = threading.Thread(target=server.serve_forever)
        server_thread.daemon = True
        server_thread.start()
        print("Listening for OSC at {0} on port {1}".format(server.server_address[0], server.server_address[1]))

    def posenet_receiver(self, _, pose_string):
        pose = json.loads(pose_string)
        print("received frame")

        points = pose['keypoints']
        for point in points:
            if point['part'] == 'leftEar':
                left_ear = Point(point['position']['x'], point['position']['y'])
            if point['part'] == 'rightEar':
                right_ear = Point(point['position']['x'], point['position']['y'])
            if point['part'] == 'nose':
                nose = Point(point['position']['x'], point['position']['y'])
            if point['part'] == 'leftEye':
                left_eye = Point(point['position']['x'], point['position']['y'])
            if point['part'] == 'rightEye':
                right_eye = Point(point['position']['x'], point['position']['y'])

        eye_midpoint = Point((right_eye.x + left_eye.x) / 2.0, (right_eye.y + left_eye.y) / 2.0)
        eye_to_nose_dist = math.sqrt(math.pow(nose.x - eye_midpoint.x, 2) + math.pow(nose.y - eye_midpoint.y, 2))

        # Make sure nose is lower than eyes, as eye_to_nose_dist is always positive
        if nose.y < eye_midpoint.y:
            neck_ud = (eye_to_nose_dist) / 100.0

            # Keep in range 0.0-1.0
            if neck_ud > 1.0:
                neck_ud = 1.0
        else:
            neck_ud = 0.0

        # Record positions
        if self.recording_start:
            if time.time() - (self.recording_start + self.recording_duration) > 0:
                # Record
                pass
            else:
                # Stop recording by setting record parameters to None
                self.recording_start = None
                self.recording_duration = None

class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y