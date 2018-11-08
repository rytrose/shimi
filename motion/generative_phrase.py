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
        torso, neck_ud = self.torso_neck_movement(tempo, length, valence, arousal)

        if torso is not None:
            moves.append(torso)
        if neck_ud is not None:
            moves.append(neck_ud)

        # Start all the moves
        for move in moves:
            move.start()

        # For testing, play the MIDI file back
        self.midi_analysis.play()

        # Wait for all the moves to stop
        for move in moves:
            move.join()

        self.shimi.initial_position()

    def torso_neck_movement(self, tempo, length, valence, arousal):
        contour_notes = self.midi_analysis.get_normalized_pitch_contour()

        # Higher valence --> more rapid matching to pitch contour
        smoothing_time = 0
        if valence >= 0:
            shortest_note_length = self.midi_analysis.get_shortest_note_length()
            longest_note_length = self.midi_analysis.get_longest_note_length()
            difference = longest_note_length - shortest_note_length
            smoothing_time = shortest_note_length + ((1 - valence) * difference)

        # Higher arousal --> larger range of motion
        adjusted_arousal = (arousal + 1) / 2
        # Caps torso between 0.3-1.0
        torso_min = 0.7 + (0.10 * (1.0 - adjusted_arousal))
        torso_max = 0.95 + (0.05 * adjusted_arousal)
        # Caps neck between 0.05-1.0
        neck_min = 0.05 * (1.0 - adjusted_arousal)
        neck_max = 0.15 + (0.85 * adjusted_arousal)

        try:
            # Keep track of timeline
            t = 0

            # Handle first note
            first_note = contour_notes.pop(0)
            initial_delay = 0

            # Find the first note to move to, per smoothing
            while first_note["start"] < smoothing_time:
                initial_delay += (first_note["end"] - t)
                t = first_note["end"]
                first_note = contour_notes.pop(0)

            torso_move = Move(self.shimi, self.shimi.torso,
                              denormalize_to_range(first_note["norm_pitch"], torso_min, torso_max),
                              first_note["start"],
                              initial_delay=0,
                              vel_algo='constant')

            neck_move = Move(self.shimi, self.shimi.neck_ud,
                             denormalize_to_range(first_note["norm_pitch"], neck_min, neck_max),
                             first_note["start"],
                             initial_delay=0,
                             vel_algo='constant')

            t = first_note["start"]
            last_move = t

            delay = 0
            while len(contour_notes) > 0:
                note = contour_notes.pop(0)

                if note["start"] > last_move + smoothing_time:
                    # Do move
                    torso_move.add_move(denormalize_to_range(note["norm_pitch"], torso_min, torso_max),
                                        note["start"] - last_move,
                                        vel_algo='constant',
                                        delay=0)
                    neck_move.add_move(denormalize_to_range(note["norm_pitch"], neck_min, neck_max),
                                       note["start"] - last_move,
                                       vel_algo='constant',
                                       delay=0)

                    t = note["start"]
                    last_move = t
                    delay = 0
                else:
                    delay += (note["start"] - t)
                    t = note["start"]

            if len(torso_move.vel_algos) > 1:
                torso_move.vel_algos[0] = 'linear_a'
                torso_move.vel_algos[-1] = 'linear_d'
                neck_move.vel_algos[0] = 'linear_a'
                neck_move.vel_algos[-1] = 'linear_d'

        except Exception as e:
            return None, None

        print(torso_move.positions, torso_move.durations)
        print(neck_move.positions, neck_move.durations)

        return torso_move, neck_move

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
