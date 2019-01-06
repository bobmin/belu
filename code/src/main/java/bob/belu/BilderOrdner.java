package bob.belu;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Liest eine Bilderquelle und zählt die möglichen Bilder.
 * 
 * @author maik@btmx.net
 */
public class BilderOrdner {

    /**
     * Programmstart erwartet Pfad zur Quelle.
     * 
     * @param args die Argumente zum Programmstart
     * @throws IOException wenn Probleme mit Dateisystem
     */
    public static void main(String[] args) throws IOException {
		String path = null;
		for (String arg : args) {
			if (arg.matches("-[pP][aA][tT][hH]=.*")) {
				path = arg.substring(6);
			}
		}
		if (null == path) {
			System.err.println("Pfad fehlt :(");
		} else {
			new BilderOrdner(path);
		}
	}

    private final long startTime;

    private final Map<String, Integer> summary = new HashMap<>();

	private final Map<String, Integer> folders = new HashMap<>();

    private BilderOrdner(final String path) throws IOException {
        startTime = System.currentTimeMillis();

		File imagesPath = new File(path);
		if (!imagesPath.isDirectory()) {
            System.err.println("Quelle kein Verzeichnis :(");
            System.exit(-1);
		}

		File outputFile = BeluUtils.createImageFolderTxt(path);
		if (outputFile.exists()) {            
            System.out.println("Quelle bekannt: " + outputFile.getAbsolutePath());
            System.exit(-1);
        } 
        
        File[] imageFolders = imagesPath.listFiles(new ImageFolderFilter());
        for (File f : imageFolders) {
            int count = searchImages(f);
            summary.put(f.getAbsolutePath(), Integer.valueOf(count));
        }
        writeResult(outputFile);
		
		// Zusammenfassung ausgeben
		int imageCounter = 0;
		for (Integer x: summary.values()) {
			imageCounter += x.intValue();
		}
		BeluUtils.printProgramFinish(startTime, "fertig: %d Bilder gefunden", imageCounter);
    }

	private int searchImages(final File path) {
		// Bilder für SUMMARY zäheln
		int countImagesRecursively = 0;
		// nur diesen PATH zählen für FOLDERS
		int countThisFolder = 0;

		// Ordner/Dateien vom aktuelen Ordner holen
		File[] files = path.listFiles(new ImageFilter(true));

		for (File f : files) {
			if (f.isDirectory()) {
				countImagesRecursively += searchImages(f);
			} else {
				countImagesRecursively += 1;
				countThisFolder += 1;
			}
		}

		// Ergebnis für aktuellen Ordner merken
		if (0 < countThisFolder) {
			folders.put(path.getAbsolutePath(), Integer.valueOf(countThisFolder));
			System.out.println(path.getAbsolutePath() + " = " + countThisFolder);
		}

		return countImagesRecursively;
	}

	private void writeResult(final File f) throws IOException {
		System.out.println("schreibe \"" + f.getAbsolutePath() + "\"");
		FileWriter writer = new FileWriter(f);
		for (Entry<String, Integer> e : folders.entrySet()) {
			writer.write(e.getKey() + ";" + e.getValue().intValue() + "\n");
		}
		writer.close();

	}

    /**
	 * Liefert TRUE wenn der Ordnername eine vierstellige Jahreszahl mit einem
	 * vorangestelltem Unterstrich ist.
	 */
	private class ImageFolderFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return (null == pathname ? false : pathname.getName().matches("_\\d\\d\\d\\d"));
		}

    }
    
}