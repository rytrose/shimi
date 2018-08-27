package mr1.core.network.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPListener {
    DatagramSocket serverSocket;

    public UDPListener(int port) {
        try {
            this.serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        while (true) {
            try {
                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                this.serverSocket.receive(receivePacket);
                System.out.println("RECEIVED: " + new String(receivePacket.getData()).substring(0, 5));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
