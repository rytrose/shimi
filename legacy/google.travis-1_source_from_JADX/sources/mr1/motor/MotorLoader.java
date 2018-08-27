package mr1.motor;

import android.util.Log;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParserFactory;
import mr1.motor.MotorController.Motor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MotorLoader extends DefaultHandler {
    public static final String MOTOR_ID = "id";
    public static final String MOTOR_MAX_POS = "maxPos";
    public static final String MOTOR_MAX_VEL = "maxVel";
    public static final String MOTOR_MIN_POS = "minPos";
    public static final String MOTOR_NAME = "name";
    public static final String MOTOR_ZERO_POS = "zero";
    private static final String TAG = "MotorLoader";
    private Map<String, String> motor;
    private List<Map<String, String>> motorMaps = new ArrayList();

    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
        Log.d(TAG, "startElement " + qName);
        if (qName.equalsIgnoreCase("Motor")) {
            this.motor = new HashMap();
            this.motor.put(MOTOR_NAME, attr.getValue(MOTOR_NAME));
            this.motor.put(MOTOR_ID, attr.getValue(MOTOR_ID));
            this.motor.put(MOTOR_MAX_POS, attr.getValue(MOTOR_MAX_POS));
            this.motor.put(MOTOR_MIN_POS, attr.getValue(MOTOR_MIN_POS));
            this.motor.put(MOTOR_MAX_VEL, attr.getValue(MOTOR_MAX_VEL));
            this.motor.put(MOTOR_ZERO_POS, attr.getValue(MOTOR_ZERO_POS));
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        Log.d(TAG, "endElement " + qName);
        if (qName.equalsIgnoreCase("Motor")) {
            this.motorMaps.add(this.motor);
        }
    }

    public Map<String, Motor> load(MotorController mc, String motorConfigFile) {
        InputStream is = MotorLoader.class.getResourceAsStream(motorConfigFile);
        if (is == null) {
            Log.e(TAG, "Couldn't open resource " + motorConfigFile);
        }
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(is, this);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't parse motor config file: " + e.getMessage());
        }
        Map<String, Motor> motorList = new HashMap();
        for (Map<String, String> motorMap : this.motorMaps) {
            Motor m = mc.createMotor(motorMap);
            motorList.put(m.getName(), m);
        }
        return motorList;
    }
}
