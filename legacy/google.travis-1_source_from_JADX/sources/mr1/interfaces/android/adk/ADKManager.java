package mr1.interfaces.android.adk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import java.io.FileOutputStream;
import java.io.IOException;

public class ADKManager {
    private static final String TAG = "ADKController";
    private UsbAccessory accessory;
    private UsbManager manager;
    private FileOutputStream outputStream;
    private ParcelFileDescriptor pfd;
    private boolean pfdOpen = false;

    /* renamed from: mr1.interfaces.android.adk.ADKManager$1 */
    class C00241 extends BroadcastReceiver {
        C00241() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.usb.action.USB_ACCESSORY_DETACHED".equals(intent.getAction()) && ((UsbAccessory) intent.getParcelableExtra("android.hardware.usb.action.USB_ACCESSORY_DETACHED")) != null && ADKManager.this.pfdOpen) {
                try {
                    ADKManager.this.pfd.close();
                    ADKManager.this.pfdOpen = false;
                } catch (IOException e) {
                }
            }
        }
    }

    public ADKManager(Context c) {
        connectToBoard(c);
    }

    private void connectToBoard(Context c) {
        this.manager = UsbManager.getInstance(c);
        UsbAccessory[] accessoryList = this.manager.getAccessoryList();
        if (accessoryList == null || accessoryList.length == 0) {
            this.accessory = null;
            return;
        }
        this.accessory = accessoryList[0];
        this.pfd = this.manager.openAccessory(this.accessory);
        if (this.pfd != null) {
            this.pfdOpen = true;
            this.outputStream = new FileOutputStream(this.pfd.getFileDescriptor());
        }
        C00241 c00241 = new C00241();
    }

    public void sendMessage(byte[] msg) {
        if (this.outputStream != null) {
            try {
                this.outputStream.write(msg);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }
}
