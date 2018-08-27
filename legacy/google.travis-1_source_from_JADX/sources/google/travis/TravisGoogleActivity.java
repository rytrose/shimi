package google.travis;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import mr1.core.network.util.MulticastListener;
import mr1.motor.MotorController;
import mr1.motor.specific.ADKMX28MotorController;
import mr1.robots.travis.TravisBehaviorController;
import mr1.robots.travis.moves.Curve;
import mr1.robots.travis.moves.Gesture;
import mr1.robots.travis.moves.GestureController;
import mr1.robots.travis.moves.Move;
import mr1.robots.travis.moves.ThreadScheduler;
import mr1.robots.travis.moves.sounds.SoundGesture;
import mr1.robots.travis.moves.sounds.SoundMove;
import mr1.robots.travis.music.TravisAudioPlayback;

public class TravisGoogleActivity extends Activity implements ThreadScheduler {
    private volatile float beatDuration = 400.0f;
    private volatile List<Float> beats;
    private volatile int beatsPerMeasure = 4;
    private TravisBehaviorController controller;
    private volatile Gesture gesture;
    private volatile GestureController gestureController;
    private String ipString;
    private volatile boolean listening = false;
    private TravisAudioPlayback mAudioPlayback;
    private WakeLock mWakeLock;
    private volatile MotorController mc;
    private volatile float measureDuration = (this.beatDuration * ((float) this.beatsPerMeasure));
    private volatile Move move;
    private volatile Map<Float, List<Curve>> moveCommands = new HashMap();
    private Runnable playThread;
    public volatile TextView progBarText = null;
    public ProgressBar progressBar = null;
    private volatile String response;
    private volatile Map<Float, List<SoundCommand>> soundCommands = new HashMap();
    private volatile SoundGesture soundGesture;
    private volatile SoundMove soundMove;
    private volatile Sounds sounds;
    private volatile long startTime = System.currentTimeMillis();
    private volatile Map<Float, List<Command>> stringCommands = new HashMap();
    private volatile int uniqueID;

    /* renamed from: google.travis.TravisGoogleActivity$1 */
    class C00131 implements OnClickListener {
        C00131() {
        }

        public void onClick(View v) {
            TravisGoogleActivity.this.gestureController.gestureScene.swoop(2.8f);
        }
    }

    /* renamed from: google.travis.TravisGoogleActivity$2 */
    class C00142 implements OnClickListener {
        C00142() {
        }

        public void onClick(View v) {
            TravisGoogleActivity.this.play();
        }
    }

    /* renamed from: google.travis.TravisGoogleActivity$3 */
    class C00153 implements Runnable {
        int count = 0;
        Thread cur = Thread.currentThread();
        long currentTime;
        float delay;
        List<Curve> tempMoveList;
        List<SoundCommand> tempSoundList;
        List<Command> tempStringList;
        float time;

        C00153() {
            this.delay = ((Float) TravisGoogleActivity.this.beats.get(0)).floatValue() * TravisGoogleActivity.this.measureDuration;
            this.currentTime = TravisGoogleActivity.this.startTime;
            this.tempStringList = new ArrayList();
            this.tempMoveList = new ArrayList();
            this.tempSoundList = new ArrayList();
        }

        public void run() {
            this.cur.setPriority(10);
            if (TravisGoogleActivity.this.soundCommands.containsKey(TravisGoogleActivity.this.beats.get(this.count))) {
                this.tempSoundList = (List) TravisGoogleActivity.this.soundCommands.get(TravisGoogleActivity.this.beats.get(this.count));
                TravisGoogleActivity.this.sounds.triggerSound(this.tempSoundList);
            }
            if (TravisGoogleActivity.this.stringCommands.containsKey(TravisGoogleActivity.this.beats.get(this.count))) {
                this.tempStringList = (List) TravisGoogleActivity.this.stringCommands.get(TravisGoogleActivity.this.beats.get(this.count));
                for (int i = 0; i < this.tempStringList.size(); i++) {
                    this.currentTime = System.currentTimeMillis();
                    this.time = (float) ((((long) ((((Command) this.tempStringList.get(i)).time.floatValue() * TravisGoogleActivity.this.beatDuration) + (TravisGoogleActivity.this.measureDuration * ((Float) TravisGoogleActivity.this.beats.get(this.count)).floatValue()))) + TravisGoogleActivity.this.startTime) - this.currentTime);
                    this.time /= TravisGoogleActivity.this.beatDuration;
                    System.out.println("time + ************" + this.time);
                    TravisGoogleActivity.this.gestureController.doFunction(((Command) this.tempStringList.get(i)).behavior, this.time);
                }
            }
            if (TravisGoogleActivity.this.moveCommands.containsKey(TravisGoogleActivity.this.beats.get(this.count))) {
                this.tempMoveList = (List) TravisGoogleActivity.this.moveCommands.get(TravisGoogleActivity.this.beats.get(this.count));
                TravisGoogleActivity.this.gestureController.gesture.move(this.tempMoveList);
            }
            this.count++;
            this.currentTime = System.currentTimeMillis();
            if (this.count < TravisGoogleActivity.this.beats.size()) {
                this.delay = (float) ((((long) (((Float) TravisGoogleActivity.this.beats.get(this.count)).floatValue() * TravisGoogleActivity.this.measureDuration)) + TravisGoogleActivity.this.startTime) - this.currentTime);
                TravisGoogleActivity.handler.postDelayed(TravisGoogleActivity.this.playThread, (long) ((int) this.delay));
            }
        }
    }

    /* renamed from: google.travis.TravisGoogleActivity$4 */
    class C00184 extends Thread {

        /* renamed from: google.travis.TravisGoogleActivity$4$1 */
        class C00161 implements Runnable {
            C00161() {
            }

            public void run() {
                TravisGoogleActivity.this.play();
            }
        }

        /* renamed from: google.travis.TravisGoogleActivity$4$2 */
        class C00172 implements Runnable {
            C00172() {
            }

            public void run() {
                TravisGoogleActivity.this.progBarText.setText("my uniqueID is " + TravisGoogleActivity.this.uniqueID);
            }
        }

        C00184() {
        }

        public void run() {
            TravisGoogleActivity.this.ipString = TravisGoogleActivity.this.getIPAddress();
            System.out.println("ip string = " + TravisGoogleActivity.this.ipString);
            MulticastListener networkListener = new MulticastListener("224.0.80.8", 34565);
            networkListener.setIpString(TravisGoogleActivity.this.ipString);
            TravisGoogleActivity.this.uniqueID = networkListener.uniqueID;
            networkListener.listen();
            do {
            } while (!networkListener.listeningOnNetwork);
            TravisGoogleActivity.this.listening = true;
            Looper.prepare();
            while (TravisGoogleActivity.this.listening) {
                TravisGoogleActivity.this.response = networkListener.update();
                if (!TravisGoogleActivity.this.response.equals("")) {
                    System.out.println("received message1 = " + TravisGoogleActivity.this.response);
                    TravisGoogleActivity.this.response.trim().equals("TAP");
                    if (TravisGoogleActivity.this.response.trim().equals("IDLE")) {
                        TravisGoogleActivity.this.controller.startBehavior("IDLE", null);
                    }
                    if (TravisGoogleActivity.this.response.trim().equals("NOD")) {
                        TravisGoogleActivity.this.controller.startBehavior("NOD", null);
                    }
                    if (TravisGoogleActivity.this.response.trim().equals("WHOA")) {
                        TravisGoogleActivity.this.controller.startBehavior("WHOA", null);
                    }
                    if (TravisGoogleActivity.this.response.trim().equals("LOOKATPHONE")) {
                        TravisGoogleActivity.this.controller.startBehavior("LOOKATPHONE", null);
                    }
                    TravisGoogleActivity.this.response.trim().equals("HEAD");
                    if (TravisGoogleActivity.this.response.trim().equals("PLAY")) {
                        TravisGoogleActivity.this.runOnUiThread(new C00161());
                    }
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TravisGoogleActivity.this.runOnUiThread(new C00172());
            }
            networkListener.closeConnection();
        }
    }

    class Command {
        String behavior;
        Float position;
        Float time;
        Float velocity;

        public Command(String behavior, Float time) {
            this.behavior = behavior;
            this.time = time;
            this.position = null;
            this.velocity = null;
        }

        public Command(String behavior, Float time, float position, float velocity) {
            this.behavior = behavior;
            this.time = time;
            this.velocity = Float.valueOf(velocity);
            this.position = Float.valueOf(position);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0012R.layout.behavior_choice);
        loadInstructions();
        this.gesture = new Gesture(this, this.beatDuration);
        this.move = new Move(this, this.gesture.getMotorController());
        this.gesture.setAlternateMove(this.move);
        this.gestureController = new GestureController(this, this.beatDuration, this.gesture);
        this.gestureController.gesture.homeDown();
        this.sounds = new Sounds(this, this.beatDuration);
        this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(26, "My Tag");
        this.mWakeLock.acquire();
        ((Button) findViewById(C0012R.id.dance)).setOnClickListener(new C00131());
        ((Button) findViewById(C0012R.id.stopButton)).setOnClickListener(new C00142());
        this.progBarText = (TextView) findViewById(C0012R.id.textView1);
        setProgressBarVisibility(false);
        this.progressBar = (ProgressBar) findViewById(C0012R.id.progressBar1);
        this.progressBar.setVisibility(4);
        this.progBarText.setText("push to home");
        listenOnNetwork();
        this.mc = new ADKMX28MotorController(this, "/mr1/robots/travis/motor_config.xml");
    }

    protected void onDestroy() {
        this.listening = false;
        super.onDestroy();
        Process.killProcess(Process.myPid());
        this.mWakeLock.release();
        cleanup();
    }

    protected void onPause() {
        super.onPause();
    }

    private void cleanup() {
        this.mAudioPlayback.stop();
    }

    private ArrayList GetUniqueValues(Collection values) {
        return new ArrayList(new HashSet(values));
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void play() {
        handler.removeCallbacks(this.playThread);
        this.startTime = System.currentTimeMillis();
        this.playThread = new C00153();
        handler.postDelayed(this.playThread, (long) ((int) (((Float) this.beats.get(0)).floatValue() * this.measureDuration)));
    }

    private void loadInstructions() {
        String beatFile = "/sdcard/Music/GoogleIO/2.csv";
        try {
            this.beats = new ArrayList();
            BufferedReader br = new BufferedReader(new FileReader(new File(beatFile)));
            List<Command> tempStringList = new ArrayList();
            List<Curve> tempMoveList = new ArrayList();
            float beatOffset = 0.0f;
            List<SoundCommand> tempSoundList = new ArrayList();
            while (true) {
                String line = br.readLine();
                if (line.contains("stop;")) {
                    break;
                }
                String[] values = line.split(";");
                if (values.length == 1) {
                    beatOffset = Float.valueOf(values[0].trim()).floatValue();
                } else {
                    this.beats.add(Float.valueOf(Float.valueOf(values[0].trim()).floatValue() + beatOffset));
                    if (values.length != 3 || values[1].contains("sound")) {
                        if (values.length > 3 && !values[1].contains("sound")) {
                            if (this.moveCommands.containsKey(this.beats.get(this.beats.size() - 1))) {
                                tempMoveList = (List) this.moveCommands.get(this.beats.get(this.beats.size() - 1));
                                tempMoveList.add(new Curve(values[1], Float.valueOf(values[3]).floatValue(), Float.valueOf(values[2]).floatValue()));
                                this.moveCommands.put((Float) this.beats.get(this.beats.size() - 1), tempMoveList);
                            } else {
                                tempMoveList = new ArrayList();
                                tempMoveList.add(new Curve(values[1], Float.valueOf(values[3]).floatValue(), Float.valueOf(values[2]).floatValue()));
                                this.moveCommands.put((Float) this.beats.get(this.beats.size() - 1), tempMoveList);
                            }
                        }
                    } else if (this.stringCommands.containsKey(this.beats.get(this.beats.size() - 1))) {
                        tempStringList = (List) this.stringCommands.get(this.beats.get(this.beats.size() - 1));
                        tempStringList.add(new Command(values[1], Float.valueOf(values[2])));
                        this.stringCommands.put((Float) this.beats.get(this.beats.size() - 1), tempStringList);
                    } else {
                        tempStringList = new ArrayList();
                        tempStringList.add(new Command(values[1], Float.valueOf(values[2])));
                        this.stringCommands.put((Float) this.beats.get(this.beats.size() - 1), tempStringList);
                    }
                    if (values[1].contains("sound")) {
                        if (this.soundCommands.containsKey(this.beats.get(this.beats.size() - 1))) {
                            tempSoundList = (List) this.soundCommands.get(this.beats.get(this.beats.size() - 1));
                            if (values.length > 3) {
                                tempSoundList.add(new SoundCommand(values[1], values[2], values[3]));
                            } else {
                                tempSoundList.add(new SoundCommand(values[1], values[2]));
                            }
                        } else {
                            tempSoundList = new ArrayList();
                            if (values.length > 3) {
                                tempSoundList.add(new SoundCommand(values[1], values[2], values[3]));
                            } else {
                                tempSoundList.add(new SoundCommand(values[1], values[2]));
                            }
                        }
                        this.soundCommands.put((Float) this.beats.get(this.beats.size() - 1), tempSoundList);
                    }
                }
            }
        } catch (IOException e) {
        }
        this.beats = GetUniqueValues(this.beats);
        Collections.sort(this.beats);
        System.out.println("stop here");
    }

    private String getIPAddress() {
        int ip = ((WifiManager) getSystemService("wifi")).getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d", new Object[]{Integer.valueOf(ip & 255), Integer.valueOf((ip >> 8) & 255), Integer.valueOf((ip >> 16) & 255), Integer.valueOf((ip >> 24) & 255)});
    }

    public void listenOnNetwork() {
        new C00184().start();
    }
}
