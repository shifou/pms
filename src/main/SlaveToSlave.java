package main;

import java.io.IOException;

import java.io.ObjectOutputStream;
import java.net.Socket;

import data.Message;

public class SlaveToSlave implements Runnable {

	private Socket toSlave;
	private Message toSend;
	private ObjectOutputStream out;
	
	public SlaveToSlave(Socket toSlave, Message toSend){
		this.toSend = toSend;
		this.toSlave = toSlave;
		try {
			this.out = new ObjectOutputStream(this.toSlave.getOutputStream());
			this.out.flush();
		} catch (IOException e){
			
		}
	}
	
	@Override
	public void run() {
		try {
			out.writeObject(this.toSend);
			out.flush();
			out.close();
		} catch (IOException e){
			
		}
	}

}
