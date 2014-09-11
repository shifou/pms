package main;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import data.Message;
import data.ProcessInfo;
import data.msgType;
public class Server implements Runnable{
	private int conId;
	private int port;
	private volatile boolean running;
	
    ServerSocket serverSocket;
    public Server(int portNum){
        port = portNum;
        conId = 0;
        running = true;
        try {
         serverSocket = new ServerSocket((short)portNum);
     } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         System.out.println("failed to create the socket server");
         System.exit(0);
     }
        System.out.println("start Server at: "+port);
     }@Override
     public void run(){
	
         System.out.println("waiting for slaves");
         while(running){
        	 Socket slaveSocket;
        	 try{
             slaveSocket = serverSocket.accept();
        	  }catch(IOException e){
        	         e.printStackTrace();
        	         System.out.println("socket server accept failed");
        	         continue;
        	     }
             System.out.println("slave: "+slaveSocket.getInetAddress()+":"+slaveSocket.getPort()+" join in");

            	 Manager.manager.slaves.put(conId, slaveSocket);
            
             Connection slaveService;
			try {
				slaveService = new Connection(conId, slaveSocket);
				slaveService.setIp(slaveSocket.getInetAddress());
	            new Thread(slaveService).start();
	            System.out.println("begin send");
	            Manager.manager.con.put(conId, slaveService);
	            Message msg= new Message(msgType.CONNECT,conId);
	            Manager.manager.send(conId, msg);
	             
	             
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
             conId++;
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

}