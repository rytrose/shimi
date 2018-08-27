package mr1.robots.travis.behaviors;

import android.os.Handler;
import android.util.Log;
import mr1.core.motion.AccelerationMover;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;

public class SurpriseBehavior extends TravisBehavior {
    private static final int PERIOD = 2000;
    protected boolean BEHAVIOR_FINISHED;
    private final String TAG;
    private AccelerationMover handLeftMover;
    private Handler handler;
    private AccelerationMover headUpMover;
    private AccelerationMover legDownMover;
    private Runnable mHandAnimation;
    private Runnable mHeadAnimation;
    private Runnable mLegAnimation;
    private Runnable mNeckAnimation;
    private Runnable mNeckRLAnimation;
    private AccelerationMover neckLeftMover;
    private AccelerationMover neckUpMover;

    /* renamed from: mr1.robots.travis.behaviors.SurpriseBehavior$1 */
    class C00441 implements Runnable {
        C00441() {
        }

        public void run() {
            Log.d("LookAtPhoneBehavior", "neck up");
            SurpriseBehavior.this.neckUpMover.start(2000);
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.SurpriseBehavior$2 */
    class C00452 implements Runnable {
        C00452() {
        }

        public void run() {
            Log.d("LookAtPhoneBehavior", "hand left");
            SurpriseBehavior.this.handLeftMover.start(2000);
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.SurpriseBehavior$3 */
    class C00463 implements Runnable {
        C00463() {
        }

        public void run() {
            Log.d("LookAtPhoneBehavior", "head up");
            SurpriseBehavior.this.headUpMover.start(2000);
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.SurpriseBehavior$4 */
    class C00474 implements Runnable {
        C00474() {
        }

        public void run() {
            Log.d("LookAtPhoneBehavior", "neck right");
            SurpriseBehavior.this.neckLeftMover.start(2000);
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.SurpriseBehavior$5 */
    class C00485 implements Runnable {
        C00485() {
        }

        public void run() {
            Log.d("LookAtPhoneBehavior", "leg down");
            SurpriseBehavior.this.legDownMover.start(2000);
        }
    }

    public SurpriseBehavior(MotorController mc) {
        super(mc);
        this.TAG = "LookAtPhoneBehavior";
        this.handler = new Handler();
        this.mNeckAnimation = new C00441();
        this.mHandAnimation = new C00452();
        this.mHeadAnimation = new C00463();
        this.mNeckRLAnimation = new C00474();
        this.mLegAnimation = new C00485();
        this.msPeriod = 3000;
    }

    public void start(Object data) {
        Log.d("LookAtPhoneBehavior", "Starting " + SurpriseBehavior.class.toString());
        this.mc.moveMotor(TravisDofs.NECKUD_MOTOR, -0.4f, 0.1f, 0.01f);
        this.mc.moveMotor(TravisDofs.HEAD_MOTOR, 0.3f, 0.25f, 0.01f);
        this.mc.moveMotor(TravisDofs.NECKRL_MOTOR, -0.9f, 0.3f, 0.0f);
        this.mc.moveMotor(TravisDofs.LEG_MOTOR, 0.3f, 0.2f, 0.01f);
        this.mc.moveMotor(TravisDofs.HAND_MOTOR, -1.3f, 0.1f, 0.0f);
        this.neckUpMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, 0.0f, 0.6f);
        this.handLeftMover = new AccelerationMover(this, TravisDofs.HAND_MOTOR, 1.0f, 1.7f);
        this.headUpMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, -0.7f, 0.5f);
        this.neckLeftMover = new AccelerationMover(this, TravisDofs.NECKRL_MOTOR, 1.1f, 2.5f);
        this.legDownMover = new AccelerationMover(this, TravisDofs.LEG_MOTOR, 1.1f, 0.5f);
        this.headUp = true;
        this.handRight = true;
        this.neckUp = true;
        this.neckRight = true;
        this.handler.postDelayed(this.mNeckAnimation, 5200);
        this.handler.postDelayed(this.mHeadAnimation, 5000);
        this.handler.postDelayed(this.mNeckRLAnimation, 5000);
        this.handler.postDelayed(this.mHandAnimation, 5000);
        this.handler.postDelayed(this.mLegAnimation, 5000);
    }

    public void stop() {
        Log.d("LookAtPhoneBehavior", "Stopping " + SurpriseBehavior.class.toString());
        this.neckUpMover.stop();
        this.handLeftMover.stop();
        this.headUpMover.stop();
        this.neckLeftMover.stop();
        this.handler.removeCallbacks(this.mNeckAnimation);
        this.handler.removeCallbacks(this.mHeadAnimation);
        this.handler.removeCallbacks(this.mNeckRLAnimation);
        this.handler.removeCallbacks(this.mHandAnimation);
    }

    public void onSongEvent(int type, Object data) {
    }

    public void setVelocity(String motorName, float goal, float vel) {
        this.mc.moveMotor(motorName, goal, (float) Math.max((double) vel, 0.05d), 0.0f);
    }
}
