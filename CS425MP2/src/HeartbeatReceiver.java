

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Vector;

public class HeartbeatReceiver implements Runnable {
	private Machine m;

	public HeartbeatReceiver(Machine m2) {
		m = m2;
	}

	@Override
	public void run() {
		byte[] recvData = new byte[1024];
		DatagramPacket recvPacket = new DatagramPacket(recvData,recvData.length);
		
		
		try {
			m.heartbeat_sock = new DatagramSocket(Machine.HEARTBEAT_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		while(true){
			String recvMsg = null;
			try {
				//System.out.println("dsadasdasdasdad");
				m.heartbeat_sock.setSoTimeout(3000);
				m.heartbeat_sock.receive(recvPacket);
				recvMsg = new String(recvPacket.getData());
				//TODO - verify whether it is the correct heartbeat from the correct node!!
				
				//System.out.println("HB from "+recvMsg);
			} catch (SocketTimeoutException e) {
				
				System.out.println("time out!!!");
				
				try {
					WriteLog.writelog(m.getMyIP(), "detected failure");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				
				Vector<String> memberList = m.getMemberList();
				String myIP = m.getMyIP();
				
				if(memberList.size()>1){
				
					int index = memberList.indexOf(myIP);
				
					String rmvIP = memberList.get((index - 1 + memberList.size()) % memberList.size());
					//index = memberList.indexOf(myIP);
								
				
					try {
						WriteLog.printList2Log(m.getMyIP(), memberList);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				
					//if(memberList.size()>1){
					
					String remMsg = "R" + rmvIP;
					for (String tempIP : m.memberList)
					{
						m.sendMsg(m.membership_sock, tempIP, remMsg, Machine.MEMBERSHIP_PORT);
					}
					
					try {
						WriteLog.writelog(m.getMyIP(), "RMV send to "+ "all nodes" + "msg: "+ remMsg);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			byte[] recvDataT = new byte[1024];
			recvPacket.setData(recvDataT);
		}
		
	}

	public Machine getM() {
		return m;
	}

	public void setM(Machine m) {
		this.m = m;
	}

}
