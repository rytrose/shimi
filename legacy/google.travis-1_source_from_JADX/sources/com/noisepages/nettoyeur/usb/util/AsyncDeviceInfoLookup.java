package com.noisepages.nettoyeur.usb.util;

import android.os.AsyncTask;
import com.noisepages.nettoyeur.usb.UsbDeviceWithInfo;

public abstract class AsyncDeviceInfoLookup extends AsyncTask<UsbDeviceWithInfo, Void, Void> {
    protected abstract void onLookupComplete();

    protected Void doInBackground(UsbDeviceWithInfo... params) {
        for (UsbDeviceWithInfo device : params) {
            device.retrieveReadableDeviceInfo();
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        onLookupComplete();
    }
}
