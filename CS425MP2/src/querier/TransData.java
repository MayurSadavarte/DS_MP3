package querier;
import java.io.Serializable;

/**
 * this is the object that will be passed from client to server and vice versa
 * 
 * @author jason
 * 
 */
public class TransData implements Serializable {

	private static final long serialVersionUID = -5482744767548393676L;
	private String[] command;
	private String response;
	private String addr;
	private int port;

	public TransData(String command) {
		this.command = command.split(" ");
	}

	public String[] getCommand() {
		return command;
	}

	public void setCommand(String[] command) {
		this.command = command;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		String retVal = "";
		for (int i = 0; i < command.length; i++)
			retVal += command[i] + " ";
		return retVal;
	}
}
