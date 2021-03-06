package main;

import data.Message;
import data.ProcessInfo;
import data.msgType;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;



/*
 * connection class is the listening thread getting messgae from one specific slave
 * and according to the messagetype we go different handlers
 */
public class Connection implements Runnable {
	private InetAddress ipaddr;
	private int listenPort;
	private int slaveId;
	private Socket socket;
	private volatile boolean running;

	private ObjectInputStream objInput;
	private ObjectOutputStream objOutput;

	public Connection(int conId, Socket slaveSocket) throws IOException {
		slaveId = conId;
		socket = slaveSocket;
		objOutput = new ObjectOutputStream(slaveSocket.getOutputStream());
		objOutput.flush();
		objInput = new ObjectInputStream(slaveSocket.getInputStream());
		running=true;
		
	}

	@Override
	public void run() {
		try {
			Message receiveMessage;
			while (running) {
				try {

					receiveMessage = (Message) objInput.readObject();
					
				} catch (ClassNotFoundException e) {
					//System.out.println("read disconnected message");
					continue;
				}
				catch(EOFException e)
				{
					//System.out.println("detect disconnected message");
					continue;
				}
				catch(Exception e)
				{
					//System.out.println("-----");
					continue;
				}
				if(receiveMessage.getResponType()!=msgType.HEARTACK)
				System.out.println("slave receive message: "+ receiveMessage.getResponType());
				switch (receiveMessage.getResponType()) {
				case CONNECT:
					
					//ipaddr=receiveMessage.getIp();
					listenPort=receiveMessage.getDestPort();
					Manager.manager.slaveStatus.put(slaveId, 5);
					ConcurrentHashMap<Integer, ProcessInfo> hold= new ConcurrentHashMap<Integer, ProcessInfo>();
					Manager.manager.processes.put(slaveId, hold);
					break;
				case STARTFAIL:
				case STARTDONE:
				case FINISH:
					handleSTART(receiveMessage);
					break;
				//case MIGRATEBEGINFAIL:
				case MIGRATEDONE:
				case MIGRATEFAIL:
					handleMIGRATE(receiveMessage);
					break;
				case KILLDONE:
				case KILLFAIL:
					handleKILL(receiveMessage);
					break;
				case HEARTACK:
					handleHEART(receiveMessage);
					break;
				default:
					System.out.println("unknow type");
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("message error in handle");

		}

	}
	//handle haertbeat message
	private void handleHEART(Message receiveMessage) {
		int st=Manager.manager.slaveStatus.get(slaveId);
		Manager.manager.slaveStatus.put(slaveId, st+1);
		
	}

	//handle migrate message
	private void handleMIGRATE(Message receiveMessage) {
		int hold = receiveMessage.getProId();
		switch (receiveMessage.getResponType()) {
		//case MIGRATEBEGIN:
		case MIGRATEDONE:
			System.out.println("slave "+slaveId+" migrate process "+hold+" from slave "+receiveMessage.getSourceID());
			Manager.manager.transferProcess(receiveMessage.getSourceID(),receiveMessage.getDestID(),hold);
			break;
		case MIGRATEFAIL:
			System.out.println("slave "+slaveId+" migrate process "+hold+" from slave "+receiveMessage.getSourceID()+" fail FOR"+receiveMessage.getStatusInfo());
			Manager.manager.keepRun(slaveId,hold);
			break;
		default:
			break;
		}
	}

	//handle process start message
	private void handleSTART(Message receiveMessage) {
		int hold = receiveMessage.getProId();
		switch (receiveMessage.getResponType()) {
		case STARTFAIL:
			System.out.println("slave "+slaveId+" start process "+hold+" fail FOR"+receiveMessage.getStatusInfo());
			break;
		case STARTDONE:
			//Manager.manager.proID++;
			Manager.manager.slaves.put(slaveId, socket);
			Manager.manager.addProcess(slaveId, hold,receiveMessage.getProcessInfo());
			System.out.println("slave "+slaveId+" start process "+hold+" success");
			break;
		case FINISH:
			System.out.println("slave "+slaveId+" finish process "+hold);
			Manager.manager.removeProcess(slaveId, hold);
			break;
		default:
			break;
		}
	}
	//handle kill message
	private void handleKILL(Message receiveMessage) {
		int hold = receiveMessage.getProId();
		switch (receiveMessage.getResponType()) {
		case KILLDONE:
			System.out.println("slave "+slaveId+" killed process "+hold+" success");
			Manager.manager.removeProcess(slaveId, hold);
			break;
		case KILLFAIL:
			System.out.println("slave "+slaveId+" start process "+hold+" fail FOR"+receiveMessage.getStatusInfo());
			Manager.manager.removeProcess(slaveId, hold);
			break;
		default:
			break;
		}
	}
	//send message throught the socket
	public int send(Message mes) throws IOException {
		synchronized(this.socket)
		{
		try
		{
			if(mes.getResponType()!=msgType.HEART)
			System.out.println("send to "+slaveId+"\t"+mes.getResponType());

				objOutput.writeObject(mes);
				objOutput.flush();
			
		}catch(Exception e)
		{
			return 0;
		}
		return 1;
		}
	}

	public void stop() {
		running = false;
	}

	public InetAddress getIp() {
		// TODO Auto-generated method stub
		return ipaddr;
	}

	public int getPort() {
		
		return listenPort;
	}

	public void setIp(InetAddress inetAddress) {
		// TODO Auto-generated method stub
		ipaddr=inetAddress;
	}
}
