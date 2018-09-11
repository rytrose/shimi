package idc.travis.behavior;

import idc.travis.behavior.AccelerationMover.iMoveListener;
import idc.travis.behavior.BehaviorController.iBehavior;
import idc.travis.motor.MotorController;
import idc.travis.detection.TravisAudioPlayback;
import idc.travis.detection.TravisAudioPlayback.OnSongBeatListener;

public abstract class GenericBehavior implements iBehavior, iMoveListener {

	public static final String HEAD_MOTOR = "Head";
	public static final String HAND_MOTOR = "Hand";
	public static final String NECKUD_MOTOR = "NeckUD";
	public static final String NECKRL_MOTOR = "NeckRL";
	public static final String LEG_MOTOR = "Leg";
	
	protected boolean D = false;
	
	public OnBehaviorEndedListener mOnBehaviorEndedListener;	
	public void setOnBehaviorEndedListener(OnBehaviorEndedListener l) {
		this.mOnBehaviorEndedListener = l;
	}
	public MotorController mc;
	
	protected long msDelay;
	protected boolean neckUp, headUp, handRight, neckRight, legUp;

	public GenericBehavior(MotorController mc) {
		this.mc = mc;
		msDelay = 1500;
		neckRight = false;
		headUp = false;
		handRight = false;
		neckUp = false;
		legUp = false;
	}
	
	public interface OnBehaviorEndedListener{
		public abstract void onBehaviorEnded(String name);
	}
}
