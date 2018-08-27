package com.noisepages.nettoyeur.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface BluetoothSppObserver {
    void onConnectionFailed();

    void onConnectionLost();

    void onDeviceConnected(BluetoothDevice bluetoothDevice);
}
