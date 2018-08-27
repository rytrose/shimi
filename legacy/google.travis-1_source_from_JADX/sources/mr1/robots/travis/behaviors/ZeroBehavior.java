package mr1.robots.travis.behaviors;

import android.util.Log;
import mr1.core.motion.AccelerationMover;
import mr1.core.motion.AccelerationMover.iMoveListener;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;

public class ZeroBehavior extends TravisBehavior implements iMoveListener {
    private final String TAG = "ZeroBehavior";

    public ZeroBehavior(MotorController mc) {
        super(mc);
    }

    public void start(Object data) {
        if (this.D) {
            Log.d("ZeroBehavior", "Starting " + ZeroBehavior.class.toString());
        }
        new AccelerationMover(this, TravisDofs.NECKUD_MOTOR, 0.0f, 0.4f).start(2000);
        new AccelerationMover(this, TravisDofs.HEAD_MOTOR, -0.6f, 0.4f).start(2000);
        new AccelerationMover(this, TravisDofs.NECKRL_MOTOR, 0.0f, 0.4f).start(2000);
        new AccelerationMover(this, TravisDofs.LEG_MOTOR, 0.0f, 0.4f).start(2000);
        new AccelerationMover(this, TravisDofs.HAND_MOTOR, 0.0f, 0.4f).start(2000);
        this.neckUp = false;
        this.headUp = false;
    }

    public void stop() {
        if (this.D) {
            Log.d("ZeroBehavior", "Stopping " + ZeroBehavior.class.toString());
        }
    }

    public void onSongEvent(int type, Object data) {
    }

    public void setVelocity(String motorName, float goal, float vel) {
        this.mc.moveMotor(motorName, goal, (float) Math.max((double) vel, 0.01d), 0.0f);
    }
}
