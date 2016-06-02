import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jgrapht.graph.DefaultWeightedEdge;

public class Link extends DefaultWeightedEdge implements Comparator<Link> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String LINK = "L";
	
	private String name;
	private int capacity;
	private int capacityUsed;
	private int capacityRemained;
	private String[] racksLinked;
	private Map<String, Integer> flowPerDemand;

	public Link() {
		super();
		init(-1);
	}
	
	public Link(int nr, int capacity) {
		super();
		name = LINK + nr;
		init(capacity);
	}
	
	public Link (String name, int capacity) {
		super();
		this.name = name;
		init(capacity);
	}
	
	public Link (int capacity, String sr1, String sr2) {
		super();
		init(capacity);
		setRacksLinked(sr1, sr2);
	}
	
	private void init(int capacity) {
		setCapacity(capacity);
		racksLinked = new String[2];
		flowPerDemand = new HashMap<String, Integer>();
	}
	
	public void setRacksLinked(String sr1, String sr2) {
		racksLinked[0] = sr1;
		racksLinked[1] = sr2;
	}
	
	public String getRackName(int index) {
		return racksLinked[index];
	}
	
	public String getName() {
		return name;
	}

	public void setName(int nr) {
		name = LINK + nr;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
		this.capacityUsed = 0;
		this.capacityRemained = capacity;
	}

	public int getCapacityUsed() {
		return capacityUsed;
	}

	public void setCapacityUsed(int capacityUsed) {
		this.capacityUsed = capacityUsed;
		capacityRemained = capacity - capacityUsed;
	}

	public int getCapacityRemained() {
		return capacityRemained;
	}

	public void setCapacityRemained(int capacityRemained) {
		this.capacityRemained = capacityRemained;
		capacityUsed = capacity - capacityRemained;
	}
	
	public int addCapacityUsed(String demandName, int usingCapacity) {
		if (usingCapacity <= capacityRemained) {
			capacityUsed += usingCapacity;
			capacityRemained -= usingCapacity;
			flowPerDemand.put(demandName, usingCapacity);
			return 0;
		}
		else if (capacityRemained > 0) {
			int remainToAddStorage = usingCapacity - capacityRemained;
			flowPerDemand.put(demandName, capacityRemained);
			capacityUsed = capacity;
			capacityRemained = 0;
			return remainToAddStorage;
		}
		return usingCapacity;
	}
	
	public int checkCapacityUsed(int toUseCapacity) {
		if (toUseCapacity <= capacityRemained) {
			return toUseCapacity;
		}
		else if (capacityRemained > 0) {
			return toUseCapacity - capacityUsed;
		}
		return 0;
	}
	
	public void addFlowPerDemand(String demandName, int usingCapacity) {
		capacityUsed += usingCapacity;
		capacityRemained -= usingCapacity;
		if (!flowPerDemand.containsKey(demandName))
			flowPerDemand.put(demandName, usingCapacity);
		else {
			int value = flowPerDemand.get(demandName);
			value += usingCapacity;
			flowPerDemand.put(demandName, value);
		}
	}
	
	public void removeCapacityUsed(String demandName) {
		if (flowPerDemand.containsKey(demandName)) {
			int storage = flowPerDemand.get(demandName);
			flowPerDemand.remove(demandName);
			capacityUsed -= storage;
			capacityRemained += storage;
		}
	}
	
	public boolean containsDemand(String demandName) {
		return flowPerDemand.containsKey(demandName);
	}
	
	public int getFlowByDemand(String demandName) {
		if (flowPerDemand.containsKey(demandName))
			return flowPerDemand.get(demandName);
		return 0;

	}
	
	public boolean isEmpty() {
		return flowPerDemand.isEmpty();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n### " + getName());
		sb.append(" (" + getRackName(0) + ", " + getRackName(1) + ")");
		sb.append(" ###\n");
		sb.append("capacity (used +remained): " + getCapacity());
		sb.append(" (" + getCapacityUsed() + " +" + getCapacityRemained() + ")");
		for (String key: flowPerDemand.keySet()) {
			sb.append("\n###- " + key + " -###\n");
			sb.append("flow used: " + flowPerDemand.get(key));
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public void customize(int nr, String sr1, String sr2) {
		setName(nr);
		setRacksLinked(sr1, sr2);
		if (capacity == -1) {
			int rand = new Random().nextInt(5) + 10;
			setCapacity(rand);
		}
	}

	@Override
	public int compare(Link l1, Link l2) {
		return l1.capacityRemained - l2.capacityRemained;
	}
	

}
