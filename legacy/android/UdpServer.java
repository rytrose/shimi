package com.example.annie_travislistening;

import java.io.IOException;
import java.net.*;

public class UdpServer {

	private DatagramSocket serverSocket;
	public String message = "";
	
	public UdpServer(int port) {
		try {
			serverSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public void listen() {
    	new Thread(){
			public void run(){
				
				try {
					byte[] buffer = new byte[1024];
					System.out.println("Listening...");

					while(true)
                	{
						DatagramPacket data = new DatagramPacket(buffer, buffer.length);
                    	serverSocket.receive(data);
                    	String sentence = new String(buffer, 0, data.getLength());
                    	System.out.println("RECEIVED: " + sentence);
                    	message = sentence;
                	}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
    	}.start();
    }
    
    public String update() {
    	if (message == null)
			message = "";
		return message;
	}
    
	public void closeConnection(){
		serverSocket.close();
	}
}
