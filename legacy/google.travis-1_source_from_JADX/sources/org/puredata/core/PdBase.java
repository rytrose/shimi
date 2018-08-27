package org.puredata.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class PdBase {
    private static final Map<String, Long> bindings = new HashMap();
    private static PdMidiReceiver midiReceiver = null;
    private static final Map<Integer, Long> patches = new HashMap();

    private static native void addFloat(float f);

    private static native void addSymbol(String str);

    public static native void addToSearchPath(String str);

    public static native int arraySize(String str);

    public static native String audioImplementation();

    private static native long bindSymbol(String str);

    public static native int blockSize();

    public static native void clearSearchPath();

    public static native void closeAudio();

    private static native void closeFile(long j);

    public static native boolean exists(String str);

    private static native int finishList(String str);

    private static native int finishMessage(String str, String str2);

    private static native int getDollarZero(long j);

    public static native boolean implementsAudio();

    private static native void initialize();

    public static native boolean isRunning();

    public static native int openAudio(int i, int i2, int i3, Map<String, String> map);

    private static native long openFile(String str, String str2);

    public static native int pauseAudio();

    private static native void pollMidiQueueInternal();

    public static native void pollPdMessageQueue();

    public static native int process(int i, double[] dArr, double[] dArr2);

    public static native int process(int i, float[] fArr, float[] fArr2);

    public static native int process(int i, short[] sArr, short[] sArr2);

    public static native int processRaw(float[] fArr, float[] fArr2);

    private static native int readArrayNative(float[] fArr, int i, String str, int i2, int i3);

    public static native int sendAftertouch(int i, int i2);

    public static native int sendBang(String str);

    public static native int sendControlChange(int i, int i2, int i3);

    public static native int sendFloat(String str, float f);

    public static native int sendMidiByte(int i, int i2);

    public static native int sendNoteOn(int i, int i2, int i3);

    public static native int sendPitchBend(int i, int i2);

    public static native int sendPolyAftertouch(int i, int i2, int i3);

    public static native int sendProgramChange(int i, int i2);

    public static native int sendSymbol(String str, String str2);

    public static native int sendSysRealTime(int i, int i2);

    public static native int sendSysex(int i, int i2);

    private static native void setMidiReceiverInternal(PdMidiReceiver pdMidiReceiver);

    public static native void setReceiver(PdReceiver pdReceiver);

    public static native int startAudio();

    private static native int startMessage(int i);

    public static native int suggestInputChannels();

    public static native int suggestOutputChannels();

    public static native int suggestSampleRate();

    private static native void unbindSymbol(long j);

    private static native int writeArrayNative(String str, int i, float[] fArr, int i2, int i3);

    static {
        try {
            Class[] inner = Class.forName("android.os.Build").getDeclaredClasses();
            System.loadLibrary("pd");
            int version = -1;
            int length = inner.length;
            int i = 0;
            while (i < length) {
                Class<?> c = inner[i];
                if (c.getCanonicalName().equals("android.os.Build.VERSION")) {
                    try {
                        version = c.getDeclaredField("SDK_INT").getInt(null);
                        break;
                    } catch (Exception e) {
                        version = 3;
                    }
                } else {
                    i++;
                }
            }
            if (version >= 9) {
                System.out.println("loading pdnativeopensl for Android");
                System.loadLibrary("pdnativeopensl");
            } else {
                System.out.println("loading pdnative for Android");
                System.loadLibrary("pdnative");
            }
        } catch (Exception e2) {
            NativeLoader.loadLibrary("pthreadGC2", "windows");
            NativeLoader.loadLibrary("pdnative");
        }
        initialize();
    }

    private PdBase() {
    }

    public static synchronized void release() {
        synchronized (PdBase.class) {
            closeAudio();
            setReceiver(null);
            setMidiReceiver(null);
            for (Long longValue : bindings.values()) {
                unbindSymbol(longValue.longValue());
            }
            bindings.clear();
            for (Long longValue2 : patches.values()) {
                closeFile(longValue2.longValue());
            }
            patches.clear();
        }
    }

    public static void setMidiReceiver(PdMidiReceiver receiver) {
        midiReceiver = receiver;
        setMidiReceiverInternal(receiver);
    }

    public static int openAudio(int inputChannels, int outputChannels, int sampleRate) {
        return openAudio(inputChannels, outputChannels, sampleRate, null);
    }

    public static synchronized int openPatch(File file) throws IOException {
        int handle;
        synchronized (PdBase.class) {
            if (file.exists()) {
                String name = file.getName();
                File dir = file.getParentFile();
                long ptr = openFile(name, dir != null ? dir.getAbsolutePath() : ".");
                if (ptr == 0) {
                    throw new IOException("unable to open patch " + file.getPath());
                }
                handle = getDollarZero(ptr);
                patches.put(Integer.valueOf(handle), Long.valueOf(ptr));
            } else {
                throw new FileNotFoundException(file.getPath());
            }
        }
        return handle;
    }

    public static synchronized int openPatch(String path) throws IOException {
        int openPatch;
        synchronized (PdBase.class) {
            openPatch = openPatch(new File(path));
        }
        return openPatch;
    }

    public static synchronized void closePatch(int handle) {
        synchronized (PdBase.class) {
            Long ptr = (Long) patches.remove(Integer.valueOf(handle));
            if (ptr != null) {
                closeFile(ptr.longValue());
            }
        }
    }

    public static void computeAudio(boolean state) {
        int i = 1;
        String str = "pd";
        String str2 = "dsp";
        Object[] objArr = new Object[1];
        if (!state) {
            i = 0;
        }
        objArr[0] = Integer.valueOf(i);
        sendMessage(str, str2, objArr);
    }

    public static synchronized int sendList(String recv, Object... args) {
        int err;
        synchronized (PdBase.class) {
            err = processArgs(args);
            if (err == 0) {
                err = finishList(recv);
            }
        }
        return err;
    }

    public static synchronized int sendMessage(String recv, String msg, Object... args) {
        int err;
        synchronized (PdBase.class) {
            err = processArgs(args);
            if (err == 0) {
                err = finishMessage(recv, msg);
            }
        }
        return err;
    }

    private static int processArgs(Object[] args) {
        if (startMessage(args.length) != 0) {
            return -100;
        }
        for (Object arg : args) {
            if (arg instanceof Integer) {
                addFloat((float) ((Integer) arg).intValue());
            } else if (arg instanceof Float) {
                addFloat(((Float) arg).floatValue());
            } else if (arg instanceof Double) {
                addFloat(((Double) arg).floatValue());
            } else if (!(arg instanceof String)) {
                return -101;
            } else {
                addSymbol((String) arg);
            }
        }
        return 0;
    }

    public static synchronized int subscribe(String symbol) {
        int i = 0;
        synchronized (PdBase.class) {
            if (bindings.get(symbol) == null) {
                long ptr = bindSymbol(symbol);
                if (ptr == 0) {
                    i = -1;
                } else {
                    bindings.put(symbol, Long.valueOf(ptr));
                }
            }
        }
        return i;
    }

    public static synchronized void unsubscribe(String symbol) {
        synchronized (PdBase.class) {
            Long ptr = (Long) bindings.remove(symbol);
            if (ptr != null) {
                unbindSymbol(ptr.longValue());
            }
        }
    }

    public static int readArray(float[] destination, int destOffset, String source, int srcOffset, int n) {
        if (destOffset < 0 || destOffset + n > destination.length) {
            return -2;
        }
        return readArrayNative(destination, destOffset, source, srcOffset, n);
    }

    public static int writeArray(String destination, int destOffset, float[] source, int srcOffset, int n) {
        if (srcOffset < 0 || srcOffset + n > source.length) {
            return -2;
        }
        return writeArrayNative(destination, destOffset, source, srcOffset, n);
    }

    public static void pollMidiQueue() {
        if (midiReceiver != null) {
            midiReceiver.beginBlock();
            pollMidiQueueInternal();
            midiReceiver.endBlock();
        }
    }
}
