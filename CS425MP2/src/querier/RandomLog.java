package querier;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class RandomLog {

	private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	public static void main(String[] args) throws IOException {
		ArrayList<String> all = new ArrayList<String>();
		Random generator = new Random(41218895);

		for (int i = 0; i < 2000000; i++) {
			double r = generator.nextDouble();
			if (r < 0.6)
				// writelog(r + " frequent");
				all.add(r + " frequent");
			else if (r < 0.9)
				// writelog(r + " somewhat-frequent");
				all.add(r + " normal");
			else
				// writelog(r + " rare");
				all.add(r + " rare");
		}
		writelog(all);
		System.out.println("Done!");
	}

	private static void writelog(ArrayList<String> logs) throws IOException {

		FileWriter fstream = new FileWriter("machine.100.log", true);
		BufferedWriter out = new BufferedWriter(fstream);
		String temp = null;
		for (String log : logs) {
			temp = now() + " ";
			temp = temp + log + "\n";
			out.write(temp);
			temp = null;
		}
		out.close();
	}

	private static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

}
