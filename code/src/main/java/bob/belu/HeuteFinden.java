package bob.belu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeuteFinden {

	private static boolean verbose = false;

	public static void main(String[] args) throws IOException {
		String path = null;
		String use = null;
		List<String> vars = new LinkedList<>();
		String out = null;
		for (String arg : args) {
			if (arg.matches("-[fF][iI][lL][eE]=.*")) {
				path = arg.substring(6);
			} else if (arg.matches("-[uU][sS][eE]=.*")) {
				use = arg.substring(5);
			} else if (arg.toLowerCase().startsWith("-var=") && -1 < arg.indexOf(':')) {
				// arg.matches("-[vV][aA][rR]=.*")
				vars.add(arg.substring(5).replaceAll("`", "\""));
			} else if (arg.matches("-[oO][uU][tT]=.*")) {
				out = arg.substring(5);
			} else if (arg.matches("-[vV][eE][rR][bB][oO][sS][eE]=.*")) {
				verbose = Boolean.parseBoolean(arg.substring(9));
			} else {
				System.out.println("argument unknown: " + mask(arg));
			}
		}
		if (null == path) {
			System.err.println("Datei fehlt :(");
		} else if (null == use) {
			System.err.println("Parameter \"use\" fehlt :(");
		} else if (0 == vars.size()) {
			System.err.println("Parameter \"var\" fehlt :(");
		} else {
			new HeuteFinden(path, use, vars, out);
		}
	}

	private Map<String,String> values = new LinkedHashMap<>();

	public HeuteFinden(final String path, String use, List<String> vars, String out) throws IOException {

		boolean overwrite = true;

		// Laden
		System.out.println("lese " + path);
		StringBuffer buffer = new StringBuffer();
		BufferedReader file = new BufferedReader(new FileReader(path));
		String line = null;
		while (null != (line = file.readLine())) {
			buffer.append(line);
		}
		file.close();

		// "(<li class=\"day current.+?</li>)"
		String value = search("use", use, buffer.toString());

		for (String v: vars) {
			String varName = v.substring(0, v.indexOf(':'));
			String varRegex = v.substring(v.indexOf(':') + 1);
			System.out.println("suche " + varName + " mit \"" + mask(varRegex) + "\"");
		
			String varValue = search(varName, varRegex, value);			
			System.out.println(varName + " ist " + varValue);

			values.put(varName, varValue);
		}

		// Ausgabe
		if (null == out) {
			System.out.println("Parameter \"out\" fehlt. Keine Ausgabe!");
		} else {
			File f = new File(out, "beluWeather.js");
			if (f.exists() && !overwrite) {
				String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				File backup = new File(f.getAbsolutePath().replaceAll("\\.js", "_" + ts + "\\.js"));
				f.renameTo(backup);
				System.out.println("sichere " + backup.getAbsolutePath());
			}
			System.out.println("schreibe \"" + f.getAbsolutePath() + "\"");
			FileWriter writer = new FileWriter(f);
			writer.write("// Daten vom Wetterdienst\n");
			for (Entry<String,String> e: values.entrySet()) {
				writer.write("var " + e.getKey() + " = \"" + e.getValue() + "\";\n");
			}
			writer.close();
		}

	}

	private String search(String label, String pattern, String text) {
		String t = "----- " + label + ": " + mask(pattern) + " -----";
		println(t);

		String x = null;
		if (null != text) {
			String p1 = pattern.replaceAll("\\\\\\\\d", "\\\\d");
			Pattern p = Pattern.compile(p1);
			Matcher m = p.matcher(text);
			if (m.find()) {
				x = m.group(1)
					.replaceAll("[\\s][\\s]+", " ")
					.replaceAll("<([^<>]+)>", "\n<$1>")
					.trim();
				println(x);
			} else {
				println("Nicht gefunden :(");
			}
		}
		
		println(new String(new char[t.length()]).replace("\0", "-"));

		return x;
	}

	private static void println(String value) {
		if (verbose) {
			System.out.println(value);
		}
	}

	private static String mask(String value) {
		return (null == value ? null : value.replaceAll("\\r", "\\\\r").replaceAll("\\n", "\\\\n"));
	}

}
