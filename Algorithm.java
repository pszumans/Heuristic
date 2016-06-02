import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Algorithm {
	
	private int bestTotalCost;
	private static String bestConfig;

	public static void main(String[] args) {

		double temp = 1.0;
		double alpha = 0.97;
		double min = 0.0001;
		int n = 3;
		int l = 3;
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
		
//		GraphBuilder graph = new GraphBuilder(n, l);
		GraphBuilder graph = new GraphBuilder(racks, links, new File(filename));
		DataCenter dc = new DataCenter(dataServers, compServers, demands, minDemandsToServe, graph);
		SimulatedAnnealing sa = new SimulatedAnnealing(temp, alpha, min);
		
		while (!sa.endCriterion()) {
		System.out.println(sa.getTemp());
		for (int i = 0; i < 1000; i++) {
			
//			Demand d1 = null, d2 = null;
//			boolean isDemandServed = dc.serveDemandInRack(d1 = dc.getRandomDemandToServe(), dc.getRandomRack(),
//					new HashSet<String>());
//			boolean isDemand2Served = dc.serveDemandInRack(d2 = dc.getRandomDemandToServe(), dc.getRandomRack(),
//					new HashSet<String>());
//			
//			
//			if (!isDemandServed && d1 != null)
//				System.out.println("NIEZREALIZOWANE: " + d1.getName());
//			if (!isDemand2Served && d2 != null)
//				System.out.println("NIEZREALIZOWANE: " + d2.getName());
//			
			boolean isDemandServed = true;
			Demand d = null;
			String dN = null;
			while (dc.getDemandsAmount() < dc.getMinDemandsToServe()) {
				if (dc.areAllDemandsChecked()) {
					isDemandServed = false;
					break;
				}
				isDemandServed = dc.serveDemandInRack(d = dc.getRandomDemandToServe(), dc.getRandomRack(),
						new HashSet<String>());
				if (!isDemandServed)
					dN = d.getName();
			}
			
			if (isDemandServed) {
				boolean b = sa.checkSolution(dc.countTotalCost());
//				System.out.println(dc);
//				System.out.println("########################## COST: " + sa.getBestSolution());
				if(b) {
					bestConfig = dc.toString();
//					System.out.println(bestConfig);
//					System.out.println();
				}
				dc.retrieveUnservedDemands();
				dc.removeDemand(dc.getRandomDemand());
//				System.out.println(dc);
//				System.out.println("########################## COST: " + sa.getBestSolution());
//
			}
			else {
				if (dN != null)
					System.out.println(dN);
//				System.out.println("##########  BANG  !!!!!!!!! " + d.getName());
//				System.out.println(dc.sb);
				dc.removeAllDemands();
//				System.out.println(dc);
//				System.out.println();
			}


//			if(isDemandServed && isDemand2Served) { 
//			int newSolution = dc.countTotalCost();
//			System.out.println("NEW " + newSolution);
//			sa.checkSolution(newSolution);
////			newPunto = dc.countTotalCost();
////			if (newPunto < punto)
////				punto = newPunto;
//			}
			
//			System.out.println(dc);
//			System.out.println("########################## COST: " + sa.getBestSolution());
////			System.out.println("########################## COST: " + punto);
			
//			Integer demandsCount = new Integer(dc.getDemandsAmount());
//			int demandsCount = dc.getDemandsAmount();
//			for (int d = 0; d < demandsCount; d++)
//				dc.removeDemand(dc.getDemand(0));
			
//			System.out.println(dc);
			
//			for (int sr = 0; sr < dc.getRacksAmount(); sr++)
//				for (int ds = 0; ds < dc.getRack(sr).getDataServersAmount(); ds++)
//					if (dc.getRack(sr).getDataServer(ds).isEmpty()) {
//						System.out.println(dc.sb.toString());
//						dc.getClass();
//					}
//			
//			for (int sr = 0; sr < dc.getRacksAmount(); sr++)
//				for (int cs = 0; cs < dc.getRack(sr).getCompServersAmount(); cs++)
//					if (dc.getRack(sr).getCompServer(cs).isEmpty()) {
//					System.out.println(dc.sb.toString());
//						dc.getClass();
//					}
			
			dc.sDC = 0;
			dc.sDCS = 0;
			dc.sb = new StringBuilder();
			}
//		for (int i = 0; i < 3; i++) {
//			System.out.println("Kabaczek " + i + " " + dc.getRack(i).getPaths().get(i).getCapacity());
//		}
//		System.err.println();
		System.out.println(sa.getBestSolution());
		sa.reduceTemp();
		}
		System.out.println(bestConfig);
		dc.removeAllDemands();
		System.out.println(dc);
		System.out.println();
//		for (Link link : graph.getLinks())
//			System.out.println(link);
	}
}