import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
	public static FileTransferServer server;
	public static DatagramSocket sock = null;
	
	public static void main(String[] args) {
		
		//args[0] is the ip address connection to 
		
		
		Scanner s = new Scanner(System.in);
		String cmd = null;
		
		//Runnable runnable = new FileTransferServer();
		//
		//
		server = new FileTransferServer();
		server.start();
		
		while ((cmd = s.nextLine()) != null) {

			if ("exit".equals(cmd)) {
				System.exit(0);
			}
			else if(cmd.startsWith("put ")){
				sendMsg(sock, args[0], cmd, Machine.FILE_TRANSFER_PORT);
				String sourceFN = cmd.substring(4, cmd.indexOf(' ', 4));
				server.setSource(sourceFN);
			}
			else if(cmd.startsWith("get ")){
				sendMsg(sock, args[0], cmd, Machine.FILE_TRANSFER_PORT);
				String serverIP = recvStrMsg();
				String copyFN = cmd.substring(cmd.lastIndexOf(' '));
				
				Runnable runnable = new FileTransferClient(copyFN, serverIP);
				Thread thread = new Thread(runnable);
				thread.start();
			}
			else if(cmd.startsWith("delete ")){
				sendMsg(sock, args[0], cmd, Machine.FILE_TRANSFER_PORT);
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
	
	public static String recvStrMsg() {
		DatagramPacket recvPacket;
		String recvMsg = null;
		byte[] recvData = new byte[1024];
		//recvPacket = new DatagramPacket(recvData,recvData.length);
		
		try {
			recvPacket = new DatagramPacket(recvData,recvData.length);
			
			sock.receive(recvPacket);
			//TODO - need to decide whether we need to define this length or not!!
			ByteArrayInputStream bais = new ByteArrayInputStream(recvData);
		
			ObjectInputStream ois = new ObjectInputStream(bais);
			recvMsg = (String)ois.readObject();
			//WriteLog.writelog(myName, "received from UDP "+recvMsg);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();			
		}
		
		return recvMsg;
	}

}
