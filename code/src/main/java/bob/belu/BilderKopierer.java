package bob.belu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;

/**
 * Kopiert die ausgewählten Bilder an den Zielort und dreht sie, wenn nötig.
 * 
 * @see https://stackoverflow.com/questions/5905868/how-to-rotate-jpeg-images-based-on-the-orientation-metadata
 */
public class BilderKopierer {

    public static void main(String[] args) throws Exception {
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
		} else if (null == to) {
			System.err.println("Ausgabe fehlt :(");
		} else {
			new BilderKopierer(path, to);
		}
	}

	private final long startTime;

    private BilderKopierer(final String path, final String to) throws Exception {
		startTime = System.currentTimeMillis();

		File selectionFile = BeluUtils.createImageSelectionTxt(path);
		if (!selectionFile.exists()) {
			System.err.println("Datei \"" + selectionFile.getName() + "\" unter \"" + selectionFile.getParent() + "\" fehlt :(");
			System.exit(-1);
		}
		
		List<File> files = readSelection(selectionFile);
		for (int idx = 0; idx < files.size(); idx++) {
			copy(files.get(idx), idx, to);
		}
		
		BeluUtils.printProgramFinish(startTime, null);
	}

	/**
	 * Liest die Auswahl und liefert eine entsprechende Liste mit Dateibeschreibungen.
	 * @param f die Auswahl
	 * @return ein Objekt, niemals <code>null</code>
	 * @throws IOException wenn Probleme mit Dateisystem
	 */
	private List<File> readSelection(File f) throws IOException {
		List<File> x = new LinkedList<>();
		System.out.println("benutze: " + f.getAbsolutePath());
		FileInputStream inputStream = new FileInputStream(f);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		do {
			String line = bufferedReader.readLine();
			x.add(new File(line));
		} while (bufferedReader.ready());
		bufferedReader.close();
		return x;
	}

	private void copy(File file, int idx, final String to) throws Exception {
		String toName = file.getName();
		// Extension
		String toExt = "";
		if(toName.lastIndexOf(".") != -1 && toName.lastIndexOf(".") != 0) {
			toExt = toName.substring(toName.lastIndexOf("."));
		}
		// Copy
		File toFile = new File(to, idx + toExt);
		System.out.println("kopiere[" + idx + "]: " + file.getAbsolutePath());
		Files.copy(file.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		// Orientation
		ImageInformation info = readImageInformation(toFile);
		if (info.orientation == 6) {
			String format = toExt.replace(".", "");
			rotate(toFile, info, format);
		}
	}

	private void rotate(File f, ImageInformation info, final String toExt) throws Exception {
		// BufferedImage image = ImageIO.read(f);
		// fixBadJPEG(image);
		System.out.println("   ermittelt: " + toExt + " - " + info);	
		// AffineTransform a = getExifTransformation(info.orientation, info.width, info.height);
		// BufferedImage img2 = transformImage(image, a);
		File target = new File(f.getParentFile(), "rotate_" + f.getName());
		try {
			System.out.println("   schreibe: " + target.getAbsolutePath());	
			// ImageIO.write(img2, toExt, f2);
			// runExternal(target.getParentFile().getAbsoluteFile(), "c:/ImageMagick-7.0.8-23-portable-Q16-x86/convert.exe", "-rotate", "90", "0.jpg", "rotate_0.jpg");
			// runExternal(target.getParentFile().getAbsoluteFile(), "c:/exiftool-10.61/exiftool.exe", "-ALL=", "rotate_0.jpg");
			runTool("rotate", f, target);
			runTool("exif", f, target);
		} catch (IOException ex) {
			ex.printStackTrace();
		}	


	}

	/**
	 * Führt ein externes Programm aus.
	 * <ul>
	 * <li>rotate</li>
	 * <li>resize</li>
	 * <li>exif</li>
	 * </ul>
	 * 
	 * @param name
	 * @throws IOException
	 */
	private void runTool(final String name, final File source, final File target) throws IOException {
		Properties props = new Properties();
		InputStream in = getClass().getResourceAsStream("/BilderKopieren.properties");
		props.load(in);
		String tool = props.getProperty(String.format("%s_tool", name));
		if (null != tool) {
			List<String> args = new LinkedList<>();
			args.add(tool);
			while (true) {
				String p = props.getProperty(String.format("%s_param_%d", name, args.size()));
				if (null != p) {
					p = p.replaceAll("\\$\\{source\\}", source.getName());
					p = p.replaceAll("\\$\\{target\\}", target.getName());
					args.add(p);
				} else {
					break;
				}
			}
			System.out.println("   starte: " + args);
		}
	}

	/*
	private void runExternal(File path, String...args) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder(args);
		builder.directory(path);
		builder.redirectErrorStream(true);
		Process process =  builder.start();
		
		Scanner s = new Scanner(process.getInputStream());
		while (s.hasNextLine()) {
		  System.out.println(">> " + s.nextLine());
		}
		s.close();
		
		int result = process.waitFor();			
		System.out.println( "fertig: " + result);
	}
	*/

	/*
	private static void fixBadJPEG(BufferedImage img) {
        int[] ary = new int[img.getWidth() * img.getHeight()];
        img.getRGB(0, 0, img.getWidth(), img.getHeight(), ary, 0, img.getWidth());
        for (int i = ary.length - 1; i >= 0; i--)
        {
            int y = ary[i] >> 16 & 0xFF; // Y
            int b = (ary[i] >> 8 & 0xFF) - 128; // Pb
            int r = (ary[i] & 0xFF) - 128; // Pr

            int g = (y << 8) + -88 * b + -183 * r >> 8; //
            b = (y << 8) + 454 * b >> 8;
            r = (y << 8) + 359 * r >> 8;

            if (r > 255)
                r = 255;
            else if (r < 0) r = 0;
            if (g > 255)
                g = 255;
            else if (g < 0) g = 0;
            if (b > 255)
                b = 255;
            else if (b < 0) b = 0;

            ary[i] = 0xFF000000 | (r << 8 | g) << 8 | b;
        }
        img.setRGB(0, 0, img.getWidth(), img.getHeight(), ary, 0, img.getWidth());
    }
	*/
	
	public static ImageInformation readImageInformation(File imageFile)  throws IOException, MetadataException, ImageProcessingException {
		Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
		Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
	
		int orientation = 1;
		try {
			orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
		} catch (MetadataException me) {
			// logger.warn("Could not get orientation");
		}
		int width = jpegDirectory.getImageWidth();
		int height = jpegDirectory.getImageHeight();
	
		return new ImageInformation(orientation, width, height);
	}


	public static BufferedImage transformImage(BufferedImage image, AffineTransform transform) throws Exception {
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
		BufferedImage destinationImage = op.createCompatibleDestImage(image,  (image.getType() == BufferedImage.TYPE_BYTE_GRAY)? image.getColorModel() : null );
		Graphics2D g = destinationImage.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
		destinationImage = op.filter(image, destinationImage);;
		return destinationImage;
	}
	
	public static AffineTransform getExifTransformation(int orientation, int width, int height) {

		AffineTransform t = new AffineTransform();

		switch (orientation) {
		case 1:
			break;
		case 2: // Flip X
			t.scale(-1.0, 1.0);
			t.translate(-width, 0);
			break;
		case 3: // PI rotation 
			t.translate(width, height);
			t.rotate(Math.PI);
			break;
		case 4: // Flip Y
			t.scale(1.0, -1.0);
			t.translate(0, -height);
			break;
		case 5: // - PI/2 and Flip X
			t.rotate(-Math.PI / 2);
			t.scale(-1.0, 1.0);
			break;
		case 6: // -PI/2 and -width
			t.translate(height, 0);
			t.rotate(Math.PI / 2);
			break;
		case 7: // PI/2 and Flip
			t.scale(-1.0, 1.0);
			t.translate(-height, 0);
			t.translate(0, width);
			t.rotate(  3 * Math.PI / 2);
			break;
		case 8: // PI / 2
			t.translate(0, width);
			t.rotate(  3 * Math.PI / 2);
			break;
		}

		return t;
	}
  
	public static AffineTransform getExifAngle(int orientation, int width, int height) {

		AffineTransform t = new AffineTransform();

		switch (orientation) {
		case 1:
			break;
		case 2: // Flip X
			t.scale(-1.0, 1.0);
			t.translate(-width, 0);
			break;
		case 3: // PI rotation 
			t.translate(width, height);
			t.rotate(Math.PI);
			break;
		case 4: // Flip Y
			t.scale(1.0, -1.0);
			t.translate(0, -height);
			break;
		case 5: // - PI/2 and Flip X
			t.rotate(-Math.PI / 2);
			t.scale(-1.0, 1.0);
			break;
		case 6: // -PI/2 and -width
			t.translate(height, 0);
			t.rotate(Math.PI / 2);
			break;
		case 7: // PI/2 and Flip
			t.scale(-1.0, 1.0);
			t.translate(-height, 0);
			t.translate(0, width);
			t.rotate(  3 * Math.PI / 2);
			break;
		case 8: // PI / 2
			t.translate(0, width);
			t.rotate(  3 * Math.PI / 2);
			break;
		}

		return t;
	}
  
	private static class ImageInformation {
		public final int orientation;
		public final int width;
		public final int height;
	
		public ImageInformation(int orientation, int width, int height) {
			this.orientation = orientation;
			this.width = width;
			this.height = height;
		}
	
		public String toString() {
			return String.format("%dx%d,%d", this.width, this.height, this.orientation);
		}
	}
	
}