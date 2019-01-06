package bob.belu;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Zentrale Hilfsfunktionen.
 * 
 * @author maik@btmx.net
 */
public class BeluUtils {

    /**
     * Liefert die Dateibeschreibung zur Bilderverzeichnis-Analyse mit der Wurzel "path".
     * 
     * @param path das Wurzelverzeichnis der Bilder
     */
	public static File createImageFolderTxt(String path) {
		Objects.requireNonNull(path);
		String p = path.replaceAll("[^a-zA-Z0-9]", "_").replaceAll("[_]+", "_");
		String n = String.format("bilderOrdner_%s.txt", p);
		return new File(System.getProperty("java.io.tmpdir"), n);
	}

	public static File createImageSelectionTxt(String path) {
		Objects.requireNonNull(path);
		String p = path.replaceAll("[^a-zA-Z0-9]", "_").replaceAll("[_]+", "_");
		String n = String.format("bilderAuswahl_%s.txt", p);
		return new File(System.getProperty("java.io.tmpdir"), n);
	}

	public static void printProgramFinish(long startTime, String text, Object...args) {		
		final long millis = System.currentTimeMillis() - startTime;
		long min = TimeUnit.MILLISECONDS.toMinutes(millis);
		long sec = TimeUnit.MILLISECONDS.toSeconds(millis)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
		if (null != text) {
			System.out.printf(text, args);
			System.out.println();
		}
		System.out.printf("ENDE (%d Min, %d Sek)", min, sec);
	}



}