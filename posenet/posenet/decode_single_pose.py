import tensorflow as tf
import numpy as np

PART_NAMES = [
    'nose', 'leftEye', 'rightEye', 'leftEar', 'rightEar', 'leftShoulder',
    'rightShoulder', 'leftElbow', 'rightElbow', 'leftWrist', 'rightWrist',
    'leftHip', 'rightHip', 'leftKnee', 'rightKnee', 'leftAnkle', 'rightAnkle'
]

NUM_KEYPOINTS = len(PART_NAMES)  # 17


#############################################################
# argmax2d
#
# Input:
#   t (H, W, D)
# Returns:
#   tensor (D, 2), with each row as [y, x] of argmax for D
############################################################
def argmax2d(t):
    if len(t.shape) > 3:
        t = tf.squeeze(t)

    if not len(t.shape) == 3:
        print("Input must be a 3D tensor, or be able to be squeezed into one.")
        return

    height, width, depth = t.shape

    reshaped_t = tf.reshape(t, [height * width, depth])
    argmax_coords = tf.cast(tf.argmax(reshaped_t, axis=0), tf.int32)
    y_coords = argmax_coords // width
    x_coords = argmax_coords % width

    return tf.concat([tf.expand_dims(y_coords, 1), tf.expand_dims(x_coords, 1)], axis=1)


###################################################################################
# get_offset_vectors
#
# Input:
#   heatmap_coords (NUM_KEYPOINTS, 2)
#   offsets (height, width, NUM_KEYPOINTS * 2)
# Returns:
#   tensor (NUM_KEYPOINTS, 2), with each row as [y, x] of offset for each keypoint
###################################################################################
def get_offset_vectors(heatmaps_coords, offsets):

    result = []

    for keypoint in range(NUM_KEYPOINTS):
        heatmap_y = heatmaps_coords[keypoint, 0]
        heatmap_x = heatmaps_coords[keypoint, 1]

        offset_y = offsets[heatmap_y, heatmap_x, keypoint]
        offset_x = offsets[heatmap_y, heatmap_x, keypoint + NUM_KEYPOINTS]

        result.append([offset_y, offset_x])

    return result


############################################################################################
# get_offset_points
#
# Input:
#   heatmap_coords (NUM_KEYPOINTS, 2)
#   offsets (height, width, NUM_KEYPOINTS * 2)
#   output_stride (scalar)
# Returns:
#   tensor (NUM_KEYPOINTS, 2), with each row as [y, x] location prediction for each keypoint
#############################################################################################
def get_offset_points(heatmaps_coords, offsets, output_stride):
    offset_vectors = get_offset_vectors(heatmaps_coords, offsets)
    scaled_heatmap = heatmaps_coords * output_stride
    return tf.cast(scaled_heatmap, tf.float32) + offset_vectors


def get_points_confidence(heatmaps, heatmaps_coords):

    result = []

    for keypoint in range(NUM_KEYPOINTS):
        # Get max value of heatmap for each keypoint
        result.append(heatmaps[heatmaps_coords[keypoint, 0], heatmaps_coords[keypoint, 1], keypoint])

    return result

def decode_single_pose(heatmaps, offsets, output_stride):
    # Squeeze into 3D tensors
    heatmaps = tf.squeeze(heatmaps)
    offsets = tf.squeeze(offsets)

    heatmaps_coords = argmax2d(heatmaps)
    offset_points = get_offset_points(heatmaps_coords, offsets, output_stride)
    keypoint_confidence = get_points_confidence(heatmaps, heatmaps_coords)

    keypoints = [{
        "position": {
            "y": offset_points[keypoint, 0].eval(),
            "x": offset_points[keypoint, 1].eval()
        },
        "part": PART_NAMES[keypoint],
        "score": score.eval()
    } for keypoint, score in enumerate(keypoint_confidence)]

    return {
        "keypoints": keypoints,
        "score": (sum(keypoint_confidence) / len(keypoint_confidence)).eval()
    }