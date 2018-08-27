package mr1.robots.travis.moves;

import mr1.robots.travis.TravisDofs;

public class GestureScene implements TravisDofs, ThreadScheduler {
    protected float beatDuration;
    protected Gesture gesture;
    protected boolean isRight;
    protected Runnable swoopAnimation;
    protected Runnable swoopCenterAnimation;

    public GestureScene(Gesture gesture) {
        this.gesture = gesture;
        this.beatDuration = gesture.getBeatDuration();
    }

    public void swoop(final float beats) {
        handler.removeCallbacks(this.swoopAnimation);
        if (this.gesture.getPosition(TravisDofs.NECKRL_MOTOR).floatValue() > 0.0f) {
            this.isRight = false;
        } else {
            this.isRight = true;
        }
        this.swoopAnimation = new Runnable() {
            int count = 0;

            public void run() {
                if (GestureScene.this.isRight) {
                    GestureScene.this.gesture.swoopLeft(beats / 2.0f);
                    GestureScene.this.isRight = false;
                    this.count++;
                } else {
                    GestureScene.this.gesture.swoopRight(beats / 2.0f);
                    GestureScene.this.isRight = true;
                    this.count++;
                }
                if (this.count < 2) {
                    GestureScene.handler.postDelayed(GestureScene.this.swoopAnimation, (long) (((int) ((beats / 2.0f) * GestureScene.this.beatDuration)) + 1));
                }
            }
        };
        handler.post(this.swoopAnimation);
    }

    public void swoopCenter(final float beats) {
        handler.removeCallbacks(this.swoopCenterAnimation);
        if (this.gesture.getPosition(TravisDofs.NECKRL_MOTOR).floatValue() > 0.0f) {
            this.isRight = false;
        } else {
            this.isRight = true;
        }
        this.swoopCenterAnimation = new Runnable() {
            int count = 0;

            public void run() {
                if (GestureScene.this.isRight) {
                    GestureScene.this.gesture.swoopCenterLeft(beats / 2.0f);
                    GestureScene.this.isRight = false;
                    this.count++;
                } else {
                    GestureScene.this.gesture.swoopCenterRight(beats / 2.0f);
                    GestureScene.this.isRight = true;
                    this.count++;
                }
                if (this.count < 2) {
                    GestureScene.handler.postDelayed(GestureScene.this.swoopCenterAnimation, (long) (((int) ((beats / 2.0f) * GestureScene.this.beatDuration)) + 1));
                }
            }
        };
        handler.post(this.swoopCenterAnimation);
    }
}
