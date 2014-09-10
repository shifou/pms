package main;

import data.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.net.Socket;
import java.util.Vector;

import data.ProcessInfo;

public class Connection implements Runnable {

	private int slaveId;
	private Socket socket;
	private volatile boolean running;

	private ObjectInputStream objInput;
	private ObjectOutputStream objOutput;

	public Connection(int conId, Socket slaveSocket) throws IOException {
		slaveId = conId;
		socket = slaveSocket;
		objInput = new ObjectInputStream(slaveSocket.getInputStream());
		objOutput = new ObjectOutputStream(slaveSocket.getOutputStream());

	}

	@Override
	public void run() {
		try {
			Message receiveMessage;
			while (running) {
				try {
					receiveMessage = (Message) objInput.readObject();
				} catch (ClassNotFoundException e) {
					continue;
				}
				System.out.println("slave receive message: "+ receiveMessage.getResponType());
				switch (receiveMessage.getResponType()) {
				case STARTFAIL:
				case STARTDONE:
				case FINISH:
					handleSTART(receiveMessage);
					break;
				case MIGRATEBEGINFAIL:
				case MIGRATEDONE:
				case MIGRATEFAIL:
					handleMIGRATE(receiveMessage);
					break;
				case KILLDONE:
				case KILLFAIL:
					handleKILL(receiveMessage);
					break;
				default:
					System.out.println("unknow type");
				}
			}
		} catch (IOException e) {
			System.out.println("read message error in connection");

		}

	}

	private void handleMIGRATE(Message receiveMessage) {
		// TODO Auto-generated method stub
		
	}

	private void handleSTART(Message receiveMessage) {
		
		switch (receiveMessage.getResponType()) {
		case STARTFAIL:
			System.out.println("slave "+slaveId+" start process "+receiveMessage.getProcessinfo().getId()+" fail "+receiveMessage.getData());
			break;
		case STARTDONE:
			Manager.manager.slaves.put(slaveId, socket);
			int hold = receiveMessage.getProcessinfo().getId();
			Manager.manager.addProcess(slaveId, hold);
			System.out.println("slave "+slaveId+" start process "+hold+" success");
			break;
		case FINISH:
			Manager.manager.removeProcess(slaveId, receiveMessage.getProcessinfo().getId());
		}
	}

	private void handleKILL(Message receiveMessage) {
		// TODO Auto-generated method stub
		
	}

	public int send(Message mes) throws IOException {
		try
		{
			objOutput.writeObject(mes);
			objOutput.flush();
		}catch(Exception e)
		{
			return 0;
		}
		return 1;
	}

	public void stop() {
		running = false;
	}
}
