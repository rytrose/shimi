import sys, os

# Add parent to path
sys.path.insert(1, os.path.join(sys.path[0], '..'))

from shimi import Shimi
from posenet.posenet import PoseNet
from utils.utils import Point, normalize, denormalize
import time


class GenerativePhrase:
    def __init__(self, shimi=None):
        if shimi:
            self.shimi = shimi
        else:
            self.shimi = Shimi()
        self.posenet = PoseNet(self.shimi, on_pred=self.on_posenet_prediction)
        self.update_freq = 0.1
        self.last_update = time.time()
        self.last_pos = 0.5

    def on_posenet_prediction(self, pose, fps):
        # **N.B.** For simplification, this isn't being loaded from the config.yaml, where it is defined.
        #   I don't want to deal with the path nonsense at the moment, but could be a TODO
        POSENET_HEIGHT = 513
        POSENET_WIDTH = 513

        points = pose['keypoints']

        # Use nose as point of reference for face tracking
        nose = None
        for point in points:
            if point['part'] == 'nose':
                nose = Point(point['position']['x'], point['position']['y'], point['score'])

        if nose:
            SCORE_THRESH = 0.7
            MOVE_THRESH = 0.05
            MIN_VEL = 40
            MAX_VEL = 100

            if nose.score > SCORE_THRESH:
                if time.time() > self.last_update + self.update_freq:
                    # Calculate where to look
                    #  Camera image is flipped
                    pos = 1 - (nose.x / POSENET_WIDTH)

                    # Calculate speed based on how far to move
                    current_pos = normalize(self.shimi.neck_lr, self.shimi.controller.get_present_speed([self.shimi.neck_lr])[0])
                    vel = max(MIN_VEL + abs(current_pos - pos) * MAX_VEL, MIN_VEL)

                    if abs(self.last_pos - pos) > MOVE_THRESH:
                        print("Moving to %f at vel %f" % (pos, vel))

                        self.shimi.controller.set_moving_speed({self.shimi.neck_lr: vel})
                        self.shimi.controller.set_goal_position({self.shimi.neck_lr: denormalize(self.shimi.neck_lr, pos)})

                        self.last_pos = pos
                    
                    self.last_update = time.time()
