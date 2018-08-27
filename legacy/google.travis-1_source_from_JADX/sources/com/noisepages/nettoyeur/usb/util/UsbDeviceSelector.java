package com.noisepages.nettoyeur.usb.util;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.noisepages.nettoyeur.midi.C0003R;
import com.noisepages.nettoyeur.usb.UsbDeviceWithInfo;
import java.util.List;

public abstract class UsbDeviceSelector<T extends UsbDeviceWithInfo> extends DialogFragment {
    private final List<T> devices;

    /* renamed from: com.noisepages.nettoyeur.usb.util.UsbDeviceSelector$1 */
    class C00101 implements OnClickListener {
        C00101() {
        }

        public void onClick(DialogInterface dialog, int which) {
            UsbDeviceSelector.this.onDeviceSelected((UsbDeviceWithInfo) UsbDeviceSelector.this.devices.get(which));
        }
    }

    /* renamed from: com.noisepages.nettoyeur.usb.util.UsbDeviceSelector$2 */
    class C00112 implements OnClickListener {
        C00112() {
        }

        public void onClick(DialogInterface dialog, int which) {
            UsbDeviceSelector.this.onNoSelection();
        }
    }

    protected abstract void onDeviceSelected(T t);

    protected abstract void onNoSelection();

    public UsbDeviceSelector(List<T> devices) {
        this.devices = devices;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity()).setCancelable(true);
        if (this.devices.isEmpty()) {
            builder.setTitle(C0003R.string.title_no_usb_devices_available).setPositiveButton(17039370, new C00112());
        } else {
            String[] items = new String[this.devices.size()];
            for (int i = 0; i < this.devices.size(); i++) {
                items[i] = ((UsbDeviceWithInfo) this.devices.get(i)).getCurrentDeviceInfo().toString();
            }
            builder.setTitle(C0003R.string.title_select_usb_midi_device).setItems(items, new C00101());
        }
        return builder.create();
    }

    public void onCancel(DialogInterface dialog) {
        onNoSelection();
    }
}
