package bob.belu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BilderAuswahl {

	/**
	 * Der Konsolenaufruf. Es werden zwei Parameter erwartet. 
	 * Die Pflichtangabe "path" zur Quelle, wo Bilder gesucht werden. 
	 * Eine optionale Angabe "to" zur Ausgabe. Ist keine Ausgabe definiert, wird nur protokolliert.
	 * 
	 * @param args die Parameter vom Programmstart
	 * @throws IOException wenn Probleme mit Dateisystem
	 */
	public static void main(String[] args) throws IOException {
		String path = null, to = null;
		for (String arg : args) {
			if (arg.matches("-[pP][aA][tT][hH]=.*")) {
				path = arg.substring(6);
			}
			if (arg.matches("-[tT][oO]=.*")) {
				to = arg.substring(4);
			}
		}
		if (null == path) {
			System.err.println("Pfad fehlt :(");
		} else {
			if (null == to) {
				System.err.println("Keine Ausgabe. Nur Analyse.");
			}
			new BilderAuswahl(path, to);
		}
	}

	private final long startTime;

	private final Map<String, Integer> summary = new HashMap<>();

	private final Map<String, Integer> folders = new HashMap<>();

	public BilderAuswahl(final String path, final String to) throws IOException {
		startTime = System.currentTimeMillis();

		File imagesPath = new File(path);
		if (!imagesPath.isDirectory()) {
			throw new IllegalArgumentException("Pfad kein Verzeichnis :(");
		}

		File outputFile = BeluUtils.createImageFolderTxt(path);
		if (! outputFile.exists()) {
			System.err.println("Quelle unbekannt: " + path);
			System.exit(-1);
		}

		File selectionFile = BeluUtils.createImageSelectionTxt(path);
		if (selectionFile.exists()) {
			System.out.println("Auswahl bekannt: " + selectionFile.getAbsolutePath());
			System.exit(-1);
		}

		usePreviousResult(path, outputFile);

		int[] numbers = determineValues();
		System.out.println("numbers: " + Arrays.toString(numbers));

		List<String> folderNames = new ArrayList<>(folders.keySet());
		Collections.sort(folderNames);

		System.out.println("Ordner gruppieren (in Cluster)...");

		int avg = numbers[0];
		int min = numbers[1];
		int max = numbers[2];
		int count = 0;
		int cluster = 1;
		Map<String, Integer> cache = new LinkedHashMap<>();
		for (String name : folderNames) {
			int imagesInFolder = folders.get(name).intValue();
			int currentFolder;
			if (imagesInFolder < min) {
				currentFolder = min;
			} else if (imagesInFolder > max) {
				currentFolder = max;
			} else {
				currentFolder = imagesInFolder;
			}
			count += currentFolder;
			if (count > avg) {
				cluster += (count / avg);
				count = 0;
			}
			cache.put(name, cluster);
			System.out.printf("Cluster %d: %s - %d Bilder%n", cluster, name, imagesInFolder);
		}

		System.out.println("Bilder in Gruppen suchen...");

		File[] files = new File[10];
		int imagesInCluster = 0;
		boolean active = false;
		List<String> folderSelection = new LinkedList<>();
		// 10 Dateien auswählen ?
		for (int r = 0; r < 10; r++) {
			// immer [avg|min|max] überspringen
			int clusterGoal = numbers[3+r];
			System.out.println("[" + r + "] suche: Cluster " + clusterGoal);
			f: for (Entry<String, Integer> e : cache.entrySet()) {
				// System.out.println(e + " - " + c);
				int currentCluster = e.getValue().intValue();
				if (active && clusterGoal < currentCluster) {
					System.out.println("   = " + imagesInCluster + " Bilder");
					// --------------------------------------------------------
					int imageRand = (int) Math.round((double) imagesInCluster * Math.random() + 0.5);
					System.out.println("   ! suche: Bild " + imageRand);
					int imagesInSelection = 0;
					int imagesOffset = 0;					
					u: for (String folderPath: folderSelection) {
						imagesInSelection += folders.get(folderPath).intValue();
						if (imagesInSelection >= imageRand) {
							int imageNumber = imageRand - imagesOffset;
							System.out.println("   ! benutze: " + folderPath + " - Bild " + imageNumber);
							File folderFile = new File(folderPath);
							files[r] = folderFile.listFiles(new ImageFilter(false))[imageNumber - 1];
							System.out.println("   ! verwende: " + files[r]);
							break u;
						}
						imagesOffset = imagesInSelection;
					}
					// --------------------------------------------------------
					folderSelection.clear();
					imagesInCluster = 0;
					active = false;
					break f;
				}
				if (currentCluster >= clusterGoal) {
					int imageInFolder = folders.get(e.getKey()).intValue();
					imagesInCluster += imageInFolder;
					System.out.println("   + Cluster " + currentCluster + ": " + imageInFolder + " Bilder aus " + e.getKey());
					folderSelection.add(e.getKey());
					active = true;
				}
			}			
		}

		writeResult(selectionFile, files);

		// Zusammenfassung ausgeben
		BeluUtils.printProgramFinish(startTime, "fertig: 10 Bilder ausgesucht");
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

	/**
	 * Liefert die Werte f&uuml;r den Prozess.
	 * 
	 * <ul>
	 *   <li>druchschnittliche Bilderanzahl in den Ordnern</li>
	 *   <li>minimale Bilderanzahl in einem Ordner</li>
	 *   <li>maximale Bilderanzahl in einem Ordner</li>
	 *   <li>10 per Zufall gewählte Cluster</li>
	 * </ul>
	 * 
	 * @return ein Array mit 13 Zahlen
	 */
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
			int x = (int) Math.round((double)cluster * Math.random());
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

	private void writeResult(final File f, final File[] files) throws IOException {		
		System.out.println("schreibe \"" + f.getAbsolutePath() + "\"");
		FileWriter writer = new FileWriter(f);
		for (File s : files) {
			writer.write(s.getAbsolutePath());
			writer.write(System.lineSeparator());
		}
		writer.close();
	}

}
