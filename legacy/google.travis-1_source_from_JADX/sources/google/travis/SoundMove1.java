package google.travis;

import android.content.Context;
import java.util.List;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;
import mr1.robots.travis.moves.Move;

public class SoundMove1 extends Move {
    private TravisPdManager pdManager;

    public SoundMove1(Context c, MotorController mc) {
        super(c, mc);
        this.pdManager = new TravisPdManager(c);
    }

    public void move(String dof, float pos, float beats) {
        if (((Integer) this.movements.get(dof)).intValue() > 0) {
            handler.removeCallbacks(((AccelerationMover) this.movers.get(dof)).animator);
        }
        float radialPos = getRadialPos(dof, pos);
        float[] values = getAvgVelocity(dof, radialPos, beats);
        triggerAudio(dof, beats, values[1], values[0]);
        setPosition(dof, Float.valueOf(radialPos));
        moveTo(dof, values[1], values[0]);
    }

    public void move(String dof, float pos, float beats, List<Float> velocities) {
        List<List<Float>> posAndVels = calculateTrajectory(dof, pos, beats, velocities);
        triggerAudio(dof, beats, pos, ((Float) ((List) posAndVels.get(1)).get(0)).floatValue());
        animate(dof, beats, posAndVels);
    }

    protected void updateAudio(String dof, float beats, float pos, float velocity) {
        if (dof.equals(TravisDofs.HEAD_MOTOR)) {
            this.pdManager.neckUDPitch(velocity / this.mc.getMotor(dof).GetMaxVel());
        }
    }

    private void triggerAudio(String dof, float beats, float pos, float velocity) {
        boolean posDirection = false;
        if (pos > ((Float) this.positions.get(dof)).floatValue()) {
            posDirection = true;
        }
        if (dof.equals(TravisDofs.NECKUD_MOTOR)) {
            this.pdManager.neckUDPitch(velocity / this.mc.getMotor(dof).GetMaxVel());
            if (posDirection) {
                this.pdManager.neckUDRampDown(this.beatDuration * beats);
            } else {
                this.pdManager.neckUDRampUp(this.beatDuration * beats);
            }
        } else if (dof.equals(TravisDofs.NECKRL_MOTOR)) {
            if (posDirection) {
                this.pdManager.neckRLUp(this.beatDuration * beats);
            } else {
                this.pdManager.neckRLDown(this.beatDuration * beats);
            }
        } else if (dof.equals(TravisDofs.HEAD_MOTOR)) {
            if (posDirection) {
                this.pdManager.headDown(this.beatDuration * beats);
            } else {
                this.pdManager.headUp(this.beatDuration * beats);
            }
        } else if (dof.equals(TravisDofs.LEG_MOTOR)) {
            if (posDirection) {
                this.pdManager.legUp(this.beatDuration * beats);
            } else {
                this.pdManager.legDown(this.beatDuration * beats);
            }
        } else if (!dof.equals(TravisDofs.HAND_MOTOR)) {
        } else {
            if (posDirection) {
                this.pdManager.handUp(this.beatDuration * beats);
            } else {
                this.pdManager.handDown(this.beatDuration * beats);
            }
        }
    }
}
