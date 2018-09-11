package idc.travis.behavior;

import idc.travis.detection.TravisAudioPlayback;
import idc.travis.detection.TravisAudioPlayback.OnSongBeatListener;
import idc.travis.motor.MotorController;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

public class BehaviorController implements GenericBehavior.OnBehaviorEndedListener{

	public Map<String, iBehavior> behaviors;
	private iBehavior currentBehavior;
	private MotorController mc;
	public TravisAudioPlayback tap;

	public BehaviorController(Context c, TravisAudioPlayback tap) {

		mc = new MotorController(c);
		
		behaviors = new HashMap<String, BehaviorController.iBehavior>();

		// initialize map
		behaviors.put("OFF", null);
		behaviors.put("IDLE", new IdleBehavior(mc));
		behaviors.put("TAP", new TapBehavior(mc));
		LookAtPhoneBehavior lapb = new LookAtPhoneBehavior(mc);
		lapb.setOnBehaviorEndedListener(this);
		behaviors.put("LOOKATPHONE",lapb);
		behaviors.put("SWING", new SwingBehavior(mc));
		this.tap = tap;		
	}

	/**
	 * When the behavior controller is starting it has to follow the following:
	 * Stop the current behavior if one was active. start the new behavior that
	 * was sent to it.
	 */
	public void startBehavior(String name, Object data) {

		Log.d("Travis", "Started Behavior " + name);
		if (currentBehavior != null)
			currentBehavior.stop();

		currentBehavior = behaviors.get(name);

		if (currentBehavior != null)
			currentBehavior.start(data);
	}
	
	/**
	 * This is the Interface that each behavior have to implement in order to
	 * work. It has to have these methods
	 */
	public interface iBehavior extends OnSongBeatListener{
		void start(Object data);

		void stop();
	}

	@Override
	public void onBehaviorEnded(String name) {
		// TODO Auto-generated method stub
		if(name.equals("LOOKATPHONE")){
			startBehavior("IDLE", null);
		} else if(name.equals("IDLE")){
			startBehavior("OFF", null);
		}
	}

	public iBehavior getCurrentBehavior() {
		return currentBehavior;
	}
}