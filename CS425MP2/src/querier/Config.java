package querier;
import java.util.Vector;

/**
 * this singleton class contains all the connection configuration such as ip
 * address, port number, etc. these stats are saved in vector form later.
 * 
 * @author jason
 * 
 */
public class Config {
	private static Config con = null;
	private Vector<Integer> ports;
	private Vector<String> addrs;

	protected Config() {
		ports = new Vector<Integer>();
		addrs = new Vector<String>();
		addStats();
	}

	public void addPortAndIP(int port, String addr){
		ports.add(port);
		addrs.add(addr);
	}
	private void addStats() {
		ports.add(8888);
		addrs.add("localhost");
		
		ports.add(8887);
		addrs.add("192.17.11.73");
		
		//ports.add(8886);
		//addrs.add("192.17.254.81");
		
		//ports.add(9898);
		//addrs.add("192.17.11.71");

		//ports.add(7777);
		//addrs.add("192.17.11.72");
	}

	/**
	 * call this function to get a singleton instance
	 * 
	 * @return a singleton instance of Config
	 */
	public static Config getInstance() {
		if (con == null)
			con = new Config();
		return con;
	}

	public Vector<Integer> getPorts() {
		return ports;
	}

	public void setPorts(Vector<Integer> ports) {
		this.ports = ports;
	}

	public Vector<String> getAddrs() {
		return addrs;
	}

	public void setAddrs(Vector<String> addrs) {
		this.addrs = addrs;
	}
}
