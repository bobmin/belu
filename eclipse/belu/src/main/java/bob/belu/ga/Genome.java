package bob.belu.ga;

import java.util.Random;

/**
 * Beschreibt ein Bild.
 * 
 * @author maik@btmx.net
 *
 */
public class Genome {

	private int scale = 0;

	/**
	 * Instanziiert das Objekt für ein Bild.
	 */
	public Genome() {
		this.scale = (int) (new Random().nextDouble() * 10.0);
	}

}
