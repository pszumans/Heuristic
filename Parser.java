import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

public class Parser {

	private final String LINKS_KEY = "edge_capacity";
	private final String DEMANDS_KEY = "s_request";
	private final String COMP_SERVERS_KEY = "cs_power";
	private final String DATA_SERVERS_KEY = "ds_storage";
	private final String RACKS_KEY = "sr_size";
	private final String MIN_DEMANDS = "min_requests_served";
	
	private final int COMP = 0;
	private final int DATA = 1;
	
	private List<Demand> demands;
	private List<Rack> racks;
	private Set<Link> links;
	private List<DataServer> dataServers;
	private List<CompServer> compServers;
	private int minDemands;
	
	private AMPLWriter wr;
	
	private File file;
	private Scanner sc;

	public Parser(String filename) throws FileNotFoundException {
		file = new File(filename);
	}

	public void parse() throws FileNotFoundException {

		String data = null;
		sc = new Scanner(new BufferedReader(new FileReader(file)));
		while (sc.hasNextLine()) {
			if (sc.hasNext())
				data = sc.next();
			if (data.matches("param:*"))
				addData(sc.next());
			else
				sc.nextLine();
		}
		sc.close();
	}

	public void addData(String dataKey) {

		String head = null;
		
		switch (dataKey) {
		case LINKS_KEY:
			head = dataKey + sc.nextLine();
			loadDataHead(head, Writer.LINKS_KEY);
			parseLinks();
			break;
		case DEMANDS_KEY:
			head = dataKey + sc.nextLine();
			loadDataHead(head, Writer.DEMANDS_KEY);
			parseDemands();
			break;
		case COMP_SERVERS_KEY:
			head = dataKey + sc.nextLine();
			loadDataHead(head, Writer.COMP_SERVERS_KEY);
			parseServers(COMP);
			break;
		case DATA_SERVERS_KEY:
			head = dataKey + sc.nextLine();
			loadDataHead(head, Writer.DATA_SERVERS_KEY);
			parseServers(DATA);
			break;
		case RACKS_KEY:
			head = dataKey + sc.nextLine();
			loadDataHead(head, Writer.RACKS_KEY);
			parseRacks();
			break;
		case MIN_DEMANDS:
			loadDataHead(dataKey, Writer.EXTRA_KEY);
			parseMinDemands();
			break;
		}
	}
	
	private void loadDataHead(String headLine, int dataType) {
		if (wr != null)
			wr.loadDataHead(headLine, dataType);
	}

	private void parseDemands() {
		demands = new ArrayList<Demand>();
		while (true) {
			String name = sc.next();
			if (name.charAt(0) == '#') {
				sc.nextLine();
				continue;
			}
			else if (name.equals(";"))
				break;
			int storage = sc.nextInt();
			int power = -1;
			if (sc.hasNext(Pattern.compile("\\d+;"))) {
				power = Integer.parseInt(sc.next().replace(";",""));
				demands.add(new Demand(name, storage, power));
				loadData(new Object[]{name, storage, power}, Writer.DEMANDS_KEY);
				break;
			}
			else {
				power = sc.nextInt();
			}
			demands.add(new Demand(name, storage, power));
			loadData(new Object[]{name, storage, power}, Writer.DEMANDS_KEY);
			sc.nextLine();
		}
	}
	
	private void parseServers(int type) {
		if (type == COMP)
			compServers = new ArrayList<CompServer>();
		else
			dataServers = new ArrayList<DataServer>();
		while (true) {
			String name = sc.next();
			if (name.charAt(0) == '#') {
				sc.nextLine();
				continue;
			}
			else if (name.equals(";"))
				break;
			int serverSpec = sc.nextInt();
			int cost = sc.nextInt();
			int size = -1;
			if (sc.hasNext(Pattern.compile("\\d+;"))) {
				size = Integer.parseInt(sc.next().replace(";",""));
				if (type == COMP) {
					compServers.add(new CompServer(name, serverSpec, cost, size));
					loadData(new Object[]{name, serverSpec, cost, size}, Writer.COMP_SERVERS_KEY);
				}
				else {
					dataServers.add(new DataServer(name, serverSpec, cost, size));
					loadData(new Object[]{name, serverSpec, cost, size}, Writer.DATA_SERVERS_KEY);
				}
				break;
			}
			else {
				size = sc.nextInt();
			}
			if (type == COMP) {
				compServers.add(new CompServer(name, serverSpec, cost, size));
				loadData(new Object[]{name, serverSpec, cost, size}, Writer.COMP_SERVERS_KEY);
			}
			else {
				dataServers.add(new DataServer(name, serverSpec, cost, size));
				loadData(new Object[]{name, serverSpec, cost, size}, Writer.DATA_SERVERS_KEY);
			}
			if (sc.hasNextLine())
				sc.nextLine();
		}
	}
	
	private void parseRacks() {
		racks = new ArrayList<Rack>();
		while (true) {
			String name = sc.next();
			if (name.charAt(0) == '#') {
				sc.nextLine();
				continue;
			}
			else if (name.equals(";"))
				break;
			int size = -1;
			if (sc.hasNext(Pattern.compile("\\d+;"))) {
				size = Integer.parseInt(sc.next().replace(";",""));
				racks.add(new Rack(name, size));
//				loadData(new Object[]{name, size}, Writer.RACKS_KEY);
				break;
			}
			else {
				size = sc.nextInt();
			}
			racks.add(new Rack(name, size));
//			loadData(new Object[]{name, size}, Writer.RACKS_KEY);
		}
		if (sc.hasNextLine())
			sc.nextLine();
	}
	
	private void parseMinDemands() {
		while (true) {
			if (sc.hasNext(Pattern.compile("\\d+;"))) {
				minDemands = Integer.parseInt(sc.next().replace(";",""));
				loadData(new Object[]{minDemands}, Writer.EXTRA_KEY);
				break;
			}
			else if (sc.hasNext(Pattern.compile("\\d+"))) {
				minDemands = sc.nextInt();
				loadData(new Object[]{minDemands}, Writer.EXTRA_KEY);
				break;
			}
			else {
				sc.next();
			}
		}
	}
	
	private void parseLinks() {
		links = new HashSet<Link>();
		while (true) {
			String sr1 = sc.next();
			if (sr1.charAt(0) == '#') {
				sc.nextLine();
				continue;
			}
			else if (sr1.equals(";"))
				break;
			String sr2 = sc.next();
			int capacity = -1;
			if (sc.hasNext(Pattern.compile("\\d+;"))) {
				capacity = Integer.parseInt(sc.next().replace(";",""));
				links.add(new Link(capacity, sr1, sr2));
//				loadData(new Object[]{sr1, sr2, capacity}, Writer.LINKS_KEY);
				break;
			}
			else {
				capacity = sc.nextInt();
			}
			links.add(new Link(capacity, sr1, sr2));
//			loadData(new Object[]{sr1, sr2, capacity}, Writer.LINKS_KEY);
		}
		if (sc.hasNextLine())
			sc.nextLine();
	}
	
	private void loadData(Object[] data, int dataType) {
		if (wr != null)
			wr.loadData(data, dataType);
		
	}

	public interface AMPLWriter {
		public void loadData(Object[] data, int dataType);
		public void loadDataHead(String headLine, int dataType);
	}
	
	public void setAMPLWriter(AMPLWriter wr) {
		this.wr = wr;
	}
	
	public List<Demand> getDemands() {
		return demands;
	}

	public List<Rack> getRacks() {
		return racks;
	}

	public Set<Link> getLinks() {
		return links;
	}

	public List<DataServer> getDataServers() {
		return dataServers;
	}

	public List<CompServer> getCompServers() {
		return compServers;
	}

	public int getMinDemands() {
		return minDemands;
	}

	public void setFile(String filename) {
		file = new File(filename);
	}

	public File getFile() {
		return file;
	}
	
}
