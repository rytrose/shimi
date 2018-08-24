package com.example.annie_travislistening;

import android.annotation.SuppressLint;
import java.net.*;

@SuppressLint("NewApi")
public class UdpClient {

	private static String ip;

	   private static int port;

	   

	   public UdpClient(String ipaddr, int pnum){

	       ip = ipaddr;

	       port = pnum;

	   }

	   

	public void sendPara(String data) throws Exception

	   {

	       try (DatagramSocket clientSocket = new DatagramSocket()) {

	           InetAddress IPAddress = InetAddress.getByName(ip);

	           byte[] sendData = data.getBytes();

	           DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

	           clientSocket.send(sendPacket);

	           clientSocket.close();

	       }

	   }
	   
}
