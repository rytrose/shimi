package google.travis;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.IOException;
import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdService;
import org.puredata.android.service.PdService.PdBinder;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.PdDispatcher;

public class TravisPdManager implements PdListener, OnSharedPreferenceChangeListener {
    private Context activity;
    private final PdDispatcher myDispatcher = new C00941();
    private final ServiceConnection pdConnection = new C00192();
    private PdService pdService = null;

    /* renamed from: google.travis.TravisPdManager$2 */
    class C00192 implements ServiceConnection {
        C00192() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            TravisPdManager.this.pdService = ((PdBinder) service).getService();
            TravisPdManager.this.init();
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    /* renamed from: google.travis.TravisPdManager$1 */
    class C00941 extends PdDispatcher {
        C00941() {
        }

        public void print(String s) {
            Log.i("Pd print", s);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void init() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0050 in list [B:9:0x004d]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/1429880200.run(Unknown Source)
*/
        /*
        r7 = this;
        r5 = r7.activity;
        r4 = r5.getResources();
        r3 = 0;
        r5 = r7.myDispatcher;	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        org.puredata.core.PdBase.setReceiver(r5);	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r5 = "android";	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        org.puredata.core.PdBase.subscribe(r5);	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r5 = r7.activity;	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r2 = r5.getFilesDir();	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r5 = 2131034152; // 0x7f050028 float:1.7678813E38 double:1.0528707646E-314;
        r5 = r4.openRawResource(r5);	 Catch:{ IOException -> 0x0051 }
        r6 = 1;	 Catch:{ IOException -> 0x0051 }
        org.puredata.core.utils.IoUtils.extractZipResource(r5, r2, r6);	 Catch:{ IOException -> 0x0051 }
    L_0x0022:
        r5 = r2.getAbsolutePath();	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        org.puredata.core.PdBase.addToSearchPath(r5);	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r5 = java.lang.System.out;	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r6 = r2.getAbsolutePath();	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r5.println(r6);	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r5 = 2131034116; // 0x7f050004 float:1.767874E38 double:1.052870747E-314;	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r1 = r4.openRawResource(r5);	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r5 = "audiogeneration2.pd";	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r6 = r7.activity;	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r6 = r6.getCacheDir();	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r3 = org.puredata.core.utils.IoUtils.extractResource(r1, r5, r6);	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        org.puredata.core.PdBase.openPatch(r3);	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r7.startAudio();	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        if (r3 == 0) goto L_0x0050;
    L_0x004d:
        r3.delete();
    L_0x0050:
        return;
    L_0x0051:
        r0 = move-exception;
        r5 = "Scene Player";	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r6 = r0.toString();	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        android.util.Log.e(r5, r6);	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        goto L_0x0022;
    L_0x005c:
        r0 = move-exception;
        r5 = "TravisPdManager";	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r6 = r0.toString();	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        android.util.Log.e(r5, r6);	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        r7.finish();	 Catch:{ IOException -> 0x005c, all -> 0x006f }
        if (r3 == 0) goto L_0x0050;
    L_0x006b:
        r3.delete();
        goto L_0x0050;
    L_0x006f:
        r5 = move-exception;
        if (r3 == 0) goto L_0x0075;
    L_0x0072:
        r3.delete();
    L_0x0075:
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: google.travis.TravisPdManager.init():void");
    }

    public TravisPdManager(Context a) {
        this.activity = a;
        PdPreferences.initPreferences(a.getApplicationContext());
        PreferenceManager.getDefaultSharedPreferences(a.getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        a.bindService(new Intent(a, PdService.class), this.pdConnection, 1);
        PdBase.computeAudio(true);
        System.out.println("block size " + PdBase.blockSize());
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        startAudio();
    }

    public void receiveList(String source, Object... args) {
    }

    public void receiveMessage(String source, String symbol, Object... args) {
        Log.i("receiveMessage symbol:", symbol);
        for (Object arg : args) {
            Log.i("receiveMessage atom:", arg.toString());
        }
    }

    public void receiveSymbol(String source, String symbol) {
        Log.i("receiveSymbol", symbol);
    }

    public void receiveFloat(String source, float x) {
        Log.i("receiveFloat", String.valueOf(x));
    }

    public void receiveBang(String source) {
        Log.i("receiveBang", "bang!");
    }

    public void neckUDPitch(float pitch) {
        PdBase.sendFloat("neckUDPitch", pitch);
    }

    public void neckUDRampUp(float duration) {
        PdBase.sendFloat("neckUDRampUp", duration);
    }

    public void neckUDRampDown(float duration) {
        PdBase.sendFloat("neckUDRampDown", duration);
    }

    public void neckRLUp(float duration) {
        PdBase.sendFloat("neckRLUp", duration);
    }

    public void neckRLDown(float duration) {
        PdBase.sendFloat("neckRLDown", duration);
    }

    public void legUp(float duration) {
        PdBase.sendFloat("legUp", duration);
    }

    public void legDown(float duration) {
        PdBase.sendFloat("legDown", duration);
    }

    public void handUp(float duration) {
        PdBase.sendFloat("handUp", duration);
    }

    public void handDown(float duration) {
        PdBase.sendFloat("handDown", duration);
    }

    public void headUp(float duration) {
        PdBase.sendFloat("headUp", duration);
    }

    public void headDown(float duration) {
        PdBase.sendFloat("headDown", duration);
    }

    public void snareSample(float delay) {
        PdBase.sendFloat("snareSample", delay);
    }

    public void bassSample(float delay) {
        PdBase.sendFloat("bassSample", delay);
    }

    public void floorTomSample(float delay) {
        PdBase.sendFloat("floorTomSample", delay);
    }

    public void highTomSample(float delay) {
        PdBase.sendFloat("highTomSample", delay);
    }

    public void tambourineSample(float delay) {
        PdBase.sendFloat("tambourineSample", delay);
    }

    public void vibraslapSample(float delay) {
        PdBase.sendFloat("vibraslapSample", delay);
    }

    public void dtsample1(float duration) {
        PdBase.sendFloat("dtsample1", duration);
    }

    public void dtsample2(float duration) {
        PdBase.sendFloat("dtsample2", duration);
    }

    public void dtsample3(float duration) {
        PdBase.sendFloat("dtsample3", duration);
    }

    public void dtsample4(float duration) {
        PdBase.sendFloat("dtsample4", duration);
    }

    public void bass41(float delay) {
        PdBase.sendFloat("ebass41", delay);
    }

    public void bass36(float delay) {
        PdBase.sendFloat("ebass36", delay);
    }

    public void bass37(float delay) {
        PdBase.sendFloat("ebass37", delay);
    }

    public void bass44(float delay) {
        PdBase.sendFloat("ebass44", delay);
    }

    public void bass43(float delay) {
        PdBase.sendFloat("ebass43", delay);
    }

    public void bass40(float delay) {
        PdBase.sendFloat("ebass40", delay);
    }

    public void bass39(float delay) {
        PdBase.sendFloat("ebass39", delay);
    }

    public void melody60(float delay) {
        PdBase.sendFloat("melody60", delay);
    }

    public void melody64(float delay) {
        PdBase.sendFloat("melody64", delay);
    }

    public void melody65(float delay) {
        PdBase.sendFloat("melody65", delay);
    }

    public void melody67(float delay) {
        PdBase.sendFloat("melody67", delay);
    }

    public void melody68(float delay) {
        PdBase.sendFloat("melody68", delay);
    }

    public void melody72(float delay) {
        PdBase.sendFloat("melody72", delay);
    }

    public void melody73(float delay) {
        PdBase.sendFloat("melody73", delay);
    }

    public void bell60(float delay) {
        PdBase.sendFloat("bell60", delay);
    }

    public void bell63(float delay) {
        PdBase.sendFloat("bell63", delay);
    }

    public void bell64(float delay) {
        PdBase.sendFloat("bell64", delay);
    }

    public void bell65(float delay) {
        PdBase.sendFloat("bell65", delay);
    }

    public void bell67(float delay) {
        PdBase.sendFloat("bell67", delay);
    }

    public void bell68(float delay) {
        PdBase.sendFloat("bell68", delay);
    }

    public void cuteSample(float duration) {
        PdBase.sendFloat("cuteSample", duration);
    }

    public void cuteSampleShort(float duration) {
        PdBase.sendFloat("cuteSampleShort", duration);
    }

    public void cuteSampleLong(float duration) {
        PdBase.sendFloat("cuteSampleLong", duration);
    }

    public void cute1Sample(float duration) {
        PdBase.sendFloat("cuteSample1", duration);
    }

    public void cute1SampleShort(float duration) {
        PdBase.sendFloat("cuteSampleShort1", duration);
    }

    public void cute2Sample(float duration) {
        PdBase.sendFloat("cuteSample2", duration);
    }

    public void cute2SampleShort(float duration) {
        PdBase.sendFloat("cuteSampleShort2", duration);
    }

    public void pbSample1(String file) {
        PdBase.sendSymbol("pbSample1", file);
    }

    public void pbDuration1(float duration) {
        PdBase.sendFloat("pbDuration1", duration);
    }

    public void pbPitch1(float pitch) {
        PdBase.sendFloat("pbPitch1", pitch);
    }

    public void pbRampSpecShift1(float specShift, float duration) {
        PdBase.sendFloat("pbRampSpecShift1b", duration);
        PdBase.sendFloat("pbRampSpecShift1a", specShift);
    }

    public void pbSample2(String file) {
        PdBase.sendSymbol("pbSample2", file);
    }

    public void pbDuration2(float duration) {
        PdBase.sendFloat("pbDuration2", duration);
    }

    public void pbPitch2(float pitch) {
        PdBase.sendFloat("pbPitch2", pitch);
    }

    public void pbRampSpecShift2(float specShift, float duration) {
        PdBase.sendFloat("pbRampSpecShift2b", duration);
        PdBase.sendFloat("pbRampSpecShift2a", specShift);
    }

    private void startAudio() {
        String name = this.activity.getResources().getString(C0012R.string.app_name);
        try {
            this.pdService.initAudio(-1, -1, -1, -1.0f);
            this.pdService.startAudio(new Intent(this.activity, TravisGoogleActivity.class), C0012R.drawable.travisicon, name, "Return to " + name + ".");
        } catch (IOException e) {
        }
    }

    public void finish() {
        PdBase.release();
        try {
            this.activity.unbindService(this.pdConnection);
        } catch (IllegalArgumentException e) {
            this.pdService = null;
        }
    }
}
