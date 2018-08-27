package mr1.robots.travis.moves.sounds;

import android.content.Context;
import mr1.robots.travis.moves.Gesture;
import mr1.robots.travis.moves.GestureController;

public class SoundGestureController extends GestureController {
    public SoundGesture soundGesture;
    public SoundGestureScene soundGestureScene;

    public SoundGestureController(Context c, float beatDuration, SoundGesture gest) {
        super(c, beatDuration, gest);
        this.soundGestureScene = new SoundGestureScene(gest);
        this.soundGesture = gest;
    }

    public Gesture getGesture() {
        return this.soundGesture;
    }

    public boolean doFunction(String command, float beats) {
        if (super.doFunction(command, beats)) {
            return true;
        }
        if (command.equalsIgnoreCase("soundSwoop")) {
            System.out.println("swoop Sound");
            this.soundGestureScene.swoopSound(beats);
            return true;
        } else if (command.equalsIgnoreCase("soundSwoopUpRight")) {
            this.soundGesture.soundSwoopRight(beats);
            return false;
        } else if (command.equalsIgnoreCase("soundSwoopUpLeft")) {
            this.soundGesture.soundSwoopLeft(beats);
            return false;
        } else if (command.equalsIgnoreCase("soundSwoopNeckRight")) {
            this.soundGesture.soundSwoopNeckRight(beats);
            return false;
        } else if (command.equalsIgnoreCase("soundSwoopNeckLeft")) {
            this.soundGesture.soundSwoopNeckLeft(beats);
            return false;
        } else if (command.equalsIgnoreCase("soundSwoopDownLeft")) {
            this.soundGesture.soundSwoopDownLeft(beats);
            return false;
        } else if (command.equalsIgnoreCase("soundSwoopDownRight")) {
            this.soundGesture.soundSwoopDownRight(beats);
            return false;
        } else if (command.equalsIgnoreCase("soundSwoopHandRight")) {
            this.soundGesture.soundSwoopHandRight(beats);
            return false;
        } else if (command.equalsIgnoreCase("soundSwoopHandLeft")) {
            this.soundGesture.soundSwoopHandLeft(beats);
            return false;
        } else if (command.equalsIgnoreCase("lookAtPhoneSound")) {
            this.soundGesture.soundLookAtPhone(beats);
            return true;
        } else if (command.equalsIgnoreCase("homeSound")) {
            this.soundGesture.soundHome(beats);
            return true;
        } else if (command.equalsIgnoreCase("lookAtLegSound")) {
            this.soundGesture.soundlookAtLeg(beats);
            return true;
        } else if (command.equalsIgnoreCase("soundTap")) {
            this.soundGesture.soundTap(beats);
            return true;
        } else if (command.equalsIgnoreCase("swoopSoundAll")) {
            this.gestureScene.swoop(beats);
            return true;
        } else if (command.equalsIgnoreCase("lookAtPhoneSoundAll")) {
            this.gesture.lookAtPhone(beats);
            return true;
        } else if (command.equalsIgnoreCase("homeSoundAll")) {
            this.gesture.home(beats);
            return true;
        } else if (command.equalsIgnoreCase("lookAtLegSoundAll")) {
            this.gesture.lookAtLeg(beats);
            return true;
        } else if (!command.equalsIgnoreCase("tapSoundAll")) {
            return false;
        } else {
            this.gesture.tap(beats);
            return true;
        }
    }
}
