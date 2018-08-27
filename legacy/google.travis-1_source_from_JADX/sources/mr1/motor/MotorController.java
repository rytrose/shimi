package mr1.motor;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class MotorController {
    public static final String TAG = "MotorController";
    /* renamed from: D */
    public boolean f10D = false;
    private Map<String, Motor> mMotors;

    public abstract class Motor {
        protected byte mId;
        protected float mMaxPos;
        protected float mMaxVel;
        protected float mMinPos;
        protected String mName;
        protected int mZeroPos;

        public abstract void move(float f, float f2, float f3);

        public Motor(Map<String, String> motorMap) {
            this.mName = (String) motorMap.get(MotorLoader.MOTOR_NAME);
            this.mId = Byte.parseByte((String) motorMap.get(MotorLoader.MOTOR_ID));
            this.mZeroPos = Integer.parseInt((String) motorMap.get(MotorLoader.MOTOR_ZERO_POS));
            this.mMinPos = Float.parseFloat((String) motorMap.get(MotorLoader.MOTOR_MIN_POS));
            this.mMaxPos = Float.parseFloat((String) motorMap.get(MotorLoader.MOTOR_MAX_POS));
            this.mMaxVel = Float.parseFloat((String) motorMap.get(MotorLoader.MOTOR_MAX_VEL));
        }

        public float getCenter() {
            return this.mMaxPos - this.mMinPos;
        }

        public String getName() {
            return this.mName;
        }

        public float GetMaxPos() {
            return this.mMaxPos;
        }

        public float GetMinPos() {
            return this.mMinPos;
        }

        public float GetMaxVel() {
            return this.mMaxVel;
        }

        public float getMaxAcc() {
            return 0.0f;
        }

        protected float capPosition(float position) {
            return Math.min(this.mMaxPos, Math.max(this.mMinPos, position));
        }

        protected float capVel(float vel) {
            if (MotorController.this.f10D) {
                Log.d(MotorController.TAG, "Capped vel " + vel + " to " + Math.min(this.mMaxVel, Math.max(0.0f, vel)));
            }
            return Math.min(this.mMaxVel, Math.max(0.0f, vel));
        }
    }

    public abstract Motor createMotor(Map<String, String> map);

    public MotorController(String motorConfigName) {
        this.mMotors = new MotorLoader().load(this, motorConfigName);
    }

    public void moveMotor(String name, float pos, float vel, float acc) {
        if (this.f10D) {
            Log.d(TAG, "Moving [" + name + "] to " + pos);
        }
        ((Motor) this.mMotors.get(name)).move(pos, vel, acc);
    }

    public void moveMotor(String name, float pos) {
        Motor m = (Motor) this.mMotors.get(name);
        m.move(pos, m.GetMaxVel(), m.getMaxAcc());
    }

    public Motor getMotor(String name) {
        return (Motor) this.mMotors.get(name);
    }

    public List<String> getMotorNames() {
        return new ArrayList(this.mMotors.keySet());
    }
}
