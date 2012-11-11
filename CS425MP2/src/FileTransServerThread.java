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
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
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
	      
		  } catch (IOException e) {
				e.printStackTrace();
		  }
		
	}
	
}
