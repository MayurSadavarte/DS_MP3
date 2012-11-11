

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.Vector;


public class HeartbeatSender implements Runnable{
	
	private Machine m;
	
	public HeartbeatSender(Machine m2) {
		m = m2;
	}

	private void sendHeartbeat(String nextIP)
	{
		String sendMsg = m.myName;
		byte[] sendbytes=null;
	//	System.out.println("in heartbeat sender: will send" + sendMsg + "in a heartbeat message");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
	    	ObjectOutputStream oos = new ObjectOutputStream(baos);
	    	oos.writeObject(sendMsg);
	    	oos.flush();
	    	//TODO - need to decide whether we need to send length also in first packet and then actual packet
	    	// get the byte array of the object
	    	sendbytes= baos.toByteArray();
	    	//sendMsg = baos.toString();
	//    	System.out.println("in heartbeat sender: will actually send" + sendMsg + "in a heartbeat message");
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
		m.sendMsg(m.heartbeat_sock, nextIP, sendbytes, Machine.HEARTBEAT_PORT);
	}
	
	@Override
	public void run() {
		while(true){
			
			Vector<String> memberList = m.getMemberList();
			String myIP = m.getMyName();
			int index = memberList.indexOf(myIP);
			
			String nextIP = memberList.get((index + 1) % memberList.size());
			
		//	Random g = new Random(4545556);
		//	double r = g.nextDouble(); 
		//	if(r>0.15){
			if(m.heartbeat_sock != null)
				sendHeartbeat(nextIP);
		//	}
			
			//System.out.println("send HB to "+ nextIP);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Machine getM() {
		return m;
	}

	public void setM(Machine m) {
		this.m = m;
	}
}
