package data;

import java.io.Serializable;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2553768344528279980L;
	
	private ProcessInfo proInfo;
	private msgType type;
	private String statusInfo;
	private int sourceID;
	private int destID;
	private String destHost;
	private int destPort;
	
	public Message(msgType type){
		this.type = type;
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

}
