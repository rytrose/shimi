package idc.travis;

import idc.travis.behavior.BehaviorController;
import idc.travis.detection.BluetoothBeatReceiver;
import idc.travis.detection.TravisAudioPlayback;
import idc.travis.detection.TravisAudioPlayback.OnSongBeatListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;
import org.puredata.core.utils.PdDispatcher;
import org.puredata.core.PdListener;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TravisDLDActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "TravisBeatDetectionActivity";
	private PowerManager.WakeLock mWakeLock;
	private TravisAudioPlayback mAudioPlayback;
	private Boolean mMediaPlayerStopped = true;
	private BehaviorController controller;
	private Boolean waitingForGetTempo = false;
	private PdService pdService = null;
	private float tempo;
	private int mode;
	public ProgressBar progressBar = null;
	public TextView progBarText = null;
	public Handler handler = new Handler();	
	private long songStartDelays[] = {130, 895};
	private boolean holdEverything = false;
	
	public static final int MESSAGE_READ = 2;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		startAudio();
	}
	
	private final PdDispatcher myDispatcher = new PdDispatcher() {
		@Override
		public void print(String s) {
			Log.i("Pd print", s);
		}
	};

	private final PdListener myListener = new PdListener() {

		@Override
		public void receiveMessage(String source, String symbol, Object... args) {
			Log.i("receiveMessage symbol:", symbol);
			for (Object arg : args) {
				Log.i("receiveMessage atom:", arg.toString());
			}
		}

		@Override
		public void receiveList(String source, final Object... args) {
			
			if (holdEverything)
				return;
			
			Log.i("receiveList atoms:", args[0].toString());
			// New Segment start
			if (Float.valueOf(args[0].toString()) == -2.0) {

//				listenForBeat();
				startMoving(null);
			}

			else if (Float.valueOf(args[0].toString()) == -1.0) {

				finishMoving();

			} else {
				if (waitingForGetTempo.booleanValue()) {

					startMoving(args);
				}
			}
		}

		private void listenForBeat() {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// start listening for inputs
					if (!waitingForGetTempo.booleanValue()) {
						waitingForGetTempo = Boolean.TRUE;
					}
					// make an animation to let user know it is working
					progressBar.setVisibility(0);
					//Text for progress bar
					progBarText.setText("Detecting Beat");
					progBarText.setVisibility(0);
				}
			});
		}

		private void startMoving(final Object... args) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					
					holdEverything = true;
					progressBar.setVisibility(4);
					progBarText.setText("Playing Song\nClap To Stop");

					if (args != null)
					{
						tempo = Float.valueOf(args[0].toString());
						Log.i("Tempo",args[0].toString());
					
						try {
							if (tempo < 2) {
								TravisDLDActivity.this.controller.tap.chooseSongFromIndex(Float.valueOf(args[0]
										.toString()));
								Thread.sleep((long) (songStartDelays[(int) tempo]));
								mode = 0;
							} else {
								TravisDLDActivity.this.controller.tap.chooseSongFromTempo(tempo);
								mode = 1;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						tempo = 0f;
					}

					TravisDLDActivity.this.controller.tap.playSong(mode);
					if(tempo == 1f) {
						TravisDLDActivity.this.controller.startBehavior("SWING", null);
						TravisDLDActivity.this.controller.tap.setOnSongBeatListener((OnSongBeatListener) 
								TravisDLDActivity.this.controller.behaviors.get("SWING"));
					} else if (tempo == 0f) {
						TravisDLDActivity.this.controller.startBehavior("TAP", null);
						TravisDLDActivity.this.controller.tap.setOnSongBeatListener((OnSongBeatListener) 
								TravisDLDActivity.this.controller.behaviors.get("TAP"));
					} else {
						TravisDLDActivity.this.controller.startBehavior("TAP", null);
						TravisDLDActivity.this.controller.tap.setOnSongBeatListener((OnSongBeatListener) 
								TravisDLDActivity.this.controller.behaviors.get("TAP"));
					}
					mMediaPlayerStopped = Boolean.FALSE;
				}
			});
			waitingForGetTempo = false;
		}

		@Override
		public void receiveSymbol(String source, String symbol) {
			Log.i("receiveSymbol", symbol);
		}

		@Override
		public void receiveFloat(String source, final float x) {
			Log.i("receiveFloat", String.valueOf(x));
		}

		@Override
		public void receiveBang(String source) {
			Log.i("receiveBang", "bang!");
		}

	};
	
	private final ServiceConnection pdConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			pdService = ((PdService.PdBinder) service).getService();
			initPd();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
		}
	};
	private BluetoothBeatReceiver mBTReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAudioPlayback = new TravisAudioPlayback(this);

		controller = new BehaviorController(this, mAudioPlayback);
		setContentView(R.layout.behavior_choice);

		// Pure Data init
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
		mWakeLock.acquire();
		PdPreferences.initPreferences(getApplicationContext());
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.registerOnSharedPreferenceChangeListener(this);
		bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);

		Button button = (Button)findViewById(R.id.dance);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				controller.tap.chooseSongFromIndex(1f);
				mMediaPlayerStopped = false;
				controller.startBehavior("TAP", null);
				controller.tap.playSong(0);
			}
		});

		((Button)findViewById(R.id.stopButton))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finishMoving();			}
		});

		mBTReceiver = new BluetoothBeatReceiver(mHandler);
		mBTReceiver.start();
		
		//Progress bar
		progBarText = (TextView) findViewById(R.id.textView1);
		setProgressBarVisibility(false);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setVisibility(4);
		progBarText.setText("Clap To Start");
		
		controller.startBehavior("LOOKATPHONE", null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBTReceiver.stop();

		android.os.Process.killProcess(android.os.Process.myPid()); //perhaps to change later - this app won't run in the background
		mWakeLock.release();
		cleanup();
		stopSong();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		stopSong();
	}

	private void stopSong() {
		if (!mMediaPlayerStopped.booleanValue()) {
			controller.tap.stopSong();
			mMediaPlayerStopped = Boolean.TRUE;
			waitingForGetTempo = Boolean.FALSE;
			controller.startBehavior("IDLE", null);
		}
	}
	
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, "Received: ["+readMessage+"]");
                controller.getCurrentBehavior().onSongBeat(500);
                break;
            }
        }
    };

	/**
	 * initiate Pure Data
	 */
	private void initPd() {
		Resources res = getResources();
		File patchFile = null;
		try {
			PdBase.setReceiver(myDispatcher);
			myDispatcher.addListener("tempo", myListener);
			PdBase.subscribe("android");
			File libDir = getFilesDir();
			try {
				IoUtils.extractZipResource(res.openRawResource(R.raw.externals), libDir, true);
			} catch (IOException e) {
				Log.e("Scene Player", e.toString());
			}
			PdBase.addToSearchPath(libDir.getAbsolutePath());
			InputStream in = res.openRawResource(R.raw.androidbeatclassification);
			patchFile = IoUtils.extractResource(in, "androidbeatclassification.pd", getCacheDir());			
			PdBase.openPatch(patchFile);
			startAudio();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			finish();
		} finally {
			if (patchFile != null)
				patchFile.delete();
		}
	}
	
	private void startAudio() {
		String name = getResources().getString(R.string.app_name);
		try {
			pdService.initAudio(-1, -1, -1, -1);// Change the values to default ones
			pdService.startAudio(new Intent(this, TravisDLDActivity.class), R.drawable.icon, name,
					"Return to " + name + ".");
		} catch (IOException e) {
		}
	}
	
	private void cleanup() {
		mAudioPlayback.stopSong();
		PdBase.release();
		try {
			unbindService(pdConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdService = null;
		}
	}

	private void finishMoving() {
		
		holdEverything = false;
		
		// Start new Segment
		if ((!waitingForGetTempo.booleanValue()) && (!mMediaPlayerStopped.booleanValue())) {
			mMediaPlayerStopped = Boolean.FALSE;
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					TravisDLDActivity.this.stopSong();
					progBarText.setText("Clap To Start");
				}
			});
		}
		
	}
}