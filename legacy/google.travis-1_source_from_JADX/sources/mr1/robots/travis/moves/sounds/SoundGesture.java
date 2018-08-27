package mr1.robots.travis.moves.sounds;

import android.content.Context;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mr1.robots.travis.TravisDofs;
import mr1.robots.travis.moves.Gesture;

public class SoundGesture extends Gesture {
    SoundMove myMove;

    public SoundGesture(Context c, SoundMove move, float beatDuration) {
        super(c, move, beatDuration);
        this.myMove = move;
    }

    public SoundGesture(Context c, float beatDuration) {
        super(c, beatDuration);
    }

    public void setAlternateMove(SoundMove myMove) {
        super.setAlternateMove(myMove);
        this.myMove = myMove;
        this.myMove.setBeatDuration(this.beatDuration);
    }

    public void generalMove(Map<String, List<Float>> moves) {
        for (String key : moves.keySet()) {
            this.myMove.move(key, ((Float) ((List) moves.get(key)).get(0)).floatValue(), ((Float) ((List) moves.get(key)).get(1)).floatValue());
        }
    }

    public void move(String dof, float pos, float beats, List<Float> velocities) {
        this.myMove.move(dof, pos, beats, velocities);
    }

    public void noSoundMove(String dof, float pos, float beats, List<Float> velocities) {
        this.myMove.noSoundMove(dof, pos, beats, velocities);
    }

    public void noSoundMove(String dof, float pos, float beats) {
        this.myMove.noSoundMove(dof, pos, beats);
    }

    public void triggerGestureSound(String gesture, float beats) {
        this.myMove.triggerGestureSound(gesture, beats);
    }

    public void soundSwoopRight(final float beats) {
        handler.removeCallbacks(this.swoopingRight);
        triggerGestureSound("soundSwoopRight", beats);
        this.swoopingRight = new Runnable() {
            boolean neckStarted = false;
            boolean up = true;

            public void run() {
                if (!this.neckStarted) {
                    SoundGesture.this.noSoundMove(TravisDofs.NECKRL_MOTOR, -0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                    this.neckStarted = true;
                }
                if (this.up) {
                    SoundGesture.this.noSoundMove(TravisDofs.HEAD_MOTOR, 1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    SoundGesture.this.noSoundMove(TravisDofs.NECKUD_MOTOR, 0.1f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.75f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    this.up = false;
                    SoundGesture.handler.postDelayed(SoundGesture.this.swoopingRight, (long) ((int) ((beats / 2.0f) * SoundGesture.this.beatDuration)));
                    return;
                }
                SoundGesture.this.noSoundMove(TravisDofs.HEAD_MOTOR, -1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                SoundGesture.this.noSoundMove(TravisDofs.NECKUD_MOTOR, 0.4f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.75f), Float.valueOf(0.1f)}));
                this.up = true;
            }
        };
        handler.post(this.swoopingRight);
    }

    public void soundSwoopLeft(final float beats) {
        handler.removeCallbacks(this.swoopingLeft);
        triggerGestureSound("soundSwoopLeft", beats);
        this.swoopingLeft = new Runnable() {
            boolean neckStarted = false;
            boolean up = true;

            public void run() {
                if (!this.neckStarted) {
                    SoundGesture.this.noSoundMove(TravisDofs.NECKRL_MOTOR, 0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                    this.neckStarted = true;
                }
                if (this.up) {
                    SoundGesture.this.noSoundMove(TravisDofs.HEAD_MOTOR, 1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    SoundGesture.this.noSoundMove(TravisDofs.NECKUD_MOTOR, 0.1f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.75f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    this.up = false;
                    SoundGesture.handler.postDelayed(SoundGesture.this.swoopingLeft, (long) ((int) ((beats / 2.0f) * SoundGesture.this.beatDuration)));
                    return;
                }
                SoundGesture.this.noSoundMove(TravisDofs.HEAD_MOTOR, -1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                SoundGesture.this.noSoundMove(TravisDofs.NECKUD_MOTOR, 0.4f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.75f), Float.valueOf(0.1f)}));
                this.up = true;
            }
        };
        handler.post(this.swoopingLeft);
    }

    public void soundSwoopDown(float beats) {
        triggerGestureSound("soundSwoopDown", beats);
        noSoundMove(TravisDofs.HEAD_MOTOR, 1.0f, beats, Arrays.asList(new Float[]{Float.valueOf(0.12f), Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        noSoundMove(TravisDofs.NECKRL_MOTOR, 0.0f, beats, Arrays.asList(new Float[]{Float.valueOf(0.12f), Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        noSoundMove(TravisDofs.NECKUD_MOTOR, 0.1f, beats, Arrays.asList(new Float[]{Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
    }

    public void soundSwoopUpRight(float beats) {
        triggerGestureSound("soundSwoopUpRight", beats);
        noSoundMove(TravisDofs.HEAD_MOTOR, -0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        noSoundMove(TravisDofs.NECKRL_MOTOR, -0.9f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        noSoundMove(TravisDofs.NECKUD_MOTOR, 0.4f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.4f)}));
    }

    public void soundSwoopUpLeft(float beats) {
        triggerGestureSound("soundSwoopUpLeft", beats);
        noSoundMove(TravisDofs.HEAD_MOTOR, -0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        noSoundMove(TravisDofs.NECKRL_MOTOR, 0.9f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        noSoundMove(TravisDofs.NECKUD_MOTOR, 0.4f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.4f)}));
    }

    public void soundSwoopDownRight(float beats) {
        triggerGestureSound("soundSwoopDownRight", beats);
        noSoundMove(TravisDofs.HEAD_MOTOR, 1.0f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        noSoundMove(TravisDofs.NECKRL_MOTOR, -0.65f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        noSoundMove(TravisDofs.NECKUD_MOTOR, -0.5f, beats);
    }

    public void soundSwoopDownLeft(float beats) {
        triggerGestureSound("soundSwoopDownLeft", beats);
        noSoundMove(TravisDofs.HEAD_MOTOR, 1.0f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        noSoundMove(TravisDofs.NECKRL_MOTOR, 0.65f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        noSoundMove(TravisDofs.NECKUD_MOTOR, -0.5f, beats);
    }

    public void soundSwoopNeckRight(float beats) {
        triggerGestureSound("soundSwoopNeckRight", beats);
        noSoundMove(TravisDofs.NECKRL_MOTOR, -0.7f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
    }

    public void soundSwoopNeckLeft(float beats) {
        triggerGestureSound("soundSwoopNeckLeft", beats);
        noSoundMove(TravisDofs.NECKRL_MOTOR, 0.7f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
    }

    public void soundSwoopHandLeft(float beats) {
        triggerGestureSound("soundSwoopHandLeft", beats);
        noSoundMove(TravisDofs.HAND_MOTOR, 0.5f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
    }

    public void soundSwoopHandRight(float beats) {
        triggerGestureSound("soundSwoopHandRight", beats);
        noSoundMove(TravisDofs.HAND_MOTOR, -0.5f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
    }

    public void soundlookAtLeg(float beats) {
        triggerGestureSound("soundLookAtLeg", beats);
        noSoundMove(TravisDofs.HEAD_MOTOR, 1.0f, beats);
        noSoundMove(TravisDofs.NECKRL_MOTOR, -0.65f, beats);
        noSoundMove(TravisDofs.NECKUD_MOTOR, -0.8f, beats);
        noSoundMove(TravisDofs.HAND_MOTOR, -0.6f, beats);
    }

    public void soundLookAtPhone(float beats) {
        triggerGestureSound("soundLookAtPhone", beats);
        noSoundMove(TravisDofs.HEAD_MOTOR, 1.0f, beats);
        noSoundMove(TravisDofs.NECKRL_MOTOR, 0.65f, beats);
        noSoundMove(TravisDofs.NECKUD_MOTOR, -0.6f, beats);
        noSoundMove(TravisDofs.HAND_MOTOR, -0.8f, beats);
    }

    public void soundHome(float beats) {
        triggerGestureSound("soundHome", beats);
        noSoundMove(TravisDofs.NECKRL_MOTOR, 0.0f, beats);
        noSoundMove(TravisDofs.NECKUD_MOTOR, 0.4f, beats);
        noSoundMove(TravisDofs.HAND_MOTOR, 0.4f, beats);
        noSoundMove(TravisDofs.HEAD_MOTOR, 0.0f, beats);
    }

    public void soundTap(final float beats) {
        triggerGestureSound("soundTap", beats);
        handler.removeCallbacks(this.tap);
        this.tap = new Runnable() {
            boolean tapStarted = false;

            public void run() {
                if (this.tapStarted) {
                    SoundGesture.this.noSoundMove(TravisDofs.LEG_MOTOR, -1.0f, beats / 2.0f);
                    return;
                }
                SoundGesture.this.noSoundMove(TravisDofs.LEG_MOTOR, 0.9f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.3f), Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                this.tapStarted = true;
                SoundGesture.handler.postDelayed(SoundGesture.this.tap, (long) ((int) ((beats / 2.0f) * SoundGesture.this.beatDuration)));
            }
        };
        handler.post(this.tap);
    }

    public void homeHead(float beats) {
        noSoundMove(TravisDofs.NECKRL_MOTOR, 0.0f, beats);
        noSoundMove(TravisDofs.NECKUD_MOTOR, 0.4f, beats);
        noSoundMove(TravisDofs.HEAD_MOTOR, 0.0f, beats);
    }
}
