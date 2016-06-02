import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 
 */

/**
 * @author Szuman
 *
 */
public class DataCenter implements Rack.ServersPool {

	private final List<Demand> demandsToServe;
	private final int minDemandsToServe;
	private List<Rack> racks;
	private Set<Link> links;
	private List<DataServer> dataServers;
	private List<CompServer> compServers;

	private List<Demand> demandsServed;
	private Set<String> demandsChosen;
	// private Set<String> racksChosen;

	public int sDC = 0;
	public int sDCS = 0;
	public StringBuilder sb = new StringBuilder();
	public StringBuilder sDCSsb = new StringBuilder();

	public DataCenter(List<DataServer> dataServers, List<CompServer> compServers, List<Demand> demandsToServe,
			int minDemandsToServe, GraphBuilder graph) {
		this.dataServers = dataServers;
		this.compServers = compServers;
		this.demandsToServe = demandsToServe;
		this.minDemandsToServe = minDemandsToServe;
		racks = graph.getRacks();
		links = graph.getLinks();
		demandsServed = new ArrayList<Demand>();
		demandsChosen = new HashSet<String>();
		// racksChosen = new HashSet<String>();
		setServersPools();
	}

	public int getRandomInt(int bound) {
		return new Random().nextInt(bound);
	}

	public int getMinDemandsToServe() {
		return minDemandsToServe;
	}

	public DataServer getDataServer(int index) {
		return new DataServer(dataServers.get(index));
	}

	public DataServer getRandomDataServer() {
		return new DataServer(dataServers.get(new Random().nextInt(dataServers.size())));
	}

	public CompServer getCompServer(int index) {
		return new CompServer(compServers.get(index));
	}

	public CompServer getRandomCompServer() {
		return new CompServer(compServers.get(new Random().nextInt(compServers.size())));
	}

	public int getDataServerAmount() {
		return dataServers.size();
	}

	public int getCompServerAmount() {
		return compServers.size();
	}

	public Demand getDemandToServe(int index) {
		return demandsToServe.get(index);
	}

	public Demand getRandomDemandToServe() {
		Demand demand = null;
		int demandsCount = demandsChosen.size();
		if (!areAllDemandsChecked()) {
			while (demandsCount == demandsChosen.size()) {
				demand = demandsToServe.get(new Random().nextInt(demandsToServe.size()));
				demandsChosen.add(demand.getName());
			}
		}
		return demand;
		// Demand demand = demandsToServe.get(new
		// Random().nextInt(demandsToServe.size()));
		// while (true) {
		// if (demandsChosen.contains(demand.getName()))
		// return demand;
		// demand = demandsToServe.get(new
		// Random().nextInt(demandsToServe.size()));
		// }
	}

	public int getDemandsToServeAmount() {
		return demandsToServe.size();
	}

	public Demand getDemand(int index) {
		return demandsServed.get(index);
	}

	public Demand getRandomDemand() {
		return demandsServed.get(new Random().nextInt(demandsServed.size()));
	}

	public int getDemandsAmount() {
		return demandsServed.size();
	}

	public int getDemandsChosenAmount() {
		return demandsChosen.size();
	}

	// public Rack getRack(int index) {
	// return racks.get(index);
	// }

	public Rack getRandomRack() {
		return racks.get(new Random().nextInt(racks.size()));
	}

	public int getRacksAmount() {
		return racks.size();
	}

	// public Link getLink(int index) {
	// return links.get(index);
	// }

	public void addDemand(Demand d) {
		demandsServed.add(d);
	}

	public void removeDemand(Demand demand) {
		clearDemand(demand);
		demandsChosen.remove(demand.getName());
		demandsServed.remove(demand);
		sb.append(demand.getName() + " removed\n");
	}

	public void clearDemand(Demand demand) {
		sb.append("clearDemand(" + demand.getName() + ")\n");
		for (Rack rack : racks) {
			// rack.resetPaths(demand.getName());
			resetLinks(demand.getName());
			if (rack.isDataDemandInstalled(demand.getName()))
				rack.removeDataDemand(demand.getDataDemand());
			if (rack.isCompDemandInstalled(demand.getName()))
				rack.removeCompDemand(demand.getCompDemand());
		}
	}

	public void retrieveUnservedDemands() {
		Set<String> temp = new HashSet<String>();
		for (String demandName : demandsChosen)
			for (Demand demand : demandsServed)
				if (demandName.equals(demand.getName()))
					temp.add(demandName);
		demandsChosen = temp;
	}

	public void removeAllDemands() {
		int demandsCount = getDemandsAmount();
		for (int d = 0; d < demandsCount; d++)
			removeDemand(getDemand(0));
		demandsChosen.removeAll(demandsChosen);
	}

	public boolean areAllDemandsChecked() {
		return demandsChosen.size() == demandsToServe.size();
	}

	public boolean serveDemandInRack(Demand d, Rack rack, Set<String> checkedRacks) {
		if (d == null)
			return false;
		sb.append("serveDemandInRack(" + d.getName() + ", " + rack.getName() + ", " + checkedRacks.size() + ") x "
				+ ++sDC + "\n");
		Demand demand = new Demand(d);
		Demand.DataDemand dd = demand.getDataDemand();
		Demand.CompDemand cd = demand.getCompDemand();

		checkedRacks.add(rack.getName());

		boolean checkDD = rack.serveDataDemand(dd);
		if (checkDD) {
			sb.append(demand.getName() + " DATA Served\n");
			boolean checkCD = serveDemandFromRack(demand, rack, new HashSet<String>());
			// boolean checkCD = rack.serveCompDemand(cd);
			if (!checkCD) {
				sb.append(demand.getName() + " COMP NOT Served\n");
				checkCD = serveDemandFromRack(demand, rack, new HashSet<String>());
			}
			if (checkCD) {
				sb.append(demand.getName() + " COMP Served\n");
				addDemand(demand);
				return true;
			}
			clearDemand(demand);
		} else
			sb.append(demand.getName() + " DATA NOT Served\n");
		if (checkedRacks.size() == racks.size()) {
			sb.append(demand.getName() + " All racks checked\n");
			clearDemand(demand);
			return false;
		}
		return serveDemandInRack(d, getRandomRack(), checkedRacks);

		// if (!checkDD) {
		// sb.append(demand.getName() + " DATA NOT Served\n");
		// clearDemand(demand);
		// }
		//
		// return checkDD;
	}

	// public boolean serveDemandInSecondRack(Demand demand, Rack rack,
	// Set<String> checkedRacks) {
	// sb.append("serveDemandInSecondRack(" + demand.getName() + ", " +
	// rack.getName() + ", " + checkedRacks.size() + ") x " + ++sDCS + " #\n");
	// Demand.DataDemand dd = demand.getDataDemand();
	// Demand.CompDemand cd = demand.getCompDemand();
	// //rack.updatePathsCapacity(links);
	//// Rack.Path path = rack.chooseRandomPath(dd.getDemandName(),
	// dd.getStorage());
	// List<Rack.Path> paths = rack.chooseRandomPath(dd.getDemandName(),
	// dd.getStorage());
	// if (path == null) {
	// sb.append("no paths #\n");
	//// clearDemand(demand);
	// return false;
	// }
	// updatePaths(path, demand.getName());
	// Rack secondRack = path.getSecondRack();
	// checkedRacks.add(secondRack.getName());
	// boolean checkCD =
	// racks.get(racks.indexOf(secondRack)).serveCompDemand(cd);
	// if (checkCD) {
	// sb.append(demand.getName() + " COMP Served #\n");
	// return true;
	// }
	// if (checkedRacks.size() != racks.size() - 1) {
	// sb.append("next second Rack #\n");
	// checkCD = serveDemandInSecondRack(demand, rack, checkedRacks);
	// }
	// if (!checkCD) {
	// sb.append(demand.getName() + " COMP NOT Served #\n");
	//// clearDemand(demand);
	// }
	// return checkCD;
	// }

	private boolean serveDemandFromRack(Demand demand, Rack rack, Set<String> checkedRacks) {
		sb.append("serveDemandInSecondRack(" + demand.getName() + ", " + rack.getName() + ", " + checkedRacks.size()
				+ ") x " + ++sDCS + " #\n");
		Demand.DataDemand dd = demand.getDataDemand();
		Demand.CompDemand cd = demand.getCompDemand();
		String demandName = demand.getName();
		// rack.updatePathsCapacity(links);
		// Rack.Path path = rack.chooseRandomPath(dd.getDemandName(),
		// dd.getStorage());
		Rack randomRack = getRandomRack();
		String randomRackName = randomRack.getName();
		checkedRacks.add(randomRackName);
		boolean checkCD = false;
		if (randomRackName.equals(rack.getName())) {
			checkCD = rack.serveCompDemand(cd);
			if (checkCD)
				return true;

		} else {
//			if (randomRack.getCompServersAmount() > 0)
//				System.out.println();
			checkCD = rack.transportDemandToRack(demandName, dd.getStorage(), randomRackName);
			// if (checkCD)
			// System.out.println();

			if (!checkCD) {
				sb.append("no paths #\n");
				// rack.resetPaths(demandName);
				resetLinks(demandName);
				if (checkedRacks.size() != racks.size())
					checkCD = serveDemandFromRack(demand, rack, checkedRacks);
				return checkCD;
			}
			// updatePaths(chosenPaths, demandName);
//			checkCD = racks.get(racks.indexOf(randomRack)).serveCompDemand(cd);
			checkCD = randomRack.serveCompDemand(cd);
			if (checkCD) {
				sb.append(demand.getName() + " COMP Served #\n");
				return true;
			}
		}
		if (checkedRacks.size() != racks.size()) {
			sb.append("next second Rack #\n");
			// rack.resetPaths(demandName);
			resetLinks(demandName);
			checkCD = serveDemandFromRack(demand, rack, checkedRacks);
		}
		if (!checkCD) {
			sb.append(demand.getName() + " COMP NOT Served #\n");
			// clearDemand(demand);
		}
		return checkCD;
	}

//	private void updatePaths(List<Rack.Path> paths, String demandName) {
//		for (Rack rack : racks)
//			for (Rack.Path path : paths)
//				rack.updatePaths(path, demandName);
//	}

	private void resetLinks(String demandName) {
		for (Link link : links)
			link.removeCapacityUsed(demandName);
	}

	private void setServersPools() {
		for (Rack rack : racks)
			rack.setServersPool(this);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Rack rack : racks)
			sb.append(rack.toString());
		for (Link link: links)
			sb.append(link.toString());
		return sb.toString();
	}

	public int countTotalCost() {
		int totalCost = 0;
		for (int sr = 0; sr < racks.size(); sr++) {
			for (int ds = 0; ds < racks.get(sr).getDataServersAmount(); ds++)
				totalCost += racks.get(sr).getDataServer(ds).getCost();
			for (int cs = 0; cs < racks.get(sr).getCompServersAmount(); cs++)
				totalCost += racks.get(sr).getCompServer(cs).getCost();
		}
		return totalCost;
	}

}