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
	
	private String sourceFN;
	
	//public FileTransferServer(String s){
	//	sourceFN = s;
	//}
	
	public void setSource(String s){
		sourceFN = s;
	}
	
	public void run(){
		while(true){
		
			try{
				ServerSocket servsock = new ServerSocket(Machine.FILE_TRANSFER_PORT);
			    
			      System.out.println("Waiting...");
	
			      Socket sock = servsock.accept();
			      System.out.println("Accepted connection : " + sock);
			      
			      
			      
			      // sendfile
			      File myFile = new File (sourceFN);
			      byte [] mybytearray  = new byte [(int)myFile.length()];
			      FileInputStream fis = new FileInputStream(myFile);
			      BufferedInputStream bis = new BufferedInputStream(fis);
			      bis.read(mybytearray,0,mybytearray.length);
			      OutputStream os = sock.getOutputStream();
			      System.out.println("Sending...");
			      os.write(mybytearray,0,mybytearray.length);
			      os.flush();
			      sock.close();
			      
			    
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}



