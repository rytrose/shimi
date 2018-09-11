package idc.travis.detection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class TravisAudioPlayback {

	public interface OnSongBeatListener {
		public abstract void onSongBeat(long beat);
	}

	class songData {
		public final String fileNumber;
		public final float tempo;
		public final long bias;

		public songData(String fn, float t, long b) {
			this.fileNumber = fn;
			this.tempo = t;
			this.bias = b;
		}
	}

	private static final String TAG = "TravisAudioPlayback";
	private List<songData> songs;
	private songData chosenSong;
	private MediaPlayer chosenSongPlayer;
	private MediaPlayer clickPlayer;
	private ArrayList<Long> beats;
	private List<Long> classTempos;
	private List<Long> classBiases;
	private int classIndex;
	private static final String BASEPATH = "mnt/sdcard/Music/TravisDatabase/";
	static final String BCPATH = "mnt/sdcard/Music/TravisDatabase/BCSongs/";
	private static final String BASEPATHBEATS = "mnt/sdcard/Music/TravisDatabase/Beats/";
	final Activity owner;
	private int duration;
	private Context context;
	private Thread clickThread;
	private boolean playing = false;

	public OnSongBeatListener mOnSongBeatDetectedListener;

	public void setOnSongBeatListener(OnSongBeatListener l) {
		this.mOnSongBeatDetectedListener = l;
	}

	public TravisAudioPlayback(Activity a) {
		owner = a;
		context = a.getApplicationContext();
		songs = Arrays.asList(
				new songData("1", 66f, 0L),
				new songData("2", 70f, 0L),
				new songData("3", 75f, -72L), // sub par
				new songData("4", 83f, -5L),
				new songData("5", 88f, -8L), // sub par
				new songData("6", 93f, 41L),
				new songData("7", 97f, -5L),
				new songData("8", 104f,	-50L),
				new songData("9", 110f, -28L),
				new songData("10", 113f, -80L),
				new songData("11", 117f, -82L), // sub par
				new songData("12", 120f, 0L),
				new songData("13", 125f, -50L),
				new songData("14", 129f, -110L),
				new songData("15", 132f, -5L),
				new songData("16", 135f, -20L),
				new songData("17", 138f, 5L), 
				new songData("18", 143f, -20L), // sub par
				new songData("19", 148f, -12L),
				new songData("20", 153f, 0)
				);

		classTempos = Arrays.asList(Long.valueOf((long) (60000f / 92.1f)), Long	.valueOf((long) (60000f / 138f)));
		classBiases = Arrays.asList(Long.valueOf(450), Long.valueOf(0));

//		clickPlayer = MediaPlayer.create(context, Uri.fromFile(new File(BASEPATH + "click.wav")));
		chosenSongPlayer = MediaPlayer.create(context, Uri
				.fromFile(new File(BASEPATH + "click.wav")));
	}

	public void chooseSongFromIndex(Float i) {
		chosenSongPlayer = MediaPlayer.create(context, Uri.fromFile(new File(BCPATH
				+ String.valueOf((int) i.floatValue()) + ".wav")));
		duration = chosenSongPlayer.getDuration();
		classIndex = (int) i.floatValue();
	}

	public void chooseSongFromTempo(float tempo) {

		float min = Float.MAX_VALUE;
		int songIndex = -1;
		float dif;
		for (int i = 0; i < songs.size(); i += 1) {
			dif = Math.abs(tempo - songs.get(i).tempo);
			if (dif < min) {
				min = dif;
				songIndex = i;
			}
		}
		Log.i("BEAT", "" + songIndex);

		chosenSong = songs.get(songIndex);
		chooseSong(songIndex);
	}

	public void chooseSong(int number) {
		makeBeatsFromFile(number);
		chosenSongPlayer = MediaPlayer.create(context, Uri.fromFile(new File(BASEPATH
				+ songs.get(number).fileNumber + ".wav")));
		if (chosenSongPlayer == null) {
			Toast.makeText(context, "Couldn't creat player from " + (songs.get(number).fileNumber), Toast.LENGTH_SHORT).show();

		}
		
	}

	public void playSong(int mode) {
		if (mode == 1) {
			clickThread = new Thread(new Runnable() {
				public void run() {
//					clickPlayer.start();
					long startTime = System.currentTimeMillis();
					long beatTimeToSend;
					long beatTime;
					long beatTimeStart;
					long beatTimeEnd;
					for (int i = 1; i < beats.size(); i = i + 1) {
						beatTimeStart = beats.get(i).longValue();
						if (i == (beats.size() - 1)) {
							beatTimeEnd = 2 * beatTimeStart;
						} else {
							beatTimeEnd = beats.get(i + 1).longValue();
						}
						beatTimeToSend = beatTimeEnd - beatTimeStart;
						if (!Thread.interrupted()) {
							beatTime = beats.get(i).longValue();
							while (System.currentTimeMillis() - startTime < beatTime); // nothing
							// Send event to the motor
							if (mOnSongBeatDetectedListener != null) {
								mOnSongBeatDetectedListener.onSongBeat(beatTimeToSend);
							}
							//Clock Sound
//							clickPlayer.start();
						} else
							break;
					}
				}
			});
			clickThread.setPriority(Thread.MAX_PRIORITY);
		} else {
			clickThread = new Thread(new Runnable() {
				public void run() {
					long startTime = System.currentTimeMillis();
					long tempTime = classTempos.get(classIndex).longValue();
					long bias = classBiases.get(classIndex).longValue();

					while (System.currentTimeMillis() - startTime < bias) {
						;
					}
//					clickPlayer.start();

					int beat = 1;
					while (System.currentTimeMillis() - startTime < duration) {
						if (!Thread.interrupted()) {
							while (System.currentTimeMillis() - startTime < tempTime * beat + bias)
								; // nothing
							if (mOnSongBeatDetectedListener != null) {
								Log.i("SendBeat","now");
								Log.i("SendBeat",(tempTime+ bias) + "");
								Log.i("SendBeat",(bias) + "");
								mOnSongBeatDetectedListener.onSongBeat((tempTime));
							}
							//Clock Sound
//							clickPlayer.start();
						} else
							break;

						beat += 1;
					}
				}
			});
			clickThread.setPriority(Thread.MAX_PRIORITY);
		}

		if (chosenSongPlayer != null)
		{
			chosenSongPlayer.start();
			clickThread.start();
		} else {
			Toast.makeText(context, "No song to play", Toast.LENGTH_SHORT).show();
		}
	}

	public void stopSong() {
		if (chosenSongPlayer != null && chosenSongPlayer.isPlaying()) {
			chosenSongPlayer.stop();
			clickThread.interrupt();
			chosenSongPlayer.reset();
		}
	}

	void makeBeatsFromFile(int si) {
		Log.i("BEAT", si + "");
		try {
			beats = new ArrayList<Long>();
			BufferedReader br = new BufferedReader(new FileReader(new File(BASEPATHBEATS
					+ String.valueOf(songs.get(si).fileNumber) + ".txt")));
			String line;
			while (!(line = br.readLine()).equals("stop")) {
				Log.d(TAG, "line = (" + line + ")");
				beats.add(Long.valueOf(Float.valueOf(line).longValue() + songs.get(si).bias));
			}
		} catch (IOException e) {
			Log.e(TAG, " makeBeatsFromFile failed miserably with an IOException: " + e.getMessage());
		}
	}

	public boolean isPlaying() {
		return playing;
	}
}
