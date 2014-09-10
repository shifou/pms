package data;

import java.io.Serializable;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2553768344528279980L;
	
	private ProcessInfo proInfo;
	private int tempId;
	private int proId;
	private msgType type;
	private String statusInfo;
	private int sourceID;
	private int destID;
	private String destHost;
	private int destPort;
	
	public Message(msgType type){
		this.type = type;
	}
	// kill message
	public Message (int conId,int killproId, msgType tp)
	{
		proId=killproId;
		tempId=conId;
		type=tp;
	}
	// assign conId Message
	public Message (int conId,msgType tp)
	{
		tempId=conId;
		type=tp;
	}
	// migrate Noti
	public Message(int sourceId, int targetId, ProcessInfo process) {
		proInfo=process;
		sourceID=sourceId;
		destID=targetId;
		type=msgType.MIGRATENOTI;
				
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

	public String getDestHost() {
		return this.destHost;
	}

	public void setDestHost(String destHost) {
		this.destHost = destHost;
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



	public Integer getHeartId() {
		
		return tempId;
	}

}
