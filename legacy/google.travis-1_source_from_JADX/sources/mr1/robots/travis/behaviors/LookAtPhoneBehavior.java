package mr1.robots.travis.behaviors;

import android.os.Handler;
import android.util.Log;
import mr1.core.motion.AccelerationMover;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;

public class LookAtPhoneBehavior extends TravisBehavior {
    protected boolean BEHAVIOR_FINISHED;
    private final String TAG;
    private AccelerationMover handLeftMover;
    private AccelerationMover handRightMover;
    private Handler handler;
    private AccelerationMover headDownMover;
    private AccelerationMover headUpMover;
    private Runnable mHandAnimation;
    private Runnable mHeadAnimation;
    private Runnable mNeckAnimation;
    private Runnable mNeckRLAnimation;
    private AccelerationMover neckDownMover;
    private AccelerationMover neckLeftMover;
    private AccelerationMover neckRightMover;
    private AccelerationMover neckUpMover;

    /* renamed from: mr1.robots.travis.behaviors.LookAtPhoneBehavior$1 */
    class C00351 implements Runnable {
        C00351() {
        }

        public void run() {
            if (LookAtPhoneBehavior.this.neckUp) {
                Log.d("LookAtPhoneBehavior", "neck down");
                LookAtPhoneBehavior.this.neckUpMover.stop();
                LookAtPhoneBehavior.this.neckDownMover.start(2000);
                LookAtPhoneBehavior.this.neckUp = !LookAtPhoneBehavior.this.neckUp;
                LookAtPhoneBehavior.this.handler.postDelayed(LookAtPhoneBehavior.this.mNeckAnimation, 5000);
                return;
            }
            Log.d("LookAtPhoneBehavior", "neck up");
            LookAtPhoneBehavior.this.neckDownMover.stop();
            LookAtPhoneBehavior.this.neckUpMover.start(2000);
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.LookAtPhoneBehavior$2 */
    class C00362 implements Runnable {
        C00362() {
        }

        public void run() {
            if (LookAtPhoneBehavior.this.handRight) {
                Log.d("LookAtPhoneBehavior", "hand left");
                LookAtPhoneBehavior.this.handRightMover.stop();
                LookAtPhoneBehavior.this.handLeftMover.start(2000);
                LookAtPhoneBehavior.this.handRight = !LookAtPhoneBehavior.this.handRight;
                LookAtPhoneBehavior.this.handler.postDelayed(LookAtPhoneBehavior.this.mHandAnimation, 5000);
                return;
            }
            Log.d("LookAtPhoneBehavior", "hand right");
            LookAtPhoneBehavior.this.handLeftMover.stop();
            LookAtPhoneBehavior.this.handRightMover.start(2000);
            if (LookAtPhoneBehavior.this.mOnBehaviorEndedListener != null) {
                LookAtPhoneBehavior.this.mOnBehaviorEndedListener.onBehaviorEnded("LOOKATPHONE");
            }
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.LookAtPhoneBehavior$3 */
    class C00373 implements Runnable {
        C00373() {
        }

        public void run() {
            if (LookAtPhoneBehavior.this.headUp) {
                Log.d("LookAtPhoneBehavior", "head down");
                LookAtPhoneBehavior.this.headUpMover.stop();
                LookAtPhoneBehavior.this.headDownMover.start(2000);
                LookAtPhoneBehavior.this.headUp = !LookAtPhoneBehavior.this.headUp;
                LookAtPhoneBehavior.this.handler.postDelayed(LookAtPhoneBehavior.this.mHeadAnimation, 5000);
                return;
            }
            Log.d("LookAtPhoneBehavior", "head up");
            LookAtPhoneBehavior.this.headDownMover.stop();
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.LookAtPhoneBehavior$4 */
    class C00384 implements Runnable {
        C00384() {
        }

        public void run() {
            if (LookAtPhoneBehavior.this.neckRight) {
                Log.d("LookAtPhoneBehavior", "neck right");
                LookAtPhoneBehavior.this.neckRightMover.stop();
                LookAtPhoneBehavior.this.neckLeftMover.start(2000);
                LookAtPhoneBehavior.this.neckRight = !LookAtPhoneBehavior.this.neckRight;
                LookAtPhoneBehavior.this.handler.postDelayed(LookAtPhoneBehavior.this.mNeckRLAnimation, 5000);
                return;
            }
            Log.d("LookAtPhoneBehavior", "neck left");
            LookAtPhoneBehavior.this.neckLeftMover.stop();
        }
    }

    public LookAtPhoneBehavior(MotorController mc) {
        super(mc);
        this.TAG = "LookAtPhoneBehavior";
        this.handler = new Handler();
        this.mNeckAnimation = new C00351();
        this.mHandAnimation = new C00362();
        this.mHeadAnimation = new C00373();
        this.mNeckRLAnimation = new C00384();
        this.msPeriod = 3000;
    }

    public void start(Object data) {
        Log.d("LookAtPhoneBehavior", "Starting " + LookAtPhoneBehavior.class.toString());
        this.neckUpMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, 0.0f, 0.6f);
        this.neckDownMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, -0.6f, 0.5f);
        this.handRightMover = new AccelerationMover(this, TravisDofs.HAND_MOTOR, 0.8f, 0.75f);
        this.handLeftMover = new AccelerationMover(this, TravisDofs.HAND_MOTOR, -1.57f, 0.7f);
        this.headUpMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, -0.7f, 0.5f);
        this.headDownMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, 0.0f, 0.5f);
        this.neckRightMover = new AccelerationMover(this, TravisDofs.NECKRL_MOTOR, 0.0f, 0.7f);
        this.neckLeftMover = new AccelerationMover(this, TravisDofs.NECKRL_MOTOR, 1.1f, 0.5f);
        this.headUp = true;
        this.handRight = true;
        this.neckUp = true;
        this.neckRight = true;
        this.handler.postDelayed(this.mNeckAnimation, 200);
        this.handler.postDelayed(this.mHeadAnimation, 0);
        this.handler.postDelayed(this.mNeckRLAnimation, 0);
        this.handler.postDelayed(this.mHandAnimation, 0);
    }

    public void stop() {
        Log.d("LookAtPhoneBehavior", "Stopping " + LookAtPhoneBehavior.class.toString());
        this.neckUpMover.stop();
        this.neckDownMover.stop();
        this.handRightMover.stop();
        this.handLeftMover.stop();
        this.headUpMover.stop();
        this.headDownMover.stop();
        this.neckRightMover.stop();
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
