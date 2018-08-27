package mr1.research.music;

import android.media.MediaPlayer;

public abstract class BeatPlayer {
    public static final int TYPE_BEAT = 0;
    public static final int TYPE_SEGMENT = 1;
    protected Runnable beatRunnable;
    protected Thread beatThread;
    protected iOnBeatListener listener;
    protected MediaPlayer mp;
    protected volatile boolean stop = false;

    public interface iOnBeatListener {
        void onSongEvent(int i, Object obj);
    }

    public void setBeatListener(iOnBeatListener l) {
        this.listener = l;
    }

    public void start(MediaPlayer mp) {
        this.mp = mp;
        this.stop = false;
        this.beatThread.start();
    }

    public void stop() {
        this.stop = true;
        this.beatThread.interrupt();
        this.beatThread = null;
    }
}
