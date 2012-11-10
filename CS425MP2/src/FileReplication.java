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
	private boolean rep_info_reformed = false;
	
	public FileReplication(Machine machine)
	{
		m=machine;
		FileServer = new FileTransferServer();
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
	
	
	private int getAvgFilesPerNode()
	{
		float actAvg = 0.0f;
		int intAvg = 0;
		int cumCnt = 0;
		
		for (String key: m.node_file_map.keySet())
		{
			cumCnt = cumCnt + m.node_file_map.get(key).size();
		}
		
		actAvg = (float)cumCnt/(float)m.node_file_map.keySet().size();
		
		intAvg = (int)actAvg;
		return(intAvg);
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
			if (m.file_node_map.get(tempKey).size() < min_rep)
			{
				while (m.file_node_map.get(tempKey).size() < min_rep)
				{
					Vector<String> nodeList = m.file_node_map.get(tempKey);
					String targetNode = node_file_keys.firstElement();
				
					Vector<String> msgList=new Vector<String>();
					msgList.add("C");
					msgList.add(nodeList.firstElement());
					msgList.add(tempKey);
			
					sendListMsg(msgList, targetNode);
					m.file_node_map.get(tempKey).add(targetNode);
					m.node_file_map.get(targetNode).add(tempKey);
					node_file_keys = sort_node_file_map();
				}
			} else
				break;
		}
		
		int lowAvgFiles = 0;
		lowAvgFiles = getAvgFilesPerNode();
		int highAvgFiles = lowAvgFiles + 1;
		
		int firstIndex=0, lastIndex=0;
		lastIndex = node_file_keys.size();
		
		String firstKey = node_file_keys.get(firstIndex);
		String lastKey = node_file_keys.get(lastIndex-1);  //TODO need to be sure about the maximum index value
		
		
		
		while(m.node_file_map.get(firstKey).size() < lowAvgFiles)
		{
			//have removed this 'm.node_file_map.get(lastIndex).size() > highAvgFiles' from the while condition
			
			if(m.node_file_map.get(firstKey).size() >= lowAvgFiles)
			{
				firstKey = node_file_keys.get(firstIndex+1); //TODO - does mod operation need to be done here?
				continue;
			}
			if(m.node_file_map.get(lastKey).size() <= highAvgFiles)
			{
				lastKey = node_file_keys.get(lastIndex-1); //same as TODO above
				continue;
			}
			
			Vector<String> filesAtLastNode = m.node_file_map.get(node_file_keys.get(lastIndex));
			String filetoCopy=null;
			String nodetoCopyFrom=null;
			for(String file: filesAtLastNode)
			{
				if(m.file_node_map.get(file).contains(firstKey))
					continue;
				filetoCopy = file;
				Vector<String> nodeList = m.file_node_map.get(file);
				for(String node: nodeList)
				{
					if(node != lastKey)
						nodetoCopyFrom = node;
						break;
				}
				break;
			}
			if (filetoCopy == null)
			{
				System.out.println("Couldn't find a file which can be replicated");
			}
			Vector<String> cpmsgList=new Vector<String>();
			cpmsgList.add("C");
			cpmsgList.add(nodetoCopyFrom);
			cpmsgList.add(filetoCopy);
	
			sendListMsg(cpmsgList, firstKey);
	
			Vector<String> rmmsgList=new Vector<String>();
			rmmsgList.add("R");
			rmmsgList.add(filetoCopy);
	
			sendListMsg(rmmsgList, lastKey);
			
			m.file_node_map.get(filetoCopy).add(firstKey);
			m.file_node_map.get(filetoCopy).remove(lastKey);
			m.node_file_map.get(firstKey).add(filetoCopy);
			m.node_file_map.get(lastKey).remove(filetoCopy);
		
		}	
		
	}
	
	public void reformFileInfo()
	{
		// TODO - called when a node recieves R message and now i am the new master
		// hence i need to reform the file replication info in the maps
		// check rep_info_reformed after sending the req to all nodes
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
		Vector<String> recvList=null;
		try {
			m.filerep_sock = new DatagramSocket(Machine.FILE_OPERATIONS_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true){
				try {
					//receive the list of strings from the udp socket
					recvList = recvListMsg();
					System.out.println(recvList);
					
					//need to review - instead of using local memberlist we can think of using central memberlist and using locks to synchronize
					Vector<String> memberList = m.getMemberList();
					WriteLog.printList2Log("Contact", memberList);
					
					if (m.master)
					{
						//need to take decision based on the recvMsg opcode
						if (recvList.firstElement() == "P")
						{
						// master receiving PUT
						
						}
						else if (recvList.firstElement() == "G")
						{
						// master receiving GET
						
						}
						else if (recvList.firstElement() == "D")
						{
						// master receiving DELETE
							
						}
						else if (recvList.firstElement() == "I")
						{
						// master receives answer to replication query	
						
							rep_info_reformed = true;
						}
					}
					else
					{
						if (recvList.firstElement() == "C")
						{
						// machine receiving COPY 
							
						}
						else if (recvList.firstElement() == "R")
						{
						// machine receiving REM
							
						}
						else if (recvList.firstElement() == "Q")
						{
						// machine receiving REPLICATION_INFO_QUERY
							
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
		}				
						
						
	}

}
