package main;

import java.net.Socket;

import data.Message;

public class SlaveToSlave implements Runnable {

	private Socket toSlave;
	private Message toSend;
	
	public SlaveToSlave(Socket toSlave, Message toSend){
		this.toSend = toSend;
		this.toSlave = toSlave;
	}
	
	@Override
	public void run() {
		
	}

}
