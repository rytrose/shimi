package mr1.robots.travis.behaviors;

import android.os.Handler;
import android.util.Log;
import mr1.core.motion.AccelerationMover;
import mr1.core.motion.AccelerationMover.iMoveListener;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;

public class IdleBehavior extends TravisBehavior implements iMoveListener {
    private final String TAG = "IdleBehavior";
    private Handler handler = new Handler();
    private AccelerationMover headDownMover;
    private AccelerationMover headUpMover;
    private Runnable mHeadAnimation = new C00342();
    private Runnable mNeckAnimation = new C00331();
    private AccelerationMover neckDownMover;
    private AccelerationMover neckUpMover;

    /* renamed from: mr1.robots.travis.behaviors.IdleBehavior$1 */
    class C00331 implements Runnable {
        C00331() {
        }

        public void run() {
            if (IdleBehavior.this.neckUp) {
                if (IdleBehavior.this.D) {
                    Log.d("IdleBehavior", "Neck down");
                }
                IdleBehavior.this.neckUpMover.stop();
                IdleBehavior.this.neckDownMover.start(IdleBehavior.this.msPeriod);
            } else {
                if (IdleBehavior.this.D) {
                    Log.d("IdleBehavior", "Neck up");
                }
                IdleBehavior.this.neckDownMover.stop();
                IdleBehavior.this.neckUpMover.start(IdleBehavior.this.msPeriod);
            }
            IdleBehavior.this.neckUp = !IdleBehavior.this.neckUp;
            IdleBehavior.this.handler.postDelayed(IdleBehavior.this.mNeckAnimation, 2000);
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.IdleBehavior$2 */
    class C00342 implements Runnable {
        C00342() {
        }

        public void run() {
            if (IdleBehavior.this.headUp) {
                if (IdleBehavior.this.D) {
                    Log.d("IdleBehavior", "Head down");
                }
                IdleBehavior.this.headUpMover.stop();
                IdleBehavior.this.headDownMover.start(IdleBehavior.this.msPeriod);
            } else {
                if (IdleBehavior.this.D) {
                    Log.d("IdleBehavior", "Head up");
                }
                IdleBehavior.this.headDownMover.stop();
                IdleBehavior.this.headUpMover.start(IdleBehavior.this.msPeriod);
            }
            IdleBehavior.this.headUp = !IdleBehavior.this.headUp;
            IdleBehavior.this.handler.postDelayed(IdleBehavior.this.mHeadAnimation, 2000);
        }
    }

    public IdleBehavior(MotorController mc) {
        super(mc);
    }

    public void start(Object data) {
        this.msPeriod = 2000;
        if (this.D) {
            Log.d("IdleBehavior", "Starting " + IdleBehavior.class.toString());
        }
        initPos(1000);
        this.neckUp = false;
        this.headUp = false;
        this.neckUpMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, -0.3f, 0.05f);
        this.neckDownMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, 0.1f, 0.05f);
        this.headUpMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, -0.6f, 0.2f);
        this.headDownMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, -0.4f, 0.2f);
        this.handler.postDelayed(this.mNeckAnimation, 1000);
        this.handler.postDelayed(this.mHeadAnimation, 1000);
    }

    private void initPos(int length) {
        new AccelerationMover(this, TravisDofs.NECKRL_MOTOR, 0.0f, 0.2f).start((long) length);
        new AccelerationMover(this, TravisDofs.LEG_MOTOR, 0.0f, 0.2f).start((long) length);
    }

    public void stop() {
        if (this.D) {
            Log.d("IdleBehavior", "Stopping " + IdleBehavior.class.toString());
        }
        this.neckUpMover.stop();
        this.neckDownMover.stop();
        this.headUpMover.stop();
        this.headDownMover.stop();
        this.handler.removeCallbacks(this.mNeckAnimation);
        this.handler.removeCallbacks(this.mHeadAnimation);
    }

    public void onSongEvent(int type, Object data) {
    }

    public void setVelocity(String motorName, float goal, float vel) {
        this.mc.moveMotor(motorName, goal, (float) Math.max((double) vel, 0.01d), 0.0f);
    }
}
