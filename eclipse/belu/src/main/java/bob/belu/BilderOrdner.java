package bob.belu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BilderOrdner {

	private static final String IMAGE_FOLDER_REGEX = "_\\d\\d\\d\\d";

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

	private final Map<String, Integer> summary;

	private final Map<String, Integer> folders;

	public BilderOrdner(final String path) throws IOException {
		startTime = System.currentTimeMillis();
		summary = new HashMap<>();
		folders = new HashMap<>();
		File imagesPath = new File(path);
		if (!imagesPath.isDirectory()) {
			throw new IllegalArgumentException("Pfad kein Verzeichnis :(");
		}
		File outputFile = fileIn(path);
		if (outputFile.exists()) {
			usePreviousResult(path, outputFile);
		} else {
			File[] imageFolders = imagesPath.listFiles(new ImageFolderFilter());
			for (File f : imageFolders) {
				int count = searchImages(f);
				summary.put(f.getAbsolutePath(), Integer.valueOf(count));
			}
			writeResult(outputFile);
		}

		int[] numbers = determineValues();
		System.out.println("numbers: " + Arrays.toString(numbers));

		List<String> folderNames = new ArrayList<>(folders.keySet());
		Collections.sort(folderNames);

		int avg = numbers[0];
		int min = numbers[1];
		int max = numbers[2];
		int count = 0;
		int cluster = 1;
		Map<String, Integer> cache = new LinkedHashMap<>();
		for (String name : folderNames) {
			int x = folders.get(name).intValue();
			int currentFolder;
			if (x < min) {
				currentFolder = min;
			} else if (x > max) {
				currentFolder = max;
			} else {
				currentFolder = x;
			}
			count += currentFolder;
			if (count > avg) {
				cluster += (count / avg);
				count = 0;
			}
			cache.put(name, cluster);
		}

		String[] files = new String[10];
		int imagesInCluster = 0;
		boolean active = false;
		for (int r = 0; r < 10; r++) {
			int c = numbers[3+r];
			f: for (Entry<String, Integer> e : cache.entrySet()) {
				System.out.println(e + " - " + c);
				int currentCluster = e.getValue().intValue();
				if (active && c < currentCluster) {
					System.out.println("= " + imagesInCluster);
					imagesInCluster = 0;
					break f;
				}
				if (currentCluster >= c) {
					imagesInCluster += folders.get(e.getKey()).intValue();
					System.out.println("+" + e.getKey() + " = " + imagesInCluster);
					active = true;
				}
			}
		}



		// formatSummary();
		printProgramFinish();
	}

	private int[] determineValues() {
		List<Integer> values = new ArrayList<>(folders.values());
		Collections.sort(values);

		// Ausreißer unten und oben abschneiden
		float fac = 0.10f;
		int size = values.size();
		int from = (int) (size * fac);
		int until = (int) (size * (1.0f - fac));
		System.out.println("size = " + size + ", fac = " + fac + ", from = " + from + ", until = " + until);

		// minimale und maximale Bilderzahl ohen Ausreißer
		for (int idx = from; idx < until; idx++) {
		}
		int min = values.get(from);
		int max = values.get(until);
		System.out.println("min = " + min + ", max = " + max);

		int sum = 0;
		for (Integer x : values) {
			int currentFolder;
			if (x.intValue() < min) {
				currentFolder = min;
			} else if (x.intValue() > max) {
				currentFolder = max;
			} else {
				currentFolder = x.intValue();
			}
			sum += currentFolder;
		}
		int avg = sum / values.size();
		System.out.println("avg = " + avg);


		int count = 0;
		int cluster = 0;
		for (Integer x : values) {
			int currentFolder;
			if (x.intValue() < min) {
				currentFolder = min;
			} else if (x.intValue() > max) {
				currentFolder = max;
			} else {
				currentFolder = x.intValue();
			}
			count += currentFolder;
			if (count > avg) {
				cluster += (count / avg);
				count = 0;
			}
		}
		System.out.println("cluster = " + cluster);
		
		List<Integer> rand = new ArrayList<>(10);
		for (int r=0; r<10; r++) {
			int x = (int) Math.round((cluster * Math.random()));
			rand.add(x);
		}
		Collections.sort(rand);
		System.out.println("random: " + rand);

		int[] ret = new int[13];
		ret[0] = avg;
		ret[1] = min;
		ret[2] = max;
		for (int r = 0; r < 10; r++) {
			ret[r + 3] = rand.get(r);
		}

		return ret;
	}

	private void usePreviousResult(String path, File outputFile) throws IOException {
		System.out.println("benutze: " + outputFile.getAbsolutePath());
		FileInputStream inputStream = new FileInputStream(outputFile);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		do {
			String line = bufferedReader.readLine();
			String[] l = line.split(";");
			int p = l[0].indexOf('\\', path.length() + 1);

			String imageFolder = (-1 == p ? l[0] : l[0].substring(0, p));

			folders.put(l[0], Integer.valueOf(l[1]));
			if (!summary.containsKey(imageFolder)) {
				summary.put(imageFolder, Integer.valueOf(0));
			}
			Integer x = summary.get(imageFolder);
			summary.put(imageFolder, Integer.valueOf(Integer.parseInt(l[1]) + x.intValue()));

		} while (bufferedReader.ready());
		bufferedReader.close();
	}

	private void formatSummary() {
		List<String> list = new ArrayList<>(summary.keySet());
		java.util.Collections.sort(list);
		for (String f : list) {
			System.out.println(f + " = " + summary.get(f));
		}
	}

	private File fileIn(String path) {
		Objects.requireNonNull(path);
		String p = path.replaceAll("[^a-zA-Z0-9]", "_").replaceAll("[_]+", "_");
		String n = String.format("bilderOrdner_%s.txt", p);
		return new File(System.getProperty("java.io.tmpdir"), n);
	}

	private void writeResult(final File f) throws IOException {
		System.out.println("schreibe \"" + f.getAbsolutePath() + "\"");
		FileWriter writer = new FileWriter(f);
		for (Entry<String, Integer> e : folders.entrySet()) {
			writer.write(e.getKey() + ";" + e.getValue().intValue() + "\n");
		}
		writer.close();

	}

	private void printProgramFinish() {
		int count = 0;
		for (Integer x: summary.values()) {
			count += x.intValue();
		}
		final long millis = System.currentTimeMillis() - startTime;
		long min = TimeUnit.MILLISECONDS.toMinutes(millis);
		long sec = TimeUnit.MILLISECONDS.toSeconds(millis)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
		System.out.printf("Found " + count + " images in %d min and %d sec.%nFinish. Bye!", min, sec);
	}

	private int searchImages(final File path) {
		// Bilder für SUMMARY zäheln
		int countImagesRecursively = 0;
		// nur diesen PATH zählen für FOLDERS
		int countThisFolder = 0;

		// Ordner/Dateien vom aktuelen Ordner holen
		File[] files = path.listFiles(new HiddenFilter());

		for (File f : files) {
			if (f.isDirectory()) {
				countImagesRecursively += searchImages(f);
			} else {
				if (f.getName().matches(".+\\.[jJ][pP][gG]$") || f.getName().matches(".+\\.[pP][nN][gG]$")) {
					countImagesRecursively += 1;
					countThisFolder += 1;
				}
			}
		}

		// Ergebnis für aktuellen Ordner merken
		if (0 < countThisFolder) {
			folders.put(path.getAbsolutePath(), Integer.valueOf(countThisFolder));
			System.out.println(path.getAbsolutePath() + " = " + countThisFolder);
		}

		return countImagesRecursively;
	}

	/**
	 * Liefert TRUE wenn der Ordnername eine vierstellige Jahreszahl mit einem
	 * vorangestelltem Unterstrich ist.
	 */
	private class ImageFolderFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return (null == pathname ? false : pathname.getName().matches(IMAGE_FOLDER_REGEX));
		}

	}

	/**
	 * Liefert TRUE wenn der Name nicht mit einem Punkt beginnt.
	 */
	private class HiddenFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return (null == pathname ? false : !pathname.getName().matches("\\..+"));
		}

	}

}
