package org.puredata.android.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.Properties;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

public class PdService extends Service {
    private static final String PD_SERVICE = "PD Service";
    private static boolean abstractionsInstalled = false;
    private static final boolean hasEclair;
    private final PdBinder binder = new PdBinder();
    private volatile float bufferSizeMillis;
    private final ForegroundManager fgManager;
    private volatile int inputChannels;
    private volatile int outputChannels;
    private volatile int sampleRate;

    private interface ForegroundManager {
        void startForeground(Intent intent, int i, String str, String str2);

        void stopForeground();
    }

    public class PdBinder extends Binder {
        public PdService getService() {
            return PdService.this;
        }
    }

    private class ForegroundCupcake implements ForegroundManager {
        protected static final int NOTIFICATION_ID = 1;
        private boolean hasForeground;

        private ForegroundCupcake() {
            this.hasForeground = false;
        }

        protected Notification makeNotification(Intent intent, int icon, String title, String description) {
            PendingIntent pi = PendingIntent.getActivity(PdService.this.getApplicationContext(), 0, intent, 0);
            Notification notification = new Notification(icon, title, System.currentTimeMillis());
            notification.setLatestEventInfo(PdService.this, title, description, pi);
            notification.flags |= 2;
            return notification;
        }

        public void startForeground(Intent intent, int icon, String title, String description) {
            stopForeground();
            versionedStart(intent, icon, title, description);
            this.hasForeground = true;
        }

        protected void versionedStart(Intent intent, int icon, String title, String description) {
            PdService.this.invokeSetForeground(true);
            ((NotificationManager) PdService.this.getSystemService("notification")).notify(1, makeNotification(intent, icon, title, description));
        }

        public void stopForeground() {
            if (this.hasForeground) {
                versionedStop();
                this.hasForeground = false;
            }
        }

        protected void versionedStop() {
            ((NotificationManager) PdService.this.getSystemService("notification")).cancel(1);
            PdService.this.invokeSetForeground(false);
        }
    }

    @TargetApi(5)
    private class ForegroundEclair extends ForegroundCupcake {
        private ForegroundEclair() {
            super();
        }

        protected void versionedStart(Intent intent, int icon, String title, String description) {
            PdService.this.startForeground(1, makeNotification(intent, icon, title, description));
        }

        protected void versionedStop() {
            PdService.this.stopForeground(true);
        }
    }

    public PdService() {
        this.fgManager = hasEclair ? new ForegroundEclair() : new ForegroundCupcake();
        this.sampleRate = 0;
        this.inputChannels = 0;
        this.outputChannels = 0;
        this.bufferSizeMillis = 0.0f;
    }

    static {
        boolean z;
        if (Properties.version >= 5) {
            z = true;
        } else {
            z = false;
        }
        hasEclair = z;
    }

    public float getBufferSizeMillis() {
        return this.bufferSizeMillis;
    }

    public int getInputChannels() {
        return this.inputChannels;
    }

    public int getOutputChannels() {
        return this.outputChannels;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public synchronized void initAudio(int srate, int nic, int noc, float millis) throws IOException {
        String s;
        this.fgManager.stopForeground();
        Resources res = getResources();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (srate < 0) {
            s = prefs.getString(res.getString(C0084R.string.pref_key_srate), null);
            if (s != null) {
                srate = Integer.parseInt(s);
            } else {
                srate = PdBase.suggestSampleRate();
                if (srate < 0) {
                    srate = AudioParameters.suggestSampleRate();
                }
            }
        }
        if (nic < 0) {
            s = prefs.getString(res.getString(C0084R.string.pref_key_inchannels), null);
            if (s != null) {
                nic = Integer.parseInt(s);
            } else {
                nic = PdBase.suggestInputChannels();
                if (nic < 0) {
                    nic = AudioParameters.suggestInputChannels();
                }
            }
        }
        if (noc < 0) {
            s = prefs.getString(res.getString(C0084R.string.pref_key_outchannels), null);
            if (s != null) {
                noc = Integer.parseInt(s);
            } else {
                noc = PdBase.suggestOutputChannels();
                if (noc < 0) {
                    noc = AudioParameters.suggestOutputChannels();
                }
            }
        }
        if (millis < 0.0f) {
            millis = 50.0f;
        }
        PdAudio.initAudio(srate, nic, noc, ((int) (((0.001f * millis) * ((float) srate)) / ((float) PdBase.blockSize()))) + 1, true);
        this.sampleRate = srate;
        this.inputChannels = nic;
        this.outputChannels = noc;
        this.bufferSizeMillis = millis;
    }

    public synchronized void startAudio() {
        PdAudio.startAudio(this);
    }

    public synchronized void startAudio(Intent intent, int icon, String title, String description) {
        this.fgManager.startForeground(intent, icon, title, description);
        PdAudio.startAudio(this);
    }

    public synchronized void stopAudio() {
        PdAudio.stopAudio();
        this.fgManager.stopForeground();
    }

    public synchronized boolean isRunning() {
        return PdAudio.isRunning();
    }

    public synchronized void release() {
        stopAudio();
        PdAudio.release();
        PdBase.release();
    }

    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public boolean onUnbind(Intent intent) {
        release();
        return false;
    }

    public void onCreate() {
        super.onCreate();
        AudioParameters.init(this);
        if (!abstractionsInstalled) {
            try {
                File dir = getFilesDir();
                IoUtils.extractZipResource(getResources().openRawResource(C0084R.raw.extra_abs), dir, true);
                abstractionsInstalled = true;
                PdBase.addToSearchPath(dir.getAbsolutePath());
                PdBase.addToSearchPath("/data/data/" + getPackageName() + "/lib");
            } catch (IOException e) {
                Log.e(PD_SERVICE, "unable to unpack abstractions:" + e.toString());
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        release();
    }

    private void invokeSetForeground(boolean foreground) {
        try {
            getClass().getMethod("setForeground", new Class[]{Boolean.TYPE}).invoke(this, new Object[]{Boolean.valueOf(foreground)});
        } catch (Exception e) {
            Log.e(PD_SERVICE, e.toString());
        }
    }
}
