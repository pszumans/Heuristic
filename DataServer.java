import java.util.HashMap;
import java.util.Map;

public class DataServer extends Server {

	private final String dataServerName = "DS";
	
	private int storage;
	private int storageUsed;
	private int storageRemained;
	private Map<String, Integer> storagePerDemand;

	
	public DataServer(DataServer ds) {
		this(ds.getName(), ds.getStorage(), ds.getCost(), ds.getSize());
	}

	public DataServer(int nr, int storage, int cost, int size) {
		super(cost, size);
		name = dataServerName + nr;
		init(storage);
	}
	
	public DataServer(String name, int storage, int cost, int size) {
		super(name, cost, size);
		init(storage);
	}
	
	private void init(int storage) {
		this.storage = storage;
		storageUsed = 0;
		storageRemained = storage;
		storagePerDemand = new HashMap<String, Integer>();
	}

	public int getStorage() {
		return storage;
	}

	public int getStorageUsed() {
		return storageUsed;
	}

	public void setStorageUsed(int storageUsed) {
		this.storageUsed = storageUsed;
		storageRemained = storage - storageUsed;
	}

	public int getStorageRemained() {
		return storageRemained;
	}

	public void setStorageRemained(int storageRemained) {
		this.storageRemained = storageRemained;
		storageUsed = storage - storageRemained;
	}

	public int addStorageUsed(String demandName, int addingStorage) {
		if (addingStorage <= storageRemained) {
			storageUsed += addingStorage;
			storageRemained -= addingStorage;
			storagePerDemand.put(demandName, addingStorage);
			return 0;
		}
		else if (storageRemained > 0) {
			int remainToAddStorage = addingStorage - storageRemained;
			storagePerDemand.put(demandName, storageRemained);
			storageUsed = storage;
			storageRemained = 0;
			return remainToAddStorage;
		}
		else 
			return addingStorage;
	}
	
	public void removeDataDemand(String demandName) {
		if (storagePerDemand.containsKey(demandName)) {
			int storage = storagePerDemand.get(demandName);
			storagePerDemand.remove(demandName);
			storageUsed -= storage;
			storageRemained += storage;
		}
	}
	
	public void transferDemands(DataServer ds) {
		for (String demandName: storagePerDemand.keySet())
			ds.addStorageUsed(demandName, storagePerDemand.get(demandName));
	}
	
	public boolean isEmpty() {
		return storagePerDemand.isEmpty();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n#### " + getName() + " ##\n");
		sb.append("#### storage: " + getStorage());
		sb.append(", cost: " + getCost());
		sb.append(", size: " + getSize());
		for (String key: storagePerDemand.keySet()) {
			sb.append("\n##### " + key + " #\n");
			sb.append("##### storage served: " + storagePerDemand.get(key));
		}
		sb.append("\n");
		return sb.toString();
		
	}

}
