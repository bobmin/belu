package bob.belu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class HoleSeite {

	public static void main(String[] args) throws IOException {
		String address = null;
		for (String arg : args) {
			if (arg.matches("-[uU][rR][lL]=.*")) {
				address = arg.substring(5);
			}
		}
		if (null == address) {
			System.err.println("Adresse fehlt :(");
		} else {
			new HoleSeite(address);
		}
	}

	public final File file;

	public HoleSeite(final String address) throws IOException {
		final Date today = new Date();
		final String filename = String.format("holeSeite_%1$tY-%1$tm-%1$td.htm", today);
		file = new File(System.getProperty("java.io.tmpdir"), filename);
		if (file.exists()) {
			System.out.println("benutze \"" + file.getAbsolutePath() + "\"");
		} else {
			String content = holeContent(address);
			schreibeDatei(content);
		}
	}

	private String holeContent(final String address) throws IOException {
		StringBuffer buffer = new StringBuffer();
		System.out.println("hole \"" + address + "\"");
		URL u = new URL(address);
		URLConnection conn = u.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			buffer.append(inputLine);
		in.close();
		return buffer.toString();
	}

	private void schreibeDatei(final String content) throws IOException {
		System.out.println("schreibe \"" + file.getAbsolutePath() + "\"");
		FileWriter writer = new FileWriter(file);
		writer.write(content);
		writer.close();
	}

}
