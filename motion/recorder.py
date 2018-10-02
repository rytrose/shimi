from pypot.utils import StoppableThread
import matplotlib.pyplot as plt
from motion.move import *
from motion.playback import *
from utils.utils import countdown
import numpy as np
import time
import pickle


class Recorder():
    def __init__(self, shimi, motors, duration, wait_time=3.0):
        self.shimi = shimi
        self.motors = motors
        self.duration = duration
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
        countdown(self.wait)

        # Make the recording
        print("Recording...")

        # Initial position/velocity/time
        self.positions.append(self.shimi.controller.get_present_position(self.motors))
        self.velocities.append([0.0 for m in self.motors])
        self.timestamps.append(0)

        start_time = time.time()
        while time.time() <= start_time + self.duration:
            # Sample the current position/velocity as fast as possible
            self.positions.append(self.shimi.controller.get_present_position(self.motors))
            vel = self.shimi.controller.get_present_speed(self.motors)
            vel = [abs(v) for v in vel]
            self.velocities.append(vel)
            t = time.time() - start_time
            self.timestamps.append(t)

        print("Done. Recorded {0} positions and {1} velocities.".format(len(self.positions), len(self.velocities)))

    def play(self, pos_ax=None, vel_ax=None, callback=None):
        playback(self.shimi, self.motors, self.duration, self.timestamps, self.positions, self.velocities, pos_ax,
                 vel_ax, callback=callback)

    def plot(self, ax):
        t = np.linspace(0, self.duration, len(self.positions))

        pos_matrix = np.array(self.positions)

        for i, _ in enumerate(self.motors):
            ax.plot(t, pos_matrix[:, i])

        ax.legend(self.motors)
        ax.set_xlabel('Time (in s)')
        ax.set_ylabel('Position (in degrees)')

    def append_recording(self, r):
        # Only allow appending if recorded motors are the same
        if self.motors != r.motors:
            print("Can't append a recording with a differrent set of motors.")
            return

        # Only allow appending if a this recording actually contains a recording
        if len(self.timestamps) == 0:
            print("Can't append to a blank recording.")
            return

        # Space between data points to add between recordings
        delta_t = self.timestamps[1] - self.timestamps[0]

        # Fix timestamps for all of r
        r_timestamps = list(map(lambda t: t + self.timestamps[-1] + delta_t, r.timestamps))

        # Add timestamps
        self.timestamps += r_timestamps

        # Add positions
        self.positions += r.positions

        # Add velocities
        self.velocities += r.velocities

        # Add duration
        self.duration += r.duration

        print("Recording appended.")

    def save(self, name, path="saved_gestures"):
        # Save pickle of this object
        gesture = {
            "motors": self.motors,
            "duration": self.duration,
            "positions": self.positions,
            "velocities": self.velocities,
            "timestamps": self.timestamps
        }
        pickle.dump(gesture, open(path + "/" + str(name) + ".p", "wb"))

    def trim(self, duration, end="front"):
        # Make timestamps a numpy array to get new front more easily
        times = np.array(self.timestamps)
        if end == "front":
            # Get the new front
            new_front_index = (np.abs(times - duration)).argmin()
            new_front_value = self.timestamps[new_front_index]

            # Trim timestamps, positions, velocities
            self.timestamps = self.timestamps[new_front_index:]
            self.positions = self.positions[new_front_index:]
            self.velocities = self.velocities[new_front_index:]

            # Re-zero timestamps
            for i, timestamp in enumerate(self.timestamps):
                self.timestamps[i] = timestamp - new_front_value

            # Shorten duration
            self.duration = self.timestamps[-1]
        else:
            # Get new duration
            new_duration = self.duration - duration

            # Get the new back
            new_back_index = (np.abs(times - new_duration)).argmin()

            # Trim timestamps, positions, velocities
            self.timestamps = self.timestamps[:new_back_index + 1]
            self.positions = self.positions[:new_back_index + 1]
            self.velocities = self.velocities[:new_back_index + 1]

            # Shorten duration
            self.duration = new_duration

        # Plot new recorder
        self.plot(plt.axes())

    def add_recording(self, new_recording):
        new_pos_matrix = np.array(new_recording.positions)
        new_vel_matrix = np.array(new_recording.velocities)

        # Add motor(s) to this recording
        for m in new_recording.motors:
            if m in self.motors:
                # Don't allow recordings with the same motors
                print("Unable to overwrite motor recording.")
                return
            else:
                self.motors.append(m)

        for i, _ in enumerate(new_recording.motors):
            for j, t in enumerate(self.timestamps):
                # Add positions and velocities from new recording based on the current recording's time stamps
                pos = list(self.positions[j])
                pos.append(np.interp(t, new_recording.timestamps, new_pos_matrix[:, i]))
                self.positions[j] = tuple(pos)
                vel = list(self.velocities[j])
                vel.append(np.interp(t, new_recording.timestamps, new_vel_matrix[:, i]))
                self.velocities[j] = tuple(vel)


def load_recorder(shimi, name, path="saved_gestures"):
    # Unpickle the gesture
    gesture = pickle.load(open(path + "/" + str(name) + ".p", "rb"))

    # Recreate the recorder
    r = Recorder(shimi, gesture["motors"], gesture["duration"])
    r.positions = gesture["positions"]
    r.velocities = gesture["velocities"]
    r.timestamps = gesture["timestamps"]

    # Return the recorder
    return r
