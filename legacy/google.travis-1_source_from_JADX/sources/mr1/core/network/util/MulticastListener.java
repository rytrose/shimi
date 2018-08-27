package mr1.core.network.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MulticastListener {
    private InetAddress group;
    private String idString;
    private String ipAddress;
    public volatile boolean listeningOnNetwork = false;
    private List<String> myList = new ArrayList();
    private int port;
    /* renamed from: s */
    private MulticastSocket f8s;
    public int uniqueID = -1;

    /* renamed from: mr1.core.network.util.MulticastListener$1 */
    class C00231 extends Thread {
        C00231() {
        }

        public void run() {
            try {
                byte[] buffer = new byte[4194304];
                DatagramPacket data = new DatagramPacket(buffer, buffer.length);
                System.out.println("listening thread started");
                MulticastListener.this.f8s.setSoTimeout(0);
                MulticastListener.this.listeningOnNetwork = true;
                while (MulticastListener.this.listeningOnNetwork) {
                    MulticastListener.this.f8s.receive(data);
                    String dataString = new String(buffer, 0, data.getLength());
                    System.out.println("Received: " + dataString);
                    System.out.println(data.getAddress().toString());
                    if (dataString.contains("ping")) {
                        MulticastListener.this.send("pong");
                    } else if (!dataString.contains("pong")) {
                        MulticastListener.this.myList.add(dataString);
                    }
                    if (dataString.contains("timeTest1")) {
                        MulticastListener.this.send("timeTest2" + Integer.toString(MulticastListener.this.uniqueID));
                    }
                    data = new DatagramPacket(buffer, buffer.length);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public MulticastListener(String IP, int port) {
        this.ipAddress = IP;
        this.port = port;
        try {
            this.group = InetAddress.getByName(this.ipAddress);
            this.f8s = new MulticastSocket(port);
            this.f8s.joinGroup(this.group);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        setUniqueID();
    }

    public String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                Enumeration<InetAddress> enumIpAddr = ((NetworkInterface) en.nextElement()).getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return new String(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
        }
        return null;
    }

    public void listen() {
        new C00231().start();
    }

    public void send(String msg) {
        try {
            this.f8s.send(new DatagramPacket(msg.getBytes(), msg.length(), this.group, this.port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setIpString(String ip) {
        this.idString = ip;
    }

    private void setUniqueID() {
        int count = 0;
        try {
            send("ping");
            byte[] buffer = new byte[10240];
            DatagramPacket data = new DatagramPacket(buffer, buffer.length);
            this.f8s.setSoTimeout(1000);
            while (true) {
                this.f8s.receive(data);
                String dataString = new String(buffer, 0, data.getLength());
                System.out.println("Received1: " + dataString);
                this.myList.add(dataString);
                count++;
            }
        } catch (SocketTimeoutException e) {
            this.uniqueID = 0;
            System.out.println("my uniqueID is " + this.uniqueID);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            this.listeningOnNetwork = false;
            this.f8s.leaveGroup(this.group);
            this.f8s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String update() {
        String returnString = "";
        synchronized (this.myList) {
            List x = new ArrayList(this.myList);
            this.myList.clear();
        }
        if (x.size() == 0) {
            return "";
        }
        String[] splits = ((String) x.get(x.size() - 1)).split(",");
        for (int i = 0; i < splits.length - 1; i++) {
            if (this.uniqueID == Integer.parseInt(splits[i].trim())) {
                return splits[splits.length - 1];
            }
        }
        return "";
    }

    public String altUpdate() {
        String returnString = "";
        synchronized (this.myList) {
            List x = new ArrayList(this.myList);
            this.myList.clear();
        }
        if (x.size() == 0) {
            return "";
        }
        return (String) x.get(x.size() - 1);
    }
}
