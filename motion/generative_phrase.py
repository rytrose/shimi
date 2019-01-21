import sys, os

# Add parent to path
sys.path.insert(1, os.path.join(sys.path[0], '..'))

from shimi import Shimi
from posenet.posenet import PoseNet
from utils.utils import Point, normalize_position, denormalize_position, denormalize_to_range, quantize, \
    normalize_to_range
from audio.midi_analysis import MidiAnalysis
from motion.move import Move
import pygame.mixer as mixer
import random
import time
import numpy as np


class GenerativePhrase:
    def __init__(self, shimi=None, posenet=False):
        if shimi is not None:
            self.shimi = shimi
        else:
            self.shimi = Shimi()

        self.posenet = None
        if posenet:
            self.posenet = PoseNet(self.shimi, on_pred=self.on_posenet_prediction)

        self.face_track = False
        self.update_freq = 0.1
        self.last_update = time.time()
        self.last_pos = 0.5
        mixer.init()

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

    def generate(self, midi_path, valence, arousal, doa_value=None, wav_path=None):
        t = time.time()

        # Analyze the MIDI
        self.midi_analysis = MidiAnalysis(midi_path)
        tempo = self.midi_analysis.get_tempo()
        length = self.midi_analysis.get_length()

        # Create the motor moves
        moves = []
        foot = self.foot_movement(tempo, length, valence, arousal)
        moves.append(foot)
        torso = self.torso_movement(valence, arousal)
        moves.append(torso)
        neck_ud = self.neck_ud_movement(length, valence, arousal, torso)
        moves.append(neck_ud)
        phone = self.phone_movement(tempo, length, valence, arousal)
        moves.append(phone)

        if not self.posenet:
            if not doa_value:
                doa_value = 75
            neck_lr = self.neck_lr_doa_movement(tempo, length, doa_value, valence, arousal)
            moves.append(neck_lr)

        # Load wav file if given
        if wav_path:
            mixer.music.load(wav_path)

        # Start all the moves
        for move in moves:
            move.start()

        # Turn on face tracking
        self.face_track = True

        # Play audio if given
        if wav_path:
            mixer.music.play()
        else:
            # For testing, play the MIDI file back
            self.midi_analysis.play()

        print("Time to setup gesture generation: %f" % (time.time() - t))

        # Wait for all the moves to stop
        for move in moves:
            move.join()

        # Turn off face tracking
        self.face_track = False

        self.shimi.initial_position()

    def neck_lr_doa_movement(self, tempo, length, doa_value, valence, arousal):
        # 120 left, 30 right
        normalized_doa = normalize_to_range(doa_value, 120, 30)
        normalized_arousal = (arousal + 1) / 2

        print("::: DOA: %f, normalized: %f :::" % (doa_value, normalized_doa))

        move_dur = 2 * tempo * ((1 - normalized_arousal) + 0.25)
        neck_lr_move = Move(self.shimi, self.shimi.neck_lr, normalized_doa, move_dur)

        t = tempo
        delay = 0.0
        while t < length:
            rest = random.choice([True, False])
            if rest:
                rest_dur = 2 * tempo * random.random()
                delay += rest_dur
                t += rest_dur
            else:
                new_pos = normalized_doa + (random.choice([-1, 1]) * ((1 + valence) / 2) * 0.3)
                move_dur = 2 * tempo * ((1 - normalized_arousal) + 0.25)
                neck_lr_move.add_move(new_pos, move_dur, delay=delay)
                delay = 0.0
                t += move_dur

        return neck_lr_move

    def neck_ud_movement(self, length, valence, arousal, torso):
        # Note: ~0.2 of neck movement accounts for torso
        # looking straight: tor 0.7 neck 0.7, tor 0.8 neck 0.5, tor 0.9, neck 0.3

        # Higher valence --> more tendency to look up (correct for leaning forward)
        adjusted_valence = (valence + 1) / 2
        torso_offset = 0.2 * adjusted_valence

        # Higher arousal --> more frequent nodding, more movement
        adjusted_arousal = (arousal + 1) / 2

        # Wait between half a beat and 2 beats to nod
        half_beat = (self.midi_analysis.get_tempo() / 2)
        nod_wait = half_beat * denormalize_to_range(adjusted_arousal, 4, 1)

        # Start direction
        direction = random.choice([-1, 1])

        # Proportion of available range (limited by torso) that can be used
        pos_range = 0
        if arousal >= 0:
            # Shorter movements for lower positive arousal
            pos_range = denormalize_to_range(arousal, 0.4, 1.0)
        else:
            # Short movements for less negative arousal
            pos_range = denormalize_to_range(abs(arousal), 0.4, 1.0)

        # Keep track of timeline
        t = 0

        # Quantize nods to half beats
        while t < nod_wait:
            t += half_beat

        pos = self.calculate_neck_ud_position(t, torso, torso_offset, pos_range, direction)
        neck_ud_move = Move(self.shimi, self.shimi.neck_ud, pos, t)
        last_move = t
        direction = not direction

        while t < length:
            if t < last_move + nod_wait:
                t += half_beat
            else:
                pos = self.calculate_neck_ud_position(t, torso, torso_offset, pos_range, direction)
                neck_ud_move.add_move(pos, t - last_move)
                last_move = t
                direction = not direction

        return neck_ud_move

    def calculate_neck_ud_position(self, t, torso, torso_offset, pos_range, direction):
        # Torso offset to make it look up when bending forward
        torso_timestamps = torso.get_timestamps()
        torso_position = np.interp(t, torso_timestamps, torso.positions)
        offset = (1 - torso_position) * 10 * torso_offset

        half_range = pos_range / 2

        # Vary the distance by 20% of possible moving distance
        pos_in_range = half_range + (direction * (half_range - (0.2 * random.random() * half_range)))

        return 1 - (offset + pos_in_range)

    def torso_movement(self, valence, arousal):
        contour_notes = self.midi_analysis.get_normalized_pitch_contour()

        # Higher valence --> more rapid matching to pitch contour
        smoothing_time = 0
        if valence < 0:
            valence = 0

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

                t = note["start"]
                last_move = t
                delay = 0
            else:
                delay += (note["start"] - t)
                t = note["start"]

        if len(torso_move.vel_algos) > 1:
            torso_move.vel_algos[0] = 'linear_a'
            torso_move.vel_algos[-1] = 'linear_d'

        return torso_move

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

    def phone_movement(self, tempo, length, valence, arousal):
        # Calculate tempo of "sway" based on arousal
        quantized_arousals = [-1, -0.5, 0, 1]
        quantized_arousal = quantize(arousal, quantized_arousals)

        # Higher arousal --> faster "swaying"
        sway_periods = [4 * tempo, 2 * tempo, 2 * tempo, tempo]
        sway_period = sway_periods[quantized_arousals.index(quantized_arousal)]

        # Abs of arousal determines speed
        abs_arousal = abs(arousal)
        # Limit max speed to 50% of time
        move_dur = denormalize_to_range(1.0 - abs_arousal, 0.5, 1.0) * sway_period

        # If valence > 0 smooth movements with vel_algo
        if valence > 0:
            vel_algo = 'linear_ad'
        else:
            vel_algo = 'constant'

        # Distance is controlled by quadrant
        if valence >= 0 and arousal >= 0:
            sway_width = denormalize_to_range(valence + arousal, 0.1, 0.5)
        elif valence >= 0 and arousal < 0:
            sway_width = denormalize_to_range(valence + abs(arousal), 0.5, 0.1)
        elif valence < 0 and arousal >= 0:
            sway_width = denormalize_to_range(abs(valence) + arousal, 0.5, 0.1)
        else:
            sway_width = denormalize_to_range(abs(valence) + abs(arousal), 0.1, 0.5)

        # Direction to start is random
        dir = random.choice([True, False])

        move = Move(self.shimi, self.shimi.phone, 0.5 + (sway_width * [1, -1][int(dir)]), move_dur, vel_algo=vel_algo,
                    initial_delay=sway_period - move_dur)

        t = move_dur
        while t < (length - sway_period):
            dir = not dir
            move.add_move(0.5 + (sway_width * [1, -1][int(dir)]), move_dur, delay=sway_period - move_dur,
                          vel_algo=vel_algo)
            t += sway_period

        return move
