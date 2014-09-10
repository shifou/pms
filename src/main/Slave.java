package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import process.MigratableProcess;
import data.*;

public class Slave {

	private ServerSocket clientListener;
	private InetAddress serverIP;
	private int myPort;
	private int serverPort;
	private boolean running;
	private int slaveID;
	private ObjectInputStream serverIn;
	private ObjectOutputStream serverOut;
	private ConcurrentHashMap<Integer, ProcessInfo> processes;

	public Slave(int serverPort, String serverIP, int myPort) {
		try {
			this.serverIP = InetAddress.getByName(serverIP);
			this.myPort = myPort;
			this.running = true;
			this.clientListener = new ServerSocket(this.myPort);
			this.processes = new ConcurrentHashMap<Integer, ProcessInfo>();

		} catch (IOException e) {

			e.printStackTrace();
			System.out
					.println("Failed to create a listening socket for slave.");
			System.exit(0);
		}
	}

	public static void main(String args[]) {
		Slave s = new Slave(Integer.parseInt(args[0]), args[1],
				Integer.parseInt(args[2]));
		try {
			Socket toServer = new Socket(s.serverIP, s.serverPort);
			s.serverIn = new ObjectInputStream(toServer.getInputStream());
			s.serverOut = new ObjectOutputStream(toServer.getOutputStream());

		} catch (IOException e) {
		}
		while (s.running) {
			Message recvMessage;
			try {
				recvMessage = (Message) s.serverIn.readObject();
				System.out.println("worker message received: id "
						+ recvMessage.getResponType());
				switch (recvMessage.getResponType()) {
				case START:
					s.handleStartProcess(recvMessage);
					break;
				case MIGRATEBEGIN:
					s.handleMigration(recvMessage);
					break;
				case KILL:
					s.handleKillProcess(recvMessage);
					break;
				default:
					System.out
							.println("Unrecognized message received from server at Slave: "
									+ Integer.valueOf(s.slaveID).toString());
				}
			} catch (IOException e) {

			} catch (ClassNotFoundException e) {
				continue;
			}

		}
	}

	private void handleKillProcess(Message received) {
		ProcessInfo pI = received.getProcessInfo();
		MigratableProcess p = pI.getProcess();
		p.kill();
		this.processes.remove(pI.getId());
	}

	private void handleStartProcess(Message received) {
		ProcessInfo pI = received.getProcessInfo();
		MigratableProcess p = pI.getProcess();
		new Thread(p).start();
		this.processes.put(pI.getId(), pI);

	}

	private void handleMigration(Message received) {
		if (received.getSourceID() == this.slaveID) {
			try {
				Socket toSlave = new Socket(received.getDestHost(),
						received.getDestPort());
				Message toSend = new Message(msgType.MIGRATEBEGIN);

			} catch (IOException e) {

			}
		} else if (received.getDestID() == this.slaveID) {
			try {
				Socket toSlave = this.clientListener.accept();

			} catch (IOException e) {

			}
		}
	}

	public void setSlaveID(int ID) {
		this.slaveID = ID;
	}

	public int getSlaveID() {
		return this.slaveID;
	}
}
