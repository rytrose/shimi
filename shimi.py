from config.definitions import *
from motion.move import *
import utils.utils as utils
import numpy as np
import pypot.dynamixel
import time
from pprint import pprint

class Shimi:
    # Constructor
    def __init__(self, silent=False):
        try:
            # Setup serial connection to motors and get the controller
            self.controller = self.setup(silent)

            # Set motors to initial positions
            self.initial_position()
        except Exception as e:
            self.controller = None
            print("WARNING, MOTOR ERROR.", e)


    # Establishes serial connection to motors
    def setup(self, silent):
        # Find USB to serial converter
        ports = pypot.dynamixel.get_available_ports()

        # Connect to first port for now
        if not silent:
            print('Connecting on', ports[0])
        controller = pypot.dynamixel.DxlIO(ports[0])

        # Search for motors
        ids = controller.scan(range(10))

        if not silent:
            print('Found motors with the following IDs:', ids)

            # Current settings for found motors
            # pprint(controller.get_control_table(ids))

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

    # Moves the motors to the initial position set in config/definitions
    def initial_position(self, duration=1.0, move_style='linear'):
        # Make sure torque is enabled
        self.enable_torque()

        # self.robot.goto_position({m.name: STARTING_POSITIONS[m.id] for m in self.robot.motors}, 1.0, wait=True)
        moves = []
        for m in self.all_motors:
            if move_style == 'linear_accel':
                move = LinearAccelMove(self, m, STARTING_POSITIONS[m], duration)
            else:
                move = LinearMove(self, m, STARTING_POSITIONS[m], duration)
            moves.append(move)

        # Start all the moves
        for move in moves:
            move.start()

        # Wait for all the moves to finish
        for move in moves:
            move.join()

    # Turns off torque so they can be moved by hand
    def disable_torque(self):
        # Disable torque for all motors
        self.controller.disable_torque(self.all_motors)

    # Turns on torque, making the motors rigid
    def enable_torque(self):
        # Enable torque for all motors
        self.controller.enable_torque(self.all_motors)

