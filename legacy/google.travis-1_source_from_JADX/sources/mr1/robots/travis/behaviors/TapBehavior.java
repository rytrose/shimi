package mr1.robots.travis.behaviors;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import mr1.core.motion.AccelerationMover;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;

public class TapBehavior extends TravisBehavior {
    private final String TAG;
    private Handler handler;
    private AccelerationMover headDownMover;
    private AccelerationMover headUpMover;
    boolean left;
    private AccelerationMover legDownMover;
    private AccelerationMover legUpMover;
    private Runnable mHeadAnimation;
    private Runnable mLegAnimation;
    private Runnable mNeckAnimation;
    private AccelerationMover neckDownMover;
    private AccelerationMover neckUpMover;
    private boolean shouldInitPositions;

    /* renamed from: mr1.robots.travis.behaviors.TapBehavior$1 */
    class C00581 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.TapBehavior$1$1 */
        class C00571 implements Runnable {
            C00571() {
            }

            public void run() {
                if (TapBehavior.this.D) {
                    Log.d("TapBehavior", "Runnin at " + SystemClock.uptimeMillis());
                }
                if (TapBehavior.this.D) {
                    Log.d("TapBehavior", "neck down");
                }
                TapBehavior.this.neckUpMover.stop();
                TapBehavior.this.neckDownMover.start(TapBehavior.this.msPeriod);
            }
        }

        C00581() {
        }

        public void run() {
            if (TapBehavior.this.D) {
                Log.d("TapBehavior", "neck up");
            }
            if (TapBehavior.this.neckDownMover != null) {
                TapBehavior.this.neckDownMover.stop();
                TapBehavior.this.neckUpMover.start(TapBehavior.this.msPeriod);
                if (TapBehavior.this.D) {
                    Log.d("TapBehavior", "Down comes in " + TapBehavior.this.msPeriod);
                }
                if (TapBehavior.this.D) {
                    Log.d("TapBehavior", "Posting at " + SystemClock.uptimeMillis());
                }
                TapBehavior.this.handler.postDelayed(new C00571(), TapBehavior.this.msPeriod);
            }
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.TapBehavior$2 */
    class C00602 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.TapBehavior$2$1 */
        class C00591 implements Runnable {
            C00591() {
            }

            public void run() {
                TapBehavior.this.legDownMover.stop();
                TapBehavior.this.legUpMover.start(TapBehavior.this.msPeriod);
            }
        }

        C00602() {
        }

        public void run() {
            if (TapBehavior.this.legUpMover != null) {
                TapBehavior.this.legUpMover.stop();
                TapBehavior.this.legDownMover.start(TapBehavior.this.msPeriod);
                TapBehavior.this.handler.postDelayed(new C00591(), TapBehavior.this.msPeriod);
            }
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.TapBehavior$3 */
    class C00623 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.TapBehavior$3$1 */
        class C00611 implements Runnable {
            C00611() {
            }

            public void run() {
                TapBehavior.this.headUpMover.stop();
                TapBehavior.this.headDownMover.start(TapBehavior.this.msPeriod);
            }
        }

        C00623() {
        }

        public void run() {
            if (TapBehavior.this.headDownMover != null) {
                TapBehavior.this.headDownMover.stop();
                TapBehavior.this.headUpMover.start(TapBehavior.this.msPeriod);
                TapBehavior.this.handler.postDelayed(new C00611(), TapBehavior.this.msPeriod);
            }
        }
    }

    public TapBehavior(MotorController mc) {
        super(mc);
        this.TAG = "TapBehavior";
        this.handler = new Handler();
        this.shouldInitPositions = false;
        this.mNeckAnimation = new C00581();
        this.mLegAnimation = new C00602();
        this.mHeadAnimation = new C00623();
        this.msPeriod = 5000;
        this.D = false;
    }

    public void start(Object data) {
        if (this.D) {
            Log.d("TapBehavior", "Starting " + TapBehavior.class.toString());
        }
        if (this.shouldInitPositions) {
            initPositions();
        }
        this.headUp = false;
        this.legUp = false;
        this.neckUp = false;
        this.neckUpMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, -0.3f, 0.4f);
        this.neckDownMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, 0.0f, 0.4f);
        this.headUpMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, -2.0f, 0.8f);
        this.headDownMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, 0.0f, 0.8f);
        this.legUpMover = new AccelerationMover(this, TravisDofs.LEG_MOTOR, 0.25f, 0.6f);
        this.legDownMover = new AccelerationMover(this, TravisDofs.LEG_MOTOR, 0.0f, 0.6f);
        onSongEvent(0, Long.valueOf(1000));
    }

    private void initPositions() {
        this.mc.moveMotor(TravisDofs.NECKUD_MOTOR, 0.0f, 0.2f, 0.01f);
        this.mc.moveMotor(TravisDofs.HEAD_MOTOR, 0.0f, 0.25f, 0.01f);
        this.mc.moveMotor(TravisDofs.NECKRL_MOTOR, 0.0f, 0.1f, 0.0f);
        this.mc.moveMotor(TravisDofs.LEG_MOTOR, 0.0f, 0.2f, 0.01f);
        this.mc.moveMotor(TravisDofs.HAND_MOTOR, 0.0f, 0.1f, 0.0f);
    }

    public void stop() {
        if (this.D) {
            Log.d("TapBehavior", "Stopping " + TapBehavior.class.toString());
        }
        this.legDownMover.stop();
        this.legUpMover.stop();
        this.neckDownMover.stop();
        this.neckUpMover.stop();
        this.headDownMover.stop();
        this.headUpMover.stop();
        this.handler.removeCallbacks(this.mHeadAnimation);
        this.handler.removeCallbacks(this.mLegAnimation);
        this.handler.removeCallbacks(this.mNeckAnimation);
    }

    public void onSongEvent(int type, Object data) {
        if (type == 0) {
            Log.d("TapBehavior", data.toString());
            int beat = beatDataToInt(data);
            if (this.D) {
                Log.d("TapBehavior", "Beat: " + beat);
            }
            this.msPeriod = (long) (beat / 2);
            this.handler.postDelayed(this.mNeckAnimation, (long) (((double) this.msPeriod) * 0.2d));
            this.handler.postDelayed(this.mHeadAnimation, 0);
            this.handler.postDelayed(this.mLegAnimation, 0);
            if (Math.random() > 0.65d) {
                turnHead();
            }
        }
    }

    private void turnHead() {
        float whereTo = ((float) (Math.random() * 0.5d)) + 0.5f;
        if (this.left) {
            setVelocity(TravisDofs.NECKRL_MOTOR, -whereTo, 0.45f);
        } else {
            setVelocity(TravisDofs.NECKRL_MOTOR, whereTo, 0.45f);
        }
        this.left = !this.left;
    }

    public void setVelocity(String motorName, float goal, float vel) {
        this.mc.moveMotor(motorName, goal, (float) Math.max((double) vel, 0.1d), 0.0f);
    }
}
