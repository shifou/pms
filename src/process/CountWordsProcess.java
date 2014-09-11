
package process;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

import transIO.TransactionalFileInputStream;
import transIO.TransactionalFileOutputStream;

public class CountWordsProcess implements MigratableProcess
{
	private static final long serialVersionUID = 718071586147799050L;

	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;

	private volatile boolean suspending;
	private volatile boolean killed;

	public CountWordsProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("usage: CountWordsProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2]);
	}

	public void run()
	{
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

		try {
			int sum=0;
			int ln=1;
			while ((!suspending) && (!killed)) {
				String line = in.readLine();
				System.out.println(line);
				if (line == null) 
					{
					out.println("Number of words total in the files: " + sum);
					
					break;
					}
				
				String[] words = line.split(" ");
				sum += words.length;
				out.println("read lines "+ln+" total words: " + sum);
				ln++;
				// Make CountWordsProcess take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			System.out.println("end of file");
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
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
		// TODO Auto-generated method stub
		return this.inFile;
	}

	@Override
	public TransactionalFileOutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return this.outFile;
	}

	@Override
	public void kill() {
		this.killed = true;
		
	}

}