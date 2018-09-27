from pypot.utils import StoppableThread
from motion.move import *
from motion.playback import *
import numpy as np
import time
import pickle

TIME_INDEX = 0
POS_INDEX = 1
ERROR = 0.0001
INTERP_FREQ = 0.1

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
        while time.time() <= start_time + self.duration:
            # Sample the current position/velocity as fast as possible
            self.positions.append(self.shimi.controller.get_present_position(self.motors))
            vel = self.shimi.controller.get_present_speed(self.motors)
            vel = [abs(v) for v in vel]
            self.velocities.append(vel)
            t = time.time() - start_time
            self.timestamps.append(t)

        print("Done. Recorded {0} positions and {1} velocities.".format(len(self.positions), len(self.velocities)))

    def play(self, pos_ax=None, vel_ax=None):
        playback(self.shimi, self.motors, self.duration, self.timestamps, self.positions, self.velocities, pos_ax, vel_ax)

    def plot(self, ax):
        t = np.linspace(0, self.duration, len(self.positions))

        pos_matrix = np.array(self.positions)

        for i, _ in enumerate(self.motors):
            ax.plot(t, pos_matrix[:,i])

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

    def save(self, name):
        # Save pickle of this object
        gesture = {
            "motors": self.motors,
            "duration": self.duration,
            "positions": self.positions,
            "velocities": self.velocities,
            "timestamps": self.timestamps
        }
        pickle.dump(gesture, open("saved_gestures/" + str(name) + ".p", "wb"))

def load_recorder(shimi, name):
    # Unpickle the gesture
    gesture = pickle.load(open("saved_gestures/" + str(name) + ".p", "rb"))

    # Recreate the recorder
    r = Recorder(shimi, gesture["motors"], gesture["duration"])
    r.positions = gesture["positions"]
    r.velocities = gesture["velocities"]
    r.timestamps = gesture["timestamps"]

    # Return the recorder
    return r