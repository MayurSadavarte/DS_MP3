import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Vector;


/*
 * CONTACTADDREMOVE INSTNACE OF MACHINE CLASS
 * THIS WILL TAKE CARE OF ALL THE MEMBERSHIP
 * LIST RELATED TASKS
 */
public class ContactAddRemove implements Runnable {
	private Machine m;

	
	public ContactAddRemove(Machine machine) {
		m=machine;
	}

	
	public void sendMemberListToIncoming(DatagramSocket s, String ip_addr) {
		byte[] mList = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
	    	ObjectOutputStream oos = new ObjectOutputStream(baos);
	    	oos.writeObject(m.memberList);
	    	oos.flush();
	    	//TODO - need to decide whether we need to send length also in first packet and then actual packet
	    	// get the byte array of the object
	    	mList = baos.toByteArray();
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
		m.sendMsg(s, ip_addr, mList, Machine.MEMBERSHIP_PORT);
	}
	
	
	/*
	 * get 'String' msg from membership UDP socket
	 * 
	 * @return
	 */
	public String recvStrMsg() {
		DatagramPacket recvPacket;
		String recvMsg = null;
		byte[] recvData = new byte[1024];
		//recvPacket = new DatagramPacket(recvData,recvData.length);
		
		try {
			recvPacket = new DatagramPacket(recvData,recvData.length);
			
			m.membership_sock.receive(recvPacket);
			//TODO - need to decide whether we need to define this length or not!!
			ByteArrayInputStream bais = new ByteArrayInputStream(recvData);
		
			ObjectInputStream ois = new ObjectInputStream(bais);
			recvMsg = (String)ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();			
		}
		try {
			WriteLog.writelog(m.myName, "received "+recvMsg +" through "+ Integer.toString(Machine.FILE_OPERATIONS_PORT));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return recvMsg;
	}
	
	
	
	public void run(){
	
		while(true){
			String recvMsg;
			
				try {
					recvMsg = recvStrMsg();
					
					Vector<String> memberList = m.getMemberList();
					WriteLog.printList2Log(m.myName, memberList);
					
					
					//need to take decision based on the recvMsg opcode
					if (recvMsg.charAt(0) == 'R')
					{
						String ip = recvMsg.substring(1).trim();
						System.out.println(ip);
						
						WriteLog.writelog(m.myName,"received REM on incoming socket for- " + ip );
						
						if (memberList.contains(ip)) {
							
							String newMaster = null;
							if (ip.equals(m.masterName))
							{
								WriteLog.writelog(m.myName,"It appears that Master is dead!!- " + ip );
								int mindex = memberList.indexOf(m.masterName);
								newMaster = memberList.get((mindex + 1) % memberList.size());
								m.masterName = newMaster;
							}
							
							memberList.remove(ip);
							
							try {
								WriteLog.writelog(m.myName, "memberList after REM - "+memberList.toString());
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//code for checking if now I am a new master, if yes, i need to run some code
							//change the mode
							//contact all the nodes in the memberlist for file replication info
							
							if(m.myName.equals(newMaster))
							{
								WriteLog.writelog(m.myName,"It appears that I am the new Master now!!");
								m.master = true;
								m.file_node_map = new HashMap<String, Vector<String>>();
								m.node_file_map = new HashMap<String, Vector<String>>();
								m.FileReplicator.reformFileInfo();
							}
						}
						
						if (m.master)
						{
							
							if(m.node_file_map.containsKey(ip))
								m.node_file_map.remove(ip);
							for (String tkey : m.file_node_map.keySet())
							{
								Vector<String> tvalue = m.file_node_map.get(tkey);
								if (tvalue.contains(ip))
								{
									// TODO - can it work?? will this affect file_node_map?
									tvalue.remove(ip);
									// TODO - trigger file balancing thread here
								}
							}
							//call to filereplicator for balancing maps
							m.FileReplicator.balanceFiles();
						}
					}
					else if (recvMsg.charAt(0) == 'A')
					{
						//received an incoming packet, append ip address to memberlist
						
						String ip = (recvMsg.substring(1)).trim();
														
						System.out.println(ip);
						
						WriteLog.writelog(m.myName ,"received incoming socket, ADD " + ip);
						
						if (!memberList.contains(ip)) {
							try {
								WriteLog.writelog(m.myName, "actual adddddddddddddd " + ip);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							memberList.add(ip);				
							
							try {
								WriteLog.printList2Log(m.myName, memberList);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					}
					else if (m.master)
					{
						if (recvMsg.charAt(0) == 'J')
						{
							String ip = (recvMsg.substring(1)).trim();
							WriteLog.writelog(m.myName ,"received incoming socket, JOIN " + ip );
						
							if (!memberList.contains(ip)) {
								//TODO
								//need to review..some more code needs to be added here
								memberList.add(ip);
								sendMemberListToIncoming(m.membership_sock, ip);
								m.sendMsgToAllNodes(ip, "ADD");
							} else {
								//TODO
								//need to review..some map related processing will be different in these scenarios
								sendMemberListToIncoming(m.membership_sock, ip);
							}
					
							if(!m.node_file_map.containsKey(ip))
							{
								//TODO - need to decide whether we are going to save the replicated file info while dying 
								// and then sending it in J message to the master
								Vector<String> emptyList = new Vector<String>();
								m.node_file_map.put(ip, emptyList);
							
								//TODO - need to run balancing algorithm here
								m.FileReplicator.balanceFiles();
								// get the id's of the files written to this node and then update the file_node_map accordingly
								// or we could update it inside balanceFiles itself
								//TODO - need to send out the ADD message to all the nodes
								//m.sendMsgToAllNodes(ip, "ADD");
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
		}	
	}
}
