
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Vector;
import java.io.ByteArrayInputStream;


import querier.Server;

/* 
 * MACHINE CLASS WHICH REPRESENTS GENERAL SERVER IN THE DISTRIBUTED FILE SYSTEM
 * ONE OF THE MACHINES WILL BE THE MASTER SERVER AND THAT IS DENOTED BY THE BOOLEAN
 * VARIABLE 'MASTER' IN MACHINE INSTANCE 
 */
public class Machine {

	public HashMap<String, Vector<String>> file_node_map;
	public HashMap<String, Vector<String>> node_file_map;
	public Vector<String> myFileList;
	public static final int MEMBERSHIP_PORT = 10001;
	public static final int FILE_OPERATIONS_PORT = 8891;
	public static final int FILE_TRANSFER_PORT = 8892;
	public static final int HEARTBEAT_PORT = 8890;
	public static final int QUERY_PORT = 10000;
	
	public DatagramSocket membership_sock;
	public DatagramSocket heartbeat_sock=null;
	public DatagramSocket filerep_sock;
	public Vector<String> memberList;
	public String myName;
	public String masterName;
	public boolean master = false;
	public FileReplication FileReplicator;
	
	public Machine(boolean mflag) {
		master = mflag;
		membership_sock = null;
		memberList = null;
		myFileList = null;
		if (master)
			file_node_map = new HashMap<String, Vector<String>>();
			node_file_map = new HashMap<String, Vector<String>>();
		try {
			myName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	public Machine() {
		membership_sock = null;
		memberList = null;
		myFileList = null;
		if (master)
			file_node_map = new HashMap<String, Vector<String>>();
			node_file_map = new HashMap<String, Vector<String>>();
		try {
			myName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * get memberlist from the master
	 * 
	 * @param ip
	 *            contact machine ip/hostname
	 */
	@SuppressWarnings("unchecked")
	public void getMemberlistFromIP(String ip) {
		String joinMsg; 
		byte[] joinbaos=null;
		joinMsg = 'J'+myName;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(joinMsg);
	    	oos.flush();
	    	joinbaos = baos.toByteArray();
		} catch(IOException e) {
	    	e.printStackTrace();
	    }
		
		sendMsg(membership_sock, ip, joinbaos, Machine.MEMBERSHIP_PORT);
		
		DatagramPacket recvPacket;
		byte[] recvData = new byte[1024];
		try {
			recvPacket = new DatagramPacket(recvData,recvData.length);
			membership_sock.receive(recvPacket);
			//TODO - need to decide whether we need to define this length or not!!
			ByteArrayInputStream bais = new ByteArrayInputStream(recvData);
		
			ObjectInputStream ois = new ObjectInputStream(bais);
			memberList = (Vector<String>)ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();			
		}
	    
		try {
			
			WriteLog.writelog(myName, "received MemberList");
			WriteLog.printList2Log(myName, memberList);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	/*
	 * send an add message to the any machine in the system
	 * through the udp socket
	 * all sockets are covered under machine instance
	 */
	@SuppressWarnings("resource")
	public void sendMsg(DatagramSocket sock, String ip, byte[] msg, int portN) {
		try {
			InetAddress ipAddr = InetAddress.getByName(ip);
			//byte[] sendData = msg.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(msg,
					msg.length, ipAddr, portN);
			sock.send(sendPacket);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
		/* 
	 * for sending 'ADD' or 'REM' message to all
	 * the servers in the file system
	 * called when any server joins the system
	 * or any existing server leaves the system
	 */
	public void sendMsgToAllNodes(String nodeIP, String cmd)
	{
		String addOrRemMsg = null;
		byte[] fmsg = null;
		if (cmd == "ADD")
			addOrRemMsg = 'A'+nodeIP;
		else if (cmd == "REM")
			addOrRemMsg = 'R'+nodeIP;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
	    	ObjectOutputStream oos = new ObjectOutputStream(baos);
	    	oos.writeObject(addOrRemMsg);
	    	oos.flush();
	    	fmsg= baos.toByteArray();
	    	//fmsg = baos.toString();
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
		
		for (String tempIP : memberList)
		{
			sendMsg(membership_sock, tempIP, fmsg, Machine.MEMBERSHIP_PORT);
		}
		
	}
	
	
	/*
	 * start the AddRem instance of machine
	 * this instance will take care of membership list management 
	 */
	public void startAddRem() {
		
		Runnable runnable = new ContactAddRemove(this);
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	
	
	/*
	 * start file replicator instance of machine
	 * this instance will take care of all the
	 * file replication and addition, deletion
	 * requests
	 */
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
		m.memberList = new Vector<String>();
		m.myFileList = new Vector<String>();
		
		
		try {
			m.membership_sock = new DatagramSocket(Machine.MEMBERSHIP_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (m.master)
		{
		//TODO - need to review the file_node_map.put call
			System.out.println("in machine main: program started in 'mastermode'!!");
			m.masterName = m.myName;
			m.memberList.add(m.myName);
			
			Vector<String> emptyList = new Vector<String>();
			m.node_file_map.put(m.myName, emptyList);
			m.startAddRem();
		}
		else
		{
			System.out.println("in machine main: program started in 'machinemode'!!");
			m.masterName = args[0];
			m.getMemberlistFromIP(args[0]);
			m.startAddRem();
		}

		m.startFileReplication();
		
		try {
			WriteLog.printList2Log(m.myName, m.memberList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		//heartbeat sender instance
		Runnable runnableS = new HeartbeatSender(m);
		Thread threadS = new Thread(runnableS);
		threadS.start();
		//heartbeat receiver instance
		Runnable runnableR = new HeartbeatReceiver(m);
		Thread threadR = new Thread(runnableR);
		threadR.start();
		//voluntary leave instance
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
	
	public Vector<String> getMemberList() {
		return memberList;
	}

	public void setMemberList(Vector<String> memberList) {
		this.memberList = memberList;
	}

	public String getContactName() {
		return masterName;
	}

	public void setContactName(String contactIP) {
		this.masterName = contactIP;
	}

	public String getMyName() {
		return myName;
	}

	public void setMyName(String myIP) {
		this.myName = myIP;
	}
}
