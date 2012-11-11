import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import failureDetection_Membership.HeartbeatSender;


public class client {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//args[0] is the ip address connection to 
		DatagramSocket sock = null;
		
		Scanner s = new Scanner(System.in);
		String cmd = null;
		while ((cmd = s.nextLine()) != null) {

			if ("exit".equals(cmd)) {
				System.exit(0);
			}
			else if(cmd.startsWith("put ")){
				sendMsg(sock, args[0], cmd, Machine.FILE_TRANSFER_PORT);
				String sourceFN = cmd.substring(4, cmd.indexOf(' ', 4));
				
				//Runnable runnable = new FileTransferServer(sourceFN);
				//Thread thread = new Thread(runnable);
				//thread.start();
				
			}
			
		}

	}
	
	public static void sendMsg(DatagramSocket sock, String ip, String msg, int portN) {
		try {
			InetAddress ipAddr = InetAddress.getByName(ip);
			byte[] sendData = msg.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, ipAddr, portN);
			sock.send(sendPacket);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
