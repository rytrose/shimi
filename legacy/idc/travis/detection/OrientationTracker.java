package idc.travis.detection;

import android.hardware.*;

public class OrientationTracker implements SensorEventListener {

	// Calculating orientation
	private float mAccelerometerValues[] = new float[3];
	private float mMagneticValues[] = new float[3];
	private float rotationMatrix[] = new float[16];
	private float mOrientationValues[] = new float[3];

	public static double getAngleDiff(double ang1, double ang2) {
		double result = ang1 - ang2;
		if (result >= Math.PI)
			result -= 2 * Math.PI;
		else if (result <= -Math.PI)
			result += 2 * Math.PI;

		return result;
	}

	public float[] getOrientationValues() {

		SensorManager.getRotationMatrix(rotationMatrix, null,
				mAccelerometerValues, mMagneticValues);
		SensorManager.getOrientation(rotationMatrix, mOrientationValues);

		// Prevent skip in o[0]
		// mOrientationValues[0] = (float) ((mOrientationValues[0] + 2 *
		// Math.PI) % (2 * Math.PI));

		return mOrientationValues;

	}

	public float[] getmAccelerometerValues() {
		return mAccelerometerValues;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				for (int i = 0; i < 3; i++) {
					mAccelerometerValues[i] = event.values[i];
					if (listener != null)
						listener.onSensorsChanged();

				}
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				for (int i = 0; i < 3; i++) {
					mMagneticValues[i] = event.values[i];
				}
				break;
			default:
				break;
			}
		}
	}

	private OnSensorsChangedListener listener;

	public interface OnSensorsChangedListener {
		public void onSensorsChanged();
	}

	public void setOnSensorsChangedListener(OnSensorsChangedListener l) {
		listener = l;
	}

	public void unsetOnSensorsChangedListener() {
		listener = null;
	}

}
