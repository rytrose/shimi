from pypot.utils import StoppableThread
import time
import utils.utils as utils
import random

class LinearAccelMove(StoppableThread):
    def __init__(self, shimi, motor, position, duration, initial_delay=0.0, update_freq=0.1, min_vel=20, normalized_positions=False):
        self.shimi = shimi
        self.motor = motor

        self.pos = None
        self.positions = [position]

        self.dur = None
        self.durations = [duration]

        self.delays = [initial_delay]

        self.freq = update_freq
        self.min_vel = min_vel
        self.norm = normalized_positions

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def setup(self):
        pass

    def teardown(self):
        pass

    def move(self):
        start_time = time.time()
        starting_position = self.shimi.controller.get_present_position([self.motor])[0]

        # Convert normalized position to degrees
        if self.norm:
            self.pos = utils.denormalize(self.motor, self.pos)

        # Compute maximum velocity, will hit at position / 2
        max_vel = 2 * (
                (abs((self.pos - starting_position) / 2) - (self.min_vel * (self.dur / 2))) / ((self.dur / 2) ** 2))

        # Set the goal position and initial speed of min_vel
        self.shimi.controller.set_moving_speed({self.motor: self.min_vel})
        self.shimi.controller.set_goal_position({self.motor: self.pos})

        # Adjust duration based off of this computation time
        new_dur = self.dur - (time.time() - start_time)

        # Increment speed over time at freq
        while time.time() <= start_time + new_dur and not self.should_stop():
            # On pause
            if self.should_pause():
                # Capture moving speed
                pause_speed = abs(self.shimi.controller.get_present_speed([self.motor])[0])

                # Stop the movement
                self.shimi.controller.set_goal_position(
                    {self.motor: self.shimi.controller.get_present_position([self.motor])[0]})

                # Capture what time in the path it paused at
                elapsed = time.time() - start_time

                # Update the "start time" so that when unpaused, it resumes at the same time in the move
                while self.should_pause():
                    start_time = time.time() - (self.dur - elapsed)

                # Continue to goal
                self.shimi.controller.set_goal_position({self.motor: self.pos})
                self.shimi.controller.set_moving_speed({self.motor: pause_speed})

            # Compute the relative position (0 - 1) in the path to goal position
            rel_pos = abs(self.dur / 2 - (time.time() - start_time))

            # Calculate the velocity at this point in time, relative to the max_vel at position/2
            vel = (max_vel * (2 * (1.0 - rel_pos / self.dur) - 1)) + self.min_vel
            self.shimi.controller.set_moving_speed({self.motor: vel})

            # Wait to update again
            time.sleep(self.freq)

        # If this was stopped, stop movement at current position
        if self.should_stop():
            self.shimi.controller.set_goal_position(
                {self.motor: self.shimi.controller.get_present_position([self.motor])[0]})

            # Clear all queued moves
            self.delays = []
            self.positions = []
            self.durations = []


    def run(self):
        while len(self.positions) > 0:
            # Sleep for delay time
            time.sleep(self.delays.pop(0))

            # Set position and duration for this move
            self.pos = self.positions.pop(0)
            self.dur = self.durations.pop(0)

            # Do the move
            t = time.time()
            self.move()

    # Add move to queue
    def add_move(self, position, duration, delay=0.0):
        self.delays.append(delay)
        self.positions.append(position)
        self.durations.append(duration)

class LinearMove(StoppableThread):
    def __init__(self, shimi, motor, position, duration, initial_delay=0.0, stop_check_freq=0.005, normalized_positions=False):
        self.shimi = shimi
        self.motor = motor

        self.pos = None
        self.positions = [position]

        self.dur = None
        self.durations = [duration]

        self.delays = [initial_delay]

        self.stop_check_freq = stop_check_freq
        self.norm = normalized_positions

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def setup(self):
        pass

    def teardown(self):
        pass

    def move(self):
        start_time = time.time()
        starting_position = self.shimi.controller.get_present_position([self.motor])[0]

        # Convert normalized position to degrees
        if self.norm:
            self.pos = utils.denormalize(self.motor, self.pos)

        # Calculate constant velocity
        vel = abs(self.pos - starting_position) / self.dur

        # Set the velocity
        self.shimi.controller.set_moving_speed({self.motor: vel})

        # Set the goal position
        self.shimi.controller.set_goal_position({self.motor: self.pos})

        # Sleep off the duration, allowing for stopping
        while time.time() <= start_time + self.dur and not self.should_stop():
            time.sleep(self.stop_check_freq)

        # If this was stopped, stop movement at current position
        if self.should_stop():
            self.shimi.controller.set_goal_position(
                {self.motor: self.shimi.controller.get_present_position([self.motor])[0]})

            # Clear all queued moves
            self.delays = []
            self.positions = []
            self.durations = []

    def run(self):
        while len(self.positions) > 0:
            # Sleep for delay time
            time.sleep(self.delays.pop(0))

            # Set position and duration for this move
            self.pos = self.positions.pop(0)
            self.dur = self.durations.pop(0)

            # Do the move
            self.move()

    # Add move to queue
    def add_move(self, position, duration, delay=0.0):
        self.delays.append(delay)
        self.positions.append(position)
        self.durations.append(duration)

class Thinking(StoppableThread):
    def __init__(self, shimi):
        # Seed RNG
        random.seed()

        self.shimi = shimi

        self.UP_LEFT = 0
        self.UP_RIGHT = 1
        self.DOWN_LEFT = 2
        self.DOWN_RIGHT = 3

        self.state_positions = {
            self.UP_LEFT: {
                self.shimi.neck_ud: 0.75,
                self.shimi.neck_lr: 0.3,
                self.shimi.torso: 0.95
            },
            self.UP_RIGHT: {
                self.shimi.neck_ud: 0.75,
                self.shimi.neck_lr: 0.7,
                self.shimi.torso: 0.9
            },
            self.DOWN_LEFT: {
                self.shimi.neck_ud: 0.2,
                self.shimi.neck_lr: 0.3,
                self.shimi.torso: 0.95
            },
            self.DOWN_RIGHT: {
                self.shimi.neck_ud: 0.2,
                self.shimi.neck_lr: 0.7,
                self.shimi.torso: 0.9
            },

        }

        self.state = -1

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def run(self):
        while not self.should_stop():
            # Wait between states some
            time.sleep(1.0 * random.random())

            # Check for stop after sleeping
            if self.should_stop():
                break

            # Change thinking position
            # self.state = (self.state + 1) % 4
            self.state = [0, 1, 2, 3][random.randrange(4)]

            # Move to new state position
            # Take between 1.0 - 1.6 seconds
            duration = 1.3 + ([-1, 1][random.randrange(2)] * random.random() * 0.3)

            moves = []
            for motor, pos in self.state_positions[self.state].items():
                # Define the move
                move = LinearAccelMove(self.shimi, motor, pos, duration, normalized_positions=True)

                # Start moving
                move.start()

                # Retain thread to join
                moves.append(move)

            # Wait for the moves to finish
            for move in moves:
                move.join()

class No(StoppableThread):
    def __init__(self, shimi):
        self.shimi = shimi

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def run(self):
        # Seed RNG
        random.seed()

        # Random speed for each movement
        self.speed = 0.2 + random.random() / 2

        # Set to initial position
        self.shimi.initial_position(0.7)

        # Random shake distance between 0.3 (large shake) and near-0 (very small shake)
        self.dist = 0.3 - ((3 * random.random()) / 16)

        # Random left right first shake
        directions = [1, -1]

        # Shake first direction
        first_dir = directions.pop(random.randrange(2))
        shake = LinearAccelMove(self.shimi, self.shimi.neck_lr, 0.5 + (first_dir * self.dist), self.speed,
                        normalized_positions=True)

        # Shake second direction
        shake.add_move(0.5 + (directions.pop() * self.dist), self.speed / 2)

        # Set back to middle
        shake.add_move(0.5, self.speed)

        # Move
        shake.start()
        shake.join()


