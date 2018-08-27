package mr1.core.network;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpCallbackServer implements Runnable {
    private static final String TAG = "Tcp Server";
    public boolean accept = true;
    protected iOnRecivedData mOnRecivedDatadListener;
    public int mPort;
    public boolean recive = true;
    /* renamed from: s */
    private Socket f7s;
    private ServerSocket ss;

    public interface iOnRecivedData {
        void recivedData(String str);
    }

    public void setOnRecivedDataListener(iOnRecivedData l) {
        this.mOnRecivedDatadListener = l;
    }

    public TcpCallbackServer(int port) {
        this.mPort = port;
    }

    public void run() {
        while (this.accept) {
            try {
                this.recive = true;
                this.ss = new ServerSocket(this.mPort);
                this.f7s = this.ss.accept();
                Log.d(TAG, "Accepted connection from " + this.f7s.getRemoteSocketAddress().toString());
                while (this.recive) {
                    String st = new BufferedReader(new InputStreamReader(this.f7s.getInputStream())).readLine();
                    if (st.equals("exit")) {
                        this.recive = false;
                    } else if (this.mOnRecivedDatadListener != null) {
                        this.mOnRecivedDatadListener.recivedData(st);
                    }
                }
                this.f7s.close();
            } catch (UnknownHostException e) {
                Log.d(TAG, "Unknown Host " + e.getMessage());
                e.printStackTrace();
                return;
            } catch (IOException e2) {
                Log.d(TAG, "IO Exception " + e2.getMessage());
                e2.printStackTrace();
                return;
            }
        }
    }

    public void stop() {
        try {
            this.recive = false;
            this.accept = false;
            if (this.f7s != null) {
                this.f7s.close();
            }
            if (this.ss != null) {
                this.ss.close();
            }
            Log.d("Tcp Example", "Closed sockets");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
