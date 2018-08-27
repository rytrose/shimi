package com.noisepages.nettoyeur.usb.midi.util;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.noisepages.nettoyeur.midi.C0003R;
import com.noisepages.nettoyeur.usb.midi.UsbMidiDevice;
import com.noisepages.nettoyeur.usb.midi.UsbMidiDevice.UsbMidiInterface;
import com.noisepages.nettoyeur.usb.midi.UsbMidiDevice.UsbMidiOutput;
import java.util.ArrayList;
import java.util.List;

public abstract class UsbMidiOutputSelector extends DialogFragment {
    private final UsbMidiDevice device;

    /* renamed from: com.noisepages.nettoyeur.usb.midi.util.UsbMidiOutputSelector$1 */
    class C00081 implements OnClickListener {
        C00081() {
        }

        public void onClick(DialogInterface dialog, int which) {
            int iface = 0;
            int index = which;
            while (index >= ((UsbMidiInterface) UsbMidiOutputSelector.this.device.getInterfaces().get(iface)).getOutputs().size()) {
                index -= ((UsbMidiInterface) UsbMidiOutputSelector.this.device.getInterfaces().get(iface)).getOutputs().size();
                iface++;
            }
            UsbMidiOutputSelector.this.onOutputSelected((UsbMidiOutput) ((UsbMidiInterface) UsbMidiOutputSelector.this.device.getInterfaces().get(iface)).getOutputs().get(index), UsbMidiOutputSelector.this.device, iface, index);
        }
    }

    /* renamed from: com.noisepages.nettoyeur.usb.midi.util.UsbMidiOutputSelector$2 */
    class C00092 implements OnClickListener {
        C00092() {
        }

        public void onClick(DialogInterface dialog, int which) {
            UsbMidiOutputSelector.this.onNoSelection(UsbMidiOutputSelector.this.device);
        }
    }

    protected abstract void onNoSelection(UsbMidiDevice usbMidiDevice);

    protected abstract void onOutputSelected(UsbMidiOutput usbMidiOutput, UsbMidiDevice usbMidiDevice, int i, int i2);

    public UsbMidiOutputSelector(UsbMidiDevice device) {
        this.device = device;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<String> items = new ArrayList();
        for (int i = 0; i < this.device.getInterfaces().size(); i++) {
            for (int j = 0; j < ((UsbMidiInterface) this.device.getInterfaces().get(i)).getOutputs().size(); j++) {
                items.add("Interface " + i + ", Output " + j);
            }
        }
        Builder builder = new Builder(getActivity()).setCancelable(true);
        if (items.isEmpty()) {
            builder.setTitle(C0003R.string.title_no_usb_midi_output_available).setPositiveButton(17039370, new C00092());
        } else {
            builder.setTitle(C0003R.string.title_select_usb_midi_output).setItems((CharSequence[]) items.toArray(new String[items.size()]), new C00081());
        }
        return builder.create();
    }

    public void onCancel(DialogInterface dialog) {
        onNoSelection(this.device);
    }
}
