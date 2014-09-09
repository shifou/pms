package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Slave implements Runnable {

	private ServerSocket clientListener;

	
	public Slave(int portNum) throws IOException{
		try {
			this.clientListener = new ServerSocket(portNum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to create a server socket for slave.");
			System.exit(0);
		}
	}
	
	@Override
	public void run() {
		
	}
}
