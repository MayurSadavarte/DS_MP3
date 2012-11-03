
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Vector;
import java.io.ByteArrayInputStream;


import querier.Server;

public class Machine {

	public HashMap<String, Vector<String>> file_node_map;
	public HashMap<String, Vector<String>> node_file_map;
	public static final int MEMBERSHIP_PORT = 8889;
	public static final int FILE_OPERATIONS_PORT = 8891;
	public static final int FILE_TRANSFER_PORT = 8892;
	//public static final int DEFAULT_CONTACT_PORT = 8888;
	public static final int HEARTBEAT_PORT = 8890;
	public static final int QUERY_PORT = 10000;
	
	public DatagramSocket membership_sock;
	public DatagramSocket heartbeat_sock;
	public DatagramSocket filerep_sock;
	public DatagramSocket outgoing;
	public Vector<String> memberList;
	private String contactIP;
	public static String myIP;
	public boolean master = false;
	public FileReplication FileReplicator;
	
	public Machine(boolean mflag) {
		master = mflag;
		membership_sock = null;
		outgoing = null;
		memberList = null;
		if (master)
			file_node_map = new HashMap<String, Vector<String>>();
		node_file_map = new HashMap<String, Vector<String>>();
		try {
			myIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	public Machine() {
		membership_sock = null;
		outgoing = null;
		memberList = null;
		if (master)
			file_node_map = new HashMap<String, Vector<String>>();
		node_file_map = new HashMap<String, Vector<String>>();
		
		try {
			myIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get memberlist from the connecting contact
	 * 
	 * @param ip
	 *            contact machine ip
	 * @param port
	 *            port number
	 */
	@SuppressWarnings("unchecked")
	public void getMemberlistFromIP(String ip) {
		String joinMsg;
		String recvMsg = null;
		DatagramPacket recvPacket;
		byte[] recvData = new byte[1024];
		
		joinMsg = 'J'+myIP;
		sendMsg(membership_sock, ip, joinMsg, Machine.MEMBERSHIP_PORT);
		
		try {
		recvPacket = new DatagramPacket(recvData,recvData.length);
		membership_sock.receive(recvPacket);
		//TODO - need to decide whether we need to define this length or not!!
		ByteArrayInputStream baos = new ByteArrayInputStream(recvData);
		
			ObjectInputStream oos = new ObjectInputStream(baos);
			memberList = (Vector<String>)oos.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();			
		}
	    
		try {
			contactIP = ip;
			
			WriteLog.writelog(myIP, "received ML");
			WriteLog.printList2Log(myIP, memberList);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		//recvMsg = new String(recvPacket.getData());
		
		//need to review - need to set memberlist using recvPkt
	}

	
	
	/**
	 * send an add message to the first machine in the system
	 */
	@SuppressWarnings("resource")
	public void sendMsg(DatagramSocket sock, String ip, String msg, int portN) {
		try {
			InetAddress ipAddr = InetAddress.getByName(ip);
			byte[] sendData = msg.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, ipAddr, portN);
			sock.send(sendPacket);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * start the server socket and listen to membership_sock connection request
	 */
	public void startAddRem() {
		
		Runnable runnable = new ContactAddRemove(this);
		Thread thread = new Thread(runnable);
		thread.start();
	}
	

	public void startFileReplication() {
		FileReplicator = new FileReplication(this);
		FileReplicator.start();
	}
	
	public static void main(String[] args) {
		boolean mflag = false;
		Machine m;
		if (args.length == 2) {
			mflag = args[1].equals("mastermode");
			m = new Machine(mflag);
		} else {
			m = new Machine();
		}
		
		m.startAddRem();
		//join
		if (m.master)
		{
		//TODO - need to review the file_node_map.put call
			m.memberList.add(myIP);
			//m.file_node_map.put(null, m.memberList);
			m.node_file_map.put(myIP, null);
		}
		else
		{
			m.getMemberlistFromIP(args[0]);
			m.node_file_map.put(myIP, null);
		}
		// r (String s : m.getMemberList())
		// System.out.println(s);
		m.startFileReplication();
		
		try {
			WriteLog.printList2Log(myIP, m.memberList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		Runnable runnableS = new HeartbeatSender(m);
		Thread threadS = new Thread(runnableS);
		threadS.start();
		Runnable runnableR = new HeartbeatReceiver(m);
		Thread threadR = new Thread(runnableR);
		threadR.start();
		Runnable commandC = new VoluntaryLeave(m);
		Thread threadC = new Thread(commandC);
		threadC.start();
	}

	public DatagramSocket getmembership_sock() {
		return membership_sock;
	}

	public void setmembership_sock(DatagramSocket membership_sock) {
		this.membership_sock = membership_sock;
	}

	public DatagramSocket getOutgoing() {
		return outgoing;
	}

	public void setOutgoing(DatagramSocket outgoing) {
		this.outgoing = outgoing;
	}

	public Vector<String> getMemberList() {
		return memberList;
	}

	public void setMemberList(Vector<String> memberList) {
		this.memberList = memberList;
	}

	public String getContactIP() {
		return contactIP;
	}

	public void setContactIP(String contactIP) {
		this.contactIP = contactIP;
	}

	public String getMyIP() {
		return myIP;
	}

	public void setMyIP(String myIP) {
		this.myIP = myIP;
	}
}
