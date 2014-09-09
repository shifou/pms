package main;

import main.Server;
import data.*;

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
    private BufferedReader console;
    
    public Manager(int listenPort){
        port = listenPort;
        console = new BufferedReader(new InputStreamReader(System.in));
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
            switch(hold[0]){
                case "start":
                    handleStartProcess(hold);
                    break;
                case "migrate":
                    handleMigrateProcess(hold);
                    break;
                case "kill":
                    handleKillProcess(hold);
                    break;
                case "ls":
                    handleLs(hold);
                    break;
                case "ps":
                    handlePs(hold);
                    break;
                case "help":
                    handleHelp(hold);
                    break;
                case "shutdown":
                    terminate();
                    System.exit(0);
                    break;
                default:
                    System.out.println(hold[0]+"is not a valid command");
            }
        }
    }
    
    private void handleLs(String[] line){
        if(0 == server.slaveSize())
            System.out.println("no slave in system");
        else{
        	ConcurrentHashMap<Integer,Socket> slaveList = server.getSlaves();
            for(int i : slaveList.keySet())
                System.out.println("Slave ID: "+i+"  IP Address: "+slaveList.get(i).getInetAddress());
        }
    }
    
    private void handlePs(String[] line){
        if(0 == server.processSize())
            System.out.println("no process information");
        else{
        	ConcurrentHashMap<Integer,Vector<ProcessInfo>> proList = server.getProcess();
            for(int i : proList.keySet()){
            	System.out.println("slave Id: "+i);
                Vector<ProcessInfo> info = proList.get(i);
                for(int j=0;j<info.size();j++)
                System.out.println("--Process ID: "+info.get(j).getId()+" Process Name: "+info.get(j).getName()+" Process Status: "+info.get(j).getStatus());
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
            slaveId = Integer.valueOf(line[1]);
            
        }catch(Exception e){
            System.out.println("the slave id is not a number");
            return;
            
        }
        if(server.slaves.containsKey(slaveId)==false){
            System.out.println("there is no slave with id number "+slaveId);
            return;
        }
       
        String[] args = new String[line.length - 3];
        for(int i=3;i<line.length;i++){
            args[i-3] = line[i];
        }
        
        
        ConcurrentHashMap<Integer,ProcessInfo> proList = server.getProcessOfSlave(slaveId);
        
        if(proList.containsKey(slaveId)){
            try{
                
            }catch (IOException e) {
                e.printStackTrace();
                System.out.println("start Command sent failed, remove slave "+slaveId);
                removeNode(slaveId);
            }
        }
        else{
            System.out.println("there is no server for slaveId "+slaveId);
        }
            
    }
    
    private void handleKillProcess(String[] line){
        int procId=-1;
        if(line.length < 2){
            System.out.println("invalid argument number, please type help for more information");
            return;
        }
        try{
            procId=Integer.valueOf(line[1]);
        }catch(Exception e){
            System.out.println("the worker id is not a number");
            return;
        }
        ConcurrentHashMap<Integer,ProcessInfo> proList = server.getProcess();
        
        if(false==proList.containsKey(procId)){
            System.out.println("no such process");
            return;
        }
        ProcessInfo procInfo = proList.get(procId);
        if (procInfo.getStatus().equals(ProcessInfo.Status.RUNNING)) {
                
                try{
                   
                }catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Kill Command sent failed, remove worker "+slaveId);
                    removeNode(slaveId);
                }
                
                
                /*update the process status when receive the reply from worker*/

            } else {
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
            System.out.println("the source worker id is not a number");
            return;
        }
        try{
            targetId=Integer.valueOf(line[3]);
        }catch(Exception e){
            System.out.println("the target worker id is not a number");
            return;
        }
        
        if(!proList.containsKey(procId)){
            System.out.println("no process with "+procId+"exist");
            return;
        }
        
        if(!proList.containsKey(sourceId)){
            System.out.println("the source worker "+sourceId+" does not exist");
            return;
        }
     
        if(!proList.containsKey(targetId)){
            System.out.println("the target worker "+targetId+" does not exist");
            return;
        }
     
        
        try{
          
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Migrate Command sent failed, remove worker "+sourceId);
            removeNode(sourceId);
        }
        



    }
    
    private void handleHelp(String[] line){
        System.out.println("Commands List:");
        System.out.println("ls : list all the slave node in the system");
        System.out.println("ps : list all the processes");
        System.out.println("start <process name> <args[]> <worker id> : start the process on the designated worker");
        System.out.println("migrate <process id> <source id> <target id> : migrate process from source to target worker");
        System.out.println("kill <process id> : kill the process");
    }
    public void removeNode(int id){
       
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
        }catch(IOException e){
            
        }
        server.stop();
        
    }
    
    private void checkAlive(){
        
        
    }
    
    public void startTimer(){
    	 System.out.println("--heartbeat--");
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask(){
            public void run(){
                checkAlive();
            }
        };
        timer.schedule(task, 0, 5000);
       
        
    }
    public static void main(String[] args){
        if(args.length != 1){
            System.out.println("wrong arguments, usage: ./Manager <port number>");
            return;
        }
        int port;
        try{
            port = Integer.valueOf(args[0]);
        }catch(Exception e){
           System.out.println("invalid port number");
           return;
        }
        Manager manager = new Manager(port);
        if(manager.startServer())
        {
        	System.out.println("--start manager--");
            manager.startTimer();
        	manager.startConsole();
        }
        else
        	 System.out.println("start server error");
        
        
        
        
    }

}