/*
 * Main class for accepting the commandline and send to slaves through server instance
 * have heartbeat thread to check the slavestatus every 5 seconds with 5 failure chance.
 */
package main;

import main.Server;
import data.*;
import process.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketException;

public class Manager {
	Server server;
	private int port;
	public int proID;
	public Timer monitor;
	private BufferedReader console;
	public ConcurrentHashMap<Integer, Socket> slaves;
	public ConcurrentHashMap<Integer, Integer> slaveStatus;
	public ConcurrentHashMap<Integer, Connection> con;
	public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ProcessInfo>> processes;

	public Manager(int listenPort) {
		port = listenPort;
		proID = 1;
		console = new BufferedReader(new InputStreamReader(System.in));
		slaves = new ConcurrentHashMap<Integer, Socket>();
		slaveStatus = new ConcurrentHashMap<Integer, Integer>();
		con = new ConcurrentHashMap<Integer, Connection>();
		processes = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ProcessInfo>>();
	}

	public void startConsole() {
		String line = null;
		while (true) {
			System.out.print(">>");
			try {
				line = console.readLine();

			} catch (IOException e) {
				System.out.println("IO Error, try again");
				// closeConsle();
			}
			String[] temp = line.trim().split(" ");

			String use=temp[0];
			for(int i=1;i<temp.length;i++)
				if(temp[i].equals("")==false)
				{
					use+=" ";
					use+=temp[i];
				}
			String []hold=use.split(" ");
			if (hold[0].equals("start")) {
				handleStartProcess(hold);
			} else if (hold[0].equals("migrate")) {
				handleMigrateProcess(hold);
			} else if (hold[0].equals("kill")) {

				handleKillProcess(hold);
			} else if (hold[0].equals("ls")) {
				handleLs(hold);
			} else if (hold[0].equals("ps")) {

				handlePs(hold);
			} else if (hold[0].equals("pn")) {
				System.out
						.println("1: HeadProcess\n2: CountWordsProcess\n3: GrepProcess");
			} else if (hold[0].equals("help")) {

				handleHelp(hold);
			} else if (hold[0].equals("shutdown")) {
				if(slaves.size()!=0)
				{
					Message msg=new Message(msgType.SHUTDOWN);
					ConcurrentHashMap<Integer, Socket> slaveList = getSlaves();
					for (int i : slaveList.keySet())
						send(i,msg);
				}
				terminate();
				System.out.println("terminating...");
				System.exit(0);
			} else
				System.out.println(hold[0] + "is not a valid command");
		}
	}

	private void handleLs(String[] line) {
		if (0 == slaveSize())
			System.out.println("no slave in system");
		else {
			ConcurrentHashMap<Integer, Socket> slaveList = getSlaves();
			for (int i : slaveList.keySet())
				System.out.println("Slave ID: " + i + "  IP Address: "
						+ slaveList.get(i).getInetAddress() + " port: "
						+ slaveList.get(i).getPort() + " health: "
						+ slaveStatus.get(i));
		}
	}

	private void handlePs(String[] line) {
		if (0 == processSize())
			System.out.println("no process information");
		else {
			ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ProcessInfo>> proList = getProcess();
			for (int i : proList.keySet()) {
				System.out.println("slave Id: " + i);
				ConcurrentHashMap<Integer, ProcessInfo> info = proList.get(i);
				for (Integer one : info.keySet())
					System.out.println("--Process ID: " + info.get(one).getId()
							+ " Process Name: " + info.get(one).getName()
							+ " Process Status: " + info.get(one).getStatus());
			}

		}
	}

	private void handleStartProcess(String[] line) {
		if (line.length < 3) {
			System.out
					.println("invalid argument, see help for more information");
			return;
		}
		int slaveId;
		try {
			slaveId = Integer.valueOf(line[2]);

		} catch (Exception e) {
			System.out.println("the slave id is not a number");
			return;

		}

		if (slaves.containsKey(slaveId) == false) {
			System.out.println("there is no slave with id number " + slaveId);
			return;

		}
		for(int i=0;i<line.length;i++)
		{
			System.out.print('#');
			System.out.print(line[i]);
			System.out.println('#');
		}
		String[] args = new String[line.length - 3];
		for (int i = 3; i < line.length; i++) {
			args[i - 3] = line[i];
		}
		String processName = line[1];
		MigratableProcess p = null;
		if(line[1].equals("CountWordsProcess"))
			try {
				p=new CountWordsProcess(args);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		else if(line[1].equals("HeadProcess"))
			try {
				p=new HeadProcess(args);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		else if(line[1].equals("GrepProcess"))
			try {
				p=new GrepProcess(args);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		else
		{
			System.out.println("unknow type process, quit start command");
			return;
		}
		/*
		try {
			Class<?> obj = Class.forName("process." + line[1]);
			Constructor<?> objConstructor = obj.getConstructor(String[].class);
			p = (MigratableProcess) objConstructor
					.newInstance(new Object[] { args });
		} catch (ClassNotFoundException e) {
			System.out.println("no such process class " + line[1]);
			return;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		*/
		ProcessInfo hold = new ProcessInfo(p, line[1], Manager.manager.proID,
				args, Status.RUNNING);

		Message msg = new Message(hold, Manager.manager.proID, msgType.START);
		send(slaveId, msg);
		Manager.manager.proID++;
	}

	private void handleKillProcess(String[] line) {
		int procId = -1;
		if (line.length < 2) {
			System.out
					.println("invalid argument number, please type help for more information");
			return;
		}
		int slaveId;
		try {
			slaveId = Integer.valueOf(line[1]);

		} catch (Exception e) {
			System.out.println("the slave id is not a number");
			return;
		}
		try {
			procId = Integer.valueOf(line[2]);

		} catch (Exception e) {
			System.out.println("the process id is not a number");
			return;
		}
		if (slaves.containsKey(slaveId) == false) {
			System.out.println("there is no slave with id number " + slaveId);
			return;
		}
		ConcurrentHashMap<Integer, ProcessInfo> proList = getProcessOfSlave(slaveId);

		if (false == proList.containsKey(procId)) {
			System.out.println("no such process in the slave");
			return;
		}
		ProcessInfo procInfo = proList.get(procId);
		if (procInfo.getStatus() == Status.RUNNING) {

			Message msg = new Message(procId, msgType.KILL);
			send(slaveId, msg);

		} else {
			System.out.println("That process is not currently running");
		}
	}

	private void handleMigrateProcess(String[] line) {
		if (line.length != 4) {
			System.out.println("wrong arguments number");
			return;
		}
		int procId, sourceId, targetId;
		try {
			procId = Integer.valueOf(line[1]);
		} catch (Exception e) {
			System.out.println("the process id is not a number");
			return;
		}
		try {
			sourceId = Integer.valueOf(line[2]);
		} catch (Exception e) {
			System.out.println("the source slave id is not a number");
			return;
		}
		try {
			targetId = Integer.valueOf(line[3]);
		} catch (Exception e) {
			System.out.println("the target slave id is not a number");
			return;
		}

		if (slaves.containsKey(sourceId) == false) {
			System.out.println("there is no source slave with id number "
					+ sourceId);
			return;
		}
		if (slaves.containsKey(targetId) == false) {
			System.out.println("there is no target slave with id number "
					+ targetId);
			return;
		}

		ConcurrentHashMap<Integer, ProcessInfo> proList = getProcessOfSlave(sourceId);

		if (false == proList.containsKey(procId)) {
			System.out.println("no such process in the source slave");
			return;
		}
		ProcessInfo process = proList.get(procId);
		if (process.getStatus() != Status.RUNNING) {
			System.out.println("this process is not in the state of running");
			return;
		}
		process.setStatus(Status.MIGRATING);
		proList.put(procId, process);
		processes.put(sourceId, proList);
		Message msg = new Message(sourceId, targetId, procId, con.get(targetId)
				.getIp(), con.get(targetId).getPort());
		send(targetId, msg);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		send(sourceId, msg);
		
	}

	private void handleHelp(String[] line) {
		System.out.println("Commands List:");
		System.out.println("ls : list all the slave node in the system");
		System.out.println("pn : list all the process name");
		System.out.println("ps : list all the processes that slave have");
		System.out
				.println("start <process name> <slaveId> <args[]>  : start the process on one slave");
		System.out
				.println("migrate <processId> <source slaveId> <target slaveId> : migrate process from one slave to another slave");
		System.out
				.println("kill <processId> <slaveId>: kill the process in slave");
		System.out.println("shutdown: quit");
	}

	public void remove(int one) {

		try {

			slaves.get(one).close();
			slaves.remove(one);
			con.get(one).stop();
			con.remove(one);
			processes.remove(one);
			slaveStatus.remove(one);
		} catch (IOException e) {
			System.err.println("remove error");
			e.printStackTrace();
		}
	}

	private boolean startServer() {
		try {
			server = new Server(port);
		} catch (Exception e) {
			return false;
		}
		Thread t = new Thread(server);
		t.start();
		return true;
	}

	private void terminate() {

		try {
			console.close();
			monitor.cancel();
			System.gc();
		} catch (IOException e) {

		}
		server.stop();

	}

	private void checkAlive() {

		ConcurrentHashMap<Integer, Integer> status = getSlaveStatus();
		// System.out.println("check: "+status.size());
		for (Integer one : status.keySet()) {
			int hh = status.get(one);
			if (status.get(one) == 0) {
				System.out.println("slave Id: " + one
						+ " disconnected, abondon all related tasks");
				remove(one);
				continue;
			}
			status.put(one, hh - 1);
			Message msg = new Message(msgType.HEART);
			send(one, msg);
		}
	}

	public synchronized int send(Integer one, Message msg) {
		try {

			int rep = con.get(one).send(msg);
			if (rep == 0 && msg.getResponType() != msgType.HEART)
				System.out
						.println("Send failure: Checking connection to slave: "
								+ one
								+ "...(Do not send a command for this slave until connection verification message appears!)");
			return 1;
		} catch (Exception e) {
			System.out.println("send error");
			e.printStackTrace();
			return 0;
		}
	}

	public void startTimer() {
		System.out.println("--heartbeat--");
		monitor = new Timer(true);
		TimerTask task = new TimerTask() {
			public void run() {
				checkAlive();
			}
		};
		monitor.schedule(task, 0, 5000);
	}

	public int slaveSize() {
		return slaves.size();
	}

	public int processSize() {
		int sum = 0;
		for (Integer one : processes.keySet())
			sum += processes.get(one).size();
		return sum;
	}

	public ConcurrentHashMap<Integer, Socket> getSlaves() {

		return slaves;

	}

	public ConcurrentHashMap<Integer, ProcessInfo> getProcessOfSlave(int slaveId) {

		return processes.get(slaveId);

	}

	public ConcurrentHashMap<Integer, Integer> getSlaveStatus() {

		return slaveStatus;

	}

	public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ProcessInfo>> getProcess() {

		return processes;

	}

	public static Manager manager;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out
					.println("wrong arguments, usage: ./Manager <port number>");
			return;
		}
		// Manager manager;
		int port;
		try {
			port = Integer.valueOf(args[0]);
		} catch (Exception e) {
			System.out.println("invalid port number");
			return;
		}
		manager = new Manager(port);
		if (manager.startServer()) {
			System.out.println("--start manager--");
			manager.startTimer();
			manager.startConsole();
		} else
			System.out.println("start server error");
	}

	public void addProcess(int slaveId, int id, ProcessInfo info) {

		// System.out.println("----\t"+processes.contains(slaveId)+"\t"+(info==null));
		ConcurrentHashMap<Integer, ProcessInfo> hold;
		if (processes.containsKey(slaveId) == false) {
			hold = new ConcurrentHashMap<Integer, ProcessInfo>();
			processes.put(slaveId, hold);
		} else {
			hold = processes.get(slaveId);
		}
		hold.put(id, info);
		processes.put(slaveId, hold);
		// System.out.println("----\t"+processes.containsKey(key));

	}

	public void removeProcess(int slaveId, int id) {
		ConcurrentHashMap<Integer, ProcessInfo> hold = processes.get(slaveId);
		// System.out.println("begin remove: "+id+" in slave "+slaveId+"\t"+hold.containsKey(id));
		hold.remove(id);
		processes.put(slaveId, hold);
	}

	public void transferProcess(int sourceID, int destID, int proId) {

		ConcurrentHashMap<Integer, ProcessInfo> src = processes.get(sourceID);
		ProcessInfo hold = src.get(proId);
		// System.out.println("migrate: "+proId+" in slave "+sourceID+"\t"+src.containsKey(proId));
		src.remove(proId);
		processes.put(sourceID, src);
		ConcurrentHashMap<Integer, ProcessInfo> des = processes.get(destID);
		hold.setStatus(Status.RUNNING);
		des.put(proId, hold);
		processes.put(destID, des);
	}

	public void keepRun(int slaveId, int hold) {
		ConcurrentHashMap<Integer, ProcessInfo> src = processes.get(slaveId);
		ProcessInfo fk = src.get(hold);
		fk.setStatus(Status.RUNNING);
		src.put(hold, fk);
		processes.put(slaveId, src);

	}
}