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

/*
 * slave class for handling all the message commands from server and reply accordingly.
 */
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
	private Socket toServer;

	public Slave(int serverPort, String serverIP, int myPort) {
		try {
			this.serverPort = serverPort;
			this.serverIP = InetAddress.getByName(serverIP);
			this.myPort = myPort;
			this.running = true;
			this.clientListener = new ServerSocket(this.myPort);
			this.processes = new ConcurrentHashMap<Integer, ProcessInfo>();
			this.processThreads = new ConcurrentHashMap<Integer, Thread>();

		} catch (IOException e) {

			e.printStackTrace();
			System.out
					.println("Failed to create a listening socket for slave.");
			System.exit(0);
		}
	}

	private void startTimer() {
		this.statusChecker = new Timer(true);
		TimerTask task = new TimerTask() {
			public void run() {
				checkProcessStatus();
			}
		};
		this.statusChecker.schedule(task, 0, 1000);
	}

	private void checkProcessStatus() {
		for (int i : this.processThreads.keySet()) {
			Thread t = this.processThreads.get(i);
			if (!t.isAlive()) {
				this.processThreads.remove(i);
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
			s.toServer = toServer;
			s.serverOut = new ObjectOutputStream(toServer.getOutputStream());
			s.serverOut.flush();

			s.serverIn = new ObjectInputStream(toServer.getInputStream());

		} catch (IOException e) {
			System.exit(-1);
		}
		s.startTimer();
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
				case SHUTDOWN:
					System.out.println("server turn down,close...");
					s.terminate();
					System.exit(0);
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
		try {
			MigratableProcess p = this.processes.get(pid).getProcess();
			p.kill();
			this.processes.remove(pid);
			this.processThreads.remove(pid);
			Message m = new Message(pid, msgType.KILLDONE);
			sendToServer(m);
		} catch (Exception e) {
			Message m = new Message(pid, msgType.KILLFAIL);
			sendToServer(m);
		}
	}

	private void handleStartProcess(Message received) {
		try {
			ProcessInfo pI = received.getProcessInfo();
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
				System.out.println("try to connect to slave "+received.getDestHost()+" port: "+received.getDestPort());
				Socket toSlave = new Socket(received.getDestHost(),
						received.getDestPort());
				System.out.println("create socket succeed");
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
				this.processes.remove(pID);
				this.processThreads.remove(pID);
				
				SlaveToSlave handler = new SlaveToSlave(toSlave, toSend);
				new Thread(handler).start();

				sendToServer(toSend);
			} catch (IOException e) {
				Message m = new Message(e.getMessage(), received.getProId(),
						msgType.MIGRATEFAIL);
				sendToServer(m);

			}
		} else if (received.getDestID() == this.slaveID) {
			try {
				System.out.println("slave begin listen at "+myPort+" for migration");
				Socket toSlave = this.clientListener.accept();
				ObjectInputStream in = new ObjectInputStream(
						toSlave.getInputStream());
				System.out.println("listen succeed for migration");
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

	private synchronized void sendToServer(Message s) {
		synchronized (this.toServer) {
			if (s.getResponType() != msgType.HEARTACK)
				System.out.println("send message: " + s.getResponType() + "\t");
			try {

				this.serverOut.writeObject(s);
				this.serverOut.flush();
			}

			catch (Exception e) {
				e.printStackTrace();
				System.out.println("slave send fail");
			}
		}
	}

	public void setSlaveID(int ID) {
		this.slaveID = ID;
	}

	public int getSlaveID() {
		return this.slaveID;
	}
	private void terminate() {

		try {
			this.toServer.close();
			System.gc();
		} catch (IOException e) {

		}
	}
}