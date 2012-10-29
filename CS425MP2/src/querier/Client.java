package querier;
import java.util.Scanner;
import java.util.Vector;

/**
 * client keeps a vector of sockets that connect with servers. each socket send
 * same query to the server and wait for response. when the server sends back
 * the response, the client will put all the sockets together to display a whole
 * response.
 * 
 * @author jason
 * 
 */
public class Client {

	private String testCommand;
	private Vector<ConnectionThread> threads;

	/**
	 * constructor for test
	 * 
	 * @param testCommand
	 */
	public Client(String testCommand) {
		this.testCommand = testCommand;
		threads = new Vector<ConnectionThread>();
	}

	/**
	 * default constructor will read command from command line
	 */
	public Client() {
		testCommand = null;
		threads = new Vector<ConnectionThread>();
	}

	/**
	 * method to wrap things around
	 */
	public void clientWrap() {
		long start = System.currentTimeMillis();
		
		Config config = Config.getInstance();
		Vector<String> addrs = config.getAddrs();
		Vector<Integer> ports = config.getPorts();

		Scanner s;
		if (testCommand == null)
			s = new Scanner(System.in);
		else
			s = new Scanner(testCommand);
		String line = s.nextLine();
		// insert thread function
		for (int i = 0; i < addrs.size(); i++) {
			ConnectionThread t = new ConnectionThread(addrs.get(i),
					ports.get(i), line);
			t.start();
			threads.add(t);
		}
		for (int i = 0; i < threads.size(); i++)
			try {
				ConnectionThread ct = threads.get(i);
				ct.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		s.close();
		long end = System.currentTimeMillis();
		System.out.println(end-start);
	}

	public static void main(String[] args) {
		new Client().clientWrap();
	}

	public Vector<ConnectionThread> getThreads() {
		return threads;
	}

	public void setThreads(Vector<ConnectionThread> threads) {
		this.threads = threads;
	}
}
