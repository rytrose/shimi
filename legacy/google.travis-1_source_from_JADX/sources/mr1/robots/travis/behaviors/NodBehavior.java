package mr1.robots.travis.behaviors;

import android.os.Handler;
import android.util.Log;
import mr1.core.motion.AccelerationMover;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;

public class NodBehavior extends TravisBehavior {
    private static final int HEAD_TURN_DELAY = 3000;
    private final String TAG;
    private Handler handler;
    private AccelerationMover headDownMover;
    private AccelerationMover headUpMover;
    private Runnable mHeadAnimation;
    private Runnable mHeadTurnAnimation;
    private Runnable mNeckAnimation;
    private int nNods;
    private AccelerationMover neckDownMover;
    private AccelerationMover neckUpMover;

    /* renamed from: mr1.robots.travis.behaviors.NodBehavior$1 */
    class C00401 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.NodBehavior$1$1 */
        class C00391 implements Runnable {
            C00391() {
            }

            public void run() {
                if (NodBehavior.this.D) {
                    Log.d("NodBehavior", "neck up");
                }
                NodBehavior.this.neckDownMover.stop();
                NodBehavior.this.neckUpMover.start(NodBehavior.this.msPeriod);
            }
        }

        C00401() {
        }

        public void run() {
            if (NodBehavior.this.D) {
                Log.d("NodBehavior", "neck down");
            }
            if (NodBehavior.this.neckUpMover != null) {
                NodBehavior.this.neckUpMover.stop();
                NodBehavior.this.neckDownMover.start(NodBehavior.this.msPeriod);
                NodBehavior.this.handler.postDelayed(new C00391(), NodBehavior.this.msPeriod);
                NodBehavior nodBehavior = NodBehavior.this;
                int access$3 = nodBehavior.nNods;
                nodBehavior.nNods = access$3 - 1;
                if (access$3 > 0) {
                    NodBehavior.this.handler.postDelayed(this, NodBehavior.this.msPeriod * 2);
                }
            }
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.NodBehavior$2 */
    class C00422 implements Runnable {

        /* renamed from: mr1.robots.travis.behaviors.NodBehavior$2$1 */
        class C00411 implements Runnable {
            C00411() {
            }

            public void run() {
                if (NodBehavior.this.D) {
                    Log.d("NodBehavior", "head down");
                }
                NodBehavior.this.headUpMover.stop();
                NodBehavior.this.headDownMover.start(NodBehavior.this.msPeriod);
            }
        }

        C00422() {
        }

        public void run() {
            if (NodBehavior.this.headDownMover != null) {
                if (NodBehavior.this.D) {
                    Log.d("NodBehavior", "head up");
                }
                NodBehavior.this.headDownMover.stop();
                NodBehavior.this.headUpMover.start(NodBehavior.this.msPeriod);
                NodBehavior.this.handler.postDelayed(new C00411(), NodBehavior.this.msPeriod);
                if (NodBehavior.this.nNods > 0) {
                    NodBehavior.this.handler.postDelayed(this, NodBehavior.this.msPeriod * 2);
                }
            }
        }
    }

    /* renamed from: mr1.robots.travis.behaviors.NodBehavior$3 */
    class C00433 implements Runnable {
        C00433() {
        }

        public void run() {
            NodBehavior.this.setVelocity(TravisDofs.NECKRL_MOTOR, 2.0f * (((float) Math.random()) - 0.5f), 0.25f);
            NodBehavior.this.handler.postDelayed(this, 3000);
            NodBehavior.this.nNods = 1;
            NodBehavior.this.handler.postDelayed(NodBehavior.this.mNeckAnimation, NodBehavior.this.msPeriod * 2);
            NodBehavior.this.handler.postDelayed(NodBehavior.this.mHeadAnimation, NodBehavior.this.msPeriod + 100);
        }
    }

    public NodBehavior(MotorController mc) {
        super(mc);
        this.TAG = "NodBehavior";
        this.handler = new Handler();
        this.nNods = 2;
        this.mNeckAnimation = new C00401();
        this.mHeadAnimation = new C00422();
        this.mHeadTurnAnimation = new C00433();
        this.msPeriod = 500;
        this.D = true;
    }

    public void start(Object data) {
        if (this.D) {
            Log.d("NodBehavior", "Starting " + NodBehavior.class.toString());
        }
        this.mc.moveMotor(TravisDofs.NECKUD_MOTOR, 0.0f, 0.2f, 0.01f);
        this.mc.moveMotor(TravisDofs.HEAD_MOTOR, 0.0f, 0.25f, 0.01f);
        this.mc.moveMotor(TravisDofs.NECKRL_MOTOR, 0.0f, 0.1f, 0.0f);
        this.mc.moveMotor(TravisDofs.LEG_MOTOR, 0.0f, 0.2f, 0.01f);
        this.mc.moveMotor(TravisDofs.HAND_MOTOR, 0.0f, 0.1f, 0.0f);
        this.headUp = false;
        this.neckUp = false;
        this.neckUpMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, -0.05f, 0.4f);
        this.neckDownMover = new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, -0.4f, 0.5f);
        this.headUpMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, -0.5f, 1.3f);
        this.headDownMover = new AccelerationMover(this, TravisDofs.HEAD_MOTOR, 0.0f, 1.3f);
        this.handler.post(this.mHeadTurnAnimation);
    }

    public void stop() {
        if (this.D) {
            Log.d("NodBehavior", "Stopping " + NodBehavior.class.toString());
        }
        this.neckDownMover.stop();
        this.neckUpMover.stop();
        this.headDownMover.stop();
        this.headUpMover.stop();
        this.handler.removeCallbacks(this.mHeadAnimation);
        this.handler.removeCallbacks(this.mNeckAnimation);
        this.handler.removeCallbacks(this.mHeadTurnAnimation);
    }

    public void setVelocity(String motorName, float goal, float vel) {
        this.mc.moveMotor(motorName, goal, (float) Math.max((double) vel, 0.1d), 0.0f);
    }

    public void onSongEvent(int type, Object data) {
    }
}
