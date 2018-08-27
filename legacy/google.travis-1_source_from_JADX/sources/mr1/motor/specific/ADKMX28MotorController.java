package mr1.motor.specific;

import android.content.Context;
import android.util.Log;
import java.util.Map;
import mr1.interfaces.android.adk.ADKManager;
import mr1.motor.MotorController;

public class ADKMX28MotorController extends MotorController {
    public static final byte MOVE_MOTOR_CMD = (byte) 0;
    private static final float POS_UNIT = 0.088f;
    private static final float VEL_UNIT = 0.114f;
    ADKManager adkManager;

    public class Motor extends mr1.motor.MotorController.Motor {
        public Motor(Map<String, String> motorMap) {
            super(motorMap);
        }

        public void move(float pos, float vel, float acc) {
            int positionValue = ((int) Math.ceil(((double) this.mZeroPos) + ((((double) (capPosition(pos) * 180.0f)) / 3.141592653589793d) / 0.08799999952316284d))) / 4;
            int velocityValue = (int) ((float) Math.floor((double) (((float) (((double) (60.0f * capVel(vel))) / 6.283185307179586d)) / ADKMX28MotorController.VEL_UNIT)));
            if (velocityValue == 0) {
                velocityValue = 1;
            }
            if (ADKMX28MotorController.this.D) {
                Log.d(MotorController.TAG, "Motor command: " + this.mId + " -> " + positionValue + "/" + velocityValue);
            }
            ADKMX28MotorController.this.sendADKMessage((byte) 0, this.mId, positionValue, velocityValue);
        }
    }

    public ADKMX28MotorController(Context c, String motorConfigName) {
        super(motorConfigName);
        this.adkManager = new ADKManager(c);
    }

    public Motor createMotor(Map<String, String> motorMap) {
        return new Motor(motorMap);
    }

    private void sendADKMessage(byte command, byte motor, int position, int velocity) {
        int max;
        int i = 255;
        byte[] buffer = new byte[10];
        buffer[0] = command;
        buffer[1] = motor;
        buffer[2] = (byte) (position <= 255 ? (byte) position : 255);
        if (position <= 511) {
            max = (byte) Math.max(0, Math.min(255, position - 255));
        } else {
            max = 255;
        }
        buffer[3] = (byte) max;
        if (position <= 767) {
            max = (byte) Math.max(0, Math.min(255, position - 510));
        } else {
            max = 255;
        }
        buffer[4] = (byte) max;
        if (position <= 1023) {
            max = (byte) Math.max(0, Math.min(255, position - 765));
        } else {
            max = 255;
        }
        buffer[5] = (byte) max;
        if (velocity <= 255) {
            max = (byte) velocity;
        } else {
            max = 255;
        }
        buffer[6] = (byte) max;
        if (velocity <= 511) {
            max = (byte) Math.max(0, Math.min(255, velocity - 255));
        } else {
            max = 255;
        }
        buffer[7] = (byte) max;
        if (velocity <= 767) {
            max = (byte) Math.max(0, Math.min(255, velocity - 510));
        } else {
            max = 255;
        }
        buffer[8] = (byte) max;
        if (velocity <= 1023) {
            i = (byte) Math.max(0, Math.min(255, velocity - 765));
        }
        buffer[9] = (byte) i;
        this.adkManager.sendMessage(buffer);
    }
}
