

import java.util.Scanner;
import java.util.Vector;

public class VoluntaryLeave implements Runnable {
	Machine m;

	public VoluntaryLeave(Machine m) {
		this.m = m;
	}

	@Override
	public void run() {
		Scanner s = new Scanner(System.in);
		String cmd = null;
		while ((cmd = s.nextLine()) != null) {

			if ("exit".equals(cmd)) {
				String ip = m.getMyName();
				//String contactIP = m.getContactName();
				if (m.getMemberList().size() > 1) {
					// send leave msg to everyone in the system
					//Vector<String> list = m.getMemberList();
					//int index = list.indexOf(ip);

					//m.sendMsg(m.membership_sock,
					//		list.get((index - 1 + list.size()) % list.size()),
					//		"R" + ip, Machine.MEMBERSHIP_PORT);
					m.sendMsgToAllNodes(ip, "REM");
				}
				//m.sendMsg(m.membership_sock, contactIP, ip, Machine.MEMBERSHIP_PORT);
				System.exit(0);
			}
		}
	}
}
