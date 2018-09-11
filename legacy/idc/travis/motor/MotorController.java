package idc.travis.motor;

import idc.travis.R;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class MotorController{
	private UsbManager manager;
	private UsbAccessory accessory;
	private ParcelFileDescriptor pfd;
	private boolean pfdOpen = false;
	private FileOutputStream outputStream;
	private Map<String, Motor> mMotors;
	public final byte MOVE_MOTOR = 0;
	private MotorLoader loader;

	/**
	 * Start the Motor Controller object that will control the motors
	 * abstraction. It is also controls the send and receive data from the
	 * Arduino board.
	 * 
	 * The constructor starts with default behavior
	 */
	public MotorController(Context c) {
		connectToArduino(c);

		// Getting all the list of motors
		InputStream is = c.getResources().openRawResource(R.raw.motor_config);
		loader = new MotorLoader(is);
		mMotors = loader.load(this);
	}

	/**
	 * Handles the connection to the ADK
	 */
	private void connectToArduino(Context c) {
		// Connect to the Arduino
		manager = UsbManager.getInstance(c);
		UsbAccessory[] accessoryList = manager.getAccessoryList();
		if (accessoryList == null || accessoryList.length == 0) {
			accessory = null;
		} else {
			accessory = accessoryList[0];
			pfd = manager.openAccessory(accessory);
			if (pfd != null) {
				pfdOpen = true;
				FileDescriptor fd = pfd.getFileDescriptor();
				outputStream = new FileOutputStream(fd);
			}

			new BroadcastReceiver() {
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
						UsbAccessory accessory = (UsbAccessory) intent
								.getParcelableExtra(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
						if (accessory != null && pfdOpen) {
							try {
								pfd.close();
								pfdOpen = false;
							} catch (IOException e) {
							}
						}
					}
				}
			};
		}
	}

	/**
	 * Moves the motor - this is performed by the controller - it sends the
	 * command to the motor. Each motor gets the command separately according to
	 * a set of variables.
	 * Motor name, position, velocity, acceleration(not yet implemented)
	 */
	public void moveMotor(String name, float pos, float vel, float acc) {
		
		mMotors.get(name).move(pos, vel, acc);
	}
	
	public Motor getMotor(String name){
		return mMotors.get((String) name);
	}

	/**
	 * Massage to the ADK to move a Motor
	 */
	private void sendMessage(byte command, byte motor, int position, int velocity) {

//		Log.d("Travis", "Sending ADK Message: " + position + "/" + velocity);
		byte[] buffer = new byte[10];

		buffer[0] = command;
		buffer[1] = motor;
		//byyte array - position/4 the arduino multiplies it by 4
		buffer[2] = (byte) (position <= 255 ? (byte) position : 255);
		buffer[3] = (byte) (position <= 511 ? (byte) Math.max(0,Math.min(255, position - 255)) : 255);
		buffer[4] = (byte) (position <= 767 ? (byte) Math.max(0,Math.min(255, position - 510)) : 255);
		buffer[5] = (byte) (position <= 1023 ? (byte)Math.max(0,Math.min(255, position - 765)) : 255);
		
		//byyte array - speed
		buffer[6] = (byte) (velocity <= 255 ? (byte) velocity : 255);
		buffer[7] = (byte) (velocity <= 511 ? (byte) Math.max(0,Math.min(255, velocity - 255)) : 255);
		buffer[8] = (byte) (velocity <= 767 ? (byte) Math.max(0,Math.min(255, velocity - 510)) : 255);
		buffer[9] = (byte) (velocity <= 1023 ? (byte)Math.max(0,Math.min(255, velocity - 765)) : 255);
		
		if (outputStream != null && buffer[1] != -1) {
			try {
				outputStream.write(buffer);
			} catch (IOException e) {
				Log.e("Motor Controler ", "write failed", e);
			}
		}
	}
	
	public class Motor {

		private String mName;
		private byte mId;
		private int mZeroPos;
		private float mMinPos;
		private float mMaxPos;
		private int mMaxVel;

		/**
		 * This is the motor constructor it does not deal with any types of
		 * errors so the data must be correct and flawless
		 */
		public Motor(Map<String, String> motorMap) {
			mName = motorMap.get(MotorLoader.MOTOR_NAME);
			mId = Byte.parseByte(motorMap.get(MotorLoader.MOTOR_ID));
			mZeroPos = Integer.parseInt(motorMap.get(MotorLoader.MOTOR_ZERO_POS));
			mMinPos = Float.parseFloat(motorMap.get(MotorLoader.MOTOR_MIN_POS));
			mMaxPos = Float.parseFloat(motorMap.get(MotorLoader.MOTOR_MAX_POS));
			mMaxVel = Integer.parseInt(motorMap.get(MotorLoader.MOTOR_MAX_VEL));
		}

		public float getCenter() {
			return mMaxPos - mMinPos;
		}

		/**
		 * Move the motor after setting the correct values
		 */
		public void move(float pos, float vel, float acc) {
			// calc the destination (divide by 4 due to the packets sent to the
			// ADK) from rad to MX-28 units location
			int destination = (int) (Math.ceil(this.mZeroPos + pos * 180 / Math.PI / 0.088));
			// calc the velocity from Rad/s to RPM
			float velocity = (float) (60f * vel / 2f / Math.PI);
			velocity = (float) Math.floor(velocity / 0.053f);

			// send data to ADK
			MotorController.this.sendMessage(MotorController.this.MOVE_MOTOR, mId,
					this.checkDest((int)destination) / 4, this.checkVel((int)velocity));
		}

		/**
		 * Returns the name
		 */
		public String getName() {
			return mName;
		}
		
		public float GetMaxPos()
		{
			return mMaxPos;
		}
		
		public float GetMinPos()
		{
			return mMinPos;
		}

		public float GetMaxVel()
		{
			return mMaxVel;
		}
		
		/**
		 * Sets the position of the motor in the abstract motor representation
		 */
		private int checkDest(int position) {
			if (position >= mZeroPos + mMaxPos * 180 / Math.PI / 0.088) {
				return (int) (mZeroPos + mMaxPos * 180 / Math.PI / 0.088);
			} else if (position <= mZeroPos + mMinPos * 180 / Math.PI / 0.088) {
				return (int) (mZeroPos + mMinPos * 180 / Math.PI / 0.088);
			} else {
				return position;
			}
		}

		/**
		 * Test and set the correct velocity
		 */
		private int checkVel(float velocity) {
			if (velocity >= mMaxVel) {
				return (int) (mMaxVel);
			} else if (velocity <= 1) {
				return 1;
			} else {
				return (int) velocity;
			}
		}
	}
}