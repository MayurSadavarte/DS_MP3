import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Vector;




public class FileReplication implements Runnable {
	private Machine m;
	
	public FileReplication(Machine machine)
	{
		m=machine;		
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
