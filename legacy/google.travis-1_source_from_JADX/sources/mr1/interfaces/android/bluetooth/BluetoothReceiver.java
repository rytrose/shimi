package mr1.interfaces.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BluetoothReceiver {
    /* renamed from: D */
    private static final boolean f9D = true;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "MR1-BluetoothReceiver";
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_NONE = 0;
    private static final String TAG = "BluetoothReceiver";
    private iBTMessageListener listener;
    private AcceptThread mAcceptThread;
    private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private ConnectedThread mConnectedThread;
    private int mState;

    private abstract class BluetoothThread extends Thread {
        public abstract void cancel();

        private BluetoothThread() {
        }
    }

    public interface iBTMessageListener {
        void messageReceived(byte[] bArr, int i);
    }

    private class AcceptThread extends BluetoothThread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            super();
            BluetoothServerSocket tmp = null;
            try {
                tmp = BluetoothReceiver.this.mAdapter.listenUsingRfcommWithServiceRecord(BluetoothReceiver.NAME, BluetoothReceiver.MY_UUID);
            } catch (IOException e) {
                Log.e(BluetoothReceiver.TAG, "listen() failed", e);
            }
            this.mmServerSocket = tmp;
        }

        public void run() {
            Log.d(BluetoothReceiver.TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            while (BluetoothReceiver.this.mState != 3) {
                try {
                    BluetoothSocket socket = this.mmServerSocket.accept();
                    if (socket != null) {
                        synchronized (BluetoothReceiver.this) {
                            switch (BluetoothReceiver.this.mState) {
                                case 0:
                                case 3:
                                    try {
                                        socket.close();
                                        break;
                                    } catch (IOException e) {
                                        Log.e(BluetoothReceiver.TAG, "Could not close unwanted socket", e);
                                        break;
                                    }
                                case 1:
                                case 2:
                                    BluetoothReceiver.this.connected(socket, socket.getRemoteDevice());
                                    break;
                            }
                        }
                    }
                } catch (IOException e2) {
                    Log.e(BluetoothReceiver.TAG, "accept() failed", e2);
                }
            }
            Log.i(BluetoothReceiver.TAG, "END mAcceptThread");
            return;
        }

        public void cancel() {
            Log.d(BluetoothReceiver.TAG, "cancel " + this);
            try {
                this.mmServerSocket.close();
            } catch (IOException e) {
                Log.e(BluetoothReceiver.TAG, "close() of server failed", e);
            }
        }
    }

    private class ConnectedThread extends BluetoothThread {
        private final InputStream mmInStream;
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            super();
            Log.d(BluetoothReceiver.TAG, "create ConnectedThread");
            this.mmSocket = socket;
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(BluetoothReceiver.TAG, "temp sockets not created", e);
            }
            this.mmInStream = tmpIn;
        }

        public void run() {
            Log.i(BluetoothReceiver.TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    int bytes = this.mmInStream.read(buffer);
                    if (BluetoothReceiver.this.listener != null) {
                        BluetoothReceiver.this.listener.messageReceived(buffer, bytes);
                    }
                } catch (IOException e) {
                    Log.e(BluetoothReceiver.TAG, "disconnected", e);
                    cancel();
                    BluetoothReceiver.this.connectionLost();
                    return;
                }
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e(BluetoothReceiver.TAG, "close() of connect socket failed", e);
            }
        }
    }

    public void registerListener(iBTMessageListener l) {
        this.listener = l;
    }

    private void deleteThread(BluetoothThread t) {
        if (t != null) {
            t.cancel();
        }
    }

    private void deleteAllThreads() {
        deleteThread(this.mConnectedThread);
        deleteThread(this.mAcceptThread);
    }

    public void start() {
        Log.d(TAG, "start");
        deleteThread(this.mConnectedThread);
        if (this.mAcceptThread == null) {
            this.mAcceptThread = new AcceptThread();
            this.mAcceptThread.start();
        }
        setState(1);
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");
        deleteAllThreads();
        setState(0);
    }

    private void setState(int state) {
        this.mState = state;
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected from " + device.getName());
        deleteAllThreads();
        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();
        setState(3);
    }

    private void connectionLost() {
        setState(1);
    }
}
