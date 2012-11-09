import java.net.*;
import java.io.*;

public class FileTransferServer implements Runnable {
	ServerSocket fservSock = null;
	DataInputStream is;
	DataOutputStream os;
	Socket fclientSock = null;
	
	public void start()
	{
		try {
			fservSock = new ServerSocket(Machine.FILE_TRANSFER_PORT);
	    }
	    catch (IOException e) {
	        System.out.println(e);
	    }   
		
		Thread server_thread = new Thread(this);
		server_thread.start();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true)
		{
			try {
				fclientSock = fservSock.accept();
				//TODO need to write code here for checking the opcode of the incoming
				// message and then transferring the file accordingly
			} catch(IOException e) {
				System.out.println(e);
			}
		}
	}

}
