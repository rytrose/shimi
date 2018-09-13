from pypot.utils import StoppableThread
import time
import utils.utils as utils

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
        # # Stop existing move for motor if it exists
        # if self.shimi.active_moves[self.motor]:
        #     self.shimi.active_moves[self.motor].stop()
        #
        # # Add self to the active moves
        # self.shimi.active_moves[self.motor] = self

    def teardown(self):
        pass
        # # Remove self from active active_moves
        # self.shimi.active_moves[self.motor] = None

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
                pause_speed = self.shimi.controller.get_moving_speed([self.motor])[0]

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

        # print("Done moving. orig goal pos: {0} | pres pos: {1}".format(self.pos,
        #                                                                self.shimi.controller.get_present_position(
        #                                                                    [self.motor])))

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
        # # Stop existing move for motor if it exists
        # if self.shimi.active_moves[self.motor]:
        #     self.shimi.active_moves[self.motor].stop()
        #
        # # Add self to the active moves
        # self.shimi.active_moves[self.motor] = self

    def teardown(self):
        pass
        # # Remove self from active active_moves
        # self.shimi.active_moves[self.motor] = None

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