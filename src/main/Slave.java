package main;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import process.MigratableProcess;
import data.*;



public class Slave implements Runnable {

	private ServerSocket clientListener;
	private String serverHost;
	private int myPort;
	private int serverPort;
	private boolean running;
	private int slaveID;
	private ObjectInputStream serverIn;
	private ObjectOutputStream serverOut;
	private ConcurrentHashMap<Integer, ProcessInfo> processes;

	public Slave(int serverPort, String serverHost, int myPort)
			throws IOException {
		try {
			this.serverHost = serverHost;
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

	@Override
	public void run() {
		try {
			Socket toServer = new Socket(this.serverHost, this.serverPort);
			this.serverIn = new ObjectInputStream(toServer.getInputStream());
			this.serverOut = new ObjectOutputStream(toServer.getOutputStream());

		} catch (IOException e) {
		}
		while (this.running){
			Message recvMessage;
			try {
				recvMessage = (Message) serverIn.readObject();
				System.out.println("worker message received: id "
						+ recvMessage.getResponType());
				switch (recvMessage.getResponType()) {
				case START:
					handleStartProcess(recvMessage);
					break;
				case MIGRATEBEGIN:
					handleMigration(recvMessage);
					break;
				case KILL:
					handleKillProcess(recvMessage);
					break;
				default:
					System.out
							.println("Unrecognized message received from server at Slave: "
									+ Integer.valueOf(this.slaveID)
											.toString());
				}
			} catch (IOException e){
				
			}catch (ClassNotFoundException e){
				continue;
			}
			
		}
	}

	private void handleKillProcess(Message received) {
		ProcessInfo pI = received.getProcessInfo();
		MigratableProcess p = pI.getProcess();
		p.kill();
		this.processes.remove(pI.getId());
		

	private void handleStartProcess(Message received) {
		ProcessInfo pI = received.getProcessInfo();
		MigratableProcess p = pI.getProcess();
		new Thread(p).start();
		this.processes.put(pI.getId(),pI);
		
	}

	private void handleMigration(Message received){
		if (received.getSourceID() == this.slaveID){
			try {
				Socket toSlave = new Socket(received.getDestHost(), received.getDestPort());
				
				
			} catch (IOException e){
				
			}
		}
		else if (received.getDestID() == this.slaveID){
			
		}
	}
	
	public void setSlaveID(int ID) {
		this.slaveID = ID;
	}

	public int getSlaveID() {
		return this.slaveID;
	}
}
