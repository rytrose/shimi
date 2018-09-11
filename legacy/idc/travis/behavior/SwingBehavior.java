package idc.travis.behavior;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import idc.travis.behavior.AccelerationMover.iMoveListener;
import idc.travis.motor.MotorController;

public class SwingBehavior extends GenericBehavior implements iMoveListener {

	private final String TAG = "SwingBehavior";

	private Handler handler = new Handler();

	private AccelerationMover neckUpMover, neckDownMover;
	private AccelerationMover headLeftMover, headRightMover;
	private AccelerationMover headUpMover, headDownMover;
	private AccelerationMover legUpMover, legDownMover;

	public SwingBehavior(MotorController mc) {
		super(mc);
		msDelay = 5000;
	}

	@Override
	public void start(Object data) {
		Log.d(TAG, "Starting " + SwingBehavior.class.toString());

		mc.moveMotor(NECKUD_MOTOR, 0f, 0.2f, 0.01f);
		mc.moveMotor(HEAD_MOTOR, 0.0f, 0.25f, 0.01f);
		mc.moveMotor(NECKRL_MOTOR, 0, 0.1f, 0);
		mc.moveMotor(LEG_MOTOR, 0, 0.2f, 0.01f);
		mc.moveMotor(HAND_MOTOR, 0f, 0.1f, 0);

		headUp = false;
		legUp = false;
		neckUp = false;

		// Neck acceleration mover
		neckUpMover = new AccelerationMover(this, NECKUD_MOTOR, 0f, .4f);
		neckDownMover = new AccelerationMover(this, NECKUD_MOTOR, -.5f, .4f);

		// Head swing acceleration mover
		headLeftMover = new AccelerationMover(this, NECKRL_MOTOR, -.4f, .9f);
		headRightMover = new AccelerationMover(this, NECKRL_MOTOR, .4f, .9f);

		// Head acceleration mover
		headUpMover = new AccelerationMover(this, HEAD_MOTOR, -2f, .8f);
		headDownMover = new AccelerationMover(this, HEAD_MOTOR, 0f, .8f);

		// Leg acceleration mover
		legUpMover = new AccelerationMover(this, LEG_MOTOR, 0.4f, 1.8f);
		legDownMover = new AccelerationMover(this, LEG_MOTOR, 0f, 1.8f);
	}

	@Override
	public void stop() {
		Log.d(TAG, "Stopping " + SwingBehavior.class.toString());
		legDownMover.stop();
		legUpMover.stop();
		neckDownMover.stop();
		neckUpMover.stop();
		headLeftMover.stop();
		headRightMover.stop();
		headDownMover.stop();
		headUpMover.stop();
		handler.removeCallbacks(mHeadAnimation);
		handler.removeCallbacks(mLegAnimation);
		handler.removeCallbacks(mNeckAnimation);
		handler.removeCallbacks(mHeadSwingAnimation);
	}

	private Runnable mNeckAnimation = new Runnable() {

		@Override
		public void run() {
			Log.d(TAG, "neck up");
			if (neckDownMover != null) {
				neckDownMover.stop();
				neckUpMover.start(msDelay*2);
				Log.d(TAG, "Down comes in " + msDelay);
				Log.d(TAG, "Posting at " + SystemClock.uptimeMillis());
				handler.postDelayed(new Runnable() {
					public void run() {
						Log.d(TAG, "Runnin at " + SystemClock.uptimeMillis());
						Log.d(TAG, "neck down");
						neckUpMover.stop();
						neckDownMover.start(msDelay*2);
					}
				}, msDelay*2);
			}
		}
	};

	private Runnable mHeadSwingAnimation = new Runnable() {

		@Override
		public void run() {
			Log.d(TAG, "head left");
			if (headLeftMover != null) {
				headLeftMover.stop();
				headRightMover.start(msDelay);
				handler.postDelayed(new Runnable() {
					public void run() {
						Log.d(TAG, "head right");
						headRightMover.stop();
						headLeftMover.start(msDelay);
					}
				}, msDelay);
			}
		}
	};

	private Runnable mLegAnimation = new Runnable() {

		@Override
		public void run() {

			if (legUpMover != null) {
				legUpMover.stop();
				legDownMover.start(msDelay);
				handler.postDelayed(new Runnable() {
					public void run() {
						legDownMover.stop();
						legUpMover.start(msDelay);
					}
				}, msDelay);
			}
		}
	};

	private Runnable mHeadAnimation = new Runnable() {

		@Override
		public void run() {

			if (headDownMover != null) {
				headDownMover.stop();
				headUpMover.start(msDelay);

				handler.postDelayed(new Runnable() {
					public void run() {
						headUpMover.stop();
						headDownMover.start(msDelay);
					}
				}, msDelay);
			}
		}
	};

	int divider = 0;
	public void onSongBeat(long beat) {
		if (++divider%2==0) {
			Log.d(TAG, "Beat: " + beat);
			msDelay = beat;
			handler.postDelayed(mHeadAnimation, 0);
			handler.postDelayed(mHeadSwingAnimation, 0);
			handler.postDelayed(mLegAnimation, 0);
		if (divider%4==0) {
			handler.postDelayed(mNeckAnimation, (long) (msDelay * 0.2));
		}

		if (Math.random() > 0.85) {
			turnHead();
		}
		}
	}

	boolean left;

	private void turnHead() {
		if (left) {
			setVelocity(NECKRL_MOTOR, -.5f, 0.25f);
		} else {
			setVelocity(NECKRL_MOTOR, .5f, 0.25f);
		}
		left = !left;
	}

	@Override
	public void setVelocity(String motorName, float goal, float vel) {
		mc.moveMotor(motorName, goal, (float) Math.max(vel, 0.1), 0);
	}
}