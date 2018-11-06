import sys, os

# Add parent to path
sys.path.insert(1, os.path.join(sys.path[0], '..'))

from shimi import Shimi
from posenet.posenet import PoseNet
from utils.utils import Point, normalize_position, denormalize_position, denormalize_to_range, quantize
from audio.midi_analysis import MidiAnalysis
from motion.move import Move
import time


class GenerativePhrase:
    def __init__(self, shimi=None):
        if shimi:
            self.shimi = shimi
        else:
            self.shimi = Shimi()
        self.posenet = PoseNet(self.shimi, on_pred=self.on_posenet_prediction)
        self.face_track = False
        self.update_freq = 0.1
        self.last_update = time.time()
        self.last_pos = 0.5

    def on_posenet_prediction(self, pose, fps):
        # **N.B.** For simplification, this isn't being loaded from the config.yaml, where it is defined.
        #   I don't want to deal with the path nonsense at the moment, but could be a TODO
        POSENET_HEIGHT = 513
        POSENET_WIDTH = 513

        points = pose['keypoints']

        # Use nose as point of reference for face tracking
        nose = None
        for point in points:
            if point['part'] == 'nose':
                nose = Point(point['position']['x'], point['position']['y'], point['score'])

        if nose:
            SCORE_THRESH = 0.7
            MOVE_THRESH = 0.05
            MIN_VEL = 40
            MAX_VEL = 100

            if nose.score > SCORE_THRESH:
                if time.time() > self.last_update + self.update_freq:
                    # Calculate where to look
                    #  Camera image is flipped
                    pos = 1 - (nose.x / POSENET_WIDTH)

                    # Calculate speed based on how far to move
                    current_pos = normalize_position(self.shimi.neck_lr,
                                                     self.shimi.controller.get_present_speed([self.shimi.neck_lr])[0])
                    vel = max(MIN_VEL + abs(current_pos - pos) * MAX_VEL, MIN_VEL)

                    if abs(self.last_pos - pos) > MOVE_THRESH:
                        # Only actually move the motors if specified
                        if self.face_track:
                            self.shimi.controller.set_moving_speed({self.shimi.neck_lr: vel})
                            self.shimi.controller.set_goal_position(
                                {self.shimi.neck_lr: denormalize_position(self.shimi.neck_lr, pos)})

                        self.last_pos = pos

                    self.last_update = time.time()

    def generate(self, path, valence, arousal):
        # Analyze the MIDI
        self.midi_analysis = MidiAnalysis(path)
        tempo = self.midi_analysis.get_tempo()
        length = self.midi_analysis.get_length()

        # Create the motor moves
        moves = []
        foot = self.foot_movement(tempo, length, valence, arousal)
        moves.append(foot)

        # Start all the moves
        for move in moves:
            move.start()

        # For testing, play the MIDI file back
        self.midi_analysis.play()

        # Wait for all the moves to stop
        for move in moves:
            move.join()

    def torso_neck_movement(self, tempo, length, valence, arousal):
        pass

    def foot_movement(self, tempo, length, valence, arousal):
        # Calculate how often it taps its foot based on arousal
        quantized_arousals = [-1, -0.2, 0, 1]
        quantized_arousal = quantize(arousal, quantized_arousals)

        # Higher arousal --> smaller subdivision of tapping
        beat_periods = [4 * tempo, 2 * tempo, tempo, 0.5 * tempo]
        beat_period = beat_periods[quantized_arousals.index(quantized_arousal)]

        move_dist = 1.0
        move_dur = beat_period / 2
        move_wait = 0.0

        if valence < 0:
            # Lower valence --> shorter movement, faster
            neg_norm = 1 + valence
            # Make sure it moves at least 0.2
            move_dist = denormalize_to_range(neg_norm, 0.2, 1.0)
            # Make sure it's moving for at least 0.1s
            move_dur = denormalize_to_range(neg_norm, 0.1, 1.0) * move_dur
            move_wait = (beat_period / 2) - move_dur

        # Params for the linear accel/decel moves
        up_change_time = 0.7
        down_change_time = 0.4

        # Wait half of a beat to start, so the ictus is on foot down
        move = Move(self.shimi, self.shimi.foot, move_dist, move_dur,
                    vel_algo='linear_a',
                    vel_algo_kwarg={'change_time': up_change_time},
                    freq=0.04, initial_delay=(beat_period / 2))
        move.add_move(0.0, move_dur,
                      vel_algo='linear_d',
                      vel_algo_kwarg={'change_time': down_change_time},
                      delay=move_wait)
        t = 2 * (move_dur + move_wait)

        while t < length:
            move.add_move(move_dist, move_dur,
                          vel_algo='linear_a',
                          vel_algo_kwarg={'change_time': up_change_time},
                          delay=move_wait)
            move.add_move(0.0, move_dur,
                          vel_algo='linear_d',
                          vel_algo_kwarg={'change_time': down_change_time},
                          delay=move_wait)
            t += 2 * (move_dur + move_wait)

        return move
