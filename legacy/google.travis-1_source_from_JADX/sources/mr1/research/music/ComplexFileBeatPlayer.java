package mr1.research.music;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

public class ComplexFileBeatPlayer extends BeatPlayer {
    private static final String TAG = "ComplexFileBeatPlayer";
    /* renamed from: D */
    private boolean f16D = true;
    private Runnable beatRunnable = new C00261();
    private ComplexFile fileData;
    private int nextBeat;
    private Segment nextSegment;

    /* renamed from: mr1.research.music.ComplexFileBeatPlayer$1 */
    class C00261 implements Runnable {
        C00261() {
        }

        public void run() {
            long startTime = System.currentTimeMillis();
            Iterator<Integer> beatIt = ComplexFileBeatPlayer.this.fileData.beats.iterator();
            Iterator<Segment> segIt = ComplexFileBeatPlayer.this.fileData.segments.iterator();
            ComplexFileBeatPlayer.this.nextBeat = ((Integer) beatIt.next()).intValue() + ComplexFileBeatPlayer.this.fileData.bias;
            ComplexFileBeatPlayer.this.nextSegment = (Segment) segIt.next();
            while (true) {
                if (!beatIt.hasNext() && !segIt.hasNext()) {
                    break;
                }
                int songPos = (int) (System.currentTimeMillis() - startTime);
                if (!ComplexFileBeatPlayer.this.stop) {
                    while (!ComplexFileBeatPlayer.this.stop && ((ComplexFileBeatPlayer.this.nextBeat == -1 || ComplexFileBeatPlayer.this.nextBeat > songPos) && (ComplexFileBeatPlayer.this.nextSegment == null || ComplexFileBeatPlayer.this.nextSegment.onset > songPos))) {
                        songPos = ComplexFileBeatPlayer.this.mp.getCurrentPosition();
                    }
                    if (ComplexFileBeatPlayer.this.stop) {
                        break;
                    }
                    if (ComplexFileBeatPlayer.this.nextSegment != null && ComplexFileBeatPlayer.this.nextSegment.onset < songPos) {
                        if (ComplexFileBeatPlayer.this.f16D) {
                            Log.d(ComplexFileBeatPlayer.TAG, "Segment " + ComplexFileBeatPlayer.this.nextSegment.name);
                        }
                        ComplexFileBeatPlayer.this.listener.onSongEvent(1, ComplexFileBeatPlayer.this.nextSegment);
                        if (segIt.hasNext()) {
                            ComplexFileBeatPlayer.this.nextSegment = (Segment) segIt.next();
                        } else {
                            ComplexFileBeatPlayer.this.nextSegment = null;
                        }
                    }
                    if (ComplexFileBeatPlayer.this.nextBeat != -1 && ComplexFileBeatPlayer.this.nextBeat < songPos) {
                        int lastBeat = ComplexFileBeatPlayer.this.nextBeat;
                        if (beatIt.hasNext()) {
                            ComplexFileBeatPlayer.this.nextBeat = ((Integer) beatIt.next()).intValue() + ComplexFileBeatPlayer.this.fileData.bias;
                        } else {
                            ComplexFileBeatPlayer.this.nextBeat = -1;
                        }
                        if (ComplexFileBeatPlayer.this.f16D) {
                            Log.d(ComplexFileBeatPlayer.TAG, "Beat @ " + lastBeat + " Dur: " + (ComplexFileBeatPlayer.this.nextBeat - lastBeat));
                        }
                        ComplexFileBeatPlayer.this.listener.onSongEvent(0, Integer.valueOf(ComplexFileBeatPlayer.this.nextBeat - lastBeat));
                    }
                } else {
                    break;
                }
            }
            if (ComplexFileBeatPlayer.this.f16D) {
                Log.d(ComplexFileBeatPlayer.TAG, "Exiting FileBeatPlayer thread " + toString());
            }
        }
    }

    public static class ComplexFile {
        List<Integer> beats;
        int bias;
        List<Segment> segments;

        public static class Segment {
            float intensity;
            String name;
            int onset;
            private String style;

            public String toString() {
                return String.format("%s (%s) @ %.2f", new Object[]{this.name, getStyle(), Float.valueOf(this.intensity)});
            }

            public String getStyle() {
                return this.style;
            }

            public float getIntensity() {
                return this.intensity;
            }
        }
    }

    public ComplexFileBeatPlayer(Context c, int resource) {
        readJSONFile(c, resource);
        this.beatThread = new Thread(this.beatRunnable);
        this.beatThread.setPriority(10);
    }

    private void readJSONFile(Context c, int resource) {
        String beatFile = c.getResources().getResourceName(resource);
        if (this.f16D) {
            Log.d(TAG, "Loading beat file " + beatFile);
        }
        InputStream is = c.getResources().openRawResource(resource);
        try {
            this.fileData = (ComplexFile) new Gson().fromJson(new BufferedReader(new InputStreamReader(is, "UTF-8")), ComplexFile.class);
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't open  " + beatFile);
            e.printStackTrace();
        }
    }

    public int getNumBeats() {
        return this.fileData.beats.size();
    }

    public int getNumSegments() {
        return this.fileData.segments.size();
    }
}
