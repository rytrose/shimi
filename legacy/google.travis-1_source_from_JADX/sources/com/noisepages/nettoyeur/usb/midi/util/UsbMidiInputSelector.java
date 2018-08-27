package com.noisepages.nettoyeur.usb.midi.util;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.noisepages.nettoyeur.midi.C0003R;
import com.noisepages.nettoyeur.usb.midi.UsbMidiDevice;
import com.noisepages.nettoyeur.usb.midi.UsbMidiDevice.UsbMidiInput;
import com.noisepages.nettoyeur.usb.midi.UsbMidiDevice.UsbMidiInterface;
import java.util.ArrayList;
import java.util.List;

public abstract class UsbMidiInputSelector extends DialogFragment {
    private final UsbMidiDevice device;

    /* renamed from: com.noisepages.nettoyeur.usb.midi.util.UsbMidiInputSelector$1 */
    class C00061 implements OnClickListener {
        C00061() {
        }

        public void onClick(DialogInterface dialog, int which) {
            int iface = 0;
            int index = which;
            while (index >= ((UsbMidiInterface) UsbMidiInputSelector.this.device.getInterfaces().get(iface)).getInputs().size()) {
                index -= ((UsbMidiInterface) UsbMidiInputSelector.this.device.getInterfaces().get(iface)).getInputs().size();
                iface++;
            }
            UsbMidiInputSelector.this.onInputSelected((UsbMidiInput) ((UsbMidiInterface) UsbMidiInputSelector.this.device.getInterfaces().get(iface)).getInputs().get(index), UsbMidiInputSelector.this.device, iface, index);
        }
    }

    /* renamed from: com.noisepages.nettoyeur.usb.midi.util.UsbMidiInputSelector$2 */
    class C00072 implements OnClickListener {
        C00072() {
        }

        public void onClick(DialogInterface dialog, int which) {
            UsbMidiInputSelector.this.onNoSelection(UsbMidiInputSelector.this.device);
        }
    }

    protected abstract void onInputSelected(UsbMidiInput usbMidiInput, UsbMidiDevice usbMidiDevice, int i, int i2);

    protected abstract void onNoSelection(UsbMidiDevice usbMidiDevice);

    public UsbMidiInputSelector(UsbMidiDevice device) {
        this.device = device;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<String> items = new ArrayList();
        for (int i = 0; i < this.device.getInterfaces().size(); i++) {
            for (int j = 0; j < ((UsbMidiInterface) this.device.getInterfaces().get(i)).getInputs().size(); j++) {
                items.add("Interface " + i + ", Input " + j);
            }
        }
        Builder builder = new Builder(getActivity()).setCancelable(true);
        if (items.isEmpty()) {
            builder.setTitle(C0003R.string.title_no_usb_midi_input_available).setPositiveButton(17039370, new C00072());
        } else {
            builder.setTitle(C0003R.string.title_select_usb_midi_input).setItems((CharSequence[]) items.toArray(new String[items.size()]), new C00061());
        }
        return builder.create();
    }

    public void onCancel(DialogInterface dialog) {
        onNoSelection(this.device);
    }
}
