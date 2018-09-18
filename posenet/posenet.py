import json
import math
import threading
import time
import subprocess
import numpy as np
from utils.utils import *
from pythonosc import dispatcher as osc_dispatcher
from pythonosc import osc_server

TIME_INDEX = 0
POS_INDEX = 1
ERROR = 0.001
INTERP_FREQ = 0.1

class PoseNet:
    def __init__(self, shimi):
        # Reference to Shimi and motor controller
        self.shimi = shimi

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
        dispatcher.map('/posenet', self.posenet_receiver)

        server = osc_server.ThreadingOSCUDPServer(("localhost", 8000), dispatcher)
        server_thread = threading.Thread(target=server.serve_forever)
        server_thread.daemon = True
        server_thread.start()
        print("Listening for OSC at {0} on port {1}".format(server.server_address[0], server.server_address[1]))

        self.posenet = None
        self.start_posenet()

    def posenet_receiver(self, _, pose_string):
        pose = json.loads(pose_string)

        if self.receiving_from_posenet == False:
            print("Receiving from PoseNet...")
            self.receiving_from_posenet = True

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
        normalize_neck_left = lambda t: (t - NECK_LR_L_HEURISTIC_LEFT) / (NECK_LR_L_HEURISTIC_MIDDLE - NECK_LR_L_HEURISTIC_LEFT) / 2

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
        # Count down to recording
        waiting = wait
        while waiting > 0:
            print("{}...".format(waiting))
            sleep_time = min(1.0, waiting)
            waiting -= sleep_time
            time.sleep(sleep_time)

        # Make the recording
        print("Recording...")
        self.recording_duration = duration
        self.recording_start = time.time()

    def play(self):
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

        velocities = []

        # Set initial velocities
        velocities.append([0.1 for _ in range(len(self.motors))])

        # Calculate all velocities except the initial velocity
        for i in range(pos_matrix.shape[0] - 1):
            vels = []
            for j, _ in enumerate(self.motors):
                print("pos1:", pos_matrix[i + 1, j], "pos0:", pos_matrix[i, j], "time1:", self.timestamps[i + 1], "time0:", self.timestamps[i])
                vel = abs(pos_matrix[i + 1, j] - pos_matrix[i, j]) / (self.timestamps[i + 1] - self.timestamps[i])
                vels.append(vel)
            velocities.append(vels)

        vel_matrix = np.array(velocities)


        #########
        # COPIED
        #########
        # Find the positions at which direction change happens, interpolated to INTERP_FREQ [s] increments
        times_positions = [[[], []] for m in self.motors]
        for i, _ in enumerate(self.motors):
            zero_pos = np.interp(0, self.timestamps, pos_matrix[:, i])
            first_pos = np.interp(INTERP_FREQ, self.timestamps, pos_matrix[:, i])
            if first_pos - zero_pos >= 0:
                incr = True
            else:
                incr = False

            t = 2 * INTERP_FREQ
            last_pos = first_pos
            while t < self.recording_duration:
                pos = np.interp(t, self.timestamps, pos_matrix[:, i])
                if incr and last_pos - pos < 0:
                    times_positions[i][TIME_INDEX].append(t)
                    times_positions[i][POS_INDEX].append(last_pos)
                    incr = not incr
                if not incr and last_pos - pos > 0:
                    times_positions[i][TIME_INDEX].append(t)
                    times_positions[i][POS_INDEX].append(last_pos)
                    incr = not incr
                last_pos = pos

                t += INTERP_FREQ

        # Add initial time (which should correspond to the first position change)
        # Add final position (which should correspond to the last position change time)
        for i, _ in enumerate(self.motors):
            times_positions[i][TIME_INDEX].insert(0, 0.0)
            times_positions[i][POS_INDEX].append(pos_matrix[-1, i])

        # Using the times and positions, and the captured speeds, set goal position on change and update speed
        t = 0
        while t < self.recording_duration:
            # Measure the time it takes for updating in order to make the sleep time such that update occurs
            #   as close to INTERP_FREQ as possible
            compute_time = time.time()

            # Queues for setting multiple values at the same time
            motor_pos_to_set = []
            pos_to_set = []
            motor_vel_to_set = []
            vel_to_set = []

            for i, m in enumerate(self.motors):
                # Set a new goal pos if needed
                if len(times_positions[i][TIME_INDEX]) > 0 and abs(times_positions[i][TIME_INDEX][0] - t) <= ERROR:
                    # Note which motor needs to be moved
                    motor_pos_to_set.append(m)

                    # Add position to set queue
                    pos_to_set.append(times_positions[i][POS_INDEX].pop(0))

                    # Remove this position change time
                    times_positions[i][TIME_INDEX].pop(0)

                # Calculate velocity at this point
                motor_vel_to_set.append(m)
                vel_to_set.append(np.interp(t, self.timestamps, vel_matrix[:, i]))

            # Set speeds for all motors
            self.shimi.controller.set_moving_speed(dict(zip(motor_vel_to_set, vel_to_set)))

            # Set new goal positions for those that need it
            if len(motor_pos_to_set) > 0:
                print("Setting positions {}".format(dict(zip(motor_pos_to_set, pos_to_set))))
                self.shimi.controller.set_goal_position(dict(zip(motor_pos_to_set, pos_to_set)))

            # Sleep for INTERP_FREQ [s] minus compute time
            time.sleep(INTERP_FREQ - (time.time() - compute_time))
            t += INTERP_FREQ

    def start_posenet(self):
        print("Starting PoseNet...")
        self.posenet = subprocess.Popen("node posenet/posenet.js", shell=True)

    def stop_posenet(self):
        if(self.posenet):
            print("Stopping PoseNet...")
            self.receiving_from_posenet = False
            self.posenet.kill()
            self.posenet = None

class Point:
    def __init__(self, x, y, score=None):
        self.x = x
        self.y = y
        self.score = score