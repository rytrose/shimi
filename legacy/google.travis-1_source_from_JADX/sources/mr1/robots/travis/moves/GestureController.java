package mr1.robots.travis.moves;

import android.content.Context;
import mr1.robots.travis.moves.gestures.emotions.EmotionConstants;

public class GestureController {
    private float beatDuration;
    public Gesture gesture;
    public GestureScene gestureScene = new GestureScene(this.gesture);

    public GestureController(Context c, float beatDuration) {
        this.gesture = new Gesture(c, beatDuration);
    }

    public GestureController(Context c, float beatDuration, Gesture gest) {
        this.gesture = gest;
        this.gesture.setBeatDuration(beatDuration);
    }

    public boolean doFunction(String command, float beats) {
        if (command.equalsIgnoreCase("swoop")) {
            this.gestureScene.swoop(beats);
            return true;
        } else if (command.equalsIgnoreCase("swoopCenter")) {
            this.gestureScene.swoopCenter(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopRight")) {
            this.gesture.swoopRight(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopLeft")) {
            this.gesture.swoopLeft(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopRightMid")) {
            this.gesture.swoopRightMid(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopLeftMid")) {
            this.gesture.swoopLeftMid(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopRightHigh")) {
            this.gesture.swoopRightHigh(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopLeftHigh")) {
            this.gesture.swoopLeftHigh(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopCenterRight")) {
            this.gesture.swoopCenterRight(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopCenterLeft")) {
            this.gesture.swoopCenterLeft(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopNeckRight")) {
            this.gesture.swoopNeckRight(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopNeckRight")) {
            this.gesture.swoopNeckRight(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopNeckLeft")) {
            this.gesture.swoopNeckLeft(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopDownLeft")) {
            this.gesture.swoopDownLeft(beats);
            return false;
        } else if (command.equalsIgnoreCase("swoopDownRight")) {
            this.gesture.swoopDownRight(beats);
            return false;
        } else if (command.equalsIgnoreCase("lookAtPhone")) {
            this.gesture.lookAtPhone(beats);
            return true;
        } else if (command.equalsIgnoreCase("lookAtPhoneAlt")) {
            this.gesture.lookAtPhone_Alt(beats);
            return true;
        } else if (command.equalsIgnoreCase("home")) {
            this.gesture.home(beats);
            return true;
        } else if (command.equalsIgnoreCase("lookAtLeg")) {
            this.gesture.lookAtLeg(beats);
            return true;
        } else if (command.equalsIgnoreCase("tap")) {
            this.gesture.tap(beats);
            return true;
        } else if (command.equalsIgnoreCase("handTwist")) {
            this.gesture.handTwist(beats);
            return true;
        } else if (command.equalsIgnoreCase("nodYes")) {
            this.gesture.nodYes(beats);
            return true;
        } else if (command.equalsIgnoreCase("headNod")) {
            this.gesture.headNod(beats);
            return true;
        } else if (command.equalsIgnoreCase("nodYesLeft")) {
            this.gesture.nodYesLeft(beats);
            return true;
        } else if (command.equalsIgnoreCase("shakeNo")) {
            this.gesture.shakeNo(beats);
            return true;
        } else if (command.equalsIgnoreCase("shakeNeck")) {
            this.gesture.shakeNeck(beats);
            return true;
        } else if (command.equalsIgnoreCase("shakeNeckCenter")) {
            this.gesture.shakeNeckCenter(beats);
            return true;
        } else if (command.equalsIgnoreCase("shakeNeckCenterReverse")) {
            this.gesture.shakeNeckCenterReverse(beats);
            return true;
        } else if (command.equalsIgnoreCase("playfulNod")) {
            this.gesture.playfulNod(beats);
            return true;
        } else if (command.equalsIgnoreCase("reverseNodLow")) {
            this.gesture.reverseNodLow(beats);
            return true;
        } else if (command.equalsIgnoreCase("reverseNodMid")) {
            this.gesture.reverseNodMid(beats);
            return true;
        } else if (command.equalsIgnoreCase("reverseNodHigh")) {
            this.gesture.reverseNodHigh(beats);
            return true;
        } else if (command.equalsIgnoreCase("hiphopNod")) {
            this.gesture.hiphopNod(beats);
            return true;
        } else if (command.equalsIgnoreCase("hiphopNodHigh")) {
            this.gesture.hiphopNodHigh(beats);
            return true;
        } else if (command.equalsIgnoreCase("hiphopNodLow")) {
            this.gesture.hiphopNodLow(beats);
            return true;
        } else if (command.equalsIgnoreCase("anger1")) {
            this.gesture.anger(beats);
            return true;
        } else if (command.equalsIgnoreCase(EmotionConstants.ANGRY_EMOTION)) {
            this.gesture.anger2(beats);
            return true;
        } else if (command.equalsIgnoreCase("angryUp")) {
            this.gesture.angryUp(beats);
            return true;
        } else if (command.equalsIgnoreCase("discoDown")) {
            this.gesture.discoDown(beats);
            return true;
        } else if (command.equalsIgnoreCase("discoRight")) {
            this.gesture.discoRight(beats);
            return true;
        } else if (!command.equalsIgnoreCase("discoLeft")) {
            return false;
        } else {
            this.gesture.discoLeft(beats);
            return true;
        }
    }
}
