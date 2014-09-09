package main;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import data.Message;
import data.ProcessInfo;
public class Server implements Runnable{
	private int conId;
	private int port;
	boolean running;
	public ConcurrentHashMap<Integer,Socket> slaves;
	public ConcurrentHashMap<Integer,Integer> slaveStatus;
	public ConcurrentHashMap<Integer,Connection> con;
	public ConcurrentHashMap<Integer,Vector<ProcessInfo>> processes;
    ServerSocket serverSocket;
    public Server(int portNum){
        port = portNum;
        conId = 0;
        running = true;
        try {
         serverSocket = new ServerSocket(portNum);
     } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         System.out.println("failed to create the socket server");
         System.exit(0);
     }
        System.out.println("start Server at: "+port);
     }@Override
     public void run(){
         
	 try{
         System.out.println("waiting for slaves");
         while(true){
             Socket slaveSocket = serverSocket.accept();
             System.out.println("slave: "+slaveSocket.getInetAddress()+":"+slaveSocket.getPort()+" join in");
             slaves.put(conId, slaveSocket);
             Connection slaveService = new Connection(conId, slaveSocket); 
             new Thread(slaveService).start();
             con.put(conId, slaveService);
             conId++;
         } 
     }catch(IOException e){
         e.printStackTrace();
         System.out.println("socket server accept failed");
     }
     try {
      serverSocket.close();
     } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      System.out.println("socket Server failed to close");
  }
  }
  
  public void stop(){
      running = false;
  }
public int slaveSize() {
	
	return slaves.size();
}
public int processSize() {
	
	return processes.size();
}
public ConcurrentHashMap<Integer, Vector<ProcessInfo> > getProcess() {
	ConcurrentHashMap<Integer, Vector<ProcessInfo>> info =new ConcurrentHashMap<Integer, Vector<ProcessInfo>>();
	for(Integer one: processes.keySet())
	{
		info.put(one, processes.get(one).getProcessInfos());
	}
	return info;
}
public ConcurrentHashMap<Integer, Socket> getSlaves() {
	return slaves;
}
public ConcurrentHashMap<Integer, ProcessInfo> getProcessOfSlave(int slaveId) {
	ConcurrentHashMap<Integer, ProcessInfo> info =new ConcurrentHashMap<Integer, ProcessInfo>();
	
	return info;
}
public ConcurrentHashMap<Integer, Integer> getSlaveStatus() {
	
	return slaveStatus;
}
public void send(Integer one, Message msg) {
	// TODO Auto-generated method stub
	
}



}