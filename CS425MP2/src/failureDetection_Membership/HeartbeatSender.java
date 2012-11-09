package failureDetection_Membership;

import java.util.Random;
import java.util.Vector;


public class HeartbeatSender implements Runnable{
	
	private Machine m;
	
	public HeartbeatSender(Machine m2) {
		m = m2;
	}

	@Override
	public void run() {
		while(true){
			
			Vector<String> memberList = m.getMemberList();
			String myIP = m.getMyIP();
			int index = memberList.indexOf(myIP);
			
			String nextIP = memberList.get((index + 1) % memberList.size());
			
		//	Random g = new Random(4545556);
		//	double r = g.nextDouble(); 
		//	if(r>0.15){
				m.sendMsg(nextIP, myIP, Contact.HEARTBEAT_PORT);
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
