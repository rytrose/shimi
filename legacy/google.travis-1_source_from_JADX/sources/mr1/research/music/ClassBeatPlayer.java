package mr1.research.music;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class ClassBeatPlayer extends BeatPlayer {
    protected static final String TAG = "ClassBeatPlayer";
    /* renamed from: D */
    private boolean f15D;
    private List<Long> biases;
    private int chosenClass;
    private int duration;
    private List<Long> tempos;

    /* renamed from: mr1.research.music.ClassBeatPlayer$1 */
    class C00251 implements Runnable {
        C00251() {
        }

        public void run() {
            long startTime = System.currentTimeMillis();
            long tempTime = ((Long) ClassBeatPlayer.this.tempos.get(ClassBeatPlayer.this.chosenClass)).longValue();
            long bias = ((Long) ClassBeatPlayer.this.biases.get(ClassBeatPlayer.this.chosenClass)).longValue();
            do {
            } while (System.currentTimeMillis() - startTime < bias);
            int beat = 1;
            while (System.currentTimeMillis() - startTime < ((long) ClassBeatPlayer.this.duration) && !Thread.interrupted() && !ClassBeatPlayer.this.stop) {
                do {
                } while (System.currentTimeMillis() - startTime < (((long) beat) * tempTime) + bias);
                if (ClassBeatPlayer.this.listener != null) {
                    Log.i("SendBeat", "now");
                    Log.i("SendBeat", new StringBuilder(String.valueOf(tempTime + bias)).toString());
                    Log.i("SendBeat", new StringBuilder(String.valueOf(bias)).toString());
                    ClassBeatPlayer.this.listener.onSongEvent(0, Long.valueOf(tempTime));
                }
                beat++;
            }
            if (ClassBeatPlayer.this.f15D) {
                Log.d(ClassBeatPlayer.TAG, "Exiting ClassBeatPlayer thread " + toString());
            }
        }
    }

    public ClassBeatPlayer() {
        this.f15D = true;
        this.tempos = new ArrayList();
        this.biases = new ArrayList();
        this.beatRunnable = new C00251();
    }

    public void addClass(long tempo, long bias) {
        this.tempos.add(Long.valueOf(tempo));
        this.biases.add(Long.valueOf(bias));
    }

    public void choose(int c, int d) {
        this.chosenClass = c;
        this.beatThread = new Thread(this.beatRunnable);
        this.beatThread.setPriority(10);
        this.duration = d;
    }
}
