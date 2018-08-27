package mr1.robots.travis.moves.sounds;

import mr1.robots.travis.TravisDofs;
import mr1.robots.travis.moves.GestureScene;

public class SoundGestureScene extends GestureScene {
    private SoundGesture soundGesture;

    public SoundGestureScene(SoundGesture gesture) {
        super(gesture);
        this.soundGesture = gesture;
    }

    public void swoopSound(final float beats) {
        handler.removeCallbacks(this.swoopAnimation);
        if (this.gesture.getPosition(TravisDofs.NECKRL_MOTOR).floatValue() > 0.0f) {
            this.isRight = false;
        } else {
            this.isRight = true;
        }
        this.swoopAnimation = new Runnable() {
            int count = 0;

            public void run() {
                if (SoundGestureScene.this.isRight) {
                    SoundGestureScene.this.soundGesture.soundSwoopLeft(beats / 2.0f);
                    SoundGestureScene.this.isRight = false;
                    this.count++;
                } else {
                    SoundGestureScene.this.soundGesture.soundSwoopRight(beats / 2.0f);
                    SoundGestureScene.this.isRight = true;
                    this.count++;
                }
                if (this.count < 2) {
                    SoundGestureScene.handler.postDelayed(SoundGestureScene.this.swoopAnimation, (long) (((int) ((beats / 2.0f) * SoundGestureScene.this.beatDuration)) + 1));
                }
            }
        };
        handler.post(this.swoopAnimation);
    }
}
