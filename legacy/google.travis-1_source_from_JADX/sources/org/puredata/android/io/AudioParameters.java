package org.puredata.android.io;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;
import org.puredata.android.utils.Properties;

public class AudioParameters {
    private static final String TAG = "AudioParameters";
    private static AudioParametersImpl impl = null;

    private interface AudioParametersImpl {
        boolean checkInputParameters(int i, int i2);

        boolean checkOutputParameters(int i, int i2);

        int suggestInputBufferSize(int i);

        int suggestInputChannels();

        int suggestOutputBufferSize(int i);

        int suggestOutputChannels();

        int suggestSampleRate();

        boolean supportsLowLatency();
    }

    private static class BasicOpenSLParameters implements AudioParametersImpl {
        private final int inputBufferSize;
        private final int outputBufferSize;

        BasicOpenSLParameters(int inputBufferSize, int outputBufferSize) {
            this.inputBufferSize = inputBufferSize;
            this.outputBufferSize = outputBufferSize;
        }

        public boolean supportsLowLatency() {
            return false;
        }

        public int suggestSampleRate() {
            return 44100;
        }

        public int suggestInputChannels() {
            return 1;
        }

        public int suggestOutputChannels() {
            return 2;
        }

        public int suggestInputBufferSize(int sampleRate) {
            return this.inputBufferSize;
        }

        public int suggestOutputBufferSize(int sampleRate) {
            return this.outputBufferSize;
        }

        public boolean checkInputParameters(int srate, int nin) {
            return srate > 0 && nin >= 0 && nin <= 2;
        }

        public boolean checkOutputParameters(int srate, int nout) {
            return srate > 0 && nout >= 0 && nout <= 2;
        }
    }

    private static class JavaAudioParameters implements AudioParametersImpl {
        private static final int COMMON_RATE = 8000;
        private static final int ENCODING = 2;
        private static final int MAX_CHANNELS = 8;
        private final int inputChannels;
        private final int outputChannels;
        private final int sampleRate;

        JavaAudioParameters() {
            int n;
            int oc = 0;
            for (n = 1; n < MAX_CHANNELS; n++) {
                if (checkOutputParameters(COMMON_RATE, n)) {
                    oc = n;
                }
            }
            this.outputChannels = oc;
            int ic = 0;
            for (n = 0; n < MAX_CHANNELS; n++) {
                if (checkInputParameters(COMMON_RATE, n)) {
                    ic = n;
                }
            }
            this.inputChannels = ic;
            int sr = COMMON_RATE;
            for (int n2 : new int[]{11025, 16000, 22050, 32000, 44100}) {
                if (checkInputParameters(n2, this.inputChannels) && checkOutputParameters(n2, this.outputChannels)) {
                    sr = n2;
                }
            }
            this.sampleRate = sr;
        }

        public boolean supportsLowLatency() {
            return false;
        }

        public int suggestSampleRate() {
            return this.sampleRate;
        }

        public int suggestInputChannels() {
            return this.inputChannels;
        }

        public int suggestOutputChannels() {
            return this.outputChannels;
        }

        public int suggestInputBufferSize(int sampleRate) {
            return -1;
        }

        public int suggestOutputBufferSize(int sampleRate) {
            return -1;
        }

        public boolean checkInputParameters(int srate, int nin) {
            if (nin != 0) {
                try {
                    if (AudioRecord.getMinBufferSize(srate, VersionedAudioFormat.getInFormat(nin), 2) <= 0) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        }

        public boolean checkOutputParameters(int srate, int nout) {
            try {
                return AudioTrack.getMinBufferSize(srate, VersionedAudioFormat.getOutFormat(nout), 2) > 0;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private static class JellyBeanOpenSLParameters extends BasicOpenSLParameters {
        private final boolean lowLatency;
        private final int nativeBufferSize;

        JellyBeanOpenSLParameters(int inputBufferSize, int outputBufferSize, int nativeBufferSize, boolean lowLatency) {
            super(inputBufferSize, outputBufferSize);
            this.nativeBufferSize = nativeBufferSize;
            this.lowLatency = lowLatency;
        }

        static JellyBeanOpenSLParameters getParameters() {
            boolean lowLatency = Build.MODEL.equals("Galaxy Nexus");
            return new JellyBeanOpenSLParameters(64, 64, lowLatency ? 384 : 64, lowLatency);
        }

        public int suggestOutputBufferSize(int sampleRate) {
            return sampleRate == suggestSampleRate() ? this.nativeBufferSize : super.suggestOutputBufferSize(sampleRate);
        }

        public boolean supportsLowLatency() {
            return this.lowLatency;
        }
    }

    @TargetApi(17)
    private static class JellyBeanMR1OpenSLParameters extends JellyBeanOpenSLParameters {
        private final int sampleRate;

        JellyBeanMR1OpenSLParameters(int sampleRate, int inputBufferSize, int outputBufferSize, int nativeBufferSize, boolean lowLatency) {
            super(inputBufferSize, outputBufferSize, nativeBufferSize, lowLatency);
            this.sampleRate = sampleRate;
        }

        public int suggestSampleRate() {
            return this.sampleRate;
        }

        static JellyBeanMR1OpenSLParameters getParameters(Context context) {
            boolean lowLatency = context.getPackageManager().hasSystemFeature("android.hardware.audio.low_latency");
            AudioManager am = (AudioManager) context.getSystemService("audio");
            int sr = 44100;
            int bs = 64;
            try {
                sr = Integer.parseInt(am.getProperty("android.media.property.OUTPUT_SAMPLE_RATE"));
                bs = Integer.parseInt(am.getProperty("android.media.property.OUTPUT_FRAMES_PER_BUFFER"));
                Log.i(AudioParameters.TAG, "sample rate: " + sr + ", buffer size: " + bs);
            } catch (Exception e) {
                Log.e(AudioParameters.TAG, "Missing or malformed audio property: " + e.toString());
            }
            return new JellyBeanMR1OpenSLParameters(sr, 64, 64, bs, lowLatency);
        }
    }

    public static synchronized void init(Context context) {
        synchronized (AudioParameters.class) {
            if (impl == null) {
                if (Properties.version > 16 && context != null) {
                    impl = JellyBeanMR1OpenSLParameters.getParameters(context);
                } else if (Properties.version > 16) {
                    Log.w(TAG, "Initializing audio parameters with null context on Android 4.2 or later.");
                    impl = new BasicOpenSLParameters(64, 64);
                } else if (Properties.version == 16) {
                    impl = JellyBeanOpenSLParameters.getParameters();
                } else if (Properties.version > 8) {
                    impl = new BasicOpenSLParameters(64, 64);
                } else {
                    impl = new JavaAudioParameters();
                }
            }
        }
    }

    public static boolean supportsLowLatency() {
        init(null);
        return impl.supportsLowLatency();
    }

    public static int suggestSampleRate() {
        init(null);
        return impl.suggestSampleRate();
    }

    public static int suggestInputChannels() {
        init(null);
        return impl.suggestInputChannels();
    }

    public static int suggestOutputChannels() {
        init(null);
        return impl.suggestOutputChannels();
    }

    public static int suggestInputBufferSize(int sampleRate) {
        init(null);
        return impl.suggestInputBufferSize(sampleRate);
    }

    public static int suggestOutputBufferSize(int sampleRate) {
        init(null);
        return impl.suggestOutputBufferSize(sampleRate);
    }

    public static boolean checkParameters(int srate, int nin, int nout) {
        return checkInputParameters(srate, nin) && checkOutputParameters(srate, nout);
    }

    public static boolean checkInputParameters(int srate, int nin) {
        init(null);
        return impl.checkInputParameters(srate, nin);
    }

    public static boolean checkOutputParameters(int srate, int nout) {
        init(null);
        return impl.checkOutputParameters(srate, nout);
    }
}
