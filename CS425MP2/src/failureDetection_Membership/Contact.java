package failureDetection_Membership;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Vector;

import querier.Client;

public class Contact {
	private Vector<String> memberList;
	private ServerSocket server;
	private HashMap<String, Integer> map;
	private boolean flag;
	public static final int ADD_AND_REMOVE_PORT = 8889;
	public static final int CONTACT_REMOVE_PORT = 8891;
	public static final int DEFAULT_CONTACT_PORT = 8888;
	public static final int HEARTBEAT_PORT = 8890;
	public static final int QUERY_PORT = 10000;
	
	
	public Contact(int portNum) {
		setMemberList(new Vector<String>());
		map = new HashMap<String, Integer>();
		flag = true;
		try {
			server = new ServerSocket(portNum);
			WriteLog.writelog("Contact" ,"created, porNum is " + portNum);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * start the server socket and listen to incoming connection request
	 */
	public void startServer() {
		
		Runnable runnable = new ContactRemove(this);
		Thread thread = new Thread(runnable);
		thread.start();
		
		while (flag) {
			try {
				Socket incoming = server.accept();
				// received an incoming socket, append ip address to memberlist
				//String incomingIP = new String(incoming.getData());
				
				String incomingIP = incoming.getInetAddress().getHostAddress();
				System.out.println(incomingIP);
				
				WriteLog.writelog("Contact" ,"received incoming socket, append " + incomingIP );
				
				//System.out.println(incomingIP.length());
				
				if(!memberList.contains(incomingIP.trim()))
					memberList.add(incomingIP);
				
				int c = map.containsKey(incomingIP)? map.get(incomingIP) : 0;
				map.put(incomingIP,c+1);
				
				
				WriteLog.printList2Log("Contact", memberList);
				sendMemberListToIncoming(incoming);
				incoming.close();
				
				//if(memberList.size()>1){
					sendAddMsg();
				//}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * send an add message to the first machine in the system
	 */
	@SuppressWarnings("resource")
	public void sendAddMsg() {
		try {
			DatagramSocket dsClient = new DatagramSocket();
			InetAddress ipAddr = InetAddress.getByName(memberList
					.firstElement());
			String addMsg = "A" + memberList.lastElement();
			byte[] sendData = addMsg.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, ipAddr, Contact.ADD_AND_REMOVE_PORT);
			WriteLog.writelog("Contact", "send add msg: " + addMsg + " to " + ipAddr);			
			dsClient.send(sendPacket);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * send the memberlist to the incoming connection request socket/machine
	 * 
	 * @param s
	 *            incoming socket from a machine
	 */
	public void sendMemberListToIncoming(Socket s) {
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(memberList);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Vector<String> getMemberList() {
		return memberList;
	}

	public void setMemberList(Vector<String> memberList) {
		this.memberList = memberList;
	}

	public static void main(String[] args) {
		Contact c = new Contact(DEFAULT_CONTACT_PORT);
		c.startServer();
	}
}
