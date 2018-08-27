package mr1.research.music;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileBeatPlayer extends BeatPlayer {
    private static final String TAG = "FileBeatPlayer";
    /* renamed from: D */
    private boolean f17D = true;
    private Runnable beatRunnable = new C00271();
    private List<Long> beats;
    private long startPos;

    /* renamed from: mr1.research.music.FileBeatPlayer$1 */
    class C00271 implements Runnable {
        C00271() {
        }

        public void run() {
            long startTime = System.currentTimeMillis();
            for (int i = 1; i < FileBeatPlayer.this.beats.size(); i++) {
                if (((Long) FileBeatPlayer.this.beats.get(i)).longValue() > FileBeatPlayer.this.startPos) {
                    long beatTimeEnd;
                    long beatTimeStart = ((Long) FileBeatPlayer.this.beats.get(i)).longValue();
                    if (i == FileBeatPlayer.this.beats.size() - 1) {
                        beatTimeEnd = 2 * beatTimeStart;
                    } else {
                        beatTimeEnd = ((Long) FileBeatPlayer.this.beats.get(i + 1)).longValue();
                    }
                    long beatTimeToSend = beatTimeEnd - beatTimeStart;
                    if (Thread.interrupted()) {
                        break;
                    }
                    long beatTime = ((Long) FileBeatPlayer.this.beats.get(i)).longValue();
                    do {
                    } while (System.currentTimeMillis() - startTime < beatTime);
                    if (FileBeatPlayer.this.listener != null) {
                        Log.d(FileBeatPlayer.TAG, "Beat @ " + beatTime);
                        FileBeatPlayer.this.listener.onSongEvent(0, Long.valueOf(beatTimeToSend));
                    }
                }
            }
            if (FileBeatPlayer.this.f17D) {
                Log.d(FileBeatPlayer.TAG, "Exiting FileBeatPlayer thread " + toString());
            }
        }
    }

    public void load(String beatFile, long bias, long startPos) {
        this.startPos = startPos;
        if (this.f17D) {
            Log.d(TAG, "Loading beat file " + beatFile + " with bias " + bias);
        }
        this.beatThread = new Thread(this.beatRunnable);
        this.beatThread.setPriority(10);
        try {
            this.beats = new ArrayList();
            BufferedReader br = new BufferedReader(new FileReader(new File(beatFile)));
            while (true) {
                String line = br.readLine();
                if (!line.equals("stop")) {
                    this.beats.add(Long.valueOf(Float.valueOf(line).longValue() + bias));
                } else {
                    return;
                }
            }
        } catch (IOException e) {
        }
    }
}
