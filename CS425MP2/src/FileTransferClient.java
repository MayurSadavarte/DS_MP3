import java.io.*;
import java.net.*;

public class FileTransferClient implements Runnable {
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

}
