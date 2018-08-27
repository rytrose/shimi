package mr1.robots.travis.behaviors;

import android.os.Handler;
import android.util.Log;
import mr1.core.motion.AccelerationMover;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;

public class FootTapBehavior extends TravisBehavior {
    private final String TAG;
    private Handler handler;
    private AccelerationMover headDownMover;
    private AccelerationMover headUpMover;
    private AccelerationMover legDownMover;
    private AccelerationMover legUpMover;
    private Runnable mHeadAnimation;
    private Runnable mLegAnimation;
    private boolean shouldInitPositions;

    /* renamed from: mr1.robots.travis.behaviors.FootTapBehavior$1 */
    class C00301 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.FootTapBehavior$1$1 */
        class C00291 implements Runnable {
            C00291() {
            }

            public void run() {
                FootTapBehavior.this.legDownMover.stop();
                FootTapBehavior.this.legUpMover.start(FootTapBehavior.this.msPeriod);
            }
        }

        C00301() {
        }

        public void run() {
            if (FootTapBehavior.this.legUpMover != null) {
                FootTapBehavior.this.legUpMover.stop();
                FootTapBehavior.this.legDownMover.start(FootTapBehavior.this.msPeriod);
                FootTapBehavior.this.handler.postDelayed(new C00291(), FootTapBehavior.this.msPeriod);
            }
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.FootTapBehavior$2 */
    class C00322 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.FootTapBehavior$2$1 */
        class C00311 implements Runnable {
            C00311() {
            }

            public void run() {
                FootTapBehavior.this.headUpMover.stop();
                FootTapBehavior.this.headDownMover.start(FootTapBehavior.this.msPeriod);
            }
        }

        C00322() {
        }

        public void run() {
            if (FootTapBehavior.this.headDownMover != null) {
                FootTapBehavior.this.headDownMover.stop();
                FootTapBehavior.this.headUpMover.start(FootTapBehavior.this.msPeriod);
                FootTapBehavior.this.handler.postDelayed(new C00311(), FootTapBehavior.this.msPeriod);
            }
        }
    }

    public FootTapBehavior(MotorController mc) {
        super(mc);
        this.TAG = "FootTapBehavior";
        this.handler = new Handler();
        this.shouldInitPositions = false;
        this.mLegAnimation = new C00301();
        this.mHeadAnimation = new C00322();
        this.msPeriod = 5000;
        this.D = false;
    }

    public void start(Object data) {
        if (this.D) {
            Log.d("FootTapBehavior", "Starting " + FootTapBehavior.class.toString());
        }
        if (this.shouldInitPositions) {
            initPositions();
        }
        this.headUp = false;
        this.legUp = false;
        this.neckUp = false;
        this.headUpMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, -2.0f, 0.6f);
        this.headDownMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, 0.0f, 0.6f);
        this.legUpMover = new AccelerationMover(this, TravisDofs.LEG_MOTOR, 0.2f, 0.6f);
        this.legDownMover = new AccelerationMover(this, TravisDofs.LEG_MOTOR, 0.0f, 0.6f);
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
            Log.d("FootTapBehavior", "Stopping " + FootTapBehavior.class.toString());
        }
        this.legDownMover.stop();
        this.legUpMover.stop();
        this.headDownMover.stop();
        this.headUpMover.stop();
        this.handler.removeCallbacks(this.mHeadAnimation);
        this.handler.removeCallbacks(this.mLegAnimation);
    }

    public void onSongEvent(int type, Object data) {
        if (type == 0) {
            Log.d("FootTapBehavior", data.toString());
            int beat = ((Integer) data).intValue();
            if (this.D) {
                Log.d("FootTapBehavior", "Beat: " + beat);
            }
            this.msPeriod = (long) (beat / 2);
            this.handler.postDelayed(this.mHeadAnimation, 0);
            this.handler.postDelayed(this.mLegAnimation, 0);
        }
    }

    public void setVelocity(String motorName, float goal, float vel) {
        this.mc.moveMotor(motorName, goal, (float) Math.max((double) vel, 0.1d), 0.0f);
    }
}
