package com.noisepages.nettoyeur.bluetooth.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.noisepages.nettoyeur.midi.C0003R;
import java.util.Set;

public class DeviceListActivity extends Activity {
    public static final String DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter btAdapter;
    private OnItemClickListener clickListener = new C00021();
    private boolean empty;
    private ArrayAdapter<String> pairedDevicesAdapter;

    /* renamed from: com.noisepages.nettoyeur.bluetooth.util.DeviceListActivity$1 */
    class C00021 implements OnItemClickListener {
        C00021() {
        }

        public void onItemClick(AdapterView<?> adapterView, View v, int arg2, long arg3) {
            if (!DeviceListActivity.this.empty) {
                String info = ((TextView) v).getText().toString();
                String address = info.substring(info.length() - 17);
                Intent intent = new Intent();
                intent.putExtra(DeviceListActivity.DEVICE_ADDRESS, address);
                DeviceListActivity.this.setResult(-1, intent);
            }
            DeviceListActivity.this.finish();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0003R.layout.device_list);
        setResult(0);
        this.pairedDevicesAdapter = new ArrayAdapter(this, C0003R.layout.device_name);
        ListView pairedListView = (ListView) findViewById(C0003R.id.paired_devices);
        pairedListView.setAdapter(this.pairedDevicesAdapter);
        pairedListView.setOnItemClickListener(this.clickListener);
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = this.btAdapter.getBondedDevices();
        this.empty = pairedDevices.isEmpty();
        if (this.empty) {
            this.pairedDevicesAdapter.add(getResources().getText(C0003R.string.none_paired).toString());
            return;
        }
        findViewById(C0003R.id.title_paired_devices).setVisibility(0);
        for (BluetoothDevice device : pairedDevices) {
            this.pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
        }
    }
}
