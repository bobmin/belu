package bob.belu.ga;

/**
 * Beschreibt eine Menge von Individuen.
 * 
 * @author maik@btmx.net
 *
 */
public class Population {

	private final Genome[] data;

	/**
	 * Instanziiert die Population für die gewünschte Menge an Individuen.
	 * 
	 * @param count
	 *            die Anzahl Individuen
	 */
	public Population(int count) {
		this.data = new Genome[count];
		for (int idx = 0; idx < count; idx++) {
			this.data[idx] = new Genome();
		}
	}

}
