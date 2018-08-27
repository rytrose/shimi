package org.puredata.android.io;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Process;
import android.util.Log;
import java.io.IOException;
import java.lang.Thread.State;
import org.puredata.android.service.C0084R;
import org.puredata.android.utils.Properties;

public abstract class AudioWrapper {
    private static final String AUDIO_WRAPPER = "AudioWrapper";
    private static final int ENCODING = 2;
    private Thread audioThread = null;
    final int bufSizeShorts;
    final int inputSizeShorts;
    final short[] outBuf;
    private final AudioRecordWrapper rec;
    private final AudioTrack track;

    /* renamed from: org.puredata.android.io.AudioWrapper$1 */
    class C00811 extends Thread {
        C00811() {
        }

        public void run() {
            Process.setThreadPriority(-19);
            if (AudioWrapper.this.rec != null) {
                AudioWrapper.this.rec.start();
            }
            AudioWrapper.this.track.play();
            try {
                short[] inBuf = AudioWrapper.this.rec != null ? AudioWrapper.this.rec.take() : new short[AudioWrapper.this.inputSizeShorts];
                while (!Thread.interrupted() && AudioWrapper.this.process(inBuf, AudioWrapper.this.outBuf) == 0) {
                    AudioWrapper.this.track.write(AudioWrapper.this.outBuf, 0, AudioWrapper.this.bufSizeShorts);
                    if (AudioWrapper.this.rec != null) {
                        short[] newBuf = AudioWrapper.this.rec.poll();
                        if (newBuf != null) {
                            inBuf = newBuf;
                        } else {
                            Log.w(AudioWrapper.AUDIO_WRAPPER, "no input buffer available");
                        }
                    }
                }
                if (AudioWrapper.this.rec != null) {
                    AudioWrapper.this.rec.stop();
                }
                AudioWrapper.this.track.stop();
            } catch (InterruptedException e) {
            }
        }
    }

    @TargetApi(9)
    private static class AudioSessionHandler {
        private AudioSessionHandler() {
        }

        private static int getAudioSessionId(AudioTrack track) {
            return track.getAudioSessionId();
        }
    }

    protected abstract int process(short[] sArr, short[] sArr2);

    public AudioWrapper(int sampleRate, int inChannels, int outChannels, int bufferSizePerChannel) throws IOException {
        AudioRecordWrapper audioRecordWrapper = null;
        int channelConfig = VersionedAudioFormat.getOutFormat(outChannels);
        if (inChannels != 0) {
            audioRecordWrapper = new AudioRecordWrapper(sampleRate, inChannels, bufferSizePerChannel);
        }
        this.rec = audioRecordWrapper;
        this.inputSizeShorts = inChannels * bufferSizePerChannel;
        this.bufSizeShorts = outChannels * bufferSizePerChannel;
        this.outBuf = new short[this.bufSizeShorts];
        int bufSizeBytes = this.bufSizeShorts * 2;
        int trackSizeBytes = bufSizeBytes * 2;
        int minTrackSizeBytes = AudioTrack.getMinBufferSize(sampleRate, channelConfig, 2);
        if (minTrackSizeBytes <= 0) {
            throw new IOException("bad AudioTrack parameters; sr: " + sampleRate + ", ch: " + outChannels + ", bufSize: " + trackSizeBytes);
        }
        while (trackSizeBytes < minTrackSizeBytes) {
            trackSizeBytes += bufSizeBytes;
        }
        this.track = new AudioTrack(3, sampleRate, channelConfig, 2, trackSizeBytes, 1);
        if (this.track.getState() != 1) {
            this.track.release();
            throw new IOException("unable to initialize AudioTrack instance for sr: " + sampleRate + ", ch: " + outChannels + ", bufSize: " + trackSizeBytes);
        }
    }

    public synchronized void start(Context context) {
        avoidClickHack(context);
        this.audioThread = new C00811();
        this.audioThread.start();
    }

    public synchronized void stop() {
        if (this.audioThread != null) {
            this.audioThread.interrupt();
            try {
                this.audioThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.audioThread = null;
        }
    }

    public synchronized void release() {
        stop();
        this.track.release();
        if (this.rec != null) {
            this.rec.release();
        }
    }

    public synchronized boolean isRunning() {
        boolean z;
        z = (this.audioThread == null || this.audioThread.getState() == State.TERMINATED) ? false : true;
        return z;
    }

    public synchronized int getAudioSessionId() {
        int version = Properties.version;
        if (version >= 9) {
        } else {
            throw new UnsupportedOperationException("audio sessions not supported in Android " + version);
        }
        return AudioSessionHandler.getAudioSessionId(this.track);
    }

    private void avoidClickHack(Context context) {
        try {
            MediaPlayer mp = MediaPlayer.create(context, C0084R.raw.silence);
            mp.start();
            Thread.sleep(10);
            mp.stop();
            mp.release();
        } catch (Exception e) {
            Log.e(AUDIO_WRAPPER, e.toString());
        }
    }
}
