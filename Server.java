
public class Server {

	protected final int cost;
	protected final int size;
	protected String name;
	
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

}
