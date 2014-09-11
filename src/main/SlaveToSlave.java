/*
 * small thread used for slave-slave communication
 */
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
			System.out.println("create slave to slave socket failed");
		}
	}
	
	@Override
	public void run() {
		try {
			out.writeObject(this.toSend);
			out.flush();
			//out.close();
			//toSlave.close();
		} catch (IOException e){
			System.out.println("trying to write to another slave failed");
		}
	}

}
