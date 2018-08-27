package com.noisepages.nettoyeur.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.noisepages.nettoyeur.common.RawByteReceiver;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothSppConnection {
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "BluetoothSppManager";
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private final int bufferSize;
    private ConnectThread connectThread = null;
    private ConnectedThread connectedThread = null;
    private volatile State connectionState = State.NONE;
    private final BluetoothSppObserver sppObserver;
    private final RawByteReceiver sppReceiver;

    private class ConnectThread extends Thread {
        private final BluetoothDevice device;
        private final BluetoothSocket socket;

        private ConnectThread(BluetoothDevice device, UUID uuid) throws IOException {
            this.device = device;
            this.socket = device.createRfcommSocketToServiceRecord(uuid);
        }

        public void run() {
            try {
                this.socket.connect();
                BluetoothSppConnection.this.connected(this.socket, this.device);
            } catch (IOException e) {
                BluetoothSppConnection.this.connectionFailed();
                try {
                    this.socket.close();
                } catch (IOException e2) {
                    Log.e(BluetoothSppConnection.TAG, "Unable to close socket after connection failure", e2);
                }
            }
        }

        private void cancel() {
            try {
                this.socket.close();
            } catch (IOException e) {
                Log.e(BluetoothSppConnection.TAG, "Unable to close socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream inStream;
        private final OutputStream outStream;
        private final BluetoothSocket socket;

        private ConnectedThread(BluetoothSocket socket) throws IOException {
            this.socket = socket;
            this.inStream = socket.getInputStream();
            this.outStream = socket.getOutputStream();
        }

        public void run() {
            byte[] buffer = new byte[BluetoothSppConnection.this.bufferSize];
            while (true) {
                try {
                    BluetoothSppConnection.this.sppReceiver.onBytesReceived(this.inStream.read(buffer), buffer);
                } catch (IOException e) {
                    BluetoothSppConnection.this.connectionLost();
                    return;
                }
            }
        }

        private void write(byte[] buffer, int offset, int count) throws IOException {
            this.outStream.write(buffer, offset, count);
        }

        private void cancel() {
            try {
                this.socket.close();
            } catch (IOException e) {
                Log.e(BluetoothSppConnection.TAG, "Unable to close socket", e);
            }
        }
    }

    public enum State {
        NONE,
        CONNECTING,
        CONNECTED
    }

    public BluetoothSppConnection(BluetoothSppObserver observer, RawByteReceiver receiver, int bufferSize) throws BluetoothUnavailableException, BluetoothDisabledException {
        if (this.btAdapter == null) {
            throw new BluetoothUnavailableException();
        } else if (this.btAdapter.isEnabled()) {
            this.sppObserver = observer;
            this.sppReceiver = receiver;
            this.bufferSize = bufferSize;
        } else {
            throw new BluetoothDisabledException();
        }
    }

    public State getConnectionState() {
        return this.connectionState;
    }

    public synchronized void stop() {
        cancelThreads();
        setState(State.NONE);
    }

    public void connect(String addr) throws IOException {
        connect(addr, SPP_UUID);
    }

    public synchronized void connect(String addr, UUID uuid) throws IOException {
        cancelThreads();
        this.connectThread = new ConnectThread(this.btAdapter.getRemoteDevice(addr), uuid);
        this.connectThread.start();
        setState(State.CONNECTING);
    }

    public void write(byte[] out, int offset, int count) throws IOException {
        ConnectedThread thread;
        synchronized (this) {
            if (this.connectionState != State.CONNECTED) {
                throw new BluetoothNotConnectedException();
            }
            thread = this.connectedThread;
        }
        thread.write(out, offset, count);
    }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) throws IOException {
        this.connectThread = null;
        cancelConnectedThread();
        this.connectedThread = new ConnectedThread(socket);
        this.connectedThread.start();
        this.sppObserver.onDeviceConnected(device);
        setState(State.CONNECTED);
    }

    private void connectionFailed() {
        setState(State.NONE);
        this.sppObserver.onConnectionFailed();
    }

    private void connectionLost() {
        setState(State.NONE);
        this.sppObserver.onConnectionLost();
    }

    private void cancelThreads() {
        if (this.connectThread != null) {
            this.connectThread.cancel();
            this.connectThread = null;
        }
        cancelConnectedThread();
    }

    private void cancelConnectedThread() {
        if (this.connectedThread != null) {
            this.connectedThread.cancel();
            this.connectedThread = null;
        }
    }

    private void setState(State state) {
        this.connectionState = state;
    }
}
