import java.util.Random;

public class SimulatedAnnealing {

	public final int INITIAL = Integer.MAX_VALUE;
	
	private double temp;
	private final double alpha;
	private final double min;
	private int bestSolution;
	
	public SimulatedAnnealing(double temp, double alpha, double min) {
		this.temp = temp;
		this.alpha = alpha;
		this.min = min;
		bestSolution = INITIAL;
	}
	
	public double getTemp() {
		return temp;
	}
	
	public int getBestSolution() {
		return bestSolution;
	}

	public boolean checkSolution(int newSolution) {
		if (newSolution <= bestSolution) {
			bestSolution = newSolution;
			return true;
		}
		else if (MetropolisTest(newSolution)) {
			bestSolution = newSolution;
			return true;
		}
		return false;
	}
	
	private boolean MetropolisTest(int newSolution) {
		double rand = new Random().nextDouble();
		return BolzmannDist(newSolution) > rand;

	}
		
	private double BolzmannDist(int newSolution) {
		double x = (double)(bestSolution - newSolution) / temp;
		return Math.exp(x);
	}
	
	public void reduceTemp() {
		temp *= alpha;
	}
	
	public boolean endCriterion() {
		return temp < min;
	}

}
