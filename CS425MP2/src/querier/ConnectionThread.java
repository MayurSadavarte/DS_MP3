package querier;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * each connection object will spawn a thread to connect to the addr with port
 * to run the command
 * 
 * @author jason
 * 
 */
public class ConnectionThread extends Thread {

	private volatile TransData td;

	public ConnectionThread(String addr, int port, String command) {
		td = new TransData(command);
		td.setAddr(addr);
		td.setPort(port);
	}

	public TransData getTd() {
		return td;
	}

	public void setTd(TransData td) {
		this.td = td;
	}

	@Override
	public void run() {
		Socket s = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		try {
			s = new Socket(td.getAddr(), td.getPort());

			out = new ObjectOutputStream(s.getOutputStream());
			out.flush();
			out.writeObject(td);
			out.flush();

			in = new ObjectInputStream(s.getInputStream());
			TransData td = (TransData) in.readObject();
			// lock here
			synchronized (this) {
				System.out.println(td.getAddr() + ": " + td.getResponse());
			}
			 //unlock here
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
