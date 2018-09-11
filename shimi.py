from config.definitions import *
from primitives.safety import *
import pypot.robot
from pypot.primitive.move import MoveRecorder, MovePlayer
import time

class Shimi():

    # Constructor
    def __init__(self, model_path):
        # Attempt to load robot model
        self.robot = pypot.robot.from_json(model_path)

        # Set the motors to dummy movement
        for m in self.robot.motors:
            m.goto_behavior = 'dummy'

        # Run temperature monitoring
        self.robot.attach_primitive(TemperatureMonitor(self.robot), 'temperature_monitoring')
        self.robot.temperature_monitoring.start()

        # Run torque limiting
        self.robot.attach_primitive(LimitTorque(self.robot), 'limit_torque')
        self.robot.limit_torque.start()

        # Set motors to initial positions
        self.initial_position()

    @property
    def torso(self):
        return self.robot.motors[TORSO - 1]

    @property
    def neck_ud(self):
        return self.robot.motors[NECK_UD - 1]

    @property
    def neck_lr(self):
        return self.robot.motors[NECK_LR - 1]

    @property
    def phone(self):
        return self.robot.motors[PHONE - 1]

    @property
    def foot(self):
        return self.robot.motors[FOOT - 1]

    @property
    def all_motors(self):
        return self.robot.motors

    # Moves the motors to the initial position set in config/definitions
    def initial_position(self):
        # Make sure the motors are not compliant
        for m in self.robot.motors:
            m.compliant = False

        # Move to starting positions over 1000ms
        print("Setting motors to starting positions {0}".format(STARTING_POSITIONS))
        self.robot.goto_position({m.name: STARTING_POSITIONS[m.id] for m in self.robot.motors}, 1.0, wait=True)

    # Sets the motors to compliant so they can be moved by hand
    def make_compliant(self):
        # Set the motors to compliant
        for m in self.robot.motors:
            m.compliant = True

    # Closes the serial connections to the motors
    def close(self):
        self.robot.close()

    # Returns a recorder with the specified motors
    def get_recorder(self, motors):
        return MoveRecorder(self.robot, 50, motors)

    # Starts and ends a recording
    def make_recording(self, recorder, wait_time=3, recording_time=10):
        # Make the motors compliant if they're not already
        self.make_compliant()

        # For counting down
        timer = wait_time

        # Count down
        while timer > 0:
            print(str(timer) + "...")
            time.sleep(1)
            timer -= 1

        # Start recording
        print("Starting to record!")
        recorder.start()

        # Sleep for record time
        time.sleep(recording_time)

        # Stop recording
        print("Recording stopped.")
        recorder.stop()

        # Print number of frames
        print("{0} frames recorded.".format(len(recorder.move.positions())))

        return recorder

    # Plays back recordings
    def play_recordings(self, recorders, blocking=True):
        players = []

        # Creates MovePlayers
        for recorder in recorders:
            players.append(MovePlayer(self.robot, recorder))

        # Start playback
        for player in players:
            player.start()

        # Wait for the longest to stop
        if(blocking):
            longest = players[0]

            # Find the longest-lasting recording
            for i in range(1, len(players)):
                if players[i].duration() > longest.duration():
                    longest = players[i]

            longest.wait_to_stop()

