from sklearn import linear_model
import pickle
import numpy as np
import os
import os.path as op

SUBJECT_NUMBER = 0
GESTURE_NUMBER = 1
POSITION = 2
INTERP_FREQ = 0.01
ALL_POSENET_POINTS = [
    'nose', 'leftEye', 'rightEye', 'leftEar', 'rightEar', 'leftShoulder',
    'rightShoulder', 'leftElbow', 'rightElbow', 'leftWrist', 'rightWrist',
    'leftHip', 'rightHip', 'leftKnee', 'rightKnee', 'leftAnkle', 'rightAnkle'
]

POSENET_POINTS_TO_USE = ['nose', 'leftEye', 'rightEye', 'leftEar', 'rightEar', 'leftShoulder',
                         'rightShoulder', 'leftWrist', 'rightWrist']

X_POINT = 0
Y_POINT = 1


def format_posenet_shimi(posenet_path=".", shimi_path="."):
    posenet_files = [op.join(posenet_path, filename) for filename in os.listdir(posenet_path)]
    shimi_files = [op.join(posenet_path, filename) for filename in os.listdir(shimi_path)]

    posenet_input = []
    shimi_targets = []

    for posenet_file in posenet_files:
        posenet_split = posenet_file.split("_")
        posenet_id = "_".join(posenet_split[0:3])
        shimi_file = [f for f in shimi_files if "_".join(f.split("_")[0:3]) == posenet_id]

        if not shimi_file:
            print("Couldn't find shimi gesture for PoseNet output %s" % posenet_file)
            continue

        shimi_file = shimi_file[0]

        posenet_object = pickle.load(open(posenet_file, 'rb'))
        shimi_object = pickle.load(open(shimi_file, 'rb'))

        posenet_vectors, posenet_timestamps = format_posenet(posenet_object)
        shimi_vectors, shimi_timestamps = format_posenet(shimi_object)

        # Use the shorter of the two recordings so as to not interpolate against non-existent data
        # They will likely be different lengths, but they should be very close, so this should not matter
        if posenet_timestamps[-1] < shimi_timestamps[-1]:
            iterating_over_vectors = posenet_vectors
            iterating_over_timestamps = posenet_timestamps
            other_vectors = shimi_vectors
            other_timestamps = shimi_timestamps
        else:
            iterating_over_vectors = shimi_vectors
            iterating_over_timestamps = shimi_timestamps
            other_vectors = posenet_vectors
            other_timestamps = posenet_timestamps

        for i, vector in enumerate(iterating_over_vectors):
            pass


def format_posenet(posenet_object):
    """
    Formats a PoseNet analyzed video into a list of feature vectors with shape 2 * len(POSENET_POINTS_TO_USE) X N,
        where N is the number of frames in the video.
    :param posenet_object: a collection of PoseNet analyzed frames for a video, with timestamps
    :return: a list of feature vectors, a list of timestamps for the feature vectors
    """
    points_vectors = []
    timestamps = []

    for prediction in posenet_object:
        points_vector = [0 for _ in range(2 * len(POSENET_POINTS_TO_USE))]

        for point in prediction["prediction"]["keypoints"]:
            try:
                part_index = POSENET_POINTS_TO_USE.index(point["part"])
                points_vector[part_index + X_POINT] = point["position"]["x"]
                points_vector[part_index + Y_POINT] = point["position"]["y"]
            except ValueError:
                continue

        points_vectors.append(points_vector)
        timestamps.append(prediction["timestamp"])

    return points_vectors, timestamps


def format_shimi(shimi_object):
    """
    Formats a recording of a Shimi gesture into a list of position vectors with shape 5 X N, where N is the
        the number of position samples in the gesture. Each vector is the position of Shimi's 5 motors in ascending
        motor ID order, i.e. index 0 is motor ID 1, and index 4 is motor ID 5
    :param shimi_object: a representation of a Shimi gesture saved from motion/recorder.py
    :return: a list of position vectors, a list of timestamps for the position vectors
    """

    motors = shimi_object["motors"]
    positions_vectors = []
    timestamps = []

    for i, positions in enumerate(shimi_object["positions"]):
        positions_vector = [0 for _ in range(5)]

        for j, position in enumerate(positions):
            positions_vector[motors[j] - 1] = position

        positions_vectors.append(positions_vector)
        timestamps.append(shimi_object["timestamps"][i])

    return positions_vectors, timestamps
