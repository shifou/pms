package process;
import java.io.Serializable;

import transIO.TransactionalFileInputStream;
import transIO.TransactionalFileOutputStream;

public interface MigratableProcess extends Runnable, Serializable {
   
	//functions
	
    public void suspend(); //will be called before object is serialized
    
    public TransactionalFileInputStream getInputStream(); //optional, can also return null
    
    public TransactionalFileOutputStream getOutputStream(); //optional, can also return null
    
    public void kill();
    
}
