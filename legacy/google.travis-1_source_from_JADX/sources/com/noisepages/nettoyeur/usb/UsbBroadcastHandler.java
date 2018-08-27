package com.noisepages.nettoyeur.usb;

import android.hardware.usb.UsbDevice;

public interface UsbBroadcastHandler {
    void onDeviceDetached(UsbDevice usbDevice);

    void onPermissionDenied(UsbDevice usbDevice);

    void onPermissionGranted(UsbDevice usbDevice);
}
