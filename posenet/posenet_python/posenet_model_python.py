import json
import struct
import tensorflow as tf
from tensorflow.python.saved_model import tag_constants
import cv2
import numpy as np
import os
import os.path as op
import yaml
import time
import argparse
import pickle
from decode_single_pose import decode_single_pose
from pythonosc import udp_client


class PoseNetPython():
    def __init__(self, ip=None, port=None, project_path="."):
        self.project_path = project_path

        self.width, \
        self.height, \
        self.checkpoint, \
        self.MobileNet_architecture, \
        self.output_stride = self.setup()

        self.model_path = os.path.join(self.project_path, "models", self.checkpoint)

        self.cam = cv2.VideoCapture(0)
        if not self.cam.isOpened():
            self.cam = None
            print("No webcam connected.")
        else:
            print("Webcam connected.")

        if not op.exists(self.model_path):
            print("Making model...")
            self.make_model()

        if ip and port:
            self.address = "/predictions"
            print("Sending predictions to %s at %s:%d" % (self.address, ip, port))
            self.client = udp_client.SimpleUDPClient(ip, port)
        else:
            self.client = None

    def setup(self):
        # Load configuration from YAML file
        cfg = yaml.load(open(op.join(self.project_path, "config.yaml"), "r+"))
        output_stride = cfg['outputStride']
        checkpoints = cfg['checkpoints']
        checkpoint = checkpoints[cfg['chk']]

        if checkpoint == 'mobilenet_v1_050':
            MobileNet_architecture = cfg['mobileNet50Architecture']
        elif checkpoint == 'mobilenet_v1_075':
            MobileNet_architecture = cfg['mobileNet75Architecture']
        # MobileNet101Architecture not implemented
        else:
            MobileNet_architecture = cfg['mobileNet100Architecture']

        width = cfg['imageSize'][0]
        height = cfg['imageSize'][1]

        return width, height, checkpoint, MobileNet_architecture, output_stride

    def make_model(self):
        # Define conv layers
        layers = toOutputStridedLayers(self.MobileNet_architecture, self.output_stride)

        # Load weights
        weights_file = open(os.path.join(self.project_path, "weights", self.checkpoint, "manifest.json"))
        variables = json.load(weights_file)
        weights_file.close()

        # Format weights and load as tensors
        for x in variables:
            filename = variables[x]["filename"]
            byte = open(os.path.join(self.project_path, "weights", self.checkpoint, filename), 'rb').read()
            fmt = str(int(len(byte) / struct.calcsize('f'))) + 'f'
            d = struct.unpack(fmt, byte)
            d = tf.cast(d, tf.float32)
            d = tf.reshape(d, variables[x]["shape"])
            variables[x]["x"] = tf.Variable(d, name=x)

        # Define network
        image = tf.placeholder(tf.float32, shape=[1, self.width, self.height, 3], name='image')
        x = image
        buff = []
        with tf.variable_scope(None, 'MobilenetV1'):
            for m in layers:
                stride = [1, m['stride'], m['stride'], 1]
                rate = [m['rate'], m['rate']]
                if (m['convType'] == "conv2d"):
                    x = conv(x, variables, stride, m['blockId'])
                    buff.append(x)
                elif (m['convType'] == "separableConv"):
                    x = separableConv(x, variables, stride, m['blockId'], rate)
                    buff.append(x)

        heatmaps = convToOutput(variables, x, 'heatmap_2')
        offsets = convToOutput(variables, x, 'offset_2')
        displacement_fwd = convToOutput(variables, x, 'displacement_fwd_2')
        displacement_bwd = convToOutput(variables, x, 'displacement_bwd_2')
        heatmaps = tf.sigmoid(heatmaps, 'heatmap')

        # Initialize and create saver
        init = tf.global_variables_initializer()
        saver = tf.train.Saver()

        # Save checkpoints and model
        with tf.Session() as sess:
            sess.run(init)

            out = sess.run([heatmaps, offsets, displacement_fwd, displacement_bwd], feed_dict={
                image: [np.ndarray(shape=(self.width, self.height, 3), dtype=np.float32)]
            })

            print("Saving model...")

            tf.saved_model.simple_save(sess, self.model_path,
                                       {
                                           "image": image
                                       },
                                       {
                                           "heatmaps": heatmaps,
                                           "offsets": offsets,
                                           "displacement_fwd": displacement_fwd,
                                           "displacement_bwd": displacement_bwd
                                       })

            print("saved.")

    def run_from_webcam(self, display=False):
        if not self.cam:
            print("No webcam connected.")
            return

        graph = tf.Graph()
        with graph.as_default():
            with tf.Session(graph=graph) as sess:
                print("Loading model...")

                tf.saved_model.loader.load(
                    sess,
                    [tag_constants.SERVING],
                    self.model_path
                )

                # Get restored tensors
                image = graph.get_tensor_by_name("image:0")
                heatmaps = graph.get_tensor_by_name("heatmap:0")
                offsets = graph.get_tensor_by_name("offset_2:0")
                displacement_fwd = graph.get_tensor_by_name("displacement_fwd_2:0")
                displacement_bwd = graph.get_tensor_by_name("displacement_bwd_2:0")
    
                print("...loaded.")
            
                start_time = time.time()
                frames_completed = 0
                fps = 0
                # Start reading from webcam
                while True:
                    ret_val, img = self.cam.read()

                    input_image = format_img(img, self.width, self.height)
                    input_image = np.array(input_image, dtype=np.float32)
                    input_image = input_image.reshape(1, self.width, self.height, 3)

                    heatmaps_result, offsets_result, displacement_fwd_result, displacement_bwd_result = sess.run(
                        [heatmaps, offsets, displacement_fwd, displacement_bwd], feed_dict={image: input_image})

                    heatmaps_result = np.array(heatmaps_result)
                    offsets_result = np.array(offsets_result)

                    prediction = decode_single_pose(heatmaps_result, offsets_result, self.output_stride)

                    if display:

                        img = cv2.resize(img, (self.width, self.height))

                        for k in prediction["keypoints"]:
                            cv2.circle(img, (int(round(float(k["position"]["x"]))), int(round(float(k["position"]["y"])))), 2,
                                   (0, 255, 0), -1)

                        cv2.imshow("PoseNet", img)

                        if cv2.waitKey(1) == 27:
                            break  # esc to quit

                    frames_completed += 1
                    time_elapsed = time.time() - start_time
                    if time_elapsed > 1:
                        fps = frames_completed / time_elapsed
                        frames_completed = 0
                        start_time = time.time()

                    if self.client:
                        self.client.send_message(self.address, [json.dumps(prediction), fps])                

                    cv2.destroyAllWindows()

    def run_on_videos(self, video_path="", out_path="."):
        if not video_path:
            print("Please specify path of videos to run PoseNet on.")
            return

        video_paths = [op.join(video_path, filename) for filename in os.listdir(video_path)]
        
        graph = tf.Graph()
        with graph.as_default():
            with tf.Session(graph=graph) as sess:
                print("Loading model...")

                tf.saved_model.loader.load(
                    sess,
                    [tag_constants.SERVING],
                    self.model_path
                )

                # Get restored tensors
                image = graph.get_tensor_by_name("image:0")
                heatmaps = graph.get_tensor_by_name("heatmap:0")
                offsets = graph.get_tensor_by_name("offset_2:0")
                displacement_fwd = graph.get_tensor_by_name("displacement_fwd_2:0")
                displacement_bwd = graph.get_tensor_by_name("displacement_bwd_2:0")

                print("...loaded.")                

                for video in video_paths:
                    print("Opening %s" % video)
                    cap = cv2.VideoCapture(video)
                    frames = []
                    
                    while(cap.isOpened()):
                        ret, img = cap.read()
                        if not ret:
                            break
                        try:
                            input_image = format_img(img, self.width, self.height)
                            input_image = np.array(input_image, dtype=np.float32)
                            input_image = input_image.reshape(1, self.width, self.height, 3)

                            heatmaps_result, offsets_result, displacement_fwd_result, displacement_bwd_result = sess.run(
                                [heatmaps, offsets, displacement_fwd, displacement_bwd], feed_dict={image: input_image})

                            heatmaps_result = np.array(heatmaps_result)
                            offsets_result = np.array(offsets_result)

                            prediction = decode_single_pose(heatmaps_result, offsets_result, self.output_stride)

                            frames.append({
                                "prediction": prediction,
                                "timestamp": cap.get(cv2.CAP_PROP_POS_MSEC) / 1000
                            })
                        except Exception:
                            print("Unable to predict frame.")

                        img = cv2.resize(img, (self.width, self.height))

                        for k in prediction["keypoints"]:
                            cv2.circle(img, (int(round(float(k["position"]["x"]))), int(round(float(k["position"]["y"])))), 2,
                                   (0, 255, 0), -1)

                        cv2.imshow("PoseNet", img)

                        if cv2.waitKey(1) == 27:
                            break  # esc to quit

                    cap.release()
                    
                    filename = video.split("/")[-1][:-4] + "_posenet.p"
                    save_path = op.join(out_path, filename)
                    pickle.dump(frames, open(save_path, 'wb'))
                    print("Saved video to %s" % save_path)       


###########################
# Model building functions
###########################
def toOutputStridedLayers(convolutionDefinition, outputStride):
    currentStride = 1
    rate = 1
    blockId = 0
    buff = []
    for _a in convolutionDefinition:
        convType = _a[0]
        stride = _a[1]

        if (currentStride == outputStride):
            layerStride = 1
            layerRate = rate
            rate *= stride
        else:
            layerStride = stride
            layerRate = 1
            currentStride *= stride

        buff.append({
            'blockId': blockId,
            'convType': convType,
            'stride': layerStride,
            'rate': layerRate,
            'outputStride': currentStride
        })
        blockId += 1

    return buff


def convToOutput(variables, mobileNetOutput, outputLayerName):
    w = tf.nn.conv2d(mobileNetOutput, weights(variables, outputLayerName), [1, 1, 1, 1], padding='SAME')
    w = tf.nn.bias_add(w, biases(variables, outputLayerName), name=outputLayerName)
    return w


def conv(inputs, variables, stride, blockId):
    return tf.nn.relu6(
        tf.nn.conv2d(inputs, weights(variables, "Conv2d_" + str(blockId)), stride, padding='SAME')
        + biases(variables, "Conv2d_" + str(blockId)))


def weights(variables, layerName):
    return variables["MobilenetV1/" + layerName + "/weights"]['x']


def biases(variables, layerName):
    return variables["MobilenetV1/" + layerName + "/biases"]['x']


def depthwiseWeights(variables, layerName):
    return variables["MobilenetV1/" + layerName + "/depthwise_weights"]['x']


def separableConv(inputs, variables, stride, blockID, dilations):
    if (dilations == None):
        dilations = [1, 1]

    dwLayer = "Conv2d_" + str(blockID) + "_depthwise"
    pwLayer = "Conv2d_" + str(blockID) + "_pointwise"

    w = tf.nn.depthwise_conv2d(inputs, depthwiseWeights(variables, dwLayer), stride, 'SAME', rate=dilations,
                               data_format='NHWC')
    w = tf.nn.bias_add(w, biases(variables, dwLayer))
    w = tf.nn.relu6(w)

    w = tf.nn.conv2d(w, weights(variables, pwLayer), [1, 1, 1, 1], padding='SAME')
    w = tf.nn.bias_add(w, biases(variables, pwLayer))
    w = tf.nn.relu6(w)

    return w


def read_imgfile(path, width, height):
    img = cv2.imread(path)
    img = cv2.resize(img, (width, height))
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = img.astype(float)
    img = img * (2.0 / 255.0) - 1.0
    return img


def format_img(img, width, height):
    img = cv2.resize(img, (width, height))
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = img.astype(float)
    img = img * (2.0 / 255.0) - 1.0
    return img

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--path", default=".", help="The path to this project directory.")
    parser.add_argument("--ip", default=None, help="The IP address of the OSC server to send to.")
    parser.add_argument("--port", type=int, default=None, help="The port of the OSC server to send to.")
    parser.add_argument("--run", default="webcam", help="What PoseNet should be run on.")
    parser.add_argument("--video_path", default=".", help="Path to directory of videos that PoseNet should be run on.")
    parser.add_argument("--out_path", default=".", help="Path that any output should be saved to.")
    parser.add_argument("--display", default=False, help="For webcam, show the annotated video feed.")
    args = parser.parse_args()

    if args.run == "webcam":
        p = PoseNetPython(ip=args.ip, port=args.port, project_path=args.path)
        p.run_from_webcam(display=args.display)
    elif args.run == "videos":
        p = PoseNetPython(project_path=args.path)
        p.run_on_videos(video_path=args.video_path, out_path=args.out_path)
