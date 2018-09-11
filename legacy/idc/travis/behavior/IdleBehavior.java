package idc.travis.behavior;

import android.os.Handler;
import android.util.Log;
import idc.travis.behavior.AccelerationMover.iMoveListener;
import idc.travis.motor.MotorController;

public class IdleBehavior extends GenericBehavior implements iMoveListener {

	private final String TAG = "IdleBehavior";  
	
    private AccelerationMover neckUpMover, neckDownMover;
    private AccelerationMover headUpMover, headDownMover;
    private AccelerationMover neckRL,leg,hand;
    
    private Handler handler = new Handler();

	public IdleBehavior(MotorController mc)
	{
		super(mc);
	}
	
	@Override
	public void start(Object data) {
		msDelay = 2000;
		if (D) Log.d(TAG, "Starting " + IdleBehavior.class.toString());
		
		AccelerationMover initNeckUpMover = new AccelerationMover(this, NECKUD_MOTOR, 0f, 0.2f);
		initNeckUpMover.start(2000);
		AccelerationMover initHeadUpMover = new AccelerationMover(this, HEAD_MOTOR, -.6f, 0.2f);
		initHeadUpMover.start(2000);
		AccelerationMover initNeckRL = new AccelerationMover(this, NECKRL_MOTOR, 1.5f, 0.2f);
		initNeckRL.start(2000);
		AccelerationMover initLeg = new AccelerationMover(this, LEG_MOTOR, 0f, 0.2f);
		initLeg.start(2000);
		AccelerationMover initHand = new AccelerationMover(this, HAND_MOTOR, 0f, 0.5f);
		initHand.start(2000);

        neckUp = false;
        headUp = false;

        // Neck acceleration mover
        neckUpMover = new AccelerationMover(this, NECKUD_MOTOR, -0.3f, 0.05f);
        neckDownMover = new AccelerationMover(this, NECKUD_MOTOR, 0.1f, 0.05f);
        
        // Head acceleration mover
        headUpMover = new AccelerationMover(this, HEAD_MOTOR, -.6f, 0.2f);
        headDownMover = new AccelerationMover(this, HEAD_MOTOR, -.4f, 0.2f);
        
        handler.postDelayed(mNeckAnimation, 3000);
        handler.postDelayed(mHeadAnimation, 3000);
	}

	@Override
	public void stop() {
		if (D) Log.d(TAG, "Stopping " + IdleBehavior.class.toString());
		neckUpMover.stop();
		neckDownMover.stop();
		headUpMover.stop();
		headDownMover.stop();
		handler.removeCallbacks(mNeckAnimation);
		handler.removeCallbacks(mHeadAnimation);
		
	}
	
	private Runnable mNeckAnimation = new Runnable() {
		
		@Override
		public void run() {
            if (neckUp) {
            	if (D) Log.d(TAG, "Neck down");
                neckUpMover.stop();
                neckDownMover.start(msDelay);
            } else {
            	if (D) Log.d(TAG, "Neck up");
                neckDownMover.stop();
                neckUpMover.start(msDelay);
            }
            neckUp = !neckUp;
			handler.postDelayed(mNeckAnimation, 2000);
		}
	};

	private Runnable mHeadAnimation = new Runnable() {

		@Override
		public void run() {
            if (headUp) {
            	if (D) Log.d(TAG, "Head down");
                headUpMover.stop();
                headDownMover.start(msDelay);
            } else {
            	if (D) Log.d(TAG, "Head up");
                headDownMover.stop();
                headUpMover.start(msDelay);
            }
            headUp = !headUp;
			handler.postDelayed(mHeadAnimation, 2000);
		}
	};

	@Override
	public void onSongBeat(long beat) {
		//Do nothing
	}

	@Override
	public void setVelocity(String motorName, float goal, float vel) {
		mc.moveMotor(motorName, goal, (float) Math.max(vel, 0.01), 0);
	}
}