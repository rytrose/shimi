package mr1.robots.travis.behaviors;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import mr1.core.motion.AccelerationMover;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;

public class SwingBehavior extends TravisBehavior {
    private final String TAG;
    int divider;
    private Handler handler;
    private AccelerationMover headDownMover;
    private AccelerationMover headLeftMover;
    private AccelerationMover headRightMover;
    private AccelerationMover headUpMover;
    boolean left;
    private AccelerationMover legDownMover;
    private AccelerationMover legUpMover;
    private Runnable mHeadAnimation;
    private Runnable mHeadSwingAnimation;
    private Runnable mLegAnimation;
    private Runnable mNeckAnimation;
    private AccelerationMover neckDownMover;
    private AccelerationMover neckUpMover;

    /* renamed from: mr1.robots.travis.behaviors.SwingBehavior$1 */
    class C00501 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.SwingBehavior$1$1 */
        class C00491 implements Runnable {
            C00491() {
            }

            public void run() {
                Log.d("SwingBehavior", "Runnin at " + SystemClock.uptimeMillis());
                Log.d("SwingBehavior", "neck down");
                SwingBehavior.this.neckUpMover.stop();
                SwingBehavior.this.neckDownMover.start(SwingBehavior.this.msPeriod * 2);
            }
        }

        C00501() {
        }

        public void run() {
            Log.d("SwingBehavior", "neck up");
            if (SwingBehavior.this.neckDownMover != null) {
                SwingBehavior.this.neckDownMover.stop();
                SwingBehavior.this.neckUpMover.start(SwingBehavior.this.msPeriod * 2);
                Log.d("SwingBehavior", "Down comes in " + SwingBehavior.this.msPeriod);
                Log.d("SwingBehavior", "Posting at " + SystemClock.uptimeMillis());
                SwingBehavior.this.handler.postDelayed(new C00491(), SwingBehavior.this.msPeriod * 2);
            }
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.SwingBehavior$2 */
    class C00522 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.SwingBehavior$2$1 */
        class C00511 implements Runnable {
            C00511() {
            }

            public void run() {
                Log.d("SwingBehavior", "head right");
                SwingBehavior.this.headRightMover.stop();
                SwingBehavior.this.headLeftMover.start(SwingBehavior.this.msPeriod);
            }
        }

        C00522() {
        }

        public void run() {
            Log.d("SwingBehavior", "head left");
            if (SwingBehavior.this.headLeftMover != null) {
                SwingBehavior.this.headLeftMover.stop();
                SwingBehavior.this.headRightMover.start(SwingBehavior.this.msPeriod);
                SwingBehavior.this.handler.postDelayed(new C00511(), SwingBehavior.this.msPeriod);
            }
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.SwingBehavior$3 */
    class C00543 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.SwingBehavior$3$1 */
        class C00531 implements Runnable {
            C00531() {
            }

            public void run() {
                SwingBehavior.this.legDownMover.stop();
                SwingBehavior.this.legUpMover.start(SwingBehavior.this.msPeriod);
            }
        }

        C00543() {
        }

        public void run() {
            if (SwingBehavior.this.legUpMover != null) {
                SwingBehavior.this.legUpMover.stop();
                SwingBehavior.this.legDownMover.start(SwingBehavior.this.msPeriod);
                SwingBehavior.this.handler.postDelayed(new C00531(), SwingBehavior.this.msPeriod);
            }
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.SwingBehavior$4 */
    class C00564 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.SwingBehavior$4$1 */
        class C00551 implements Runnable {
            C00551() {
            }

            public void run() {
                SwingBehavior.this.headUpMover.stop();
                SwingBehavior.this.headDownMover.start(SwingBehavior.this.msPeriod);
            }
        }

        C00564() {
        }

        public void run() {
            if (SwingBehavior.this.headDownMover != null) {
                SwingBehavior.this.headDownMover.stop();
                SwingBehavior.this.headUpMover.start(SwingBehavior.this.msPeriod);
                SwingBehavior.this.handler.postDelayed(new C00551(), SwingBehavior.this.msPeriod);
            }
        }
    }

    public SwingBehavior(MotorController mc) {
        super(mc);
        this.TAG = "SwingBehavior";
        this.handler = new Handler();
        this.mNeckAnimation = new C00501();
        this.mHeadSwingAnimation = new C00522();
        this.mLegAnimation = new C00543();
        this.mHeadAnimation = new C00564();
        this.divider = 0;
        this.msPeriod = 5000;
    }

    public void start(Object data) {
        float f = 0.4f;
        float intensity = 1.0f;
        if (data != null) {
            intensity = ((Float) data).floatValue();
        }
        Log.d("SwingBehavior", "Starting " + SwingBehavior.class.toString() + " @ " + intensity);
        this.headUp = false;
        this.legUp = false;
        this.neckUp = false;
        this.neckUpMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, 0.0f, 0.4f);
        this.neckDownMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, -0.5f * intensity, 0.4f);
        this.headLeftMover = new AccelerationMover(this, TravisDofs.NECKRL_MOTOR, -0.4f * intensity, 0.9f);
        this.headRightMover = new AccelerationMover(this, TravisDofs.NECKRL_MOTOR, 0.4f * intensity, 0.9f);
        this.headUpMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, -2.0f * intensity, 0.8f);
        this.headDownMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, 0.0f, 0.8f);
        String str = TravisDofs.LEG_MOTOR;
        if (((double) intensity) != 1.0d) {
            f = 0.0f;
        }
        this.legUpMover = new AccelerationMover(this, str, f, 1.8f);
        this.legDownMover = new AccelerationMover(this, TravisDofs.LEG_MOTOR, 0.0f, 1.8f);
        onSongEvent(0, Integer.valueOf(1000));
    }

    public void stop() {
        Log.d("SwingBehavior", "Stopping " + SwingBehavior.class.toString());
        this.legDownMover.stop();
        this.legUpMover.stop();
        this.neckDownMover.stop();
        this.neckUpMover.stop();
        this.headLeftMover.stop();
        this.headRightMover.stop();
        this.headDownMover.stop();
        this.headUpMover.stop();
        this.handler.removeCallbacks(this.mHeadAnimation);
        this.handler.removeCallbacks(this.mLegAnimation);
        this.handler.removeCallbacks(this.mNeckAnimation);
        this.handler.removeCallbacks(this.mHeadSwingAnimation);
    }

    public void onSongEvent(int type, Object data) {
        if (type == 0) {
            int beat = beatDataToInt(data);
            int i = this.divider + 1;
            this.divider = i;
            if (i % 2 == 0) {
                Log.d("SwingBehavior", "Beat: " + beat);
                this.msPeriod = (long) beat;
                this.handler.postDelayed(this.mHeadAnimation, 0);
                this.handler.postDelayed(this.mHeadSwingAnimation, 0);
                this.handler.postDelayed(this.mLegAnimation, 0);
                if (this.divider % 4 == 0) {
                    this.handler.postDelayed(this.mNeckAnimation, (long) (((double) this.msPeriod) * 0.2d));
                }
                if (Math.random() > 0.85d) {
                    turnHead();
                }
            }
        }
    }

    private void turnHead() {
        if (this.left) {
            setVelocity(TravisDofs.NECKRL_MOTOR, -0.5f, 0.25f);
        } else {
            setVelocity(TravisDofs.NECKRL_MOTOR, 0.5f, 0.25f);
        }
        this.left = !this.left;
    }

    public void setVelocity(String motorName, float goal, float vel) {
        this.mc.moveMotor(motorName, goal, (float) Math.max((double) vel, 0.1d), 0.0f);
    }
}
