import java.io.*;
import java.net.*;

public class FileTransferClient implements Runnable {
	/*
	Socket fclientSock = null;  
    DataOutputStream os = null;
    DataInputStream is = null;
	InetAddress ftransferServer = null;
    
	public void start(String fserver)
	{
		try {
			InetAddress ftransferServer = InetAddress.getByName(fserver);
			fclientSock = new Socket(ftransferServer, Machine.FILE_TRANSFER_PORT);
			os = new DataOutputStream(fclientSock.getOutputStream());
            is = new DataInputStream(fclientSock.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: hostname");
		}
		Thread client_thread = new Thread(this);
		client_thread.start();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	*/
	private String copyFN;
	private String serverIP;
	
	public FileTransferClient(String s, String ip){
		copyFN = s;
		serverIP = ip;
	}
	
	public void run(){
		
		int filesize=6022386; // filesize temporary hardcoded

	    long start = System.currentTimeMillis();
	    int bytesRead;
	    int current = 0;
	   
	    Socket sock;
			    
	    
		try {
			sock = new Socket(serverIP, Machine.FILE_TRANSFER_PORT);
			System.out.println("Connecting...");
			// receive file
			byte [] mybytearray  = new byte [filesize];
		    InputStream is;
			is = sock.getInputStream();
			FileOutputStream fos = new FileOutputStream(copyFN);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
		    bytesRead = is.read(mybytearray,0,mybytearray.length);
		    current = bytesRead;
		    do {
			       bytesRead =
			          is.read(mybytearray, current, (mybytearray.length-current));
			       if(bytesRead >= 0) current += bytesRead;
			    } while(bytesRead > -1);

			    bos.write(mybytearray, 0 , current);
			    bos.flush();
			    long end = System.currentTimeMillis();
			    System.out.println(end-start);
			    bos.close();
			    sock.close();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    

	   
		
	}
}
