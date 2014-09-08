package main;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;

import data.ProcessInfo;
public class Server implements Runnable{
	private int conId;
	private int port;
	boolean running;
	private ConcurrentHashMap<Integer,Socket> slaves;
	private ConcurrentHashMap<Integer,ProcessInfo> processes;
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
             manageService slaveService = new manageService(conId, slaveSocket); 
             new Thread(slaveService).start();
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
	// TODO Auto-generated method stub
	return 0;
}
public int processSize() {
	// TODO Auto-generated method stub
	return 0;
}
public ConcurrentHashMap<Integer, ProcessInfo> getProcess() {
	// TODO Auto-generated method stub
	return null;
}
public ConcurrentHashMap<Integer, Socket> getSlaves() {
	// TODO Auto-generated method stub
	return null;
}



}