from config.definitions import *
import bisect
import time


def normalize_position(id, position):
    """Normalizes motor positions based on their angle limits in degrees.

    Args:
        id (int): Motor ID.
        position (float): Motor position in degrees.

    Returns:
        float: Normalized motor position in range [0.0, 1.0].
    """
    return (position - ANGLE_LIMITS[id][0]) / (ANGLE_LIMITS[id][1] - ANGLE_LIMITS[id][0])


def denormalize_position(id, position):
    """Denormalizes motor positions based on their angle limits in degrees.

    Args:
        id (int): Motor ID.
        position (float): Motor position as a normalized value in range [0.0, 1.0].

    Returns:
        float: Denormalized motor position in degrees.
    """
    return (position * (ANGLE_LIMITS[id][1] - ANGLE_LIMITS[id][0])) + ANGLE_LIMITS[id][0]


def denormalize_to_range(value, range_min, range_max):
    """Takes a normalized value and denormalizes it into a specified range.

    Args:
        value (float): Normalized value in range [0.0, 1.0].
        range_min (float): The minimum of the range to denormalize into.
        range_max (float): The maximum of the range to denormalize into.

    Returns:
        float: Denormalized value in range [range_min, range_max].
    """
    return (value * (range_max - range_min)) + range_min


def normalize_to_range(value, range_min, range_max):
    """Takes a value and normalizes it with respect to a specified range.

    Args:
        value (float): A float value to normalize.
        range_min (float): The minimum of the range to normalize within.
        range_max (float): The maximum of the range to normalize within.

    Returns:
        float: Normalized value. Will be in range [0.0, 1.0] if value is in range [range_min, range_max].
    """
    return (value - range_min) / (range_max - range_min)


def countdown(duration):
    """Waits and prints the status of a countdown over a given duration.

    Args:
        duration (float): Duration of time to wait for.
    """
    waiting = duration
    while waiting > 0:
        print("{}...".format(waiting))
        sleep_time = min(1.0, waiting)
        waiting -= sleep_time
        time.sleep(sleep_time)


def quantize(value, quant):
    """Quantizes a value to the closest value in a list of quantized values.

    Args:
        value (float): Value to be quantized
        quant (List[float]): Quantized value options.

    Returns:
        float: Quantized input value.
    """
    mids = [(quant[i] + quant[i + 1]) / 2.0
            for i in range(len(quant) - 1)]
    ind = bisect.bisect_right(mids, value)
    return quant[ind]


def get_bit(byte_val, i):
    """Returns the value of a specified bit in a value.

    Args:
        byte_val (int): Number to check bit value from.
        i (int): Bit index to get.

    Returns:
        int: Value of the bit at index i in byte_val.
    """

    return (byte_val & (1 << i)) != 0


class Point:
    """Abstraction for a point from PoseNet."""

    def __init__(self, x, y, score=None):
        self.x = float(x)
        self.y = float(y)
        if score:
            self.score = float(score)
        else:
            self.score = None
