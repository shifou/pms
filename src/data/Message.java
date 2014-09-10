package data;

import java.io.Serializable;
import java.net.InetAddress;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2553768344528279980L;
	
	private ProcessInfo proInfo;
	private int proId;
	private msgType type;
	private String statusInfo;
	private int sourceID;
	private int destID;
	private InetAddress destIP;
	private int destPort;
	private int slaveID;

	
	public Message(msgType type){
		this.type = type;
	}
	// kill message
	public Message (int killproId, msgType tp)
	{
		proId=killproId;
		type=tp;
	}
	// assign slaveID Message
	public Message (msgType tp, int slaveID)
	{
		this.slaveID = slaveID;

		type=tp;
	}
	// migrate notification
	public Message(int sourceId2, int targetId, InetAddress ip) {
		sourceID= sourceId2;
		destID= targetId;
		destIP=ip;
		type=msgType.MIGRATENOTI;
	}
	//start a process
	public Message(ProcessInfo hold, msgType start) {
		type=start;
		proInfo=hold;
	}
	public Message(String message, msgType startfail) {
		statusInfo=message;
		type=startfail;
	}
	public msgType getResponType() {
		return this.type;
	}

	public int getDestPort() {
		return this.destPort;
	}

	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}
	public int getProId()
	{
		return proId;
	}
	public InetAddress getDestHost() {
		return this.destIP;
	}

	public void setDestIP(InetAddress destIP) {
		this.destIP = destIP;
	}

	public int getDestID() {
		return this.destID;
	}

	public void setDestID(int destID) {
		this.destID = destID;
	}

	public int getSourceID() {
		return this.sourceID;
	}

	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}


	public String getStatusInfo() {
		return this.statusInfo;
	}


	public void setStatusInfo(String statusInfo) {
		this.statusInfo = statusInfo;
	}
	
	public ProcessInfo getProcessInfo() {
		return this.proInfo;
	}

	public void setProcessInfo(ProcessInfo proInfo) {
		this.proInfo = proInfo;
	}

	
	public int getSlaveID(){
		return this.slaveID;
	}


	public InetAddress getIp() {
		
		return destIP;
	}

}
