from pypot.utils import StoppableThread
from config.definitions import STARTING_POSITIONS
from utils.utils import normalize_position
import time
import utils.utils as utils
import random
from numpy import cumsum

VERBOSE = False


class Move(StoppableThread):
    def __init__(self, shimi, motor, position, duration, vel_algo='constant', vel_algo_kwarg={}, initial_delay=0.0,
                 freq=0.1,
                 stop_check_freq=0.005,
                 normalized_positions=True):
        """A threaded process in charge of moving a motor of Shimi over time.

        Args:
            shimi (Shimi): An instance of the Shimi motor controller class.
            motor (int): The motor ID to generate random movements for.
            position (float): The position to move to.
            duration (float): The duration the movement should last.
            vel_algo (str, optional): Defaults to 'constant'. Defines the velocity algorithm to use for the move, out of the following:
                'constant: constant velocity,
                'linear_ad': constant acceleration to the midpoint of the movement, then constant deceleration to the end,
                'linear_a': constant acceleration to a point, then constant velocity for the rest of the movement,
                'linear_d': constant velocity to a point, then constant aceleration for the rest of the movement.
            vel_algo_kwarg (dict, optional): Defaults to {}. Keyword arguments if needed for a velocity algorithm.
            initial_delay (float, optional): Defaults to 0.0. The time to wait before starting the move when the thread is started.
            freq (float, optional): Defaults to 0.1. The interval time in seconds a new velocity value should be sent to the motors.
            stop_check_freq (float, optional): Defaults to 0.005. The time in seconds of how often the StoppableThread should check if it should be stopped.
            normalized_positions (bool, optional): Defaults to True. Determines whether pos should be interpeted as a value [0.0, 1.0] or as an angle in degrees.
        """
        self.shimi = shimi
        self.motor = motor

        self.pos = None
        self.positions = [position]

        self.dur = None
        self.durations = [duration]

        self.vel_algo = None
        self.vel_algos = [vel_algo]

        self.vel_algo_kwarg = vel_algo_kwarg
        self.vel_algo_kwargs = [vel_algo_kwarg]

        self.vel_algo_map = {
            'constant': self.constant_vel,
            'linear_a': self.linear_accel_vel,
            'linear_d': self.linear_decel_vel,
            'linear_ad': self.linear_accel_decel_vel
        }

        self.delays = [initial_delay]

        self.freq = freq
        self.stop_check_freq = stop_check_freq
        self.norm = normalized_positions

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def constant_vel(self, **kwargs):
        """Executes a Move with constant velocity."""
        start_time = time.time()

        starting_position = self.shimi.controller.get_present_position([self.motor])[
            0]

        # Convert normalized position to degrees
        if self.norm:
            self.pos = utils.denormalize_position(self.motor, self.pos)

        # Calculate constant velocity
        vel = abs(self.pos - starting_position) / self.dur

        # Set the velocity
        self.shimi.controller.set_moving_speed({self.motor: vel})

        # Set the goal position
        self.shimi.controller.set_goal_position({self.motor: self.pos})

        # Sleep off the duration, allowing for stopping
        while time.time() <= start_time + self.dur and not self.should_stop():
            time.sleep(self.stop_check_freq)

        if VERBOSE:
            # Print time statistics
            self.time_stats(start_time, self.dur)

        # If this was stopped, stop movement at current position
        if self.should_stop():
            self.stop_move()

    def linear_accel_decel_vel(self, **kwargs):
        """Executes the move with constant acceleration to the midpoint of the move, then constant deceleration until the end of the move.

        Args:
            kwargs["min_vel"] (float): The minimum velocity allowed, i.e. the start/end velocity offset.
        """
        start_time = time.time()

        min_vel = 20
        if "min_vel" in kwargs:
            min_vel = kwargs["min_vel"]

        starting_position = self.shimi.controller.get_present_position([self.motor])[
            0]

        # Convert normalized position to degrees
        if self.norm:
            self.pos = utils.denormalize_position(self.motor, self.pos)

        # Compute maximum velocity, will hit at position / 2
        max_vel = 2 * (
            (abs((self.pos - starting_position) / 2) - (min_vel * (self.dur / 2))) / (
                (self.dur / 2) ** 2))

        # Set the goal position and initial speed of min_vel
        self.shimi.controller.set_moving_speed({self.motor: min_vel})
        self.shimi.controller.set_goal_position({self.motor: self.pos})

        # Adjust duration based off of this computation time
        #   Getting the current position can take a non-trivial amount of time
        #   This got better with the USB2AX controller, but no harm in keeping this logic
        new_dur = self.dur - (time.time() - start_time)

        # Increment speed over time at freq
        while time.time() <= start_time + new_dur and not self.should_stop():
            # On pause
            if self.should_pause():
                start_time = self.pause_move(start_time)

            # Compute the relative position (0 - 1) in the path to goal position
            rel_pos = abs(self.dur / 2 - (time.time() - start_time))

            # Calculate the velocity at this point in time, relative to the max_vel at position/2
            vel = (max_vel * (2 * (1.0 - rel_pos / self.dur) - 1)) + min_vel
            self.shimi.controller.set_moving_speed({self.motor: vel})

            # Wait to update again
            time.sleep(self.freq)

        if VERBOSE:
            # Print time statistics
            self.time_stats(start_time, self.dur)

        # If this was stopped, stop movement at current position
        if self.should_stop():
            self.stop_move()

    def linear_accel_vel(self, **kwargs):
        """Executes the move with constant acceleration over time until a point in the duration, then remains at a constant velocity for the rest of the move.

        Args:
            kwargs["change_time"] (float): A normalized value [0.0, 1.0] representing the portion of the move to accelerate for.
        """
        start_time = time.time()

        change_time = 0.5
        if "change_time" in kwargs:
            change_time = kwargs["change_time"]

        # Convert normalized position to degrees
        if self.norm:
            self.pos = utils.denormalize_position(self.motor, self.pos)

        # Calculate the max velocity
        current_pos = self.shimi.controller.get_present_position([self.motor])[
            0]
        max_vel = abs(current_pos - self.pos) / (change_time * self.dur)

        # Set the goal position
        self.shimi.controller.set_goal_position({self.motor: self.pos})

        # Increment speed over time at freq
        while time.time() <= start_time + self.dur and not self.should_stop():
            # On pause
            if self.should_pause():
                start_time = self.pause_move(start_time)

            # Calculate the velocity at this point in time
            t = time.time() - start_time
            if t < (change_time * self.dur):
                vel = max_vel * (t / (change_time * self.dur))
            else:
                vel = max_vel

            # Prevent vel == 0
            if vel < 1:
                vel = 1

            # Update velocity
            self.shimi.controller.set_moving_speed({self.motor: vel})

            # Sleep only as much as there is time left
            time_left = (start_time + self.dur) - time.time()
            if self.freq > time_left:
                if time_left > 0:
                    time.sleep(time_left)
                else:
                    break
            else:
                # Wait to update again
                time.sleep(self.freq)

        if VERBOSE:
            # Print time statistics
            self.time_stats(start_time, self.dur)

        # If this was stopped, stop movement at current position
        if self.should_stop():
            self.stop_move()

    def linear_decel_vel(self, **kwargs):
        """Executes the move with constant acceleration over time until a point in the duration, then remains at a constant velocity for the rest of the move.

        Args:
            kwargs["change_time"] (float): A normalized value [0.0, 1.0] representing the portion of the move to move at constant velocity.
        """
        start_time = time.time()

        change_time = 0.5
        if "change_time" in kwargs:
            change_time = kwargs["change_time"]

        # Convert normalized position to degrees
        if self.norm:
            self.pos = utils.denormalize_position(self.motor, self.pos)

        # Calculate the max velocity
        current_pos = self.shimi.controller.get_present_position([self.motor])[
            0]
        max_vel = abs(current_pos - self.pos) / ((1 - change_time) * self.dur)

        # Set the goal position
        self.shimi.controller.set_goal_position({self.motor: self.pos})

        # Increment speed over time at freq
        while time.time() <= start_time + self.dur and not self.should_stop():
            # On pause
            if self.should_pause():
                start_time = self.pause_move(start_time)

            # Calculate the velocity at this point in time
            t = time.time() - start_time
            if t < (change_time * self.dur):
                vel = max_vel
            else:
                vel = max_vel * ((self.dur - t) /
                                 ((1 - change_time) * self.dur))

            # Prevent vel == 0
            if vel < 1:
                vel = 1

            # Update velocity
            self.shimi.controller.set_moving_speed({self.motor: vel})

            # Sleep only as much as there is time left
            time_left = (start_time + self.dur) - time.time()
            if self.freq > time_left:
                if time_left > 0:
                    time.sleep(time_left)
                else:
                    break
            else:
                # Wait to update again
                time.sleep(self.freq)

        if VERBOSE:
            # Print time statistics
            self.time_stats(start_time, self.dur)

        # If this was stopped, stop movement at current position
        if self.should_stop():
            self.stop_move()

    def pause_move(self, start_time):
        """Pauses the currently running move.

        Args:
            start_time (float): The time at which the move was started.

        Returns:
            float: The updated start time to account for time paused.
        """

        # Capture moving speed
        pause_speed = abs(
            self.shimi.controller.get_present_speed([self.motor])[0])

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

        return start_time

    def stop_move(self):
        """Stops the currently executing move."""
        self.shimi.controller.set_goal_position(
            {self.motor: self.shimi.controller.get_present_position([self.motor])[0]})

        # Clear all queued moves
        self.delays = []
        self.positions = []
        self.durations = []

    def run(self):
        """Actuates the motor in accordance with the specified parameters."""
        while len(self.positions) > 0:
            # Sleep for delay time
            time.sleep(self.delays.pop(0))

            # Set position and duration for this move
            self.pos = self.positions.pop(0)
            self.dur = self.durations.pop(0)

            if len(self.vel_algos) > 0:
                self.vel_algo = self.vel_algos.pop(0)
            if len(self.vel_algo_kwargs) > 0:
                self.vel_algo_kwarg = self.vel_algo_kwargs.pop(0)

            # Do the move, based on the specified velocity algorithm
            self.vel_algo_map[self.vel_algo](**self.vel_algo_kwarg)

    def time_stats(self, start_time, duration):
        """Prints the difference between how long the move actually took vs. how long it was supposed to take. 

        Used for diagnostic purposes only. To be called upon completion of a Move.

        Args:
            start_time (float): Time in seconds when the Move began.
            duration (float): Time in seconds the Move was supposed to take.
        """

        time_taken = time.time() - start_time
        print("duration: %.4f\ntime taken: %.4f\n difference: %.4f" %
              (duration, time_taken, duration - time_taken))

    def add_move(self, position, duration, vel_algo=None, vel_algo_kwarg={}, delay=0.0):
        """Adds a new position, duration, and velocity parameters to the current Move sequence.

        Args:
            position (float): The position to move to.
            duration (float): The duration the movement should last.
            vel_algo (str, optional): Defaults to 'constant'. Defines the velocity algorithm to use for the move, out of the following:
                'constant: constant velocity,
                'linear_ad': constant acceleration to the midpoint of the movement, then constant deceleration to the end,
                'linear_a': constant acceleration to a point, then constant velocity for the rest of the movement,
                'linear_d': constant velocity to a point, then constant aceleration for the rest of the movement.
            vel_algo_kwarg (dict, optional): Defaults to {}. Keyword arguments if needed for a velocity algorithm.
            delay (float, optional): Defaults to 0.0. Time to wait before executing this movement from the end of the previous movement.
        """

        self.delays.append(delay)
        self.positions.append(position)
        self.durations.append(duration)

        # Retain previous velocity algorithm/args
        if vel_algo:
            self.vel_algos.append(vel_algo)
        else:
            self.vel_algos.append(self.vel_algos[-1])

        if vel_algo_kwarg:
            self.vel_algo_kwargs.append(vel_algo_kwarg)
        else:
            self.vel_algo_kwargs.append(self.vel_algo_kwargs[-1])

    def get_timestamps(self):
        """Returns the timestamps of each sequenced movement."""
        if len(self.durations) == 0:
            return []
        else:
            return cumsum(self.durations)


class Thinking(StoppableThread):
    """Moves Shimi in a way to suggest Shimi is thinking about something."""
    def __init__(self, shimi, **kwargs):
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
            duration = 1.3 + ([-1, 1][random.randrange(2)] *
                              random.random() * 0.3)

            moves = []
            for motor, pos in self.state_positions[self.state].items():
                # Define the move
                move = Move(self.shimi, motor, pos, duration,
                            vel_algo='linear_ad', normalized_positions=True)

                # Start moving
                move.start()

                # Retain thread to join
                moves.append(move)

            # Wait for the moves to finish
            for move in moves:
                move.join()


class No(StoppableThread):
    """Shakes Shimi's head to express negation."""
    def __init__(self, shimi, **kwargs):
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
        shake = Move(self.shimi, self.shimi.neck_lr, 0.5 + (first_dir * self.dist), self.speed, vel_algo='linear_ad',
                     normalized_positions=True)

        # Shake second direction
        shake.add_move(0.5 + (directions.pop() * self.dist), self.speed / 2)

        # Set back to middle
        shake.add_move(0.5, self.speed)

        # Move
        shake.start()
        shake.join()


class Alert(StoppableThread):
    """Makes Shimi lean forward as to be alert or listening."""
    def __init__(self, shimi, **kwargs):
        self.shimi = shimi

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def run(self):
        # Move forward and lift head up
        torso = Move(self.shimi, self.shimi.torso,
                     normalize_position(
                         self.shimi.torso, STARTING_POSITIONS[self.shimi.torso]) - 0.1,
                     0.5,
                     vel_algo='constant',
                     normalized_positions=True)

        neck_ud = Move(self.shimi, self.shimi.neck_ud,
                       normalize_position(
                           self.shimi.neck_ud, STARTING_POSITIONS[self.shimi.neck_ud]) - 0.3,
                       0.5,
                       vel_algo='constant',
                       normalized_positions=True)

        torso.start()
        neck_ud.start()

        torso.join()
        neck_ud.join()

    def stop(self, wait=True):
        self.shimi.initial_position()
        super().stop(wait)
