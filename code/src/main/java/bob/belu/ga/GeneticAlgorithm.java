package bob.belu.ga;

public class GeneticAlgorithm {

	private static final int ANZAHL_BILDER = 10;

	public static void main(String[] args) {
		GeneticAlgorithm algo = new GeneticAlgorithm();
		System.out.println("best solution = " + algo.bestSolution);
	}

	private int populationSize = 512;

	private int crossoverProbability = 45;

	private int mutationProbability = 50;

	private int elitismPercentage = 10;

	private int stableGenerations = 200;

	private int maxGenerations = 600;

	// ---------------------------------------

	private int generations = 0;

	private Genome bestSolution = null;

	/**
	 * Instanziiert den Algorithmus und berechnet...
	 */
	public GeneticAlgorithm() {
		int stableGenerationCount = 0;

		Population population = new Population(ANZAHL_BILDER);

		// int eliteElements = (int) (m_pPopulation -> size() * ((double)
		// elitismPercentage / 100.0)) / 2;
	}

}
