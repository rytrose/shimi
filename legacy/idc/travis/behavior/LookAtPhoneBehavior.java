package idc.travis.behavior;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import idc.travis.behavior.AccelerationMover.iMoveListener;
import idc.travis.motor.MotorController;

public class LookAtPhoneBehavior extends GenericBehavior implements iMoveListener{
	
	private final String TAG = "LookAtPhoneBehavior";
	
	private AccelerationMover neckUpMover, neckDownMover;
	private AccelerationMover headUpMover, headDownMover;
	private AccelerationMover handRightMover, handLeftMover;
	private AccelerationMover neckRightMover, neckLeftMover;
	private Handler handler = new Handler();
	
	protected boolean BEHAVIOR_FINISHED;
    
	public LookAtPhoneBehavior(MotorController mc) {
		super(mc);
		msDelay = 3000;
	}

	@Override
	public void start(Object data) {
		Log.d(TAG, "Starting " + LookAtPhoneBehavior.class.toString());
		
		// Neck up/down acceleration mover
		neckUpMover = new AccelerationMover(this, NECKUD_MOTOR, 0.0f, 0.6f);
		neckDownMover = new AccelerationMover(this, NECKUD_MOTOR, -0.6f, 0.5f);
		
		// Hand left/right acceleration mover
		handRightMover = new AccelerationMover(this, HAND_MOTOR, 0.8f, 0.75f);
		handLeftMover = new AccelerationMover(this, HAND_MOTOR, -1.57f, 0.7f);
		
		// Head up/down acceleration mover
		headUpMover = new AccelerationMover(this, HEAD_MOTOR, -0.7f, 0.5f);
		headDownMover = new AccelerationMover(this, HEAD_MOTOR, 0f, 0.5f);
		
		// Neck right/left acceleration mover
		neckRightMover = new AccelerationMover(this, NECKRL_MOTOR, 0f, 0.7f);
		neckLeftMover = new AccelerationMover(this, NECKRL_MOTOR, 1.1f, 0.5f);
		
		headUp = true;
		handRight = true;
		neckUp = true;
		neckRight = true;
		
		handler.postDelayed(mNeckAnimation, 200);
        handler.postDelayed(mHeadAnimation, 0);
        handler.postDelayed(mNeckRLAnimation, 0);
        handler.postDelayed(mHandAnimation, 0);
	}

	@Override
	public void stop() {
		Log.d(TAG, "Stopping " + LookAtPhoneBehavior.class.toString());
		
		neckUpMover.stop();
		neckDownMover.stop();
		
		handRightMover.stop();
		handLeftMover.stop();
		
		headUpMover.stop();
		headDownMover.stop();
		
		neckRightMover.stop();
		neckLeftMover.stop();
		
		handler.removeCallbacks(mNeckAnimation);
		handler.removeCallbacks(mHeadAnimation);
		handler.removeCallbacks(mNeckRLAnimation);
		handler.removeCallbacks(mHandAnimation);
	}
	
	private Runnable mNeckAnimation = new Runnable() {
		
		@Override
		public void run() {
            if (neckUp) {
            	Log.d(TAG, "neck down");
            	neckUpMover.stop();
            	neckDownMover.start(2000);
            	neckUp = !neckUp;
            	handler.postDelayed(mNeckAnimation, 5000);
            } else {
            	Log.d(TAG, "neck up");
            	neckDownMover.stop();
            	neckUpMover.start(2000);
            }
		}
	};
	
	private Runnable mHandAnimation = new Runnable() {
		
		@Override
		public void run() {
            if (handRight) {
            	Log.d(TAG, "hand left");
            	handRightMover.stop();
            	handLeftMover.start(2000);
            	handRight = !handRight;
            	handler.postDelayed(mHandAnimation, 5000);
            } else {
            	Log.d(TAG, "hand right");
            	handLeftMover.stop();
            	handRightMover.start(2000);
            	if(mOnBehaviorEndedListener != null){
            		mOnBehaviorEndedListener.onBehaviorEnded("LOOKATPHONE");
            	}
            }
		}
	};
	
	private Runnable mHeadAnimation = new Runnable() {
		@Override
		public void run() {
            if (headUp) {
            	Log.d(TAG, "head down");
            	headUpMover.stop();
            	headDownMover.start(2000);
            	headUp = !headUp;
            	handler.postDelayed(mHeadAnimation, 5000);
            } else {
            	Log.d(TAG, "head up");
            	headDownMover.stop();
//            	headUpMover.start(2000);
            }
		}
	};

	private Runnable mNeckRLAnimation = new Runnable() {
		
		@Override
		public void run() {
            if (neckRight) {
            	Log.d(TAG, "neck right");
            	neckRightMover.stop();
            	neckLeftMover.start(2000);
            	neckRight = !neckRight;
            	handler.postDelayed(mNeckRLAnimation, 5000);
            } else {
            	Log.d(TAG, "neck left");
            	neckLeftMover.stop();
 //           	neckRightMover.start(2000);
            }
		}
	};

	

	@Override
	public void onSongBeat(long beat) {
		// Do nothing
	}

	@Override
	public void setVelocity(String motorName, float goal, float vel) {
		mc.moveMotor(motorName, goal, (float) Math.max(vel, 0.05), 0);
	}
}