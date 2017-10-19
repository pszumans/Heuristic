import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Algorithm {

	public static double temp = 1;
	public static double alpha = 0.999;
	public static double min = 0.001;
	public static int insideLoop = 2000;
	public static int nodesCount = 5;
	public static int linksCount = 10;
	public static String filename = "DataCenter.dat";
	public static String AMPLFilename = "RandomDataCenter.dat";
	public static Parser parser;
	public static Writer wr;

	private int bestTotalCost;
	private String bestConfig;
	private DataCenter dc;
	private SimulatedAnnealing sa;

//	private boolean visualization;

	public Algorithm(DataCenter dc, SimulatedAnnealing sa) {
		this.dc = dc;
		this.sa = sa;
		bestTotalCost = sa.INITIAL;
//		visualization = true;
	}

	public void start() {

		System.out.println("\nAlgoritm started!\n");

		Demand removedDemand = null;
		Demand chosenDemand = null;

		while (!sa.endCriterion()) {

			for (int i = 0; i < insideLoop; i++) {

				boolean areDemandsServed = true;
				Set<String> chosenDemands = dc.getChosenDemands();
				while (dc.getDemandsCount() < dc.getMinDemandsToServe()) {
					if (chosenDemands.size() == dc.getDemandsToServeCount()) {
						areDemandsServed = false;
						break;
					}
					chosenDemand = dc.getRandomDemandToServe(chosenDemands);
					if (removedDemand == null || !chosenDemand.getName().equals(removedDemand.getName()))
						areDemandsServed = dc.serveDemandInRack(chosenDemand, dc.getRandomRack(),
								new HashSet<String>());
				}

				if (areDemandsServed) {
					
					boolean isBestChanged = sa.checkSolution(dc.countTotalCost());

					if (isBestChanged) {
						
						bestTotalCost = sa.getBestSolution();
						bestConfig = dc.toString();

					}
				}

				removedDemand = dc.getRandomDemand();
				dc.removeDemand(removedDemand);
				if (dc.getDemandsToServeCount() == dc.getMinDemandsToServe()
						&& dc.getDemandsCount() == dc.getMinDemandsToServe() - 1)
					removedDemand = null;

			}
			if (bestTotalCost == sa.INITIAL)
				return;
			printBest();
			sa.reduceTemp();
		}
		try {
			playWin();
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			java.awt.Toolkit.getDefaultToolkit().beep();
		}
	}

//	private void setVisualization(boolean b) {
//		visualization = b;
//	}

	private void printBest() {
//		if (visualization) {
			StringBuilder view = new StringBuilder();
			for (int i = 0; i < bestTotalCost; i++)
				view.append("|");
			view.append(" " + bestTotalCost);
			System.out.println(view.toString());
//		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (bestTotalCost != sa.INITIAL) {
			sb.append("\nTOTAL_COST: " + bestTotalCost + "\n\n");
			sb.append(bestConfig);
			sb.append("\nTOTAL_COST: " + bestTotalCost + "\n\n");
		} else
			sb.append("INFEASIBLE");
		return sb.toString();
	}

	private static boolean chooseValues(Scanner sc) {

		boolean isRandom = false;
		String answer;

		System.out.println("## DEFAULT SETTINGS for SIMULATED ANNEALING ##");
		System.out
				.println("temp = " + temp + ", alpha = " + alpha + ", min = " + min + ", insideLoops = " + insideLoop);
		System.out.println("\nDo you want change default settings for Simulated Annealing? (y/n) (default: no)");
		answer = sc.nextLine();
		if (answer.matches("y|Y")) {
			chooseSAParams(sc);
		}

		System.out.println("\nDo you want graph from file (y) or random (n)? (default: no)");
		answer = sc.nextLine();
		System.out.print(
				"Input File Name (with directory if file isn't local) (default: " + filename + " - press ENTER): ");
		String name = sc.nextLine();
		if (name.length() > 0)
			filename = name;

		if (!answer.matches("y|Y")) {
			isRandom = true;
			chooseForRandom(sc);
		}

		// sc.close();
		return isRandom;
	}

	private static void printDefault() {
		System.out.println("### DEFAULT SETTINGS ###");
		System.out.println("Simulated Annealing: temp = " + temp + ", alpha = " + alpha + ", min = " + min
				+ ", insideLoop = " + insideLoop);
		System.out.println("Random Graph with " + nodesCount + " nodes and " + linksCount + " links");
		System.out.println("Local file with data: " + filename);
		System.out.println("Local file for AMPL data: " + AMPLFilename);
		System.out.println();
	}

	private static void chooseForRandom(Scanner sc) {
		System.out.print("Input File Name for AMPL (with directory if file isn't local) (default: " + AMPLFilename
				+ " - press ENTER) :");
		String AMPLName = sc.nextLine();
		if (AMPLName.length() > 0)
			AMPLFilename = AMPLName;
		wr = new Writer(AMPLFilename);
		System.out.print("Number of nodes (" + nodesCount + ") = ");
		String nodes = sc.nextLine();
		if (nodes.length() > 0)
			nodesCount = Integer.parseInt(nodes);
		System.out.print("Number of links (" + linksCount + ") = ");
		String links = sc.nextLine();
		if (links.length() > 0)
			linksCount = Integer.parseInt(links);
		System.out.println("#nodes = " + nodesCount + ", #links = " + linksCount);
	}

	private static void chooseSAParams(Scanner sc) {
		System.out.println(
				"Set values: initial_temperature_value, alpha_parameter and minimal_temerature_value ? (press ENTER fo default)");
		System.out.print("Initial temperature (" + temp + ") = ");
		String param = sc.nextLine();
		if (param.length() > 0)
			temp = Double.parseDouble(param);
		System.out.print("Alpha (" + alpha + ") = ");
		param = sc.nextLine();
		if (param.length() > 0)
			alpha = Double.parseDouble(param);
		System.out.print("Minimum temperature (" + min + ") = ");
		param = sc.nextLine();
		if (param.length() > 0)
			min = Double.parseDouble(param);
		System.out.print("Number of inside loops (" + insideLoop + ") = ");
		param = sc.nextLine();
		if (param.length() > 0)
			insideLoop = Integer.parseInt(param);

		System.out.println("temp = " + temp + ", alpha = " + alpha + ", min = " + min + ", insideLoops = " + insideLoop);

	}

	private void playWin() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("win.wav"));
		Clip clip = AudioSystem.getClip();
		clip.open(audioIn);
		clip.start();
	}
	
	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);

		Algorithm.printDefault();
		System.out.println("Do you want change default settings? (y/n) (default: no)");
		String answer = sc.nextLine();
		boolean isRandom = true;
		if (answer.matches("y|Y"))
			isRandom = Algorithm.chooseValues(sc);

		while (true)
			try {
				parser = new Parser(filename);
				if (isRandom) {
					if (wr == null)
						wr = new Writer(AMPLFilename);
					parser.setAMPLWriter(wr);
				}
				parser.parse();
				break;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("\nGive another Input File Name: ");
				filename = sc.nextLine();
			}

		List<DataServer> dataServers = parser.getDataServers();
		List<CompServer> compServers = parser.getCompServers();
		List<Demand> demands = parser.getDemands();
		int minDemandsToServe = parser.getMinDemands();

		GraphBuilder graph = null;
		if (isRandom) {
			graph = new GraphBuilder(nodesCount, linksCount, wr);
		} else {
			List<Rack> racks = parser.getRacks();
			Set<Link> links = parser.getLinks();
			graph = new GraphBuilder(racks, links);
		}

		graph.draw();

		if (isRandom)
			try {
				wr.write();
			} catch (IOException e) {
				e.printStackTrace();
			}
		DataCenter dc = new DataCenter(dataServers, compServers, demands, minDemandsToServe, graph);
		SimulatedAnnealing sa = new SimulatedAnnealing(temp, alpha, min);

		Algorithm alg = new Algorithm(dc, sa);

//		System.out.println("\nDo you want visualization? (y/n) (default: yes)");
//		if (!sc.nextLine().matches("n|N"))
//			alg.setVisualization(false);

		long start = System.nanoTime();
		alg.start();
		long elapsedTime = System.nanoTime() - start;

		System.out.println(alg);
		System.out.println("\nAlgorithm ran " + elapsedTime * 10E-10 + " seconds.");

		System.out.println("\nPress ENTER to exit...");
		while (true) {
			sc.nextLine();
			break;
		}
		sc.close();
		System.exit(0);

		return;
	}

}