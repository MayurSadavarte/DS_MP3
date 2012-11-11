import java.net.*;
import java.io.*;

public class FileTransferServer implements Runnable {
	
	ServerSocket fservSock = null;
	DataInputStream is;
	DataOutputStream os;
	Socket fclientSock = null;
	
	public void start()
	{
		Thread server_thread = new Thread(this);
		server_thread.start();
	}
	/*
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
	*/
	
	//private String sourceFN;
	
	//public FileTransferServer(String s){
	//	sourceFN = s;
	//}
	
	//public void setSource(String s){
	//	sourceFN = s;
	//}
	
	public void run(){
		
		ServerSocket servsock=null;
		try {
			servsock = new ServerSocket(Machine.FILE_TRANSFER_PORT);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while(true){
		
			try{
				
			    
			      System.out.println("Waiting...");
	
			      Socket sock = servsock.accept();
			      System.out.println("Accepted connection : " + sock);
			      
			      
			      
			      FileTransServerThread ftthread = new FileTransServerThread(sock);
			      ftthread.start();
			      // sendfile
			     
			      
			    
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}



