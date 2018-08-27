package mr1.research.music;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomBeatPlayer extends BeatPlayer {
    private static final String TAG = "RandomBeatPlayer";
    /* renamed from: D */
    private boolean f18D = true;
    private final int RANGE = 30;
    long beat = 0;
    private Runnable beatRunnable = new C00281();
    private List<Long> beats = null;
    Random rand;
    Random randBool;
    private long songBeat = 0;

    /* renamed from: mr1.research.music.RandomBeatPlayer$1 */
    class C00281 implements Runnable {
        C00281() {
        }

        public void run() {
            System.out.println("Randomizing");
            RandomBeatPlayer.this.rand = new Random();
            RandomBeatPlayer.this.randBool = new Random();
            float calc = 1.0f + (((float) RandomBeatPlayer.this.rand.nextInt(30)) / 100.0f);
            RandomBeatPlayer.this.beat = (long) (((float) RandomBeatPlayer.this.songBeat) * calc);
            long tempTime = System.currentTimeMillis();
            while (tempTime - System.currentTimeMillis() < ((Long) RandomBeatPlayer.this.beats.get(RandomBeatPlayer.this.beats.size() - 1)).longValue() && !Thread.interrupted()) {
                calc = 1.0f + (((float) RandomBeatPlayer.this.rand.nextInt(30)) / 100.0f);
                RandomBeatPlayer.this.beat = (long) (((float) RandomBeatPlayer.this.songBeat) * calc);
                do {
                } while (System.currentTimeMillis() - System.currentTimeMillis() < RandomBeatPlayer.this.beat);
                long startTime = System.currentTimeMillis();
                if (((double) RandomBeatPlayer.this.randBool.nextFloat()) <= 0.9d && RandomBeatPlayer.this.listener != null) {
                    RandomBeatPlayer.this.listener.onSongEvent(0, Long.valueOf(RandomBeatPlayer.this.beat));
                }
            }
        }
    }

    public void load(String beatFile, long bias) {
        if (this.f18D) {
            Log.d(TAG, "Loading beat file " + beatFile);
        }
        try {
            this.beats = new ArrayList();
            BufferedReader br = new BufferedReader(new FileReader(new File(beatFile)));
            while (true) {
                String line = br.readLine();
                if (line.equals("stop")) {
                    break;
                }
                this.beats.add(Long.valueOf(Float.valueOf(line).longValue() + bias));
            }
        } catch (IOException e) {
        }
        long avarage = 0;
        for (int i = 0; i < this.beats.size() - 2; i++) {
            avarage += ((Long) this.beats.get(i + 1)).longValue() - ((Long) this.beats.get(i)).longValue();
        }
        this.songBeat = avarage / ((long) this.beats.size());
        this.beatThread = new Thread(this.beatRunnable);
        this.beatThread.setPriority(10);
    }
}
