package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
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
	private ConcurrentHashMap<Integer, Thread> processThreads;
	private Timer statusChecker;

	public Slave(int serverPort, String serverIP, int myPort) {
		try {
			this.serverPort = serverPort;
			this.serverIP = InetAddress.getByName(serverIP);
			this.myPort = myPort;
			this.running = true;
			this.clientListener = new ServerSocket(this.myPort);
			this.processes = new ConcurrentHashMap<Integer, ProcessInfo>();
			this.processThreads = new ConcurrentHashMap<Integer, Thread>();
			this.startTimer();

		} catch (IOException e) {

			e.printStackTrace();
			System.out
					.println("Failed to create a listening socket for slave.");
			System.exit(0);
		}
	}

	private void startTimer(){
		this.statusChecker = new Timer(true);
		TimerTask task = new TimerTask(){
			public void run(){
				checkProcessStatus();
			}
		};
		this.statusChecker.schedule(task, 0, 1000);
	}
	
	private void checkProcessStatus(){
		for (int i : this.processThreads.keySet()){
			Thread t = this.processThreads.get(i);
			if (!t.isAlive()){
				Message m = new Message(i, msgType.FINISH);
				this.sendToServer(m);
			}
		}
	}
	
	public static void main(String args[]) {
		Slave s = new Slave(Integer.parseInt(args[0]), args[1],
				Integer.parseInt(args[2]));
		try {
			Socket toServer = new Socket(s.serverIP, s.serverPort);

			s.serverOut = new ObjectOutputStream(toServer.getOutputStream());
			s.serverOut.flush();

			s.serverIn = new ObjectInputStream(toServer.getInputStream());

		} catch (IOException e) {
			System.exit(-1);
		}
		System.out.println("listen");
		while (s.running) {

			Message recvMessage;
			try {
				recvMessage = (Message) s.serverIn.readObject();
				if (recvMessage.getResponType() != msgType.HEART)
					System.out.println("worker message received: id "
							+ recvMessage.getResponType());
				switch (recvMessage.getResponType()) {
				case CONNECT:
					s.handleFirstConnection(recvMessage);
					break;
				case START:
					s.handleStartProcess(recvMessage);
					break;
				case MIGRATENOTI:
					s.handleMigration(recvMessage);
					break;
				case KILL:
					s.handleKillProcess(recvMessage);
					break;
				case HEART:
					s.handlePollingReq(recvMessage);
					break;
				default:
					System.out
							.println("Unrecognized message received from server at Slave: "
									+ Integer.valueOf(s.slaveID).toString());
					break;
				}
			} catch (IOException e) {

			} catch (ClassNotFoundException e) {
				continue;
			}

		}
	}

	private void handlePollingReq(Message recvMessage) {
		Message m = new Message(msgType.HEARTACK);
		try {
			sendToServer(m);
		} catch (Exception e) {
			System.out.println("send heart error");
		}

	}

	private void handleKillProcess(Message received) {
		int pid = received.getProId();
		MigratableProcess p = this.processes.get(pid).getProcess();
		p.kill();
		this.processes.remove(pid);
		this.processThreads.remove(pid);
	}

	private void handleStartProcess(Message received) {
		try {
			ProcessInfo pI = received.getProcessInfo();
			// ProcessInfo p2=pI;
			Message rep = new Message(pI, received.getProId(),
					msgType.STARTDONE);
			sendToServer(rep);
			MigratableProcess p = pI.getProcess();
			Thread t = new Thread(p);
			t.start();
			pI.setStatus(Status.RUNNING);
			this.processes.put(pI.getId(), pI);
			this.processThreads.put(pI.getId(), t);

		} catch (Exception e) {
			Message rep = new Message(e.getMessage(), msgType.STARTFAIL);
			sendToServer(rep);
		}
	}

	private void handleMigration(Message received) {
		if (received.getSourceID() == this.slaveID) {
			try {
				Socket toSlave = new Socket(received.getDestHost(),
						received.getDestPort());
				int pID = received.getProId();
				ProcessInfo pI = this.processes.get(new Integer(pID));
				pI.getProcess().suspend();
				if (pI.getProcess().getInputStream() != null) {
					pI.getProcess().getInputStream().changeMigrated(true);
				}
				if (pI.getProcess().getOutputStream() != null) {
					pI.getProcess().getOutputStream().changeMigrated(true);
				}
				Message toSend = new Message(msgType.MIGRATEBEGIN);
				pI.setStatus(Status.MIGRATING);
				toSend.setProcessInfo(pI);
				SlaveToSlave handler = new SlaveToSlave(toSlave, toSend);
				new Thread(handler).start();

			} catch (IOException e) {
				Message m = new Message(e.getMessage(), received.getProId(),
						msgType.MIGRATEFAIL);
				sendToServer(m);

			}
		} else if (received.getDestID() == this.slaveID) {
			try {
				Socket toSlave = this.clientListener.accept();
				ObjectInputStream in = new ObjectInputStream(
						toSlave.getInputStream());
				Message rcvd = (Message) in.readObject();
				ProcessInfo pI = rcvd.getProcessInfo();
				Thread t = new Thread(pI.getProcess());
				t.start();
				pI.setStatus(Status.RUNNING);
				this.processes.put(pI.getId(), pI);
				this.processThreads.put(pI.getId(), t);
				Message rep = new Message(msgType.MIGRATEDONE, pI.getId(),
						received.getSourceID(), received.getDestID());
				sendToServer(rep);

			} catch (IOException e) {
				Message m = new Message(e.getMessage(), received.getProId(),
						msgType.MIGRATEFAIL);
				sendToServer(m);
			} catch (ClassNotFoundException e) {
				Message m = new Message("read class error",
						received.getProId(), msgType.MIGRATEFAIL);
				sendToServer(m);
			}
		}
	}

	private void handleFirstConnection(Message received) {
		this.slaveID = received.getSlaveID();
		Message m = new Message(msgType.CONNECT);
		try {
			m.setDestIP(InetAddress.getLocalHost());
			m.setDestPort(myPort);
			sendToServer(m);
		} catch (IOException e) {

		}

	}

	private void sendToServer(Message s) {
		if (s.getResponType() != msgType.HEARTACK)
			System.out.println("send message: " + s.getResponType() + "\t");
		try {
			synchronized(this.serverOut){
				this.serverOut.writeObject(s);
				this.serverOut.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("slave send fail");
		}
	}

	public void setSlaveID(int ID) {
		this.slaveID = ID;
	}

	public int getSlaveID() {
		return this.slaveID;
	}
}