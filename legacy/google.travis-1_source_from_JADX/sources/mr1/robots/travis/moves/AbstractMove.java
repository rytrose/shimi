package mr1.robots.travis.moves;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;

public abstract class AbstractMove implements TravisDofs, ThreadScheduler {
    protected float beatDuration;
    protected MotorController mc;
    protected volatile Map<String, Integer> movements = new HashMap();
    protected volatile Map<String, AccelerationMover> movers = new HashMap();
    protected volatile Map<String, Float> normPositions = new HashMap();
    protected volatile Map<String, Float> positions = new HashMap();

    protected class AccelerationMover {
        public Runnable animator;

        /* renamed from: mr1.robots.travis.moves.AbstractMove$AccelerationMover$1 */
        class C00641 implements Runnable {
            int count;
            int interval;
            int nVels;
            private final /* synthetic */ float val$beats;
            private final /* synthetic */ String val$dof;
            private final /* synthetic */ List val$posAndVels;

            C00641(List list, float f, String str) {
                this.val$posAndVels = list;
                this.val$beats = f;
                this.val$dof = str;
                this.nVels = ((List) list.get(0)).size();
                this.interval = (int) ((AbstractMove.this.beatDuration * f) / ((float) ((List) list.get(0)).size()));
                this.count = ((Integer) AbstractMove.this.movements.get(str)).intValue();
            }

            public void run() {
                AbstractMove.this.updateAudio(this.val$dof, this.val$beats, ((Float) ((List) this.val$posAndVels.get(0)).get(this.nVels - 1)).floatValue(), ((Float) ((List) this.val$posAndVels.get(1)).get(this.count)).floatValue());
                AbstractMove.this.moveTo(this.val$dof, ((Float) ((List) this.val$posAndVels.get(0)).get(this.nVels - 1)).floatValue(), ((Float) ((List) this.val$posAndVels.get(1)).get(this.count)).floatValue());
                AbstractMove.this.setPosition(this.val$dof, (Float) ((List) this.val$posAndVels.get(0)).get(this.count));
                this.count++;
                if (this.count == this.nVels) {
                    AbstractMove.this.movements.put(this.val$dof, Integer.valueOf(0));
                    return;
                }
                AbstractMove.this.movements.put(this.val$dof, Integer.valueOf(this.count));
                AbstractMove.handler.postDelayed(AccelerationMover.this.animator, (long) this.interval);
            }
        }

        public void start(String dof, float beats, List<List<Float>> posAndVels) {
            AbstractMove.handler.removeCallbacks(this.animator);
            AbstractMove.this.movements.put(dof, Integer.valueOf(0));
            this.animator = new C00641(posAndVels, beats, dof);
            this.animator.run();
        }

        public void stop() {
            AbstractMove.handler.removeCallbacks(this.animator);
        }
    }

    public AbstractMove(MotorController mc) {
        this.mc = mc;
    }

    protected void moveTo(final String dof, final float goal, final float vel) {
        new Runnable() {
            public void run() {
                AbstractMove.this.mc.moveMotor(dof, goal, vel, 0.0f);
            }
        }.run();
    }

    protected void setPosition(String dof, Float pos, Float normPos) {
        this.positions.put(dof, pos);
        this.normPositions.put(dof, normPos);
    }

    protected void setPosition(String dof, Float pos) {
        this.positions.put(dof, pos);
    }

    public Float getPosition(String dof) {
        return (Float) this.positions.get(dof);
    }

    protected float getRadialPos(String dof, float pos) {
        return (((1.0f + pos) / 2.0f) * (this.mc.getMotor(dof).GetMaxPos() - this.mc.getMotor(dof).GetMinPos())) + this.mc.getMotor(dof).GetMinPos();
    }

    protected void updateAudio(String dof, float beats, float pos, float vel) {
    }
}
