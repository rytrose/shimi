package mr1.core.motion;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

public class AccelerationMover {
    /* renamed from: D */
    private boolean f5D = false;
    private final long VEL_TIMER = 20;
    private Runnable animator;
    private Handler handler;
    protected String name;
    protected float period;
    private long startTime;

    public interface iMoveListener {
        void setVelocity(String str, float f, float f2);
    }

    public AccelerationMover(iMoveListener motor, String motorName, float goal, float maxVel) {
        this.name = new StringBuilder(String.valueOf(motorName)).append("->").append(goal).append("(").append(this).append(")").toString();
        this.handler = new Handler();
        final float f = maxVel;
        final iMoveListener imovelistener = motor;
        final String str = motorName;
        final float f2 = goal;
        this.animator = new Runnable() {
            public void run() {
                long m = SystemClock.uptimeMillis();
                if (((double) (m - AccelerationMover.this.startTime)) <= ((double) AccelerationMover.this.period) * 1.3d) {
                    imovelistener.setVelocity(str, f2, (float) (((double) f) * ((2.0d * (1.0d - ((double) (Math.abs((AccelerationMover.this.period / 2.0f) - ((float) (m - AccelerationMover.this.startTime))) / AccelerationMover.this.period)))) - 1.0d)));
                    AccelerationMover.this.handler.postDelayed(this, 20);
                }
            }
        };
    }

    public void start(long msDelay) {
        if (this.f5D) {
            Log.d("AccelerationMover", "Started " + this.name);
        }
        this.startTime = SystemClock.uptimeMillis();
        this.period = (float) msDelay;
        this.handler.post(this.animator);
    }

    public void stop() {
        if (this.f5D) {
            Log.d("AccelerationMover", "Stopped " + this.name);
        }
        this.handler.removeCallbacks(this.animator);
    }
}
