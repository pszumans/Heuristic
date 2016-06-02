import java.util.Random;

public class SimulatedAnnealing {

	private final int INITIAL = -1;
	
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
		if (newSolution <= bestSolution || bestSolution == INITIAL) {
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
//		System.out.println("RAND " + rand);
		return BolzmannDist(newSolution) > rand;

	}
		
	private double BolzmannDist(int newSolution) {
//		System.out.println((double)((bestSolution - newSolution) / temp));
		double x = (double)(bestSolution - newSolution) / temp;
//		System.out.println(Math.exp(x));
//		return Math.exp((bestSolution - newSolution) / temp);
		return Math.exp(x);
	}
	
	public void reduceTemp() {
		temp *= alpha;
	}
	
	public boolean endCriterion() {
		return temp < min;
	}

}
