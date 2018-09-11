package idc.travis.motor;

import idc.travis.motor.MotorController.Motor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MotorLoader extends DefaultHandler {
	/**
	 * This class loads the motor data from motor_config.xml
	 * each motor has its limits, name and id.
	 * 
	 */
	
	
	private Map<String, String> mTempMotor;
	private List<Map<String, String>> motorMaps;

	// Constant keys for the map
	public static final String MOTOR_NAME = "name";
	public static final String MOTOR_ID = "id";
	public static final String MOTOR_MIN_POS = "minPos";
	public static final String MOTOR_MAX_POS = "maxPos";
	public static final String MOTOR_MAX_VEL = "maxVel";
	public static final String MOTOR_ZERO_POS = "zero";


	public MotorLoader(InputStream is) {
		
		motorMaps = new ArrayList<Map<String,String>>();
		
		SAXParserFactory sf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = sf.newSAXParser();
			sp.parse(is, this);

		} catch (Exception e) {
			Log.e("Travis", "MyException: " + e.getClass());
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attr) throws SAXException {

		Log.d("Travis", "StartElement " + qName);

		if (qName.equalsIgnoreCase("Motor")) {
			mTempMotor = new HashMap<String, String>();
			mTempMotor.put(MOTOR_NAME, attr.getValue("name"));
			mTempMotor.put(MOTOR_ID, attr.getValue("id"));
			mTempMotor.put(MOTOR_MAX_POS, attr.getValue("maxPos"));
			mTempMotor.put(MOTOR_MIN_POS, attr.getValue("minPos"));
			mTempMotor.put(MOTOR_MAX_VEL, attr.getValue("maxVel"));
			mTempMotor.put(MOTOR_ZERO_POS, attr.getValue("zero"));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		Log.d("Travis", "EndElement " + qName);

		if (qName.equalsIgnoreCase("Motor")) {
			motorMaps.add(mTempMotor);
		}
	}

	public Map<String, Motor> load(MotorController mc)
	{
		Map<String, Motor> motorList = new HashMap<String,Motor>();
		
		for (Map<String, String> motorMap : motorMaps) {
			Motor m = mc.new Motor(motorMap);
			motorList.put(m.getName(), m);
		}
		return motorList;
	}
}
