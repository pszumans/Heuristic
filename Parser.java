import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
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
//	private final String[] KEYS = {LINKS_KEY, DEMANDS_KEY, COMP_SERVERS_KEY, DATA_SERVERS_KEY, RACKS_KEY};
	
	private List<Demand> demands;
	private List<Rack> racks;
	private Set<Link> links;
	private List<DataServer> dataServers;
	private List<CompServer> compServers;
	private int minDemands;
	
	private AMPLWriter wr;
	
	private File file;// = new File(filename);
	private Scanner s;// = new Scanner(file);

	public Parser(String filename) throws FileNotFoundException {
		file = new File(filename);
	}

	public void parse() throws FileNotFoundException {

		s = new Scanner(new BufferedReader(new FileReader(file)));
		while (s.hasNextLine()) {
			String data = s.next();
			if (data.charAt(0) != '#')
				addData(data);
			else
				s.nextLine();
		}
//		System.out.println(demands.get(0));
	}

	public void addData(String dataKey) {

		switch (dataKey) {
		case LINKS_KEY:
			parseLinks();
			break;
		case DEMANDS_KEY:
			parseDemands();
			break;
		case COMP_SERVERS_KEY:
			parseServers(COMP);
			break;
		case DATA_SERVERS_KEY:
			parseServers(DATA);
			break;
		case RACKS_KEY:
			parseRacks();
			break;
		case MIN_DEMANDS:
			parseMinDemands();
			break;
		}
	}
	
	private void parseDemands() {
		demands = new ArrayList<Demand>();
		while (true) {
			s.nextLine();
			String name = s.next();
			if (name.charAt(0) == '#')
				continue;
			else if (name.equals(";"))
				break;
			int storage = s.nextInt();
			int power = -1;
			if (s.hasNext(Pattern.compile("\\d+;"))) {
				power = Integer.parseInt(s.next().replace(";",""));
				demands.add(new Demand(name, storage, power));
				wr.loadData(name, Writer.DEMANDS_KEY);
				break;
			}
			else {
				power = s.nextInt();
			}
			wr.loadData(name, Writer.DEMANDS_KEY);
			demands.add(new Demand(name, storage, power));
		}
	}
	
	private void parseServers(int type) {
		if (type == COMP)
			compServers = new ArrayList<CompServer>();
		else
			dataServers = new ArrayList<DataServer>();
		while (true) {
			s.nextLine();
			String name = s.next();
			if (name.charAt(0) == '#')
				continue;
			else if (name.equals(";"))
				break;
			int serverSpec = s.nextInt();
			int cost = s.nextInt();
			int size = -1;
			if (s.hasNext(Pattern.compile("\\d+;"))) {
				size = Integer.parseInt(s.next().replace(";",""));
				if (type == COMP) {
					compServers.add(new CompServer(name, serverSpec, cost, size));
					wr.loadData(name, Writer.COMP_SERVERS_KEY);
				}
				else {
					dataServers.add(new DataServer(name, serverSpec, cost, size));
					wr.loadData(name, Writer.DATA_SERVERS_KEY);
				}
				break;
			}
			else {
				size = s.nextInt();
			}
			if (type == COMP) {
				compServers.add(new CompServer(name, serverSpec, cost, size));
				wr.loadData(name, Writer.COMP_SERVERS_KEY);
			}
			else {
				dataServers.add(new DataServer(name, serverSpec, cost, size));
				wr.loadData(name, Writer.DATA_SERVERS_KEY);
			}
		}
	}
	
	private void parseRacks() {
		racks = new ArrayList<Rack>();
		while (true) {
			s.nextLine();
			String name = s.next();
			if (name.charAt(0) == '#')
				continue;
			else if (name.equals(";"))
				break;
			int size = -1;
			if (s.hasNext(Pattern.compile("\\d+;"))) {
				size = Integer.parseInt(s.next().replace(";",""));
				racks.add(new Rack(name, size));
				wr.loadData(name, Writer.RACKS_KEY);
				break;
			}
			else {
				size = s.nextInt();
			}
			racks.add(new Rack(name, size));
			wr.loadData(name, Writer.RACKS_KEY);
		}
	}
	
	private void parseMinDemands() {
		while (true) {
			if (s.hasNext(Pattern.compile("\\d+;"))) {
				minDemands = Integer.parseInt(s.next().replace(";",""));
				break;
			}
			else if (s.hasNext(Pattern.compile("\\d+"))) {
				minDemands = s.nextInt();
				break;
			}
			else {
				s.next();
			}
		}
	}
	
	private void parseLinks() {
		links = new HashSet<Link>();
		while (true) {
			s.nextLine();
			String sr1 = s.next();
			if (sr1.charAt(0) == '#')
				continue;
			else if (sr1.equals(";"))
				break;
			String sr2 = s.next();
			int capacity = -1;
			if (s.hasNext(Pattern.compile("\\d+;"))) {
				capacity = Integer.parseInt(s.next().replace(";",""));
				links.add(new Link(capacity, sr1, sr2));
				wr.loadData(sr1, sr2, Writer.LINKS_KEY);
				break;
			}
			else {
				capacity = s.nextInt();
			}
			links.add(new Link(capacity, sr1, sr2));
			wr.loadData(sr1, sr2, Writer.LINKS_KEY);
		}
	}
	
	public interface AMPLWriter {
		public void loadData(String data, int dataType);
		public void loadData(String data1, String data2, int dataType);
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
