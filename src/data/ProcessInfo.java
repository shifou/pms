package data;

import process.MigratableProcess;


public class ProcessInfo {
    private int proId;
    private String proName;
    private Status status;
    public MigratableProcess processObject;
    public ProcessInfo(String name, int id, Status now)
    {
    	proName= name;
    	proId = id;
    	status= now;
    }
    public void setName(String name){
    	proName= name;
    }
	public String getName() {
		
		return proName;
	}
	public void setStatus(Status st){
    	status= st;
    }
	public Status getStatus() {
		return status;
	}
	public void setId(int id){
    	proId= id;
    }
	public int getId() {
		return proId;
	}
	
}