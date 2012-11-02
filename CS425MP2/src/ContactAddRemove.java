

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import failureDetection_Membership.WriteLog;


public class ContactAddRemove implements Runnable {
	private Machine m;

	
	public ContactAddRemove(Machine machine) {
		m=machine;
	}

	
	public void sendMemberListToIncoming(DatagramSocket s, String ip_addr) {
		String mList;
		//TODO
		mList = m.memberList.toString();
		mList = "I"+mList;
		m.sendMsg(s, ip_addr, mList, Machine.MEMBERSHIP_PORT);
	}
	
	/**
	 * get msg string from UDP
	 * 
	 * @return
	 */
	public String recvMsg() {
		DatagramPacket recvPacket;
		String recvMsg = null;
		byte[] recvData = new byte[1024];
		recvPacket = new DatagramPacket(recvData,recvData.length);
		
		try {
			
			m.membership_sock.receive(recvPacket);
			recvMsg = new String(recvPacket.getData());
			
			WriteLog.writelog(m.myIP, "received from UDP "+recvMsg);
			
			//System.out.println(recvMsg);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return recvMsg;
	}

	
	
	
	
	
	public void run(){
		byte[] recvData = new byte[1024];
		DatagramPacket recvPacket = new DatagramPacket(recvData,recvData.length);

		try {
			m.membership_sock = new DatagramSocket(Machine.MEMBERSHIP_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true){
			String recvMsg = null;
			
				try {
					m.membership_sock.receive(recvPacket);
					recvMsg = new String(recvPacket.getData());
					//System.out.println(recvMsg);
					
					//need to review - instead of using local memberlist we can think of using central memberlist and using locks to synchronize
					Vector<String> memberList = m.getMemberList();
					WriteLog.printList2Log("Contact", memberList);
					
					
					//need to take decision based on the recvMsg opcode
					if (recvMsg.charAt(0) == 'R')
					{
						String ip = recvMsg.substring(1).trim();
						System.out.println(ip);
						
						WriteLog.writelog("Contact" ,"received incoming socket, remove " + ip );
						
						if (memberList.contains(ip)) {
							memberList.remove(ip);
							int index = memberList.indexOf(m.myIP);
							String prevIP = memberList.get((index - 1 + memberList.size()) % memberList.size());
							m.sendMsg(m.membership_sock, prevIP, recvMsg, Machine.MEMBERSHIP_PORT);
							
							String prevprevIP = memberList.get((index - 1 + memberList.size()) % memberList.size());
							m.sendMsg(m.membership_sock, prevprevIP, recvMsg, Machine.MEMBERSHIP_PORT);
							//need to review - need to add the code for updating map here
							try {
								WriteLog.printList2Log(m.myIP, memberList);
								WriteLog.writelog(m.myIP, "send to "+prevIP+" msg is " + recvMsg);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						//memberList.remove(recvMsg.trim());
						WriteLog.printList2Log("Contact", memberList);
					}
					else if (recvMsg.charAt(0) == 'A')
					{
						// received an incoming socket, append ip address to memberlist
						//String incomingIP = new String(incoming.getData());
						String ip = (recvMsg.substring(1)).trim();
						
						
						
									
						System.out.println(ip);
						
						WriteLog.writelog("Contact" ,"received incoming socket, append " + ip);
						
						//System.out.println(incomingIP.length());
						
						
						if (!memberList.contains(ip)) {
							//System.out.println("999999999999!!!!!!!");
							try {
								WriteLog.writelog(m.myIP, "adddddddddddddd " + ip);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							memberList.add(ip);
							int c = m.map.containsKey(ip)? m.map.get(ip) : 0;
							m.map.put(ip,c+1);   //need to review
							
							int index = m.memberList.indexOf(m.myIP);
							String nextIP = m.memberList.get((index + 1) % memberList.size());
							m.sendMsg(m.membership_sock, nextIP, recvMsg, Machine.MEMBERSHIP_PORT);
							
							//need to review
							String nextnextIP = memberList.get((index + 2) % memberList.size());
							m.sendMsg(m.membership_sock, nextnextIP, recvMsg, Machine.MEMBERSHIP_PORT);				
							
							try {
								WriteLog.printList2Log(m.myIP, memberList);
								WriteLog.writelog(m.myIP, "send to "+nextIP+" msg is " + recvMsg);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						
						WriteLog.printList2Log("Contact", memberList);
						
						//m.membership_sock.close();
						
						//if(memberList.size()>1){
						//sendAddMsg();
						//}
						
					}
					else if (recvMsg.charAt(0) == 'J')
					{
						String ip = (recvMsg.substring(1)).trim();
						WriteLog.writelog("Contact" ,"received incoming socket, remove " + ip );
						
						if (!memberList.contains(ip)) {
							//TODO
							//need to review..some more code needs to be added here
							sendMemberListToIncoming(m.membership_sock, ip);
						} else {
							//TODO
							//need to review..some map related processing will be different in these scenarios
							
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
		}	
	}
}
