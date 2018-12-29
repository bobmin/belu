package bob.belu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeuteFinden {

	private static final String REGEX = ".+[/\\\\]holeSeite_\\d{4,4}\\-(\\d{2,2})\\-(\\d{2,2})\\.htm$";

	public static void main(String[] args) throws IOException {
		String path = null;
		for (String arg : args) {
			if (arg.matches("-[fF][iI][lL][eE]=.*")) {
				path = arg.substring(6);
			}
		}
		if (null == path) {
			System.err.println("Datei fehlt :(");
		} else {
			new HeuteFinden(path);
		}
	}

	public String temp = null;

	public String icon = null;

	public String cond = null;

	public HeuteFinden(final String path) throws IOException {
		int month = 0;
		int day = 0;
		Pattern p = Pattern.compile(REGEX);
		Matcher m = p.matcher(path);
		if (m.find()) {
			month = Integer.parseInt(m.group(1));
			day = Integer.parseInt(m.group(2));
		} else {
			throw new IllegalArgumentException("Datum nicht erkannt :(");
		}

		// Laden
		System.out.println("lese \"" + path + "\"");
		StringBuffer buffer = new StringBuffer();
		BufferedReader file = new BufferedReader(new FileReader(path));
		String line = null;
		while (null != (line = file.readLine())) {
			buffer.append(line);
		}
		file.close();

		String[] mmmAll = new String[] { "Jan", "Feb", "Mar", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov",
				"Dez" };
		String mmm = mmmAll[month - 1];

		System.out.println("suche \"" + day + ". " + mmm + "\"");

		String value = search(".*(<h4>" + day + ". " + mmm + "</h4>.*<!-- /.bg -->).*", buffer.toString());

		if (null == value) {
			System.out.println("Nicht gefunden :(");
		} else {
			value = value.replaceAll("<([^<>]+)>", "\n<$1>");
			// System.out.println(value);

			// Icon
			// http://vortex.accuweather.com/adc2010/images/slate/icons/16.svg
			icon = search(".*icon i-(\\d\\d)-l.*", value);
			System.out.println("Symbol: " + icon);

			// Temperatur
			// 9°
			String largeTemp = search(".*<span class=\"large-temp\">(.*)\n</span>.*", value);
			temp = largeTemp.replace("&deg;", "");
			System.out.println("Temperatur: " + temp);

			// Bedingungen
			// Windig, nachts einige Schauer
			cond = search(".*<span class=\"cond\">(.*)\n</span>.*", value);
			System.out.println("Bedingungen: " + cond);
		}

	}

	private String search(String pattern, String text) {
		String x = null;
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		if (m.find()) {
			x = m.group(1).replaceAll("[\\s][\\s]+", "");
		}
		return x;
	}

}
