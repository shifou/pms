package transIO;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.RandomAccessFile;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{	
	
	private static final long serialVersionUID = 4576890066480014205L;

	private File file;
	private transient RandomAccessFile fileHandle;
	private long fileOffset;
	private boolean migrated;
	
	public TransactionalFileOutputStream(String fileName){
		this.file = new File(fileName);
		this.fileOffset = 0;
		this.migrated = false;
	}
	@Override
	public void write(int b) throws IOException {
		if ((this.fileHandle == null) || (this.migrated == true)){
			this.fileHandle = new RandomAccessFile(this.file, "rw");
			this.fileHandle.seek(this.fileOffset);
			this.migrated = false;
		}
		this.fileHandle.write(b);
		this.fileOffset += 1;
		
	}
	@Override
	public void close() throws IOException {
		this.fileHandle.close();
	}
	
	public void changeMigrated(boolean m){
		this.migrated = m;
	}

}

