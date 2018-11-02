import json
import math
import threading
import time
from pprint import pprint
import subprocess
import numpy as np
from utils.utils import *
from motion.playback import playback
from pythonosc import dispatcher as osc_dispatcher
from pythonosc import osc_server

TIME_INDEX = 0
POS_INDEX = 1
ERROR = 0.001
INTERP_FREQ = 0.1


class PoseNet:
    def __init__(self, shimi, on_pred=None):
        # Reference to Shimi and motor controller
        self.shimi = shimi

        # Function for handling real-time predictions from posenet
        #   Should receive the pose in a dict as the first argument
        self.on_prediction = on_pred

        # Start time of recording
        self.recording_start = None

        # Length of recording
        self.recording_duration = None

        # Flag to see if posenet is sending frames
        self.receiving_from_posenet = False

        # self.motors = [self.shimi.torso, self.shimi.neck_lr, self.shimi.neck_ud]
        self.motors = [self.shimi.torso, self.shimi.neck_ud]
        self.positions = []
        self.timestamps = []

        # Start listening for OSC from posenet
        dispatcher = osc_dispatcher.Dispatcher()

        # Listen for messages from PoseNet
        dispatcher.map('/predictions', self.posenet_receiver)

        server = osc_server.ThreadingOSCUDPServer(("localhost", 8000), dispatcher)
        server_thread = threading.Thread(target=server.serve_forever)
        server_thread.daemon = True
        server_thread.start()
        print("Listening for OSC at {0} on port {1}".format(server.server_address[0], server.server_address[1]))

        self.posenet = None
        self.start_posenet()

    def posenet_receiver(self, _, pose_string, fps):
        pose = json.loads(pose_string)

        if self.receiving_from_posenet == False:
            print("Receiving from PoseNet...")
            self.receiving_from_posenet = True

        if self.on_prediction:
            # Call provided function
            self.on_prediction(pose, fps)
        else:
            # Attempt Shimi mapping
            points = pose['keypoints']
            left_ear = None
            right_ear = None
            left_eye = None
            right_eye = None
            nose = None

            for point in points:
                if point['part'] == 'leftEar':
                    left_ear = Point(point['position']['x'], point['position']['y'], point['score'])
                if point['part'] == 'rightEar':
                    right_ear = Point(point['position']['x'], point['position']['y'], point['score'])
                if point['part'] == 'nose':
                    nose = Point(point['position']['x'], point['position']['y'], point['score'])
                if point['part'] == 'leftEye':
                    left_eye = Point(point['position']['x'], point['position']['y'], point['score'])
                if point['part'] == 'rightEye':
                    right_eye = Point(point['position']['x'], point['position']['y'], point['score'])

            #############
            # NECK_UD
            #############
            # Use eye line as reference point for nose
            eye_midpoint = Point((right_eye.x + left_eye.x) / 2.0, (right_eye.y + left_eye.y) / 2.0)
            eye_to_nose_dist = math.sqrt(math.pow(nose.x - eye_midpoint.x, 2) + math.pow(nose.y - eye_midpoint.y, 2))

            NECK_UD_HEURISTIC = 100.0

            # Make sure nose is lower than eyes, as eye_to_nose_dist is always positive
            if nose.y > eye_midpoint.y:
                neck_ud = eye_to_nose_dist / NECK_UD_HEURISTIC

                # Keep in range 0.0 - 1.0
                if neck_ud > 1.0:
                    neck_ud = 1.0
            else:
                # Nose is higher than eyes, neck should be all the way up
                neck_ud = 0.0

            ###########
            # TORSO
            ###########
            # Use the area of eye-nose-ear triangles to gauge bending, smaller --> more upright
            triangle_area = lambda a, b, c: abs((a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y)) / 2)

            left_area = triangle_area(left_eye, left_ear, nose)
            right_area = triangle_area(right_eye, right_ear, nose)

            # Sum the areas
            area = left_area + right_area

            TORSO_HEURISTIC_UP = 5000.0
            TORSO_HEURISTIC_DOWN = 50000.0

            # Normalize area between heuristics
            normalize_torso = lambda t: (t - TORSO_HEURISTIC_UP) / (TORSO_HEURISTIC_DOWN - TORSO_HEURISTIC_UP)
            torso = normalize_torso(area)

            # Keep in range 0.0 - 1.0
            if torso > 1.0:
                torso = 1.0
            if torso < 0.0:
                torso = 0.0

            torso = abs(1.0 - torso)

            ############
            # NECK_LR
            ############
            NECK_LR_L_HEURISTIC_LEFT = 0.1
            NECK_LR_L_HEURISTIC_MIDDLE = 1.0

            NECK_LR_R_HEURISTIC_MIDDLE = 1.0
            NECK_LR_R_HEURISTIC_RIGHT = 10.0

            # Normalizes to 0.0 - 0.5
            normalize_neck_left = lambda t: (t - NECK_LR_L_HEURISTIC_LEFT) / (
                    NECK_LR_L_HEURISTIC_MIDDLE - NECK_LR_L_HEURISTIC_LEFT) / 2

            # Normalizes to 0.5 - 1.0
            normalize_neck_right = lambda t: 0.5 + (1.0 - math.pow(0.3, t / NECK_LR_R_HEURISTIC_RIGHT))

            # Use the ratio of eye-nose-ear triangle areas to determine left right
            ratio = left_area / right_area

            if left_ear.score < 0.2:
                neck_lr = 0.0
            elif right_ear.score < 0.2:
                neck_lr = 1.0
            else:
                if ratio < 1:
                    # Moving left
                    neck_lr = normalize_neck_left(ratio)
                else:
                    # Moving right
                    neck_lr = normalize_neck_right(ratio)

            # Record positions
            if self.recording_start:
                if (self.recording_start + self.recording_duration) - time.time() > 0:
                    # Record
                    self.timestamps.append(time.time() - self.recording_start)
                    # pos = [torso, neck_lr, neck_ud]
                    pos = [torso, neck_ud]
                    self.positions.append(pos)
                else:
                    # Stop recording by setting record parameters to None
                    print("Done. Recorded {0} positions.".format(len(self.positions)))
                    self.recording_start = None

    def record(self, duration=5.0, wait=3.0):
        # Erase previous recording if any
        self.positions = []

        # Count down to recording
        countdown(wait)

        # Make the recording
        print("Recording...")
        self.recording_duration = duration
        self.recording_start = time.time()

    def play(self, pos_ax=None, vel_ax=None):
        # If there was nothing recorded, return
        if len(self.positions) == 0:
            print("No recording to play.")
            return

        # Covert the position measurements to rows for each motor
        pos_matrix = np.array(self.positions)

        # Denormalize the position matrix
        for i in range(pos_matrix.shape[0]):
            for j, m in enumerate(self.motors):
                pos_matrix[i, j] = denormalize(m, pos_matrix[i, j])

        # Playback
        playback(self.shimi, self.motors, self.recording_duration, self.timestamps, pos_matrix, None, pos_ax, vel_ax)

    def start_posenet(self):
        print("Starting PoseNet...")
        self.posenet = subprocess.Popen(
            "/usr/bin/python3.5 posenet/posenet_python/posenet_model_python.py --path posenet/posenet_python --ip 127.0.0.1 --port 8000",
            shell=True)

    def stop_posenet(self):
        if (self.posenet):
            print("Stopping PoseNet...")
            self.receiving_from_posenet = False
            try:
                self.posenet.kill()
            except Exception:
                pass
            self.posenet = None
