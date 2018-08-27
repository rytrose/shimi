package org.puredata.android.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import org.puredata.android.io.AudioParameters;
import org.puredata.core.PdBase;

public class PdPreferences extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AudioParameters.init(this);
        initPreferences(getApplicationContext());
        addPreferencesFromResource(C0084R.xml.preferences);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public static void initPreferences(Context context) {
        Resources res = context.getResources();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.contains(res.getString(C0084R.string.pref_key_srate))) {
            Editor editor = prefs.edit();
            int srate = PdBase.suggestSampleRate();
            String string = res.getString(C0084R.string.pref_key_srate);
            StringBuilder stringBuilder = new StringBuilder();
            if (srate <= 0) {
                srate = AudioParameters.suggestSampleRate();
            }
            editor.putString(string, stringBuilder.append(srate).toString());
            int nic = PdBase.suggestInputChannels();
            string = res.getString(C0084R.string.pref_key_inchannels);
            stringBuilder = new StringBuilder();
            if (nic <= 0) {
                nic = AudioParameters.suggestInputChannels();
            }
            editor.putString(string, stringBuilder.append(nic).toString());
            int noc = PdBase.suggestOutputChannels();
            string = res.getString(C0084R.string.pref_key_outchannels);
            stringBuilder = new StringBuilder();
            if (noc <= 0) {
                noc = AudioParameters.suggestOutputChannels();
            }
            editor.putString(string, stringBuilder.append(noc).toString());
            editor.commit();
        }
    }
}
