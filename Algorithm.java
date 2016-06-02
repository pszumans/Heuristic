import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Generated;

public class Algorithm {

	private int bestTotalCost;
	private static String bestConfig;

	public static void main(String[] args) {

		double temp = 100;
		double alpha = 0.97;
		double min = 0.01;
		int n = 10;
		int l = 10;
		String filename = "ok.dat";
		Parser parser = null;

		try {
			parser = new Parser(filename);
			parser.setAMPLWriter(new Writer());
			parser.parse();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<DataServer> dataServers = parser.getDataServers();
		List<CompServer> compServers = parser.getCompServers();
		List<Demand> demands = parser.getDemands();
		int minDemandsToServe = parser.getMinDemands();

		List<Rack> racks = parser.getRacks();
		Set<Link> links = parser.getLinks();

//		 GraphBuilder graph = new GraphBuilder(n, l);
		GraphBuilder graph = new GraphBuilder(racks, links, new File(filename));
		DataCenter dc = new DataCenter(dataServers, compServers, demands, minDemandsToServe, graph);
		SimulatedAnnealing sa = new SimulatedAnnealing(temp, alpha, min);

		while (!sa.endCriterion()) {
			System.out.println(sa.getTemp());
			for (int i = 0; i < 1000; i++) {

				boolean isDemandServed = true;
				Demand d = null;
				String dN = null;
				Set<String> chosenDemands = dc.getChosenDemands();
				while (dc.getDemandsAmount() < dc.getMinDemandsToServe()) {
					if (chosenDemands.size() == dc.getDemandsToServeAmount()) {
						isDemandServed = false;
						break;
					}
					isDemandServed = dc.serveDemandInRack(d = dc.getRandomDemandToServe(chosenDemands), dc.getRandomRack(),
							new HashSet<String>());
					if (!isDemandServed)
						dN = d.getName();
				}

				if (isDemandServed) {
					boolean b = sa.checkSolution(dc.countTotalCost());

					if (b) {
						bestConfig = dc.toString();

					}
//					dc.retrieveUnservedDemands();
					dc.removeDemand(dc.getRandomDemand());
					//
				} else {

					dc.removeAllDemands();

				}

				dc.sDC = 0;
				dc.sDCS = 0;
				dc.sb = new StringBuilder();
			}

			System.out.println(sa.getBestSolution());
			sa.reduceTemp();
		}
		System.out.println(bestConfig);

	}
}