package org.puredata.android.io;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.util.Arrays;
import org.puredata.core.PdBase;

public class PdAudio {
    private static AudioWrapper audioWrapper = null;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Runnable pollRunner = new C00821();

    /* renamed from: org.puredata.android.io.PdAudio$1 */
    class C00821 implements Runnable {
        C00821() {
        }

        public void run() {
            PdBase.pollMidiQueue();
            PdBase.pollPdMessageQueue();
            PdAudio.handler.postDelayed(this, 5);
        }
    }

    /* renamed from: org.puredata.android.io.PdAudio$3 */
    class C00833 implements Runnable {
        C00833() {
        }

        public void run() {
            PdBase.pollMidiQueue();
            PdBase.pollPdMessageQueue();
        }
    }

    /* renamed from: org.puredata.android.io.PdAudio$2 */
    class C00922 extends AudioWrapper {
        private final /* synthetic */ int val$ticksPerBuffer;

        C00922(int $anonymous0, int $anonymous1, int $anonymous2, int $anonymous3, int i) throws IOException {
            this.val$ticksPerBuffer = i;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3);
        }

        protected int process(short[] inBuffer, short[] outBuffer) {
            Arrays.fill(outBuffer, (short) 0);
            int err = PdBase.process(this.val$ticksPerBuffer, inBuffer, outBuffer);
            PdBase.pollMidiQueue();
            PdBase.pollPdMessageQueue();
            return err;
        }
    }

    private PdAudio() {
    }

    public static synchronized void initAudio(int sampleRate, int inChannels, int outChannels, int ticksPerBuffer, boolean restart) throws IOException {
        synchronized (PdAudio.class) {
            if (!isRunning() || restart) {
                stopAudio();
                if (PdBase.openAudio(inChannels, outChannels, sampleRate, null) != 0) {
                    throw new IOException("unable to open Pd audio: " + sampleRate + ", " + inChannels + ", " + outChannels);
                } else if (!PdBase.implementsAudio()) {
                    if (!AudioParameters.checkParameters(sampleRate, inChannels, outChannels) || ticksPerBuffer <= 0) {
                        throw new IOException("bad Java audio parameters: " + sampleRate + ", " + inChannels + ", " + outChannels + ", " + ticksPerBuffer);
                    }
                    audioWrapper = new C00922(sampleRate, inChannels, outChannels, ticksPerBuffer * PdBase.blockSize(), ticksPerBuffer);
                }
            }
        }
    }

    public static synchronized void startAudio(Context context) {
        synchronized (PdAudio.class) {
            PdBase.computeAudio(true);
            if (PdBase.implementsAudio()) {
                handler.post(pollRunner);
                PdBase.startAudio();
            } else if (audioWrapper == null) {
                throw new IllegalStateException("audio not initialized");
            } else {
                audioWrapper.start(context);
            }
        }
    }

    public static synchronized void stopAudio() {
        synchronized (PdAudio.class) {
            if (PdBase.implementsAudio()) {
                PdBase.pauseAudio();
                handler.removeCallbacks(pollRunner);
                handler.post(new C00833());
            } else if (isRunning()) {
                audioWrapper.stop();
            }
        }
    }

    public static synchronized boolean isRunning() {
        boolean isRunning;
        synchronized (PdAudio.class) {
            isRunning = PdBase.implementsAudio() ? PdBase.isRunning() : audioWrapper != null && audioWrapper.isRunning();
        }
        return isRunning;
    }

    public static synchronized void release() {
        synchronized (PdAudio.class) {
            stopAudio();
            if (PdBase.implementsAudio()) {
                PdBase.closeAudio();
            } else if (audioWrapper != null) {
                audioWrapper.release();
                audioWrapper = null;
            }
        }
    }
}
