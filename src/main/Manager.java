package main;
import main.Server;
import data.*;
import process.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.*;
import java.net.Socket;
import java.net.SocketException;
public class Manager {
	Server server;
	private int port;
	public int proID;
	public Timer monitor;
    private BufferedReader console;
    public ConcurrentHashMap<Integer,Socket> slaves;
	public ConcurrentHashMap<Integer,Integer> slaveStatus;
	public ConcurrentHashMap<Integer,Connection> con;
	public ConcurrentHashMap<Integer,ConcurrentHashMap<Integer, ProcessInfo>> processes;
    public Manager(int listenPort){
        port = listenPort;
        proID=1;
        console = new BufferedReader(new InputStreamReader(System.in));
        slaves=new ConcurrentHashMap<Integer,Socket>();
        slaveStatus = new ConcurrentHashMap<Integer,Integer>();
        con =new ConcurrentHashMap<Integer,Connection>();
        processes= new ConcurrentHashMap<Integer,ConcurrentHashMap<Integer, ProcessInfo>>();
    }
    
    public void startConsole(){
        String line=null;
        while(true){
            System.out.print(">>");
            try{
                line = console.readLine();
                
            }catch(IOException e){
                System.out.println("IO Error, try again");
                //closeConsle();
            }            
            
            String[] hold = line.split(" ");
           if(hold[0].equals("start"))
           {
                    handleStartProcess(hold);
           }
           else if(hold[0].equals("migrate"))
           {
                    handleMigrateProcess(hold);
           } 
           else if(hold[0].equals("kill"))
           {
            
                    handleKillProcess(hold);
           }
           else if(hold[0].equals("ls"))
           {
                    handleLs(hold);
           }
           else if(hold[0].equals("ps"))
           {
            
                    handlePs(hold);
           }
           else if(hold[0].equals("help"))
           {
             
                    handleHelp(hold);
           }
           else if(hold[0].equals("shutdown"))
           {
             
                    terminate();
                    System.out.println("terminating...");
                    System.exit(0);
           }
           else
                    System.out.println(hold[0]+"is not a valid command");
            }
        }
    
    
    private void handleLs(String[] line){
        if(0 == slaveSize())
            System.out.println("no slave in system");
        else{
        	ConcurrentHashMap<Integer,Socket> slaveList = getSlaves();
            for(int i : slaveList.keySet())
                System.out.println("Slave ID: "+i+"  IP Address: "+slaveList.get(i).getInetAddress()+" port: "+slaveList.get(i).getPort());
        }
    }
    
    private void handlePs(String[] line){
        if(0 == processSize())
            System.out.println("no process information");
        else{
        	ConcurrentHashMap<Integer,ConcurrentHashMap<Integer,ProcessInfo>> proList = getProcess();
            for(int i : proList.keySet()){
            	System.out.println("slave Id: "+i);
            	ConcurrentHashMap<Integer,ProcessInfo> info = proList.get(i);
                for(Integer one: info.keySet())
                System.out.println("--Process ID: "+info.get(one).getId()+" Process Name: "+info.get(one).getName()+" Process Status: "+info.get(one).getStatus());
            }
                
        }
    }
    
    private void handleStartProcess(String[] line){
        if(line.length < 3){
            System.out.println("invalid argument, see help for more information");
            return;
        }
        int slaveId;
        try{
            slaveId = Integer.valueOf(line[2]);
            
        }catch(Exception e){
            System.out.println("the slave id is not a number");
            return;
            
        }

        if(slaves.containsKey(slaveId)==false){
            System.out.println("there is no slave with id number "+slaveId);
            return;
        
        }
       
        String[] args = new String[line.length - 3];
        for(int i=3;i<line.length;i++){
            args[i-3] = line[i];
        }
        String processName = line[1];
        MigratableProcess p=null;
        if(processName.equals("GrepProcess"))
        {
        	try {
				p=new GrepProcess(args);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("process class not found");
			}
        }
        ProcessInfo hold=new ProcessInfo(p,line[1],slaveId,args,Status.RUNNING);
       
            Message msg = new Message(hold,Manager.manager.proID,msgType.START);
			send(slaveId, msg);
        }
        
            
    
    
    private void handleKillProcess(String[] line){
        int procId=-1;
        if(line.length < 2){
            System.out.println("invalid argument number, please type help for more information");
            return;
        }
        int slaveId;
        try{
            slaveId = Integer.valueOf(line[1]);
            
        }catch(Exception e){
            System.out.println("the slave id is not a number");
            return;   
        }
        if(slaves.containsKey(slaveId)==false){
            System.out.println("there is no slave with id number "+slaveId);
            return;
        }
        ConcurrentHashMap<Integer,ProcessInfo> proList = getProcessOfSlave(slaveId);
        
        if(false==proList.containsKey(procId)){
            System.out.println("no such process in the slave");
            return;
        }
        ProcessInfo procInfo = proList.get(procId);
        if (procInfo.getStatus()==Status.RUNNING) {
                
                Message msg = new Message(procId,msgType.KILL);
				send(slaveId, msg);
                
                
               
            } else{
                System.out.println("That process is not currently running");
            }
    } 
    
    private void handleMigrateProcess(String[] line){
        if(line.length != 4){
            System.out.println("wrong arguments number");
            return;
        }
        int procId,sourceId,targetId;
        try{
            procId=Integer.valueOf(line[1]);
        }catch(Exception e){
            System.out.println("the process id is not a number");
            return;
        }
        try{
            sourceId=Integer.valueOf(line[2]);
        }catch(Exception e){
            System.out.println("the source slave id is not a number");
            return;
        }
        try{
            targetId=Integer.valueOf(line[3]);
        }catch(Exception e){
            System.out.println("the target slave id is not a number");
            return;
        }
       
        if(slaves.containsKey(sourceId)==false){
            System.out.println("there is no source slave with id number "+sourceId);
            return;
        }
        if(slaves.containsKey(targetId)==false){
            System.out.println("there is no target slave with id number "+targetId);
            return;
        }
        
        
        ConcurrentHashMap<Integer,ProcessInfo> proList = getProcessOfSlave(sourceId);
        
        if(false==proList.containsKey(procId)){
            System.out.println("no such process in the source slave");
            return;
        }
        ProcessInfo process= proList.get(procId);
        if(process.getStatus()!=Status.RUNNING)
        {
        	System.out.println("this process is not in the state of running");
            return;
        }
        process.setStatus(Status.MIGRATING);
        Message msg = new Message(sourceId, targetId, con.get(targetId).getIp());
		send(targetId,msg);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			send(sourceId, msg);
    }
    
    private void handleHelp(String[] line){
        System.out.println("Commands List:");
        System.out.println("ls : list all the slave node in the system");
        System.out.println("ps : list all the processes that slave have");
        System.out.println("start <process name> <slaveId> <args[]>  : start the process on one slave");
        System.out.println("migrate <processId> <source slaveId> <target slaveId> : migrate process from one slave to another slave");
        System.out.println("kill <processId> <slaveId>: kill the process in slave");
    }
    public void remove(int one){
    	
		try {
			
			slaves.get(one).close();
			slaves.remove(one);
			con.get(one).stop();
			con.remove(one);
			processes.remove(one);
			slaveStatus.remove(one);
		} catch (IOException e) {
			System.err.println("remove error");
			e.printStackTrace();
		}
    }
    private boolean startServer(){
        try
        {
        	server = new Server(port);
        }
        catch(Exception e) {
        	return false;
        }
        Thread t = new Thread(server);
        t.start();
        return true;
    }
    
    private void terminate(){
        
        try{
            console.close();
            monitor.cancel();
            System.gc();
        }catch(IOException e){
            
        }
        server.stop();
        
    }
    
    private void checkAlive(){
    	
    	ConcurrentHashMap<Integer,Integer> status = getSlaveStatus();
    	//System.out.println("check: "+status.size());
        for(Integer one : status.keySet())
        {
        	if(status.get(one)==0)
        	{
        		System.out.println("slave Id: "+one+" disconnected, abondon all related tasks");
        		remove(one);
        		continue;
        	}
        	status.put(one, 0);
        	Message msg=new Message(msgType.HEART);
        	send(one,msg);
        }
    }
    public int send(Integer one, Message msg) {
    	try {
    			con.get(one).send(msg);
    		return 1;
    	} catch (IOException e) {
    		System.out.println("send error");
    		e.printStackTrace();
    		return 0;
    	}
    }
    public void startTimer(){
    	 System.out.println("--heartbeat--");
        monitor = new Timer(true);
        TimerTask task = new TimerTask(){
            public void run(){
                checkAlive();
            }
        };
        monitor.schedule(task, 0, 5000);
    }
    public int slaveSize() {
    	return slaves.size();
    }
    public int processSize() {
    	int sum=0;
    	for(Integer one : processes.keySet()) sum+=processes.get(one).size();
    	return sum;
    }
    public ConcurrentHashMap<Integer, Socket> getSlaves() {
    	
    		return slaves;
       
    }
    public ConcurrentHashMap<Integer, ProcessInfo> getProcessOfSlave(int slaveId) {

    		return processes.get(slaveId);
        
    }
    public ConcurrentHashMap<Integer, Integer> getSlaveStatus() {
    	
    	return slaveStatus;
      
    }
    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ProcessInfo>> getProcess() {
    	
    	return processes;
        
    }
    public static Manager manager;
    public static void main(String[] args){
        if(args.length != 1){
            System.out.println("wrong arguments, usage: ./Manager <port number>");
            return;
        }
        //Manager manager;
        int port;
        try{
            port = Integer.valueOf(args[0]);
        }catch(Exception e){
           System.out.println("invalid port number");
           return;
        }
        manager = new Manager(port);
        if(manager.startServer())
        {
        	System.out.println("--start manager--");
            manager.startTimer();
        	manager.startConsole();
        }
        else
        	 System.out.println("start server error");     
    }

	public void addProcess(int slaveId, int id,ProcessInfo info) {
		ConcurrentHashMap<Integer, ProcessInfo> hold= processes.get(slaveId);
		hold.put(id, info);
		processes.put(slaveId, hold);
		
	}

	public void removeProcess(int slaveId, int id) {
		ConcurrentHashMap<Integer, ProcessInfo> hold= processes.get(slaveId);
		hold.remove(slaveId);
		processes.put(slaveId, hold);
	}
	public void transferProcess(int sourceID, int destID, int proId) {
		
		ConcurrentHashMap<Integer, ProcessInfo> src= processes.get(sourceID);
		ProcessInfo hold= src.get(proId);
		src.remove(proId);
		processes.put(sourceID, src);
		ConcurrentHashMap<Integer, ProcessInfo> des= processes.get(destID);
		des.put(destID,hold);
		processes.put(destID, des);
	}
}