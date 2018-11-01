import sys, os
# Add parent to path
sys.path.insert(1, os.path.join(sys.path[0], '..'))

from shimi import Shimi
from posenet.posenet import PoseNet
from pprint import pprint

def on_posenet_prediction(pose, fps):
    pprint(pose)
    print("FPS: %f" % fps)

shimi = Shimi()
posenet = PoseNet(shimi, on_pred=on_posenet_prediction)