from config.definitions import *
import time


def normalize_position(id, position):
    return (position - ANGLE_LIMITS[id][0]) / (ANGLE_LIMITS[id][1] - ANGLE_LIMITS[id][0])


def denormalize_position(id, position):
    return (position * (ANGLE_LIMITS[id][1] - ANGLE_LIMITS[id][0])) + ANGLE_LIMITS[id][0]


def denormalize_to_range(value, range_min, range_max):
    return (value * (range_max - range_min)) + range_min


def countdown(duration):
    waiting = duration
    while waiting > 0:
        print("{}...".format(waiting))
        sleep_time = min(1.0, waiting)
        waiting -= sleep_time
        time.sleep(sleep_time)


# A class to abstract a point from PoseNet
class Point:
    def __init__(self, x, y, score=None):
        self.x = float(x)
        self.y = float(y)
        if score:
            self.score = float(score)
        else:
            self.score = None
