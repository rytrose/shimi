package org.puredata.android.io;

import android.media.AudioRecord;
import android.os.Process;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class AudioRecordWrapper {
    private static final int ENCODING = 2;
    private final int bufSizeShorts;
    private Thread inputThread = null;
    private final BlockingQueue<short[]> queue = new SynchronousQueue();
    private final AudioRecord rec;

    /* renamed from: org.puredata.android.io.AudioRecordWrapper$1 */
    class C00801 extends Thread {
        C00801() {
        }

        public void run() {
            Process.setThreadPriority(-19);
            AudioRecordWrapper.this.rec.startRecording();
            short[] buf = new short[AudioRecordWrapper.this.bufSizeShorts];
            short[] auxBuf = new short[AudioRecordWrapper.this.bufSizeShorts];
            while (!Thread.interrupted()) {
                int nRead = 0;
                while (nRead < AudioRecordWrapper.this.bufSizeShorts && !Thread.interrupted()) {
                    nRead += AudioRecordWrapper.this.rec.read(buf, nRead, AudioRecordWrapper.this.bufSizeShorts - nRead);
                }
                if (nRead < AudioRecordWrapper.this.bufSizeShorts) {
                    break;
                }
                try {
                    AudioRecordWrapper.this.queue.put(buf);
                    short[] tmp = buf;
                    buf = auxBuf;
                    auxBuf = tmp;
                } catch (InterruptedException e) {
                }
            }
            AudioRecordWrapper.this.rec.stop();
        }
    }

    public AudioRecordWrapper(int sampleRate, int inChannels, int bufferSizePerChannel) throws IOException {
        int channelConfig = VersionedAudioFormat.getInFormat(inChannels);
        this.bufSizeShorts = inChannels * bufferSizePerChannel;
        int bufSizeBytes = this.bufSizeShorts * 2;
        int recSizeBytes = bufSizeBytes * 2;
        int minRecSizeBytes = AudioRecord.getMinBufferSize(sampleRate, channelConfig, 2);
        if (minRecSizeBytes <= 0) {
            throw new IOException("bad AudioRecord parameters; sr: " + sampleRate + ", ch: " + inChannels + ", bufSize: " + bufferSizePerChannel);
        }
        while (recSizeBytes < minRecSizeBytes) {
            recSizeBytes += bufSizeBytes;
        }
        this.rec = new AudioRecord(1, sampleRate, channelConfig, 2, recSizeBytes);
        if (this.rec != null && this.rec.getState() != 1) {
            this.rec.release();
            throw new IOException("unable to initialize AudioRecord instance for sr: " + sampleRate + ", ch: " + inChannels + ", bufSize: " + bufferSizePerChannel);
        }
    }

    public synchronized void start() {
        this.inputThread = new C00801();
        this.inputThread.start();
    }

    public synchronized void stop() {
        if (this.inputThread != null) {
            this.inputThread.interrupt();
            try {
                this.inputThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.inputThread = null;
        }
    }

    public synchronized void release() {
        stop();
        this.rec.release();
        this.queue.clear();
    }

    public short[] poll() {
        return (short[]) this.queue.poll();
    }

    public short[] take() throws InterruptedException {
        return (short[]) this.queue.take();
    }
}
