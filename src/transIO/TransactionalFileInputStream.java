package transIO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;



public class TransactionalFileInputStream extends InputStream implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -212220946013637199L;

	private File file;
	private RandomAccessFile fileHandle;
	private long fileOffset;
	private boolean migrated;
	
	public TransactionalFileInputStream(String fileName){
		this.file = new File(fileName);
		this.fileOffset = 0;
		this.migrated = false;
	}
	
	@Override
	public int read() throws IOException {
		if ((this.fileHandle == null) || (this.migrated == true)){
			this.fileHandle = new RandomAccessFile(this.file, "r");
			this.fileHandle.seek(this.fileOffset);
			this.migrated = false;
		}
		int read = this.fileHandle.read();
		this.fileOffset += 1;
		return read;
	}
	
	@Override 
	public void close() throws IOException {
		this.fileHandle.close();
	}
	
	public void changeMigrated(boolean m){
		this.migrated = m;
	}
	
}
