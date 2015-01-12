package process;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

import transIO.TransactionalFileInputStream;
import transIO.TransactionalFileOutputStream;

/*
 * display the first N lines in the given file
 */
public class HeadProcess implements MigratableProcess
{


	private static final long serialVersionUID = 80524725177656445L;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private int num;

	private volatile boolean suspending;
	private volatile boolean killed;

	public HeadProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("usage: HeadProcess <number> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		try
		{
		num = Integer.valueOf(args[0]);
		}catch(Exception e)
		{
			System.out.println("please enter the integer number");
		}
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2]);
	}

	public void run()
	{
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

		try {
			int ct=0;
			while ((!suspending) && (!killed)) {
				String line = in.readLine();
				if (line == null) 
					{
					
					break;
					}
				System.out.println("To verify migration: "+line);
				if(ct<num) ct++;
				out.println(line);
				
				
				// Make Head take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			System.out.println("end of file");
		} catch (IOException e) {
			System.out.println ("HeadProcess: Error: " + e);
		}


		suspending = false;
	}

	public void suspend()
	{
		suspending = true;
		while (suspending);
	}

	@Override
	public TransactionalFileInputStream getInputStream() {
		return this.inFile;
	}

	@Override
	public TransactionalFileOutputStream getOutputStream() {
		return this.outFile;
	}

	@Override
	public void kill() {
		this.killed = true;
		
	}

}