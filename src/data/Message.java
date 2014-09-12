package data;
import java.io.Serializable;
import java.net.InetAddress;

/*
 * Message class is a class used for communication through network 
 * including the ProcessInfo and other information, each time we will send one message
 */
public class Message implements Serializable {
	
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

	// heart beat message
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
	// migrate notification message
	public Message(int sourceId2, int targetId, int id,InetAddress ip,int port) {
		proId=id;
		sourceID= sourceId2;
		destID= targetId;
		destIP=ip;
		type=msgType.MIGRATENOTI;
		destPort=port;
	}
	//start process message
	public Message(ProcessInfo hold, int proId,msgType start) {
		type=start;
		proInfo=hold;
		this.proId=proId;
	}
	// migrate fail message
	public Message(String message, msgType startfail) {
		statusInfo=message;
		type=startfail;
	}
	// start done message
	public Message(String message, int id,msgType start) {
		proId=id;
		statusInfo=message;
		type=start;
	}
	//migrate done
	public Message(msgType migratedone, int id, int sourceID2, int destID2) {
		type=migratedone;
		proId=id;
		sourceID=sourceID2;
		destID=destID2;
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
