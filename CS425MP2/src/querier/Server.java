package querier;
import java.io.*;
import java.net.*;

/**
 * server listens to any incoming connections, run the grep command, and then
 * return the grep result
 * 
 * @author jason
 * 
 */
public class Server {

	ServerSocket serverSocket;

	public Server(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * wrapper method
	 */
	public void serverWrapper() {
		System.out.println("Server is running...");
		while (true) {
			Socket connect = null;
			ObjectOutputStream out = null;
			ObjectInputStream in = null;

			try {
				connect = serverSocket.accept();
				System.out.println("get connection from: "
						+ connect.getInetAddress().getHostAddress());
				in = new ObjectInputStream(connect.getInputStream());
				TransData recv = (TransData) in.readObject();
				TransData sendBack = runCommand(recv);

				out = new ObjectOutputStream(connect.getOutputStream());
				out.flush();
				out.writeObject((Object) sendBack);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					out.close();
					in.close();
					connect.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * process the command in the transdata
	 * 
	 * @param td
	 *            incoming transdata object
	 * @return processed transdata object that is ready to sent back to the
	 *         client
	 * @throws IOException
	 */
	private TransData runCommand(TransData td) {

		System.out.println(td.toString());
		InputStreamReader isr = null;
		BufferedReader br = null;
		Process p;
		try {
			p = Runtime.getRuntime().exec(td.getCommand());
			isr = new InputStreamReader(p.getInputStream());
			br = new BufferedReader(isr);

		} catch (IOException e) {
			e.printStackTrace();
		}

		String response = "";
		String line = null;

		try {
			while ((line = br.readLine()) != null)
				if (line != null)
					response += (line + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		td.setResponse(response);
		return td;
	}

	public static void main(String[] args) {
		new Server(8888).serverWrapper();
	}
}
