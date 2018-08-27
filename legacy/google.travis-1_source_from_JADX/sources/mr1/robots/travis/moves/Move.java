package mr1.robots.travis.moves;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;

public class Move extends AbstractMove {
    public Move(Context c, MotorController mc) {
        super(mc);
        this.positions.put(TravisDofs.HEAD_MOTOR, Float.valueOf(0.0f));
        this.positions.put(TravisDofs.HAND_MOTOR, Float.valueOf(0.0f));
        this.positions.put(TravisDofs.NECKUD_MOTOR, Float.valueOf(0.0f));
        this.positions.put(TravisDofs.NECKRL_MOTOR, Float.valueOf(0.0f));
        this.positions.put(TravisDofs.LEG_MOTOR, Float.valueOf(0.0f));
        this.normPositions.put(TravisDofs.HEAD_MOTOR, Float.valueOf(0.0f));
        this.normPositions.put(TravisDofs.HAND_MOTOR, Float.valueOf(0.0f));
        this.normPositions.put(TravisDofs.NECKUD_MOTOR, Float.valueOf(0.0f));
        this.normPositions.put(TravisDofs.NECKRL_MOTOR, Float.valueOf(0.0f));
        this.normPositions.put(TravisDofs.LEG_MOTOR, Float.valueOf(0.0f));
        this.movements.put(TravisDofs.HEAD_MOTOR, Integer.valueOf(0));
        this.movements.put(TravisDofs.HAND_MOTOR, Integer.valueOf(0));
        this.movements.put(TravisDofs.NECKUD_MOTOR, Integer.valueOf(0));
        this.movements.put(TravisDofs.NECKRL_MOTOR, Integer.valueOf(0));
        this.movements.put(TravisDofs.LEG_MOTOR, Integer.valueOf(0));
        this.movers.put(TravisDofs.HEAD_MOTOR, new AccelerationMover());
        this.movers.put(TravisDofs.HAND_MOTOR, new AccelerationMover());
        this.movers.put(TravisDofs.NECKUD_MOTOR, new AccelerationMover());
        this.movers.put(TravisDofs.NECKRL_MOTOR, new AccelerationMover());
        this.movers.put(TravisDofs.LEG_MOTOR, new AccelerationMover());
    }

    public void move(String dof, float pos, float beats) {
        if (((Integer) this.movements.get(dof)).intValue() > 0) {
            handler.removeCallbacks(((AccelerationMover) this.movers.get(dof)).animator);
        }
        float radialPos = getRadialPos(dof, pos);
        float[] values = getAvgVelocity(dof, radialPos, beats);
        setPosition(dof, Float.valueOf(radialPos), Float.valueOf(pos));
        moveTo(dof, values[1], values[0]);
    }

    public void moveMaxSpeed(String dof, float pos) {
        moveWithVel(dof, pos, this.mc.getMotor(dof).GetMaxVel());
    }

    public void move(String dof, String pos, float beats) {
        if (pos.equals("positive")) {
            moveWithVel(dof, 1.0f, this.mc.getMotor(dof).GetMaxVel());
        } else if (pos.equals("negative")) {
            moveTo(dof, this.mc.getMotor(dof).GetMinPos(), beats);
            moveWithVel(dof, -1.0f, this.mc.getMotor(dof).GetMaxVel());
        }
    }

    public void move(String dof, float pos, float beats, List<Float> velocities) {
        animate(dof, beats, calculateTrajectory(dof, pos, beats, velocities));
    }

    public void moveWithVel(String dof, float pos, float vel) {
        float radialPos = getRadialPos(dof, pos);
        moveTo(dof, radialPos, vel);
        setPosition(dof, Float.valueOf(radialPos), Float.valueOf(pos));
    }

    public void setBeatDuration(float dur) {
        this.beatDuration = dur;
    }

    public float getBeatDuration() {
        return this.beatDuration;
    }

    protected List<List<Float>> calculateTrajectory(String dof, float pos, float beats, List<Float> velocities) {
        int i;
        List<List<Float>> results = new ArrayList();
        float[] values = getAvgVelocity(dof, getRadialPos(dof, pos), beats);
        float max = -1.0f;
        for (i = 0; i < velocities.size(); i++) {
            if (((Float) velocities.get(i)).floatValue() > max) {
                max = ((Float) velocities.get(i)).floatValue();
            }
        }
        if (max != 1.0f) {
            for (i = 0; i < velocities.size(); i++) {
                velocities.set(i, Float.valueOf(((Float) velocities.get(i)).floatValue() / max));
            }
        }
        HashMap<Float, Integer> uniqueVels = new HashMap();
        for (i = 0; i < velocities.size(); i++) {
            Float tempKey = (Float) velocities.get(i);
            uniqueVels.put(tempKey, Integer.valueOf(Integer.valueOf(uniqueVels.containsKey(tempKey) ? ((Integer) uniqueVels.get(tempKey)).intValue() : 0).intValue() + 1));
        }
        float sum = 0.0f;
        for (Float key : uniqueVels.keySet()) {
            sum += (((float) ((Integer) uniqueVels.get(key)).intValue()) / ((float) velocities.size())) * key.floatValue();
        }
        float maxVel = values[0] / sum;
        this.mc.getMotor(dof).GetMaxVel();
        int interval = (int) ((this.beatDuration * beats) / ((float) velocities.size()));
        List<Float> updatedVels = new ArrayList();
        List<Float> updatedPos = new ArrayList();
        float previousPos = ((Float) this.positions.get(dof)).floatValue();
        for (i = 0; i < velocities.size(); i++) {
            updatedVels.add(Float.valueOf(((Float) velocities.get(i)).floatValue() * maxVel));
            if (values[1] > ((Float) this.positions.get(dof)).floatValue()) {
                updatedPos.add(Float.valueOf(((((Float) updatedVels.get(i)).floatValue() * ((float) interval)) / 1000.0f) + previousPos));
            } else {
                updatedPos.add(Float.valueOf(previousPos - ((((Float) updatedVels.get(i)).floatValue() * ((float) interval)) / 1000.0f)));
            }
            previousPos = ((Float) updatedPos.get(i)).floatValue();
        }
        results.add(updatedPos);
        results.add(updatedVels);
        return results;
    }

    protected void animate(String dof, float beats, List<List<Float>> posAndVels) {
        if (((Integer) this.movements.get(dof)).intValue() > 0) {
            ((AccelerationMover) this.movers.get(dof)).stop();
            this.movements.put(dof, Integer.valueOf(0));
        }
        ((AccelerationMover) this.movers.get(dof)).start(dof, beats, posAndVels);
    }

    protected float[] getAvgVelocity(String dof, float pos, float beats) {
        float totalTime = (this.beatDuration * beats) / 1000.0f;
        float distance = Math.abs(((Float) this.positions.get(dof)).floatValue() - pos);
        float vel = distance / totalTime;
        if (vel > this.mc.getMotor(dof).GetMaxVel()) {
            vel = this.mc.getMotor(dof).GetMaxVel();
            float difference = Math.abs(distance - (vel * totalTime));
            if (pos > ((Float) this.positions.get(dof)).floatValue()) {
                pos -= difference;
            } else {
                pos += difference;
            }
        }
        return new float[]{vel, pos};
    }

    public void cancelMoves() {
        handler.removeCallbacks(((AccelerationMover) this.movers.get(TravisDofs.HEAD_MOTOR)).animator);
        handler.removeCallbacks(((AccelerationMover) this.movers.get(TravisDofs.NECKUD_MOTOR)).animator);
        handler.removeCallbacks(((AccelerationMover) this.movers.get(TravisDofs.NECKRL_MOTOR)).animator);
        handler.removeCallbacks(((AccelerationMover) this.movers.get(TravisDofs.LEG_MOTOR)).animator);
        handler.removeCallbacks(((AccelerationMover) this.movers.get(TravisDofs.HAND_MOTOR)).animator);
    }
}
