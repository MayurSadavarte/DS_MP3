package failureDetection_Membership;

import java.io.*;
import java.net.*;
import java.util.Vector;

import querier.Server;

public class Machine {

	
	private DatagramSocket incoming;
	private DatagramSocket outgoing;
	private Vector<String> memberList;
	private String contactIP;
	private static String myIP;
	DatagramPacket recvPacket;
	
	
	public Machine() {
		incoming = null;
		outgoing = null;
		memberList = null;
		try {
			myIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		byte[] recvData = new byte[1024];
		recvPacket = new DatagramPacket(recvData,
				recvData.length);
		try {
			incoming = new DatagramSocket(Contact.ADD_AND_REMOVE_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
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
	public void getMemberlistFromIP(String ip, int port) {
		try {
			contactIP = ip;
			Socket s = new Socket(ip, port);
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			memberList = (Vector<String>) ois.readObject();
			
			WriteLog.writelog(myIP, "received ML");
			WriteLog.printList2Log(myIP, memberList);
			
			s.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get msg string from UDP
	 * 
	 * @return
	 */
	public String getMsgFromUDP() {
		String recvMsg = null;
		try {
			
			incoming.receive(recvPacket);
			recvMsg = new String(recvPacket.getData());
			
			WriteLog.writelog(myIP, "received from UDP "+recvMsg);
			
			//System.out.println(recvMsg);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] recvData = new byte[1024];
		recvPacket.setData(recvData);
		
		return recvMsg;
	}

	/**
	 * process received msg
	 * 
	 * @param msg
	 */
	public void processMsg(String msg) {
		if (msg.charAt(0) == 'A') {
			String ip = (msg.substring(1)).trim();
			boolean contains = false;
			
			for(int i=0; i<memberList.size(); i++){
//				System.out.println(memberList.get(i));
				if(memberList.get(i).equals(ip) ){
					contains = true;
				//	System.out.println("!!!!!!!!!!!!!");
					break;
				}
			}
			
			
			
			if (!contains) {
				//System.out.println("999999999999!!!!!!!");
				try {
					WriteLog.writelog(myIP, "adddddddddddddd " + ip);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				memberList.add(ip);
				int index = memberList.indexOf(myIP);
				String nextIP = memberList.get((index + 1) % memberList.size());
				sendMsg(nextIP, msg, Contact.ADD_AND_REMOVE_PORT);
				String nextnextIP = memberList.get((index + 2) % memberList.size());
				sendMsg(nextnextIP, msg, Contact.ADD_AND_REMOVE_PORT);				
				
				try {
					WriteLog.printList2Log(myIP, memberList);
					WriteLog.writelog(myIP, "send to "+nextIP+" msg is " + msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if (msg.charAt(0) == 'R'){
			String ip = msg.substring(1).trim();
			if (memberList.contains(ip)) {
				memberList.remove(ip);
				int index = memberList.indexOf(myIP);
				String prevIP = memberList.get((index - 1 + memberList.size()) % memberList.size());
				sendMsg(prevIP, msg, Contact.ADD_AND_REMOVE_PORT);
				String prevprevIP = memberList.get((index - 1 + memberList.size()) % memberList.size());
				sendMsg(prevprevIP, msg, Contact.ADD_AND_REMOVE_PORT);
				
				try {
					WriteLog.printList2Log(myIP, memberList);
					WriteLog.writelog(myIP, "send to "+prevIP+" msg is " + msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * send an add message to the first machine in the system
	 */
	@SuppressWarnings("resource")
	public void sendMsg(String ip, String msg, int portN) {
		try {
			DatagramSocket dsClient = new DatagramSocket();
			InetAddress ipAddr = InetAddress.getByName(ip);
			byte[] sendData = msg.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, ipAddr, portN);
			dsClient.send(sendPacket);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

	public static void main(String[] args) {
		Machine m = new Machine();
		//join
		m.getMemberlistFromIP(args[0], 8888);
		// r (String s : m.getMemberList())
		// System.out.println(s);
		
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
		
		while(true){
			String msg = m.getMsgFromUDP();
			m.processMsg(msg);
		}
	}

	public DatagramSocket getIncoming() {
		return incoming;
	}

	public void setIncoming(DatagramSocket incoming) {
		this.incoming = incoming;
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
