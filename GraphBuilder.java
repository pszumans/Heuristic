import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.SimpleWeightedGraph;


public class GraphBuilder extends JApplet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private static final Color DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 530, 320 );
	
	private SimpleWeightedGraph<Rack, Link> DataCenterGraph;
	private List<Rack> racks;
	private Set<Link> links;
	
	private Parser.AMPLWriter wr;

	public GraphBuilder(int racksNum, int linksNum, Parser.AMPLWriter wr) {
		setAMPLWriter(wr);
		racks = new ArrayList<Rack>();
		DataCenterGraph = getRandomGraph(racksNum, linksNum);
		setLinks();
		setRacks();
	}

	public GraphBuilder(List<Rack> racks, Set<Link> links) {
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
				Rack rack = new Rack(nr, size);
				racks.add(rack);
				return rack;
			}

		};
		rGG.generateGraph(graph, vF, new HashMap<>());
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
		for (Rack rack : racks) {
			loadData(new Object[]{rack.getName(), rack.getSize()}, Writer.RACKS_KEY);
			searchForPaths(rack, 1000);
		}
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
					loadData(new Object[]{sr1.getName(), sr2.getName(), link.getCapacity()}, Writer.LINKS_KEY);
				}
			}
		links = DataCenterGraph.edgeSet();
	}

	private void loadData(Object[] data, int dataType) {
		if (wr != null)
			wr.loadData(data, dataType);
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
	
	public void setAMPLWriter(Parser.AMPLWriter wr) {
		this.wr = wr;
	}
	
	public void draw() {
		initToDraw();
        JFrame frame = new JFrame("Graph");
        frame.getContentPane().add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
	}
	
    private void initToDraw(){
    	JGraphModelAdapter<Rack, Link> m_jgAdapter = new JGraphModelAdapter<Rack, Link>(DataCenterGraph);
        JGraph jgraph = new JGraph( m_jgAdapter );
        jgraph.getScale();
        adjustDisplaySettings( jgraph );
        getContentPane(  ).add( jgraph );
        resize( DEFAULT_SIZE );
        jgraph.setAutoResizeGraph(true);      
    }


    private void adjustDisplaySettings(JGraph jg) {
        jg.setPreferredSize(DEFAULT_SIZE);
        Color c = DEFAULT_BG_COLOR;
        String colorStr = null;
        try {
            colorStr = getParameter("bgcolor");
        }
         catch(Exception e) {}
        if (colorStr != null) {
            c = Color.decode(colorStr);
        }
        jg.setBackground(c);
    }
    
//    private void positionVertexAt(JGraphModelAdapter m_jgAdapter, Object vertex, int x, int y ) {
//        DefaultGraphCell cell = m_jgAdapter.getVertexCell( vertex );
//        Map              attr = cell.getAttributes(  );
//        Rectangle2D        b    = GraphConstants.getBounds( attr );
//        
//        int width = new Double(b.getWidth()).intValue();
//        int height =  new Double(b.getHeight()).intValue();
//
//        GraphConstants.setBounds( attr, new Rectangle( x, y, width, height));
//        Map cellAttr = new HashMap(  );
//        cellAttr.put( cell, attr );
//        m_jgAdapter.edit(cellAttr, null, null, null);
//    }
}
