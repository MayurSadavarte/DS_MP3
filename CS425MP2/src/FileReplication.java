import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;




public class FileReplication implements Runnable {
	private Machine m;
	public FileTransferClient FileClient;
	public FileTransferServer FileServer;
	public int min_rep=3;
	
	public FileReplication(Machine machine)
	{
		m=machine;		
	}
	
	private void sendListMsg(Vector<String> msgList, String nodeName)
	{
		String mList = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
	    	ObjectOutputStream oos = new ObjectOutputStream(baos);
	    	oos.writeObject(msgList);
	    	oos.flush();
	    	// get the byte array of the object
	    	//byte[] Buf= baos.toByteArray();
	    	mList = baos.toString();
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
		m.sendMsg(m.filerep_sock, nodeName, mList, Machine.FILE_OPERATIONS_PORT);
	}
	
	
	private Vector<String> recvListMsg()
	{
		DatagramPacket recvPacket;
		byte[] recvData = new byte[1024];
		Vector<String> returnList=null;
		try {
			recvPacket = new DatagramPacket(recvData,recvData.length);
			m.filerep_sock.receive(recvPacket);
			//TODO - need to decide whether we need to define this length or not!!
			ByteArrayInputStream bais = new ByteArrayInputStream(recvData);
		
			ObjectInputStream ois = new ObjectInputStream(bais);
			returnList = (Vector<String>)ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();			
		}
		
		return(returnList);
	}
	
	private Vector<String> sort_node_file_map()
	{
		Vector<String> keys = new Vector<String>(m.node_file_map.keySet());
		final HashMap<String, Vector<String>> temp_file_node = m.node_file_map;
		//List<String> tempKeys = (List<String>)keys;
		//List<String> tempKeys = new ArrayList<String>(keys);
		
		Collections.sort(keys, new Comparator<String>(){
					public int compare(String firstkey, String secondkey){
						//String firstkey = (String)first;
						//String secondkey = (String)second;
						Vector<String> firstValue = temp_file_node.get(firstkey);
						Vector<String> secondValue = temp_file_node.get(secondkey);
						int firstLength = firstValue.size();
						int secondLength = secondValue.size();
						
						return (firstLength - secondLength);
					}
				});

		return keys;  //TODO need to verify that sorting on tempkeys actually affects also keys
	
	}
	
	private Vector<String> sort_file_node_map()
	{
		Vector<String> keys = new Vector<String>(m.file_node_map.keySet());
		final HashMap<String, Vector<String>> temp_file_node = m.file_node_map;
		//List<String> tempKeys = (List<String>)keys;
		//List<String> tempKeys = new ArrayList<String>(keys);
		
		Collections.sort(keys, new Comparator<String>(){
					public int compare(String firstkey, String secondkey){
						//String firstkey = (String)first;
						//String secondkey = (String)second;
						Vector<String> firstValue = temp_file_node.get(firstkey);
						Vector<String> secondValue = temp_file_node.get(secondkey);
						int firstLength = firstValue.size();
						int secondLength = secondValue.size();
						
						return (firstLength - secondLength);
					}
				});

		return keys;  //TODO need to verify that sorting on tempkeys actually affects also keys
	}
	
	public void balanceFiles()
	{
		// TODO - called from ContactAddRemove when any node adds or leaves the network, used only by master
		// will first sort the map keys according to the length of value field
		//
		Vector<String> node_file_keys, file_node_keys;
		
		node_file_keys = sort_node_file_map();
		
		file_node_keys = sort_file_node_map();
		
		for(String tempKey: file_node_keys)
		{
			while (m.file_node_map.get(tempKey).size() < min_rep)
			{
				Vector<String> nodeList = m.file_node_map.get(tempKey);
				String targetNode = node_file_keys.firstElement();
				
				
			}
			
		}
		
	}
	
	public void reformFileInfo()
	{
		// TODO - called when a node recieves R message and now i am the new master
		// hence i need to reform the file replication info in the maps
		
	}
	
	public void start()
	{
		FileServer.start();
		//FileClient.start(); //need not start client here..will be started whenever required
		Thread fr_thread = new Thread(this);
		fr_thread.start();
	}
	
	
	/**
	 * @param args
	 */
	public void run(){
		// TODO Auto-generated method stub
		/* start udp socket for listening to control messages
		 * while loop for listening to udp socket
		 * listen to messages based on whether you are a master
		 * start transfer thread based on the control messages
		 */
		byte[] recvData = new byte[1024];
		DatagramPacket recvPacket = new DatagramPacket(recvData,recvData.length);
		try {
			m.filerep_sock = new DatagramSocket(Machine.FILE_OPERATIONS_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true){
			String recvMsg = null;
			
				try {
					m.filerep_sock.receive(recvPacket);
					recvMsg = new String(recvPacket.getData());
					//System.out.println(recvMsg);
					
					//need to review - instead of using local memberlist we can think of using central memberlist and using locks to synchronize
					Vector<String> memberList = m.getMemberList();
					WriteLog.printList2Log("Contact", memberList);
					
					if (m.master)
					{
						//need to take decision based on the recvMsg opcode
						if (recvMsg.charAt(0) == 'P')
						{
						// master receiving PUT
						
						}
						else if (recvMsg.charAt(0) == 'G')
						{
						// master receiving GET
						
						}
						else if (recvMsg.charAt(0) == 'D')
						{
						// master receiving DELETE
							
						}
					}
					else
					{
						if (recvMsg.charAt(0) == 'C')
						{
						// machine receiving COPY 
							
						}
						else if (recvMsg.charAt(0) == 'R')
						{
						// machine receiving REM
							
						}
						else if (recvMsg.charAt(0) == 'Q')
						{
						// machine receiving REPLICATION_INFO_QUERY
							
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();;
				}
		}				
						
						
	}

}
