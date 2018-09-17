from pypot.utils import StoppableThread
from motion.move import *
import numpy as np
import time

TIME_INDEX = 0
POS_INDEX = 1
ERROR = 0.001
INTERP_FREQ = 0.1

class Recorder():
    def __init__(self, shimi, motors, duration, wait_time=3.0):
        self.shimi = shimi
        self.motors = motors
        self.dur = duration
        self.wait = wait_time

        self.positions = []
        self.velocities = []
        self.timestamps = []

    def record(self):
        # Erase previous recording
        self.positions = []
        self.velocities = []
        self.timestamps = []

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

        # Initial position/velocity/time
        self.positions.append(self.shimi.controller.get_present_position(self.motors))
        self.velocities.append([0.0 for m in self.motors])
        self.timestamps.append(0)

        start_time = time.time()
        while time.time() <= start_time + self.dur:
            # Sample the current position/velocity as fast as possible
            self.positions.append(self.shimi.controller.get_present_position(self.motors))
            vel = self.shimi.controller.get_present_speed(self.motors)
            vel = [abs(v) for v in vel]
            self.velocities.append(vel)
            t = time.time() - start_time
            self.timestamps.append(t)

        print("Done. Recorded {0} positions and {1} velocities.".format(len(self.positions), len(self.velocities)))

    def old_play(self, accel_style='linear'):
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

    def play(self):
        # Start the gesture at the initial position it read
        moves = []
        for i, m in enumerate(self.motors):
            move = LinearMove(self.shimi, m, self.positions[0][i], 1.0)
            moves.append(move)

        # Start all the moves
        for move in moves:
            move.start()

        # Wait for all the moves to finish
        for move in moves:
            move.join()

        # Covert the position measurements to rows for each motor
        pos_matrix = np.array(self.positions)
        vel_matrix = np.array(self.velocities)

        # Make sure the initial moving speed isn't 0 (which means move-as-fast-as-possible)
        for i, _ in enumerate(self.motors):
            if vel_matrix[0,i] < 1.0:
                vel_matrix[0,i] = 1.0

        # Find the positions at which direction change happens, interpolated to INTERP_FREQ [s] increments
        times_positions = [[[], []] for m in self.motors]
        for i, _ in enumerate(self.motors):
            zero_pos = np.interp(0, self.timestamps, pos_matrix[:,i])
            first_pos = np.interp(INTERP_FREQ, self.timestamps, pos_matrix[:,i])
            if first_pos - zero_pos >= 0:
                incr = True
            else:
                incr = False

            t = 2 * INTERP_FREQ
            last_pos = first_pos
            while t < self.dur:
                pos = np.interp(t, self.timestamps, pos_matrix[:,i])
                if incr and last_pos - pos < 0:
                    times_positions[i][TIME_INDEX].append(t)
                    times_positions[i][POS_INDEX].append(last_pos)
                    incr = not incr
                if not incr and last_pos - pos > 0:
                    times_positions[i][TIME_INDEX].append(t)
                    times_positions[i][POS_INDEX].append(last_pos)
                    incr = not incr
                last_pos = pos

                t += INTERP_FREQ

        # Add initial time (which should correspond to the first position change)
        # Add final position (which should correspond to the last position change time)
        for i, _ in enumerate(self.motors):
            times_positions[i][TIME_INDEX].insert(0, 0.0)
            times_positions[i][POS_INDEX].append(pos_matrix[-1,i])

        # Using the times and positions, and the captured speeds, set goal position on change and update speed
        t = 0
        while t < self.dur:
            # Measure the time it takes for updating in order to make the sleep time such that update occurs
            #   as close to INTERP_FREQ as possible
            compute_time = time.time()

            # Queues for setting multiple values at the same time
            motor_pos_to_set = []
            pos_to_set = []
            motor_vel_to_set = []
            vel_to_set = []

            for i, m in enumerate(self.motors):
                # Set a new goal pos if needed
                if len(times_positions[i][TIME_INDEX]) > 0 and abs(times_positions[i][TIME_INDEX][0] - t) <= ERROR:
                    # Note which motor needs to be moved
                    motor_pos_to_set.append(m)

                    # Add position to set queue
                    pos_to_set.append(times_positions[i][POS_INDEX].pop(0))

                    # Remove this position change time
                    times_positions[i][TIME_INDEX].pop(0)

                # Calculate velocity at this point
                motor_vel_to_set.append(m)
                vel_to_set.append(np.interp(t, self.timestamps, vel_matrix[:,i]))

            # Set speeds for all motors
            self.shimi.controller.set_moving_speed(dict(zip(motor_vel_to_set, vel_to_set)))

            # Set new goal positions for those that need it
            if len(motor_pos_to_set) > 0:
                print("Setting positions {}".format(dict(zip(motor_pos_to_set, pos_to_set))))
                self.shimi.controller.set_goal_position(dict(zip(motor_pos_to_set, pos_to_set)))

            # Sleep for INTERP_FREQ [s] minus compute time
            time.sleep(INTERP_FREQ - (time.time() - compute_time))
            t += INTERP_FREQ

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



