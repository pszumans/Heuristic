import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.SimpleWeightedGraph;

public class GraphBuilder {

	private SimpleWeightedGraph<Rack, Link> DataCenterGraph;
	private List<Rack> racks;
	private Set<Link> links;

	public GraphBuilder(int racksNum, int linksNum) {
		racks = new ArrayList<Rack>();
		DataCenterGraph = getRandomGraph(racksNum, linksNum);
		setLinks();
		setRacks();
	}

	public GraphBuilder(List<Rack> racks, Set<Link> links, File file) {
		this.racks = racks;
		DataCenterGraph = getGraph(racks, links);
		setLinks();
		setRacks();
	}
	
	public List<Rack> getRacks() {
		return racks;
	}

	public Set<Link> getLinks() {
		return links;
	}

	private SimpleWeightedGraph<Rack, Link> getRandomGraph(int racksNum, int linksNum) {
		racks.removeAll(racks);
		SimpleWeightedGraph<Rack, Link> graph = new SimpleWeightedGraph<Rack, Link>(Link.class);
		ConnectivityInspector<Rack, Link> cI = new ConnectivityInspector<>(graph);
		RandomGraphGenerator<Rack, Link> rGG = new RandomGraphGenerator<Rack, Link>(racksNum, linksNum);
		VertexFactory<Rack> vF = new VertexFactory<Rack>() {

			@Override
			public Rack createVertex() {
				int nr = racks.size() + 1;
				int size = new Random().nextInt(10) + 5;
				racks.add(new Rack(nr, size));
				return racks.get(racks.size() - 1);
			}

		};
		rGG.generateGraph(graph, vF, new HashMap<>());
		System.out.println(!cI.isGraphConnected());
//		}
		if (cI.isGraphConnected())
			return graph;
		else
			return getRandomGraph(racksNum, linksNum);

	}
	
	private SimpleWeightedGraph<Rack, Link> getGraph(List<Rack> racks, Set<Link> links) {
		SimpleWeightedGraph<Rack, Link> graph = new SimpleWeightedGraph<Rack, Link>(Link.class);
		for (Rack rack: racks)
			graph.addVertex(rack);
		for (Link link: links)
			graph.addEdge(getRackByName(link.getRackName(0)), getRackByName(link.getRackName(1)), link);
		return graph;
	}

	private void setRacks() {
		for (Rack rack : racks)
			searchForPaths(rack, 1000);
	}

	private void setLinks() {
		int nr = 1;
		for (int i = 0; i < racks.size(); i++)
			for (int j = i + 1; j < racks.size(); j++) {
				Rack sr1 = racks.get(i);
				Rack sr2 = racks.get(j);
				if (DataCenterGraph.containsEdge(sr1, sr2)) {
					DataCenterGraph.getEdge(sr1, sr2).customize(nr++, sr1.getName(), sr2.getName());
					Link link = DataCenterGraph.getEdge(sr1, sr2);
					DataCenterGraph.setEdgeWeight(link, new Double(1.0 / link.getCapacity()));
				}
			}
		links = DataCenterGraph.edgeSet();
		System.out.println();
	}

	private Rack getRackByName(String rackName) {
		for (Rack rack: racks)
			if (rack.getName().equals(rackName))
				return rack;
		return null;
	}
	
	private void searchForPaths(Rack sr1, int k) {
		KShortestPaths<Rack, Link> shortestPaths;
		while (true)
			try {
				shortestPaths = new KShortestPaths<Rack, Link>(DataCenterGraph, sr1, k);
				break;
			} catch (IllegalArgumentException e) {
				k--;
		}
		for (Rack sr2 : racks) {
			if (sr2 != sr1) {
				List<GraphPath<Rack, Link>> paths = shortestPaths.getPaths(sr2);
				for (int i = 0; i < paths.size(); i++) {
					sr1.addPath(sr2.getName(), sr1.new Path(i, sr2.getName(), paths.get(i).getEdgeList()));
				}
			}
		}
	}
	
}
