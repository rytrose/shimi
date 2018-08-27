package mr1.robots.travis.behaviors;

import android.util.Log;
import mr1.core.motion.AccelerationMover.iMoveListener;
import mr1.motor.MotorController;
import mr1.research.behaviors.BehaviorController.iBehavior;
import mr1.research.music.BeatPlayer.iOnBeatListener;
import mr1.robots.travis.TravisDofs;

public abstract class TravisBehavior implements iBehavior, iMoveListener, iOnBeatListener, TravisDofs {
    private static final String TAG = "Travis Behavior";
    /* renamed from: D */
    protected boolean f19D = false;
    protected boolean handRight;
    protected boolean headUp;
    protected boolean legUp;
    public iOnBehaviorEndedListener mOnBehaviorEndedListener;
    public MotorController mc;
    protected long msPeriod;
    protected boolean neckRight;
    protected boolean neckUp;

    public interface iOnBehaviorEndedListener {
        void onBehaviorEnded(String str);
    }

    public void setOnBehaviorEndedListener(iOnBehaviorEndedListener l) {
        this.mOnBehaviorEndedListener = l;
    }

    public TravisBehavior(MotorController mc) {
        this.mc = mc;
        this.msPeriod = 1500;
        this.neckRight = false;
        this.headUp = false;
        this.handRight = false;
        this.neckUp = false;
        this.legUp = false;
    }

    protected int beatDataToInt(Object data) {
        if (this.f19D) {
            Log.d(TAG, "Class: " + data.getClass());
        }
        if (data.getClass() == Long.class) {
            return ((Long) data).intValue();
        }
        if (data.getClass() == Integer.class) {
            return ((Integer) data).intValue();
        }
        Log.e(TAG, "Data not Long or Integer in onSongEvent");
        return 0;
    }
}
