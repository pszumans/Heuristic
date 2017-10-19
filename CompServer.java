import java.util.HashMap;
import java.util.Map;

public class CompServer extends Server {

	private final String COMP_SERVER = "CS";
	
	private int power;
	private int powerUsed;
	private int powerRemained;
	private Map<String, Integer> powerPerDemand;

	public CompServer(CompServer cs) {
		this(cs.getName(), cs.getPower(), cs.getCost(), cs.getSize());
	}
	
	public CompServer(int nr, int power, int cost, int size) {
		super(cost, size);
		name = COMP_SERVER + nr;
		init(power);
}
	
	public CompServer(String name, int power, int cost, int size) {
		super(name, cost, size);
		init(power);
	}

	private void init(int power) {
		this.power = power;
		powerUsed = 0;
		powerRemained = power;
		powerPerDemand = new HashMap<String, Integer>();
	
	}
	
	public int getPower() {
		return power;
	}

	public int getPowerUsed() {
		return powerUsed;
	}

	public void setPowerUsed(int powerUsed) {
		this.powerUsed = powerUsed;
		powerRemained = power - powerUsed;
	}

	public int getPowerRemained() {
		return powerRemained;
	}

	public void setPowerRemained(int powerRemained) {
		this.powerRemained = powerRemained;
		powerUsed = power - powerRemained;
	}

	public int addPowerUsed(String demandName, int addingPower) {
		if (addingPower <= powerRemained) {
			powerUsed += addingPower;
			powerRemained -= addingPower;
			powerPerDemand.put(demandName, addingPower);
			return 0;
		}
		else if (powerRemained > 0) {
			int remainToAddPower = addingPower - powerRemained;
			powerPerDemand.put(demandName, powerRemained);
			powerUsed = power;
			powerRemained = 0;
			return remainToAddPower;
		}
		else
			return addingPower;
	}
	
	public void removeCompDemand(String demandName) {
		if (powerPerDemand.containsKey(demandName)) {
			int power = powerPerDemand.get(demandName);
			powerPerDemand.remove(demandName);
			powerUsed -= power;
			powerRemained += power;
		}
	}
	
	public void transferDemands(CompServer cs) {
		for (String demandName: powerPerDemand.keySet())
			cs.addPowerUsed(demandName, powerPerDemand.get(demandName));		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n#### " + getName() + " ###\n");
		sb.append("#### power: " + getPower());
		sb.append(", cost: " + getCost());
		sb.append(", size: " + getSize());
		for (String key: powerPerDemand.keySet()) {
			sb.append("\n##### " + key + " ##\n");
			sb.append("##### computation served: " + powerPerDemand.get(key));
		}
		sb.append("\n");
		return sb.toString();
		
	}

	public boolean isEmpty() {
		return powerPerDemand.isEmpty();
	}

}
