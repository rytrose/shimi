package mr1.research.motion;

import android.util.Log;
import mr1.motor.MotorController;

public class SmoothTracker {
    private static final float DELTA = 0.03f;
    /* renamed from: D */
    private boolean f12D = false;
    private float lastPos;
    private MotorController mc;
    private String motorName;

    public SmoothTracker(MotorController mc, String motorName) {
        this.mc = mc;
        this.motorName = motorName;
        mc.moveMotor(motorName, 0.0f);
        this.lastPos = 0.0f;
    }

    public void feedback(float error) {
        if (this.f12D) {
            Log.d("SmoothTracker", "Error: " + error + " . pos " + this.lastPos + "->" + (this.lastPos + (error * DELTA)));
        }
        this.lastPos -= error * DELTA;
        this.mc.moveMotor(this.motorName, this.lastPos);
    }

    public float getPos() {
        return this.lastPos;
    }
}
