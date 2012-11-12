import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Vector;

public class FileTransServerThread implements Runnable {
	Socket sock = null;
	String sourceFN = null;
	
	public FileTransServerThread(Socket s) {
		// TODO Auto-generated constructor stub
		sock = s;
		
	}
	
	public void start()
	{
		Thread thread  = new Thread(this);
		thread.start();
	}

	public void run(){
		  try {
			  ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
			  try {
				sourceFN = (String) ois.readObject();
				//ois.close();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
			
			
			try {
				String myName = InetAddress.getLocalHost().getHostName();
				WriteLog.writelog(myName, "sourceFN: "+sourceFN);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		  File myFile = new File (sourceFN);
		  long flength = (long)myFile.length();
		  int current=0, bytesWritten=0;
	      byte [] mybytearray  = new byte [4096];
	      FileInputStream fis = new FileInputStream(myFile);
	      BufferedInputStream bis = new BufferedInputStream(fis);
	      OutputStream os = sock.getOutputStream();
	      System.out.println("Sending...");
	      
	      do {
	    	  current = bis.read(mybytearray,0,mybytearray.length);
	    	  os.write(mybytearray,0,current);
	    	  bytesWritten = bytesWritten + current;
	    	  flength = flength - current;
	      } while(flength > 0);
	      
	      
	      os.flush();
	      sock.close();
	      os.close();
		  } catch (IOException e) {
				e.printStackTrace();
		  }
		
	}
	
}
