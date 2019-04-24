import os
import sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))
from pypot.utils import StoppableThread
from motion.move import Move
import random
from utils.utils import denormalize_to_range, quantize


class Jam(StoppableThread):
    """General \"music appreciation\" movement for moving with audio."""

    def __init__(self, shimi, tempo, length, energy=None):
        """Generates sequenced movements.

        Args:
            shimi (Shimi): An instance of the Shimi motor controller class.
            tempo (float): Tempo of the audio file in seconds per beat.
            length (float): Length of the audio file in seconds.
            energy (float, optional): Defaults to None. A normalized measure of energy in the audio file.
        """

        self.shimi = shimi
        self.tempo = tempo
        self.length = length
        self.energy = energy

        self.foot = self.foot_move(self.energy)
        self.torso = self.torso_move(self.energy)
        self.neck_ud = self.neck_ud_move(self.energy)
        self.neck_lr = self.neck_lr_move(self.energy)

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def run(self):
        """Starts the gesture."""
        self.foot.start()
        self.torso.start()
        self.neck_ud.start()
        self.neck_lr.start()

        self.foot.join()
        self.torso.join()
        self.neck_ud.join()
        self.neck_lr.join()

        self.shimi.initial_position()

    def stop(self):
        """Stops the gesture."""
        self.foot.stop()
        self.torso.stop()
        self.neck_ud.stop()
        self.neck_lr.stop()

    def foot_move(self, energy):
        """Moves the foot up and down according to the tempo and potentially energy of the audio file.

        Args:
            energy (float): A normalized measure of energy in the audio file.

        Returns:
            Move: A Thread of properly sequenced movements.
        """
        foot_dir = True

        tap_period = self.tempo

        if energy is not None:
            quantized_energies = [0.2, 0.7, 1.0]
            quantized_energy = quantize(energy, quantized_energies)
            tap_periods = [self.tempo * 4, self.tempo * 2, self.tempo]
            tap_period = tap_periods[quantized_energies.index(
                quantized_energy)]

        foot = Move(self.shimi, self.shimi.foot, 1.0, tap_period / 2)

        t = tap_period / 2

        while t < self.length:
            if foot_dir:
                foot.add_move(0.0, tap_period / 2)
            else:
                foot.add_move(1.0, tap_period / 2)

            t += tap_period / 2
            foot_dir = not foot_dir

        return foot

    def torso_move(self, energy):
        """Moves the torso forward and back according to the tempo and potentially energy of the audio file.

        Args:
            energy (float): A normalized measure of energy in the audio file.

        Returns:
            Move: A Thread of properly sequenced movements.
        """
        torso_dir = True

        torso_period = self.tempo * 8

        if energy is not None:
            quantized_energies = [0.2, 0.7, 1.0]
            quantized_energy = quantize(energy, quantized_energies)
            torso_periods = [self.tempo * 8, self.tempo * 6, self.tempo * 4]
            torso_period = torso_periods[quantized_energies.index(
                quantized_energy)]

        randomness = 0.1 * random.random() * random.choice([-1, 1])

        torso = Move(self.shimi, self.shimi.torso, 0.7
                     + randomness, torso_period / 2, vel_algo='linear_ad')

        t = torso_period / 2

        while t < self.length:
            randomness = 0.1 * random.random() * random.choice([-1, 1])
            if torso_dir:
                torso.add_move(0.9 + randomness, torso_period / 2)
            else:
                torso.add_move(0.7 + randomness, torso_period / 2)

            t += torso_period / 2
            torso_dir = not torso_dir

        return torso

    def neck_ud_move(self, energy):
        """Moves the neck up and down according to the tempo and potentially energy of the audio file.

        Args:
            energy (float): A normalized measure of energy in the audio file.

        Returns:
            Move: A Thread of properly sequenced movements.
        """
        num_move = 3
        num_dont_move = 2

        if energy is not None:
            quantized_energies = [0.2, 0.7, 1.0]
            quantized_energy = quantize(energy, quantized_energies)
            num_moves = [1, 2, 3]
            num_dont_moves = [4, 3, 2]
            num_move = num_moves[quantized_energies.index(quantized_energy)]
            num_dont_move = num_dont_moves[quantized_energies.index(
                quantized_energy)]

        neck_ud_dir = True
        neck_ud = Move(self.shimi, self.shimi.neck_ud, 0.2, self.tempo / 2)

        t = self.tempo / 2
        delay = 0.0

        while t < self.length:
            should_move = random.choice([True for _ in range(
                num_move)] + [False for _ in range(num_dont_move)])
            if should_move:
                if neck_ud_dir:
                    neck_ud.add_move(0.9, self.tempo / 2, delay=delay)
                else:
                    neck_ud.add_move(0.2, self.tempo / 2, delay=delay)
                neck_ud_dir = not neck_ud_dir
                delay = 0.0
            else:
                delay += self.tempo / 2

            t += self.tempo / 2

        return neck_ud

    def neck_lr_move(self, energy):
        """Moves the neck left and right according to the tempo and potentially energy of the audio file.

        Args:
            energy (float): A normalized measure of energy in the audio file.

        Returns:
            Move: A Thread of properly sequenced movements.
        """
        delay_max = 3  # Maximum delay in seconds

        if energy is not None:
            quantized_energies = [0.2, 0.7, 1.0]
            quantized_energy = quantize(energy, quantized_energies)
            delay_maxes = [4, 3, 2]
            delay_max = delay_maxes[quantized_energies.index(quantized_energy)]

        t = 0.5 + random.random()
        neck_lr = Move(self.shimi, self.shimi.neck_lr,
                       0.5, t, vel_algo='linear_ad')

        delay = 0.0
        prev_pos = 0.5

        while t < self.length:
            delay += self.tempo * delay_max * random.random()
            pos = denormalize_to_range(random.random(), 0.1, 0.9)
            dur = (1 / abs(prev_pos - pos)) * self.tempo * 0.5
            dur = min(self.tempo * 8, dur)
            neck_lr.add_move(pos, dur, delay=delay)
            t += delay + dur

        return neck_lr

