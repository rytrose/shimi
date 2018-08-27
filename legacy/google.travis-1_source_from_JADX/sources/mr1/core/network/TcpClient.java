package mr1.core.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {
    private Socket clientSocket = new Socket();
    private String exitMessage = "exit";
    DataOutputStream outToServer;

    public void sendToServer(String message) throws Exception {
        System.out.println("Sending to server: " + message);
        this.outToServer.writeBytes(new StringBuilder(String.valueOf(message)).append('\n').toString());
    }

    public boolean connect(int port, byte[] addressInBytes) {
        try {
            System.out.println("Connecting to server...");
            this.clientSocket = new Socket(InetAddress.getByAddress(addressInBytes), port);
            System.out.println("Connected");
            this.outToServer = new DataOutputStream(this.clientSocket.getOutputStream());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void disconnect() throws Exception {
        if (this.clientSocket.isConnected()) {
            System.out.println("Disconnecting from server...");
            sendToServer(this.exitMessage);
            this.clientSocket.close();
        }
    }

    public boolean isConnected() {
        return this.clientSocket.isConnected();
    }
}
