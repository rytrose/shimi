from pypot.utils import StoppableThread
import time
import utils.utils as utils


class LinearAccelMove(StoppableThread):
    def __init__(self, shimi, motor, position, duration, update_freq=0.1, min_vel=20, normalized_positions=False):
        self.shimi = shimi
        self.motor = motor
        self.pos = position
        self.dur = duration
        self.freq = update_freq
        self.min_vel = min_vel
        self.norm = normalized_positions

        StoppableThread.__init__(self,
                                 setup=self.setup,
                                 target=self.run,
                                 teardown=self.teardown)

    def setup(self):
        # Stop existing move for motor if it exists
        if self.shimi.active_moves[self.motor]:
            self.shimi.active_moves[self.motor].stop()

        # Add self to the active moves
        self.shimi.active_moves[self.motor] = self

    def teardown(self):
        # Remove self from active active_moves
        self.shimi.active_moves[self.motor] = None

    def run(self):
        start_time = time.time()
        starting_position = self.shimi.controller.get_present_position([self.motor])[0]
        done = False

        # Convert normalized position to degrees
        if self.norm:
            self.pos = utils.denormalize(self.motor, self.pos)

        # Compute maximum velocity, will hit at position / 2
        max_vel = 2 * (
                (abs((self.pos - starting_position) / 2) - (self.min_vel * (self.dur / 2))) / ((self.dur / 2) ** 2))

        # Set the goal position and initial speed of min_vel
        self.shimi.controller.set_moving_speed({self.motor: self.min_vel})
        self.shimi.controller.set_goal_position({self.motor: self.pos})

        # Increment speed over time at freq
        while time.time() <= start_time + self.dur and not self.should_stop():
            # On pause
            if self.should_pause():
                # Capture moving speed
                pause_speed = self.shimi.controller.get_moving_speed([self.motor])[0]

                # Stop the movement
                self.shimi.controller.set_goal_position(
                    {self.motor: self.shimi.controller.get_present_position([self.motor])[0]})

                # Capture what time in the path it paused at
                elapsed = start_time - time.time()

                print("Paused", self.motor)

                # Update the "start time" so that when unpaused, it resumes at the same time
                while self.should_pause():
                    start_time = time.time() - elapsed

                # Continue to goal
                self.shimi.controller.set_goal_position({self.motor: self.pos})
                self.shimi.controller.set_moving_speed({self.motor: pause_speed})
                print("resuming")

            # Compute the relative position (0 - 1) in the path to goal position
            rel_pos = abs(self.dur / 2 - (time.time() - start_time))

            # Calculate the velocity at this point in time, relative to the max_vel at position/2
            vel = (max_vel * (2 * (1.0 - rel_pos / self.dur) - 1)) + self.min_vel
            self.shimi.controller.set_moving_speed({self.motor: vel})

            # Wait to update again
            time.sleep(self.freq)



        # Set goal position to current position, for the case of stopping mid-move
        # self.shimi.controller.set_goal_position({self.motor: self.shimi.controller.get_present_position([self.motor])[0]})

        print("Done moving. orig goal pos: {0} | pres pos: {1}".format(self.pos,
                                                                       self.shimi.controller.get_present_position(
                                                                           [self.motor])))
