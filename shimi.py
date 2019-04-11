from config.definitions import *
from motion.move import *
import utils.utils as utils
import numpy as np
import pypot.dynamixel
import time
from pprint import pprint


class Shimi:
    """Abstraction around Shimi's motor controller."""

    def __init__(self, silent=False):
        """Sets up motor controller and sets Shimi to initial position.
            silent (bool, optional): Defaults to False. Suppresses print information on motor connections.
        """
        try:
            # Setup serial connection to motors and get the controller
            self.controller = self.setup(silent)
            self.initial_position()  # Set motors to initial positions
        except Exception as e:
            self.controller = None
            print("WARNING, MOTOR ERROR.", e)

    def setup(self, silent):
        """Establishes serial connection to Shimi's motors.

        Args:
            silent (bool): Suppresses print information on motor connections.

        Returns:
            pypot.dynamixel.DxlIO: The motor controller connected to Shimi's motors.
        """
        ports = pypot.dynamixel.get_available_ports()  # Find USB to serial converter
        if not silent:
            print('Connecting on', ports[0])

        # Connect to first port by default
        controller = pypot.dynamixel.DxlIO(ports[0])
        ids = controller.scan(range(10))  # Search for motors with ids 0-9

        if not silent:
            print('Found motors with the following IDs:', ids)

        return controller

    @property
    def torso(self):
        return TORSO

    @property
    def neck_ud(self):
        return NECK_UD

    @property
    def neck_lr(self):
        return NECK_LR

    @property
    def phone(self):
        return PHONE

    @property
    def foot(self):
        return FOOT

    @property
    def all_motors(self):
        return [TORSO, NECK_UD, NECK_LR, PHONE, FOOT]

    def initial_position(self, duration=1.0):
        """Moves Shimi's motors to the initial position set in config/definitions.

        Args:
            duration (float, optional): Defaults to 1.0. Time to take Shimi's motors to their initial positions.
        """
        self.enable_torque()  # Make sure torque is enabled
        moves = []
        for m in self.all_motors:
            move = Move(
                self, m, STARTING_POSITIONS[m], duration, normalized_positions=False)
            moves.append(move)

        # Start all the moves
        for move in moves:
            move.start()

        # Wait for all the moves to finish
        for move in moves:
            move.join()

    def disable_torque(self):
        """Turns off torque for Shimi's motors so they can be moved by hand."""
        self.controller.disable_torque(self.all_motors)

    def enable_torque(self):
        """Turns on torque for Shimi's motors so they can be moved programmatically and are rigid."""
        self.controller.enable_torque(self.all_motors)
