from config.definitions import *

# Maps 0 - 1 normalized positions to angle limits
def normalized_to_angle_positions(ids, positions):
    for i, id in enumerate(ids):
        positions[i] = denormalize(id, positions[i])

    return positions

def normalize(id, position):
    return (position - ANGLE_LIMITS[id][0]) / (ANGLE_LIMITS[id][1] - ANGLE_LIMITS[id][0])

def denormalize(id, position):
    return (position * (ANGLE_LIMITS[id][1] - ANGLE_LIMITS[id][0])) + ANGLE_LIMITS[id][0]