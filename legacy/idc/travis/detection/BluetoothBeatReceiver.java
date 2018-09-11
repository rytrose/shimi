package idc.travis.detection;

import idc.travis.TravisDLDActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.bluetooth.*;
import android.os.Handler;
import android.util.Log;

public class BluetoothBeatReceiver {

    // Debugging
    private static final String TAG = "BluetoothBeatReceiver";
    private static final boolean D = true;

    // Bluetooth
	private BluetoothAdapter mAdapter;
	private AcceptThread mAcceptThread;
	private ConnectedThread mConnectedThread;

    private int mState;
	private Handler mHandler;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

	
    private static final String NAME = "BluetoothBeatReceiver";
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public BluetoothBeatReceiver(Handler handler) {
	
        // Get local Bluetooth adapter
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        
        mHandler = handler;

	}
	
	private void deleteThread(BluetoothThread t){

		if (t != null) {
			t.cancel(); 
			t = null;
		}
	}
	
	private void deleteAllThreads() {
		deleteThread(mConnectedThread);
		deleteThread(mAcceptThread);
	}

	public void start() {

    	if (D) Log.d(TAG, "start");

		deleteThread(mConnectedThread);

        
		// Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

        setState(STATE_LISTEN);

	}

    public synchronized void stop() {

    	if (D) Log.d(TAG, "stop");
    	deleteAllThreads();
        setState(STATE_NONE);
    }
    
    

    private void setState(int state) {
    	mState = state;		
	}

    
    private abstract class BluetoothThread extends Thread {
    	public abstract void cancel();
    }
    
	/**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends BluetoothThread {

    	// The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {

        	if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothBeatReceiver.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected from " + device.getName());

        deleteAllThreads();
        
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

     }


    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends BluetoothThread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {

        	Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;

            // Get the BluetoothSocket input stream
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(TravisDLDActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    cancel();
                    connectionLost();
                    break;
                }
            }
        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
	
}
