package bob.belu;

import java.io.IOException;

public class BeluApp {

	private static final String ADDRESS = "https://www.accuweather.com/de/de/schlos-holte-stukenbrock/33758/current-weather/128536_pc";

	public static void main(String[] args) throws IOException {
		final HoleSeite s = new HoleSeite(ADDRESS);
		final HeuteFinden h = new HeuteFinden(s.file.getAbsolutePath());
		System.out.println("Fertig: Temp " + h.temp + " Icon " + h.icon + " (" + h.cond + ")");
	}

}
