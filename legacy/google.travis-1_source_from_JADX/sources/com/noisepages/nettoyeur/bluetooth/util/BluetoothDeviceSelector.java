package com.noisepages.nettoyeur.bluetooth.util;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.noisepages.nettoyeur.bluetooth.BluetoothDisabledException;
import com.noisepages.nettoyeur.bluetooth.BluetoothUnavailableException;
import com.noisepages.nettoyeur.midi.C0003R;
import java.util.ArrayList;
import java.util.List;

@TargetApi(11)
public abstract class BluetoothDeviceSelector extends DialogFragment {
    private final String[] deviceLabels;
    private final List<BluetoothDevice> devices;

    /* renamed from: com.noisepages.nettoyeur.bluetooth.util.BluetoothDeviceSelector$1 */
    class C00001 implements OnClickListener {
        C00001() {
        }

        public void onClick(DialogInterface dialog, int which) {
            BluetoothDeviceSelector.this.onDeviceSelected((BluetoothDevice) BluetoothDeviceSelector.this.devices.get(which));
        }
    }

    /* renamed from: com.noisepages.nettoyeur.bluetooth.util.BluetoothDeviceSelector$2 */
    class C00012 implements OnClickListener {
        C00012() {
        }

        public void onClick(DialogInterface dialog, int which) {
            BluetoothDeviceSelector.this.onNoSelection();
        }
    }

    protected abstract void onDeviceSelected(BluetoothDevice bluetoothDevice);

    protected abstract void onNoSelection();

    public BluetoothDeviceSelector() throws BluetoothUnavailableException, BluetoothDisabledException {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            throw new BluetoothUnavailableException();
        } else if (btAdapter.isEnabled()) {
            this.devices = new ArrayList(btAdapter.getBondedDevices());
            this.deviceLabels = new String[this.devices.size()];
            for (int i = 0; i < this.deviceLabels.length; i++) {
                this.deviceLabels[i] = new StringBuilder(String.valueOf(((BluetoothDevice) this.devices.get(i)).getName())).append("\n").append(((BluetoothDevice) this.devices.get(i)).getAddress()).toString();
            }
        } else {
            throw new BluetoothDisabledException();
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity()).setCancelable(true);
        if (this.devices.isEmpty()) {
            builder.setTitle(C0003R.string.title_no_paired_devices).setPositiveButton(17039370, new C00012());
        } else {
            builder.setTitle(C0003R.string.title_select_bluetooth_device).setItems(this.deviceLabels, new C00001());
        }
        return builder.create();
    }

    public void onCancel(DialogInterface dialog) {
        onNoSelection();
    }
}
