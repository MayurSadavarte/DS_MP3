package failureDetection_Membership;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Vector;


public class ContactRemove implements Runnable {
	private Contact c;
	
	public ContactRemove(Contact contact) {
		c=contact;
	}

	public void run(){
		byte[] recvData = new byte[1024];
		DatagramPacket recvPacket = new DatagramPacket(recvData,recvData.length);
		DatagramSocket incoming = null;
		
		try {
			incoming = new DatagramSocket(Contact.CONTACT_REMOVE_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		while(true){
			String recvMsg = null;
			
				try {
					incoming.receive(recvPacket);
					recvMsg = new String(recvPacket.getData());
					//System.out.println(recvMsg);
					Vector<String> memberList = c.getMemberList();
					WriteLog.printList2Log("Contact", memberList);
					memberList.remove(recvMsg.trim());
					WriteLog.printList2Log("Contact", memberList);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
		}	
	}
}
