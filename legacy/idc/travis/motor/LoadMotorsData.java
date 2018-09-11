package idc.travis.motor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class LoadMotorsData extends DefaultHandler {

	private HashMap<String, ArrayList<HashMap<String, String>>> mTravisBehavior = new HashMap<String, ArrayList<HashMap<String, String>>>();
	private HashMap<String, String> mTempMotor;
	private ArrayList<HashMap<String, String>> motors;
	public static final String TAP_BEHAVIOR = "tap";
	public static final String IDLE_BEHAVIOR = "idle";
	public static final String HEADBANG_BEHAVIOR = "headbang";

	// Constant keys for the map
	public static final String MOTOR_NAME = "Name";
	public static final String MOTOR_ID = "ID";
	public static final String MOTOR_MIN_VAL = "MIN";
	public static final String MOTOR_MAX_VAL = "MAX";
	public static final String MOTOR_ZERO_POS_VAL = "ZEROPOS";

	private String behaviorName = "";

	public LoadMotorsData(InputStream is) {
		SAXParserFactory sf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = sf.newSAXParser();
			sp.parse(is, this);

		} catch (Exception e) {
			Log.e("Error", e.getMessage());
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attr) throws SAXException {
		if (qName.equalsIgnoreCase("Behavior")) {
			motors = new ArrayList<HashMap<String, String>>();
			mTravisBehavior.put(attr.getValue("name"), motors);
			behaviorName = attr.getValue("name");
			System.out.println(behaviorName);
		}
		if (qName.equalsIgnoreCase("Motor")) {
			mTempMotor = new HashMap<String, String>();
			mTempMotor.put(MOTOR_NAME, attr.getValue("name"));
			mTempMotor.put(MOTOR_ID, attr.getValue("id"));
			mTempMotor.put(MOTOR_MAX_VAL, attr.getValue("maxVal"));
			mTempMotor.put(MOTOR_MIN_VAL, attr.getValue("minVal"));
			mTempMotor.put(MOTOR_ZERO_POS_VAL, attr.getValue("ZeroPosition"));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equalsIgnoreCase("Behavior")) {
			mTravisBehavior.put(behaviorName, motors);
		}
		if (qName.equalsIgnoreCase("Motor")) {
			motors.add(mTempMotor);
		}
	}

	public ArrayList<HashMap<String, String>> getMotors(String behavior) {
		if (isNameValide(behavior)) {
			return mTravisBehavior.get(behavior);
		}
		return null;
	}

	private boolean isNameValide(String behavior) {
		boolean retVal = false;
		if (behavior.equals(HEADBANG_BEHAVIOR) || behavior.equals(TAP_BEHAVIOR)
				|| behavior.equals(IDLE_BEHAVIOR)) {
			retVal = true;
		}
		return retVal;
	}
}
