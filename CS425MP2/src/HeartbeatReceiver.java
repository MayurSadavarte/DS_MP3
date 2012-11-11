

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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

	
	/**
	 * get msg string from UDP
	 * 
	 * @return
	 */
	public String recvStrMsg() {
		DatagramPacket recvPacket;
		String recvMsg = null;
		byte[] recvData = new byte[1024];
		//recvPacket = new DatagramPacket(recvData,recvData.length);
	//	System.out.println("in heartbeat receiver: will now try to receive hbeat!!");
		
		try {
			m.heartbeat_sock.setSoTimeout(3000);
			recvPacket = new DatagramPacket(recvData,recvData.length);
			
			m.heartbeat_sock.receive(recvPacket);
			//TODO - need to decide whether we need to define this length or not!!
			ByteArrayInputStream bais = new ByteArrayInputStream(recvData);
		
			ObjectInputStream ois = new ObjectInputStream(bais);
			recvMsg = (String)ois.readObject();
			//WriteLog.writelog(m.myName, "received from UDP "+recvMsg);
	//		System.out.println("in heartbeat receiver: received heartbeat from" + recvMsg);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();			
		} catch (SocketTimeoutException e) {
			
			System.out.println("time out!!!");
			
			try {
				WriteLog.writelog(m.getMyName(), "detected failure");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			
			Vector<String> memberList = m.getMemberList();
			String myIP = m.getMyName();
			
			if(memberList.size()>1){
			
				int index = memberList.indexOf(myIP);
			
				String rmvIP = memberList.get((index - 1 + memberList.size()) % memberList.size());
				//index = memberList.indexOf(myIP);
							
			
				try {
					WriteLog.printList2Log(m.getMyName(), memberList);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
				//if(memberList.size()>1){
				
				m.sendMsgToAllNodes(rmvIP, "REM");
				
				try {
					WriteLog.writelog(m.getMyName(), "RMV send to "+ "all nodes" + "for "+ rmvIP);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} 
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return recvMsg;
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
		
			//System.out.println("dsadasdasdasdad");
				
			//m.heartbeat_sock.receive(recvPacket);
			//recvMsg = new String(recvPacket.getData());
			recvMsg = recvStrMsg();
			//TODO - verify whether it is the correct heartbeat from the correct node!!
				
			//System.out.println("HB from "+recvMsg);

				
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//TODO: what are these two lines? couldn't understand hence have commented
			//byte[] recvDataT = new byte[1024];
			//recvPacket.setData(recvDataT);
		}
		
	}

	public Machine getM() {
		return m;
	}

	public void setM(Machine m) {
		this.m = m;
	}

}
