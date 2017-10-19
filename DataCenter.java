import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class DataCenter implements Rack.ServersPool {

	private final List<Demand> demandsToServe;
	private final int minDemandsToServe;
	private List<Rack> racks;
	private Set<Link> links;
	private List<DataServer> dataServers;
	private List<CompServer> compServers;

	private List<Demand> servedDemands;

	
	public DataCenter(List<DataServer> dataServers, List<CompServer> compServers, List<Demand> demandsToServe,
			int minDemandsToServe, GraphBuilder graph) {
		Collections.sort(dataServers, new Server());
		Collections.sort(compServers, new Server());
		this.dataServers = dataServers;
		this.compServers = compServers;
		this.demandsToServe = demandsToServe;
		this.minDemandsToServe = minDemandsToServe;
		racks = graph.getRacks();
		links = graph.getLinks();
		servedDemands = new ArrayList<Demand>();
		setServersPools();
	}

	public int getRandomInt(int bound) {
		return new Random().nextInt(bound);
	}

	public int getMinDemandsToServe() {
		return minDemandsToServe;
	}

	@Override
	public DataServer getDataServer(int index) {
		return new DataServer(dataServers.get(index));
	}
	
	@Override
	public CompServer getCompServer(int index) {
		return new CompServer(compServers.get(index));
	}


	@Override
	public DataServer getRandomDataServer() {
		return new DataServer(dataServers.get(new Random().nextInt(dataServers.size())));
	}

	@Override
	public CompServer getRandomCompServer() {
		return new CompServer(compServers.get(new Random().nextInt(compServers.size())));
	}

	@Override
	public int getDataServersCount() {
		return dataServers.size();
	}

	@Override
	public int getCompServersCount() {
		return compServers.size();
	}

	public Demand getDemandToServe(int index) {
		return demandsToServe.get(index);
	}

	public Demand getRandomDemandToServe(Set<String> servedDemands) {
		Demand demand = null;
		int demandsCount = servedDemands.size();
		if (servedDemands.size() != demandsToServe.size()) {
			while (demandsCount == servedDemands.size()) {
				demand = demandsToServe.get(new Random().nextInt(demandsToServe.size()));
				servedDemands.add(demand.getName());
			}
		}
		if (demand != null)
			demand = new Demand(demand);
		return demand;

	}

	public int getDemandsToServeCount() {
		return demandsToServe.size();
	}

	public Demand getDemand(int index) {
		return servedDemands.get(index);
	}

	public Demand getRandomDemand() {
		return servedDemands.get(new Random().nextInt(servedDemands.size()));
	}

	public int getDemandsCount() {
		return servedDemands.size();
	}

	// public Rack getRack(int index) {
	// return racks.get(index);
	// }

	public Rack getRandomRack() {
		return racks.get(new Random().nextInt(racks.size()));
	}

	public int getRacksCount() {
		return racks.size();
	}

	public Set<String> getChosenDemands() {
		Set<String> chosenDemands = new HashSet<String>();
		for (Demand demand: servedDemands)
			chosenDemands.add(demand.getName());
		return chosenDemands;
	}

	public void addDemand(Demand d) {
		servedDemands.add(d);
	}

	public void removeDemand(Demand demand) {
		clearDemand(demand);
		servedDemands.remove(demand);
	}

	public void clearDemand(Demand demand) {
		for (Rack rack : racks) {
			resetLinks(demand.getName());
			if (rack.isDataDemandInstalled(demand.getName()))
				rack.removeDataDemand(demand.getDataDemand());
			if (rack.isCompDemandInstalled(demand.getName()))
				rack.removeCompDemand(demand.getCompDemand());
		}
	}

	public void removeAllDemands() {
		int demandsCount = getDemandsCount();
		for (int d = 0; d < demandsCount; d++)
			removeDemand(getDemand(0));
	}

	public boolean serveDemandInRack(Demand demand, Rack rack, Set<String> checkedRacks) {
		
		if (demand == null)
			return false;
		
		Demand.DataDemand dd = demand.getDataDemand();

		checkedRacks.add(rack.getName());

		boolean isDDServed = rack.serveDataDemand(dd);
		if (isDDServed) {
			boolean isCDServed = serveDemandFromRack(demand, rack, new HashSet<String>());
			if (!isCDServed) {
				isCDServed = serveDemandFromRack(demand, rack, new HashSet<String>());
			}
			if (isCDServed) {
				addDemand(demand);
				return true;
			}
			clearDemand(demand);
		}
		else if (checkedRacks.size() == racks.size()) {
			clearDemand(demand);
			return false;
		}
		return serveDemandInRack(demand, getRandomRack(), checkedRacks);

	}
	
	private boolean serveDemandFromRack(Demand demand, Rack rack, Set<String> checkedRacks) {
		
		String demandName = demand.getName();
		Demand.DataDemand dd = demand.getDataDemand();
		Demand.CompDemand cd = demand.getCompDemand();
		
		Rack randomRack = getRandomRack();
		String randomRackName = randomRack.getName();
		
		checkedRacks.add(randomRackName);
		boolean isCDServed = false;
		if (randomRackName.equals(rack.getName())) {
			isCDServed = rack.serveCompDemand(cd);
			if (isCDServed)
				return true;

		} else {
			isCDServed = rack.transportDemandToRack(demandName, dd.getStorage(), randomRackName);

			if (!isCDServed) {
				resetLinks(demandName);
				if (checkedRacks.size() != racks.size())
					isCDServed = serveDemandFromRack(demand, rack, checkedRacks);
				return isCDServed;
			}
			isCDServed = randomRack.serveCompDemand(cd);
			if (isCDServed) {
				return true;
			}
		}
		if (checkedRacks.size() != racks.size()) {
			resetLinks(demandName);
			isCDServed = serveDemandFromRack(demand, rack, checkedRacks);
		}
		return isCDServed;
	}


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
			sb.append(rack.toAdvString());
		for (Link link: links)
			sb.append(link.toAdvString());
		return sb.toString();
	}

	public int countTotalCost() {
		int totalCost = 0;
		for (Rack rack: racks) {
			List<Server[]> replacedServerInRack = rack.tryToDecreaseCost();
			for (int ds = 0; ds < rack.getDataServersCount(); ds++)
				totalCost += rack.getDataServer(ds).getCost();
			for (int cs = 0; cs < rack.getCompServersCount(); cs++)
				totalCost += rack.getCompServer(cs).getCost();
			for (Server[] s: replacedServerInRack)
				rack.replaceServers(s[0], s[1]);
		}
		return totalCost;
	}
	
}