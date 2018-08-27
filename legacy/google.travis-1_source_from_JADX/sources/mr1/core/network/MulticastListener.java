package mr1.core.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MulticastListener {
    private InetAddress group;
    private String ipAddress;
    private volatile boolean listeningOnNetwork;
    private List<String> myList = new ArrayList();
    private int port;
    /* renamed from: s */
    private MulticastSocket f6s;

    /* renamed from: mr1.core.network.MulticastListener$1 */
    class C00211 extends Thread {
        C00211() {
        }

        public void run() {
            MulticastListener.this.listeningOnNetwork = true;
            System.out.println("waiting for response1");
            try {
                byte[] buffer = new byte[10240];
                DatagramPacket data = new DatagramPacket(buffer, buffer.length);
                while (MulticastListener.this.listeningOnNetwork) {
                    MulticastListener.this.f6s.receive(data);
                    String dataString = new String(buffer, 0, data.getLength());
                    System.out.println("Received message: " + dataString);
                    MulticastListener.this.myList.add(dataString);
                    System.out.println("size = " + MulticastListener.this.myList.size());
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /* renamed from: mr1.core.network.MulticastListener$2 */
    class C00222 extends Thread {
        C00222() {
        }

        public void run() {
            MulticastListener networkListener = new MulticastListener("224.0.80.8", 34562);
            networkListener.listen();
            while (true) {
                System.out.println("listening");
                String response = networkListener.update();
                System.out.println(response);
                if (response.equals("Greeting from Mason")) {
                    System.out.println("received message = " + response);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("listening");
            }
            networkListener.closeConnection();
        }
    }

    public MulticastListener(String IP, int port) {
        this.ipAddress = IP;
        this.port = port;
        try {
            this.group = InetAddress.getByName(this.ipAddress);
            this.f6s = new MulticastSocket(port);
            this.f6s.joinGroup(this.group);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public void listen() {
        new C00211().start();
    }

    public void send(String msg) {
        try {
            this.f6s.send(new DatagramPacket(msg.getBytes(), msg.length(), this.group, this.port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            this.listeningOnNetwork = false;
            this.f6s.leaveGroup(this.group);
            this.f6s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String update() {
        System.out.println("test");
        String returnString = "";
        synchronized (this.myList) {
            List<String> x = new ArrayList(this.myList);
            this.myList.clear();
        }
        if (x.size() == 0) {
            return "";
        }
        return (String) x.get(x.size() - 1);
    }

    public static void main() {
        new C00222().start();
    }
}
