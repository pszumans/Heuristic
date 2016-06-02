public class Demand {
	
	private final String DEMAND = "D";
	
	private final String name;
	private DataDemand dataDemand;
	private CompDemand compDemand;
	
	public Demand(int nr) {
		name = DEMAND + nr;
	}
	
	public Demand(String name) {
		this.name = name;
	}

	public Demand(Demand d) {
		this(d.getName(), d.getDataDemand().getStorage(), d.getCompDemand().getComputation());
	}
	
	public Demand(int nr, int storage, int computation) {
		name = DEMAND + nr;
		dataDemand = new DataDemand(name, storage);
		compDemand = new CompDemand(name, computation);
	}
	
	public Demand(String name, int storage, int computation) {
		this.name = name;
		dataDemand = new DataDemand(name, storage);
		compDemand = new CompDemand(name, computation);
	}
	
	public String getName() {
		return name;
	}

	public DataDemand getDataDemand() {
		return dataDemand;
	}

	public void setDataDemand(DataDemand dataDemand) {
		this.dataDemand = dataDemand;
	}

	public CompDemand getCompDemand() {
		return compDemand;
	}

	public void setCompDemand(CompDemand compDemand) {
		this.compDemand = compDemand;
	}

	public void addDataDemand(int storage) {
		dataDemand = new DataDemand(name, storage);
	}

	public void addCompDemand(int computation) {
		compDemand = new CompDemand(name, computation);
	}

	public class DataDemand {
		
		private final String demandName;
		private final int storage;
		private int storageServed;
		private int storageRemained;
//		private List<DataServer> usedDataServers;
		
		public DataDemand(String demandName, int storage) {
			this.demandName = demandName;
			this.storage = storage;
			storageServed = 0;
			storageRemained = storage;
		}

		public String getDemandName() {
			return demandName;
		}

		public int getStorage() {
			return storage;
		}

		public int getStorageServed() {
			return storageServed;
		}
//
		public void setStorageServed(int storageServed) {
			this.storageServed = storageServed;
			storageRemained = storage - storageServed;
		}
//
//		public void addDataServer(DataServer ds) {
//			usedDataServers.add(ds);
//		}

		public int getStorageRemained() {
			return storageRemained;
		}

		public void setStorageRemained(int storageRemained) {
			this.storageRemained = storageRemained;
			storageServed = storage - storageRemained;
		}
		
		public void serveStorage(int storage) {
			storageServed += storage;
			storageRemained -= storage;
		}
		
	}
	
	public class CompDemand {
		
		private final String demandName;
		private final int computation;
		private int computationServed;
		private int computationRemained;
//		private List<CompServer> usedCompServers;

		public CompDemand(String demandName, int computation) {
			this.demandName = demandName;
			this.computation = computation;
			computationServed = 0;
			computationRemained = computation;
		}

		public String getDemandName() {
			return demandName;
		}

		public int getComputation() {
			return computation;
		}

		public int getComputationServed() {
			return computationServed;
		}

		public void setComputationServed(int computationServed) {
			this.computationServed = computationServed;
			computationRemained = computation - computationServed;
		}
		
		public int getComputationRemained() {
			return computationRemained;
		}

		public void setComputationRemained(int computationRemained) {
			this.computationRemained = computationRemained;
			computationServed = computation - computationRemained;
		}
//
//		public void addCompServer(CompServer cs) {
//			usedCompServers.add(cs);
//		}
//		
//		public void removeCompServer(CompServer cs) {
//			usedCompServers.remove(cs);
//		}
	}

}
