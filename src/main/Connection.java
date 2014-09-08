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
	private boolean running;

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
				System.out.println("worker message received: id "
						+ receiveMessage.getResponType());
				switch (receiveMessage.getResponType()) {
				case START:
					handleSTART(receiveMessage);
					break;
				case MIGARATESOURCERES:
					handleMigrateSourceRes(receiveMessage);
					break;
				case MIGRATETARGETRES:
					hanleMigrateTargetRes(receiveMessage);
					break;
				case KILL:
					handleKILL(receiveMessage);
					break;
				default:
					System.out.println("unrecagnized message");
				}
			}
		} catch (IOException e) {

		}

	}

	private void handleSTART(Message receiveMessage) {
		// TODO Auto-generated method stub
		
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

	public Vector<ProcessInfo> getProcessInfos() {
		// TODO Auto-generated method stub
		return null;
	}

}
