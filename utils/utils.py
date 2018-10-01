from config.definitions import *
import time

# Maps 0 - 1 normalized positions to angle limits
def normalized_to_angle_positions(ids, positions):
    for i, id in enumerate(ids):
        positions[i] = denormalize(id, positions[i])

    return positions

def normalize(id, position):
    return (position - ANGLE_LIMITS[id][0]) / (ANGLE_LIMITS[id][1] - ANGLE_LIMITS[id][0])

def denormalize(id, position):
    return (position * (ANGLE_LIMITS[id][1] - ANGLE_LIMITS[id][0])) + ANGLE_LIMITS[id][0]

def countdown(duration):
    waiting = duration
    while waiting > 0:
        print("{}...".format(waiting))
        sleep_time = min(1.0, waiting)
        waiting -= sleep_time
        time.sleep(sleep_time)