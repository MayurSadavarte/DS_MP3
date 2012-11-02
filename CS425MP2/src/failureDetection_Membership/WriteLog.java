package failureDetection_Membership;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

public class WriteLog {
	
	private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	
	public static void writelog(String from, String log) throws IOException {
		
		FileWriter fstream = new FileWriter( from + ".log", true);
		BufferedWriter out = new BufferedWriter(fstream);
		String temp = null;
		temp = now() + "---";
		temp = temp + log + "\n";
		out.write(temp);
		out.close();
	}
	
	private static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
	
	public static void printList2Log (String from ,Vector<String> memberList) throws IOException{
		String msg = "MemberList: " ;
		if (memberList==null){
			writelog(from, "MemberList is NULL!!!");
		}else{
			for(int i=0; i<memberList.size(); i++){
				msg += (i + "-" + memberList.elementAt(i) + " ");
			}
			writelog(from,msg);
		}
	}
	
}
