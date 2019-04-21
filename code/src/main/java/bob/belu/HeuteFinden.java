package bob.belu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeuteFinden {

	public static void main(String[] args) throws IOException {
		String path = null;
		String out = null;
		for (String arg : args) {
			if (arg.matches("-[fF][iI][lL][eE]=.*")) {
				path = arg.substring(6);
			} else if (arg.matches("-[oO][uU][tT]=.*")) {
				out = arg.substring(5);
			}
		}
		if (null == path) {
			System.err.println("Datei fehlt :(");
		} else {
			new HeuteFinden(path, out);
		}
	}

	public String temp = null;

	public String icon = null;

	public String cond = null;

	public HeuteFinden(final String path, String out) throws IOException {
		System.out.println("lese " + path);
		// Laden
		StringBuffer buffer = new StringBuffer();
		BufferedReader file = new BufferedReader(new FileReader(path));
		String line = null;
		while (null != (line = file.readLine())) {
			buffer.append(line);
		}
		file.close();

		String value = search("Infoblock", "(<li class=\"day current.+?</li>)", buffer.toString());

		// Icon
		// http://vortex.accuweather.com/adc2010/images/slate/icons/16.svg
		icon = search("Symbol", "icon i-(\\d+)-xl", value);

		// Temperatur
		// 9°
		temp = search("Temperatur", "<span class=\"large-temp\">(.*)&deg;\n</span>", value);

		// Bedingungen
		// Windig, nachts einige Schauer
		cond = search("Bedingungen", "<span class=\"cond\">(.*)\n</span>", value);

		// Ausgabe
		if (null == out) {
			System.out.println("Parameter \"out\" fehlt. Keine Ausgabe!");
		} else {
			File f = new File(out, "beluData.js");
			if (f.exists()) {
				String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				File backup = new File(f.getAbsolutePath().replaceAll("\\.js", "_" + ts + "\\.js"));
				f.renameTo(backup);
				System.out.println("sichere " + backup.getAbsolutePath());
			}
			System.out.println("schreibe \"" + f.getAbsolutePath() + "\"");
			FileWriter writer = new FileWriter(f);
			writer.write("// Daten vom Wetterdienst\n");
			writer.write("var weatherTemp = " + temp + ";\n");
			writer.write("var weatherIcon = " + icon + ";\n");
			writer.write("var weatherCond = \"" + cond + "\";\n");
			writer.close();
		}

	}

	private String search(String label, String pattern, String text) {
		String t = "----- " + label + ": " + pattern.replaceAll("\\r", "\\\\r").replaceAll("\\n", "\\\\n") + " -----";
		System.out.println(t);

		String x = null;
		if (null != text) {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(text);
			if (m.find()) {
				x = m.group(1)
					.replaceAll("[\\s][\\s]+", " ")
					.replaceAll("<([^<>]+)>", "\n<$1>")
					.trim();
				System.out.println(x);
			} else {
				System.out.println("Nicht gefunden :(");
			}
		}
		System.out.println(new String(new char[t.length()]).replace("\0", "-"));
		return x;
	}

}
