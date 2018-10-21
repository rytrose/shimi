from sklearn import linear_model
from sklearn.preprocessing import PolynomialFeatures
from sklearn.pipeline import make_pipeline
from sklearn.metrics import mean_squared_error, r2_score
import matplotlib.pyplot as plt
import pickle
import numpy as np
import os
import os.path as op
from motion.recorder import Recorder

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
    shimi_files = [op.join(shimi_path, filename) for filename in os.listdir(shimi_path)]

    posenet_input = None
    shimi_targets = None

    for posenet_file in posenet_files:
        posenet_split = posenet_file.split("/")[-1].split("_")
        if len(posenet_split) < 6:
            continue
        posenet_id = "_".join(posenet_split[0:3])
        shimi_file = [f for f in shimi_files if "_".join(f.split("/")[-1].split("_")[0:3]) == posenet_id]

        if not shimi_file:
            print("Couldn't find shimi gesture for PoseNet output %s" % posenet_file)
            continue

        shimi_file = shimi_file[0]

        posenet_object = pickle.load(open(posenet_file, 'rb'))
        shimi_object = pickle.load(open(shimi_file, 'rb'))

        posenet_vectors, posenet_timestamps = format_posenet(posenet_object)
        shimi_vectors, shimi_timestamps = format_shimi(shimi_object)

        # Use the shorter of the two recordings so as to not interpolate against non-existent data
        # They will likely be different lengths, but they should be very close, so this should not matter
        if posenet_timestamps[-1] < shimi_timestamps[-1]:
            duration = posenet_timestamps[-1]
        else:
            duration = shimi_timestamps[-1]

        timesteps = [INTERP_FREQ * i for i in range(int(duration / INTERP_FREQ))] + [duration]
        posenet_vectors_np = np.array(posenet_vectors)
        shimi_vectors_np = np.array(shimi_vectors)

        file_posenet_input = None
        file_shimi_targets = None

        for i in range(posenet_vectors_np.shape[1]):
            posenet_feature_interp = np.interp(timesteps, posenet_timestamps, posenet_vectors_np[:, i])
            if file_posenet_input is None:
                file_posenet_input = np.expand_dims(posenet_feature_interp, 0)
            else:
                file_posenet_input = np.concatenate((file_posenet_input, np.expand_dims(posenet_feature_interp, 0)),
                                                    axis=0)

        if posenet_input is None:
            posenet_input = file_posenet_input
        else:
            posenet_input = np.concatenate((posenet_input, file_posenet_input), axis=1)

        for i in range(shimi_vectors_np.shape[1]):
            shimi_position_interp = np.interp(timesteps, shimi_timestamps, shimi_vectors_np[:, i])
            if file_shimi_targets is None:
                file_shimi_targets = np.expand_dims(shimi_position_interp, 0)
            else:
                file_shimi_targets = np.concatenate((file_shimi_targets, np.expand_dims(shimi_position_interp, 0)),
                                                    axis=0)

        if shimi_targets is None:
            shimi_targets = file_shimi_targets
        else:
            shimi_targets = np.concatenate((shimi_targets, file_shimi_targets), axis=1)

    return posenet_input, shimi_targets


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
                points_vector[part_index + X_POINT] = float(point["position"]["x"])
                points_vector[part_index + Y_POINT] = float(point["position"]["y"])
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

def test_linear_regression(input_train, target_train, input_test, target_test):
    regr = make_pipeline(PolynomialFeatures(1), linear_model.Ridge())
    regr.fit(input_train.T, target_train.T)
    predictions = regr.predict(input_test.T)
    print("Mean squared error: %.2f"
          % mean_squared_error(target_test, predictions.T))
    print('Variance score: %.2f' % r2_score(target_test, predictions.T))

    target = Recorder(None, [1, 2, 3, 4, 5], target_test.T.shape[1] * 0.1)
    target.positions = target_test.T

    pred = Recorder(None, [1, 2, 3, 4, 5], predictions.shape[1] * 0.1)
    pred.positions = predictions

    a = plt.axes((0.0, 0.5, 1.0, 0.5))
    b = plt.axes((0.0, 0.0, 1.0, 0.5))

    target.plot(a)
    pred.plot(b)

    plt.show()

if __name__ == "__main__":
    posenet_train, shimi_train = format_posenet_shimi(posenet_path="../data_collection/posenet_gestures",
                         shimi_path="../data_collection/shimi_gestures")

    posenet_test, shimi_test = format_posenet_shimi(posenet_path="../data_collection/posenet_test",
                                                      shimi_path="../data_collection/shimi_test")

    test_linear_regression(posenet_train, shimi_train, posenet_test, shimi_test)
