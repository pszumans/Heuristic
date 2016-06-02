import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Rack {

	private final String RACK = "SR";
	
	private String name;
	private int numberOfShelves;
	private int shelvesUsed;
	private int shelvesEmpty;
	private List<Demand.DataDemand> dataDemands;
	private List<Demand.CompDemand> compDemands;
	private List<DataServer> dataServers;
	private List<CompServer> compServers;
//	private List<Server> servers;
	private Map<String, List<Path>> paths;
	
	private ServersPool servers;
	
	public Rack(int nr, int numberOfShelves) {
		name = RACK + nr;
		init(numberOfShelves);
	}
	
	public Rack(String name, int numberOfShelves) {
		this.name = name;
		init(numberOfShelves);
	}
	
	private void init(int numberOfShelves) {
		this.numberOfShelves = numberOfShelves;
		shelvesUsed = 0;
		shelvesEmpty = numberOfShelves;
		dataDemands = new ArrayList<Demand.DataDemand>();
		compDemands = new ArrayList<Demand.CompDemand>();
		dataServers = new ArrayList<DataServer>();
		compServers = new ArrayList<CompServer>();
		paths = new HashMap<String, List<Path>>();
		
	}
	
	public String getName() {
		return name;
	}

	public int getSize() {
		return numberOfShelves;
	}

	public int getShelvesUsed() {
		return shelvesUsed;
	}

	public void setShelvesUsed(int shelvesUsed) {
		this.shelvesUsed = shelvesUsed;
		shelvesEmpty = numberOfShelves - shelvesUsed;
	}
	
	public int getShelvesEmpty() {
		return shelvesEmpty;
	}

	public void setShelvesEmpty(int shelvesEmpty) {
		this.shelvesEmpty = shelvesEmpty;
		shelvesUsed = numberOfShelves - shelvesEmpty;
	}
	
	public List<Path> getPathsBySecondRack(String secondRack) {
		return paths.get(secondRack);
	}

//	public void setPaths(List<Path> paths) {
//		this.paths = paths;
//	}
	
	public void addDataDemand(Demand.DataDemand dd) {
		dataDemands.add(dd);
	}

	public void addCompDemand(Demand.CompDemand cd) {
		compDemands.add(cd);
	}
	
	public void removeDataDemand(Demand.DataDemand dd) {
		
		int i = 0;
		while (i != dataServers.size()) {
			DataServer ds = dataServers.get(i);
			ds.removeDataDemand(dd.getDemandName());
			if (ds.getStorageUsed() == 0)
				removeDataServer(i);
			else i++;
		}
		dataDemands.remove(dd);
		dd.setStorageServed(0);
	}
	
	public void removeCompDemand(Demand.CompDemand cd) {

		int i = 0;
		while (i != compServers.size()) {
			CompServer cs = compServers.get(i);
			cs.removeCompDemand(cd.getDemandName());
			if (cs.getPowerUsed() == 0)
				removeCompServer(i);
			else i++;
		}
		compDemands.remove(cd);
		cd.setComputationServed(0);
	}
	
	public void addServer(Server s) {
		if (s instanceof DataServer)
			dataServers.add((DataServer) s);
		else
			compServers.add((CompServer) s);
		shelvesUsed += s.size;
		shelvesEmpty -= s.size;
	}
	
	public void removeDataServer(int index) {
		DataServer ds = dataServers.get(index);
		dataServers.remove(index);
		shelvesUsed -= ds.size;
		shelvesEmpty += ds.size;
	}

	public boolean useDataServer(Demand.DataDemand dd, DataServer ds) {

		int remainToAddStorage = ds.addStorageUsed(dd.getDemandName(), dd.getStorageRemained());
		if (remainToAddStorage < dd.getStorageRemained()) {
			if (!isServerInstalled(ds))
				addServer(ds);
			if (!isDataDemandInstalled(dd.getDemandName()))
				addDataDemand(dd);
		}
		dd.setStorageRemained(remainToAddStorage);

		return remainToAddStorage == 0;
	}
	
	public void removeCompServer(int index) {
		CompServer cs = compServers.get(index);
		compServers.remove(index);
		shelvesUsed -= cs.size;
		shelvesEmpty += cs.size;
	}
	
	public boolean useCompServer(Demand.CompDemand cd, CompServer cs) {
		
		int remainToAddPower = cs.addPowerUsed(cd.getDemandName(), cd.getComputationRemained());
		if (remainToAddPower < cd.getComputationRemained()) {
			if (!isServerInstalled(cs))
				addServer(cs);
			if (!isCompDemandInstalled(cd.getDemandName()))
				addCompDemand(cd);
		}
		cd.setComputationRemained(remainToAddPower);

		return remainToAddPower == 0;
		
	}
	
	public boolean serveDataDemand(Demand.DataDemand dd) {
		
		boolean isDDServed = useInstalledDataServers(dd);
		
		if (!isDDServed) {
			isDDServed = useNewDataServers(dd);
		}
		
		if (!isDDServed && isDataDemandInstalled(dd.getDemandName())) {
			removeDataDemand(dd);
		}
		
		return isDDServed;
	}
	
	private boolean useInstalledDataServers(Demand.DataDemand dd) {
		List<DataServer> shuffledDataServers = dataServers;
		Collections.shuffle(shuffledDataServers);
		
		boolean isDDServed = false;
		
		for (DataServer ds: shuffledDataServers) {
			if (isEnoughSpace(ds))
				isDDServed = useDataServer(dd, ds);
			if (isDDServed)
				return true;				
		}
		return false;
	}
	
	private boolean useNewDataServers(Demand.DataDemand dd) {
		
		boolean isDDServed = false;
		
		Set<String> tooSmallDataServers = new HashSet<String>();
		int dataServersAmount = servers.getDataServerAmount();
		while (!isDDServed && tooSmallDataServers.size() < dataServersAmount) {
			DataServer ds = servers.getRandomDataServer();
			if (isEnoughSpace(ds)) {
				isDDServed = useDataServer(dd, ds);
			}
			else
				tooSmallDataServers.add(ds.getName());
		}
		return isDDServed;
	}
	
	public boolean serveCompDemand(Demand.CompDemand cd) {
		
		boolean isCDServed = useInstalledCompServers(cd);
		
		if (!isCDServed) {
			isCDServed = useNewCompServers(cd);
		}
		
		if (!isCDServed && isCompDemandInstalled(cd.getDemandName())) {
			removeCompDemand(cd);
		}
		
		return isCDServed;

	}
	
	private boolean useInstalledCompServers(Demand.CompDemand cd) {
		List<CompServer> shuffledCompServers = compServers;
		Collections.shuffle(shuffledCompServers);
		
		boolean isCDServed = false;
		
		for (CompServer cs: shuffledCompServers) {
				isCDServed = useCompServer(cd, cs);
			if (isCDServed)
				return true;				
		}
		return false;
	}
	
	private boolean useNewCompServers(Demand.CompDemand cd) {
		
		boolean isCDServed = false;
		
		Set<String> tooSmallCompServers = new HashSet<String>();
		int compServersAmount = servers.getCompServerAmount();
		while (!isCDServed && tooSmallCompServers.size() < compServersAmount) {
			CompServer cs = servers.getRandomCompServer();
			if (isEnoughSpace(cs))
				isCDServed = useCompServer(cd, cs);
			else
				tooSmallCompServers.add(cs.getName());
		}
		
		return isCDServed;
	}
	
	public interface ServersPool {
		public DataServer getRandomDataServer();
		public CompServer getRandomCompServer();
		public int getDataServerAmount();
		public int getCompServerAmount();
	}
	
	public void addPath(String secondRack, Path path) {
		if (!paths.containsKey(secondRack)) {
			paths.put(secondRack, new ArrayList<Path>());
		}
		paths.get(secondRack).add(path);		
	}
		
	public Path getRandomPath(String secondRack) {
//		Path path = null;
//		int pathsCount = chosenPaths.size();
//		if (pathsCount != paths.get(secondRack).size()) {
//			while (pathsCount == chosenPaths.size()) {
//				List<Path> pathsToChoose = paths.get(secondRack);
//				path = pathsToChoose.get(new Random().nextInt(pathsToChoose.size()));
//				chosenPaths.add(path.getIndex());
//			}
//		}
//		return path;
		return paths.get(secondRack).get(new Random().nextInt(this.paths.get(secondRack).size()));
	}
	
	public DataServer getDataServer(int index) {
		return dataServers.get(index );
	}
	
	public int getDataServersAmount() {
		return dataServers.size();
	}
	
	public CompServer getCompServer(int index) {
		return compServers.get(index);
	}
		
	public int getCompServersAmount() {
		return compServers.size();
	}
	
	public boolean transportDemandToRack(String demandName, int toUseCapacity, String secondRack) {
		Set<Integer> chosenPaths = new HashSet<Integer>();
		while (chosenPaths.size() != paths.get(secondRack).size()) {
			Path path = getRandomPath(secondRack);
			int pathsCount = chosenPaths.size();
			chosenPaths.add(path.getIndex());
			if (pathsCount == chosenPaths.size())
				toUseCapacity = path.transportDemand(demandName, toUseCapacity);
			if (toUseCapacity == 0)
				return true;
		}
		return false;
	}
	
	public void resetPaths(String demandName) {
		for (String secondRack: paths.keySet())
			for (Path path: paths.get(secondRack)) {
				path.clearDemand(demandName);
		}
	}
	
	public void setServersPool(ServersPool sP) {
		servers = sP;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n###---> " + getName() + " <---###\n");
		sb.append("size: " + getSize() + "\n");

		for (DataServer ds: dataServers) {
			sb.append(ds);
		}
		for (CompServer cs: compServers) {
			sb.append(cs);
		}
		
		sb.append("\n");
		return sb.toString();
		
	}
	
	private boolean isEnoughSpace(Server server) {
		return server.getSize() <= shelvesEmpty;
	}
	
	private boolean isServerInstalled(Server s) {
		if (s instanceof DataServer) {
			for (DataServer ds: dataServers) {
				if (ds == s)
					return true;
			}
		}
		else {
			for (CompServer cs: compServers) {
				if (cs == s)
					return true;
			}
		}
		return false;
	}
	
	public boolean isDataDemandInstalled(String demandName) {
		for (Demand.DataDemand dataDamand: dataDemands) {
			if (dataDamand.getDemandName().equals(demandName))
				return true;
		}
		return false;
	}
		
	public boolean isCompDemandInstalled(String demandName) {
		for (Demand.CompDemand compDemand: compDemands) {
			if (compDemand.getDemandName().equals(demandName))
				return true;
		}
		return false;
	}
	
	public class Path {

		private int index;
		private String secondRack;
		private List<Link> links;
//		private int capacity;
		

		public Path(int index, String secondRack, List<Link> links) {
			this.index = index;
			this.setSecondRack(secondRack);
			this.links = links;
//			countCapacity();
			}


		public int getIndex() {
			return index;
		}


		public void setIndex(int index) {
			this.index = index;
		}


		public String getSecondRack() {
			return secondRack;
		}

		public void setSecondRack(String secondRack) {
			this.secondRack = secondRack;
		}

//		public int getCapacity() {
//			return capacity;
//		}
//		
//		public void setCapacity(int capacity) {
//			this.capacity = capacity;
//		}

//		public void countCapacity() {
//			List<Link> temp = links;
//			Collections.sort(temp, new Link());
//			capacity = temp.get(0).getCapacityRemained();
//		}
		
		private Link getThinnestLink() {
			Collections.sort(links, new Link());
			return links.get(0);
		}
		
		public int transportDemand(String demandName, int toUseCapacity) {
			
//			int usedCapacity = Integer.MAX_VALUE;
//			for (Link link: links) {
//				int temp = link.checkCapacityUsed(toUseCapacity);
//				if (temp < usedCapacity)
//					usedCapacity = temp;
//				if (usedCapacity == 0)
//					break;
//			}
			
			int usedCapacity = getThinnestLink().checkCapacityUsed(toUseCapacity);
			
			if (usedCapacity > 0) {
				for (Link link: links) {
					link.addFlowPerDemand(demandName, usedCapacity);
				}
			}
			
			return toUseCapacity - usedCapacity;
		}
		
		public void clearDemand(String demandName) {
			for (Link link: links)
				link.removeCapacityUsed(demandName);
		}
		
		public boolean isUsed() {
			for (Link link: links)
				if (!link.isEmpty())
					return true;
			return false;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("\n###* " + getIndex() + " *###\n");
			sb.append("secondRack: " + getSecondRack() + "\n");
			for (Link link: links)
				sb.append(link);
			sb.append("\n");
			return sb.toString();
		}
		
	}
}
