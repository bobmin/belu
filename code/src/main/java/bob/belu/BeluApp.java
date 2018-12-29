package bob.belu;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BeluApp {

	private static final String ADDRESS = "https://www.accuweather.com/de/de/schlos-holte-stukenbrock/33758/current-weather/128536_pc";

	public static void main(String[] args) throws IOException {
		String path = null;
		for (String arg : args) {
			if (arg.matches("-[pP][aA][tT][hH]=.*")) {
				path = arg.substring(6);
			}
		}

		final HoleSeite s = new HoleSeite(ADDRESS);
		final HeuteFinden h = new HeuteFinden(s.file.getAbsolutePath());

		if (null != path) {
			File f = new File(path, "beluData.js");
			System.out.println("schreibe \"" + f.getAbsolutePath() + "\"");
			FileWriter writer = new FileWriter(f);
			writer.write("// Daten vom Wetterdienst\n");
			writer.write("var weatherTemp = " + h.temp + ";\n");
			writer.write("var weatherIcon = " + h.icon + ";\n");
			writer.write("var weatherCond = \"" + h.cond + "\";\n");
			writer.close();
		}

		System.out.println("Fertig: Temp " + h.temp + " Icon " + h.icon + " (" + h.cond + ")");
	}

}
