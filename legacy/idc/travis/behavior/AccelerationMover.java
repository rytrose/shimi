package idc.travis.behavior;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

class AccelerationMover {

	public interface iMoveListener {
		public void setVelocity(String motorName, float goal, float vel);
	}

	private long startTime;
	private final long VEL_TIMER = 200;
	private Handler handler;
	private Runnable animator;
	protected float period;
	protected String name;
	private boolean D = false;
	
	AccelerationMover(final iMoveListener motor, final String motorName, final float goal, final float maxVel) {
		
		this.name = motorName + "->" + goal + "(" + this + ")";

		handler = new Handler();
		
		animator = new Runnable() {
			@Override
			public void run() {

				long m = SystemClock.uptimeMillis();
				float vel;

				if (m-startTime > period * 1.3)
				{
					stop();
					return;
				}
				// This happens every 100ms - manage acceleration / deceleration
				// how far are we from the edge of the period
				// posCurve is the distance from the center of the movement 
				// - so will be between 0 and 0.5 * period
				float posInCurve = Math.abs(period / 2 - (m - startTime));

				// Velocity is (1-posInCurve) normalized to 0-1
				vel = (float) (maxVel * (2 * (1.0 - posInCurve / AccelerationMover.this.period) - 1));

				motor.setVelocity(motorName, goal, vel);
				
				handler.postDelayed(this, VEL_TIMER);
			}
		};
	}
	
	public void start(long msDelay) {
		if (D) Log.d("AccelerationMover", "Started " + name); 
		this.startTime = SystemClock.uptimeMillis();
		period = msDelay;
		handler.post(animator);
	}
	public void stop(){
		if (D) Log.d("AccelerationMover", "Stopped " + name); 
		handler.removeCallbacks(animator);
	}

	
}
