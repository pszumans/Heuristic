import java.util.Comparator;

public class Server implements Comparator<Server> {

	protected final int cost;
	protected final int size;
	protected String name;
	
	public Server() {
		cost = -1;
		size = -1;
	}
	
	public Server(int cost, int size) {
		this.cost = cost;
		this.size = size;
	}

	public Server(String name, int cost, int size) {
		this.name = name;
		this.cost = cost;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public int getCost() {
		return cost;
	}

	public int getSize() {
		return size;
	}

	@Override
	public int compare(Server s1, Server s2) {
		return s1.cost - s2.cost;
	}
	
}
