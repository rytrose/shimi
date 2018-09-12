from pypot.utils import StoppableThread
from motion.move import *
import numpy as np
import time

class Recorder():
    def __init__(self, shimi, motors, duration, wait_time=3.0):
        self.shimi = shimi
        self.motors = motors
        self.dur = duration
        self.wait = wait_time

        self.positions = []

    def record(self):
        # Erase previous recording
        self.positions = []

        # Disable torque
        self.shimi.disable_torque()

        # Define the thread
        r = StoppableThread()
        r.__init__(setup=self.setup, target=self._record, teardown=self.teardown)

        # Run the recording
        r.start()

        # Wait for it to stop
        r.join()

    def setup(self):
        pass

    def teardown(self):
        pass

    def _record(self):
        # Count down to recording
        waiting = self.wait
        while waiting > 0:
            print("{}...".format(waiting))
            sleep_time = min(1.0, waiting)
            waiting -= sleep_time
            time.sleep(sleep_time)

        # Make the recording
        print("Recording...")

        # Initial position
        self.positions.append(self.shimi.controller.get_present_position(self.motors))

        start_time = time.time()
        while time.time() <= start_time + self.dur:
            # Sample the current position as fast as possible
            self.positions.append(self.shimi.controller.get_present_position(self.motors))

        print("Done. Recorded {} positions.".format(len(self.positions)))

    def play(self, accel_style='linear'):
        # Calculate time between movements
        move_dur = self.dur / len(self.positions)

        # Over all sampled positions
        for pos in self.positions:
            moves = []
            for i, m in enumerate(self.motors):
                if accel_style == 'linear_accel_decel':
                    move = LinearAccelMove(self.shimi, m, pos[i], move_dur)
                else:
                    move = LinearMove(self.shimi, m, pos[i], move_dur)
                moves.append(move)

            # Start all the moves
            start = time.time()
            for move in moves:
                move.start()

            # Wait for all the moves to finish
            for move in moves:
                move.join()

            print("Time for move: {0}, Freq: {1}".format(time.time() - start, move_dur))

    def plot(self, ax):
        t = np.linspace(0, self.dur, len(self.positions))

        pos_matrix = np.array(self.positions)
        positions_per_motor = []

        for i, _ in enumerate(self.motors):
            positions_per_motor.append(pos_matrix[:,i])

        for m in positions_per_motor:
            ax.plot(t, m)

        ax.legend(self.motors)
        ax.set_xlabel('Time (in s)')
        ax.set_ylabel('Position (in degrees)')