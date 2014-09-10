package data;

import java.io.Serializable;

import process.MigratableProcess;


public class ProcessInfo implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4034010548176885851L;
	private int proId;
    private String proName;
    private Status status;
    private MigratableProcess process;
    private String args[];
    public ProcessInfo(String name, int id, String[]a, Status now)
    {
    	proName= name;
    	args=a;
    	proId = id;
    	status= now;
    }
    public void setName(String name){
    	proName= name;
    }
	public String getName() {
		return proName;
	}
	public void setArgs(String[] arg){
    	args= arg;
    }
	public String[] getArgs() {
		
		return args;
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
	public MigratableProcess getProcess() {
		return this.process;
	}
	public void setProcess(MigratableProcess process) {
		this.process = process;
	}
	
}