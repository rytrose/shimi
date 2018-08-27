package com.noisepages.nettoyeur.usb;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

@TargetApi(12)
public class UsbDeviceWithInfo {
    private static final String ACTION_USB_PERMISSION = "com.noisepages.nettoyeur.usb.USB_PERMISSION";
    private static BroadcastReceiver broadcastReceiver = null;
    protected final UsbDevice device;
    private volatile boolean hasReadableInfo = false;
    private volatile DeviceInfo info;

    /* renamed from: com.noisepages.nettoyeur.usb.UsbDeviceWithInfo$1 */
    class C00041 extends BroadcastReceiver {
        private final /* synthetic */ UsbBroadcastHandler val$handler;

        C00041(UsbBroadcastHandler usbBroadcastHandler) {
            this.val$handler = usbBroadcastHandler;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
            if (device != null) {
                if (UsbDeviceWithInfo.ACTION_USB_PERMISSION.equals(action)) {
                    if (intent.getBooleanExtra("permission", false)) {
                        this.val$handler.onPermissionGranted(device);
                    } else {
                        this.val$handler.onPermissionDenied(device);
                    }
                } else if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                    this.val$handler.onDeviceDetached(device);
                }
            }
        }
    }

    public static void installBroadcastHandler(Context context, UsbBroadcastHandler handler) {
        uninstallBroadcastHandler(context);
        broadcastReceiver = new C00041(handler);
        context.registerReceiver(broadcastReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        context.registerReceiver(broadcastReceiver, new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED"));
    }

    public static void uninstallBroadcastHandler(Context context) {
        if (broadcastReceiver != null) {
            try {
                context.unregisterReceiver(broadcastReceiver);
            } catch (IllegalArgumentException e) {
            }
            broadcastReceiver = null;
        }
    }

    public void requestPermission(Context context) {
        if (broadcastReceiver == null) {
            throw new IllegalStateException("installPermissionHandler must be called before requesting permission");
        }
        ((UsbManager) context.getSystemService("usb")).requestPermission(this.device, PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0));
    }

    public UsbDeviceWithInfo(UsbDevice device) {
        this.device = device;
        this.info = new DeviceInfo(device);
    }

    public boolean matches(UsbDevice otherDevice) {
        return this.device.equals(otherDevice);
    }

    public DeviceInfo getCurrentDeviceInfo() {
        return this.info;
    }

    public boolean retrieveReadableDeviceInfo() {
        if (this.hasReadableInfo) {
            return true;
        }
        DeviceInfo readableInfo = DeviceInfo.retrieveDeviceInfo(this.device);
        if (readableInfo != null) {
            this.info = readableInfo;
            this.hasReadableInfo = true;
        }
        return this.hasReadableInfo;
    }

    public String toString() {
        return this.device.toString();
    }
}
