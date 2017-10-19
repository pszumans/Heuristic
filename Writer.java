import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Writer implements Parser.AMPLWriter {

	public static final int DEMANDS_KEY = 0;
	public static final int DATA_SERVERS_KEY = 1;
	public static final int COMP_SERVERS_KEY = 2;
	public static final int RACKS_KEY = 3;
	public static final int LINKS_KEY = 4;
	public static final int EXTRA_KEY = 5;
	
	private final String DEMANDS = "REQUESTS";
	private final String DATA_SERVERS = "DATA_SERVERS";
	private final String COMP_SERVERS = "COMPUTING_SERVERS";
	private final String RACKS = "RACKS";
	private final String LINKS = "LINKS";
	private final String ARCS = "ARCS";
	private final String SETS[] = {DEMANDS, DATA_SERVERS, COMP_SERVERS, RACKS, LINKS, ARCS};

	
	private File file;
	private PrintWriter pw;
	
	private StringBuilder[] head;
	private StringBuilder[] data;
	
	public Writer(File file) {
		this.file = file;
		init();
	}
	
	public Writer(String filename) {
		file = new File(filename);
		init();
	}

	private void init() {
		head = new StringBuilder[6];
		data = new StringBuilder[6];
		for (int i = 0; i < 6; i++) {
			head[i] = new StringBuilder();
			data[i] = new StringBuilder();
		}
		appendSet();
	}
	
	
	@Override
	public void loadDataHead(String headLine, int dataType) {
		data[dataType].append("param");
		if (dataType != EXTRA_KEY)
			data[dataType].append(":");
		data[dataType].append(" " + headLine);
		if (dataType == EXTRA_KEY)
			data[dataType].append(" := ");
		else
			data[dataType].append("\n");
	}
	
	@Override
	public void loadData(Object[] dataLine, int dataType) {
		if (dataType < LINKS_KEY)
			loadHead(dataLine[0], dataType);
		else if (dataType == LINKS_KEY)
			loadHead(dataLine[0], dataLine[1], dataType);
		for (int i = 0; i < dataLine.length; i++) {
			data[dataType].append(dataLine[i]);
			data[dataType].append(" ");
		}
		data[dataType].append("\n");
	}
	
	public void loadHead(Object headLine, int dataType) {
		head[dataType].append(headLine);
		head[dataType].append(", ");
	}
	
	public void loadHead(Object head1, Object head2, int dataType) {
		head[dataType].append("(" + head1 + ", " + head2 + "), ");		
		
		head[dataType + 1].append("(" + head1 + ", " + head2 + "), ");	
		head[dataType + 1].append("(" + head2 + ", " + head1 + "), ");	

	}

	public void write() throws IOException {
		pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		for (int i = 0; i < head.length; i++) {
			changeEnd(head[i]);
			head[i].toString();
			pw.write(head[i].toString());
		}
		
		for (int i = 0; i < data.length; i++) {
			data[i].append(";\n\n");
			data[i].toString();
			pw.write(data[i].toString());
		}
		
		pw.close();
	}
	
	private void changeEnd(StringBuilder sb) {
		int start = sb.length() - 2;
		int end = sb.length();
		sb.replace(start, end, ";\n");
		if (sb == head[head.length - 1])
			sb.append("\n");
	}

	private void appendSet() {
		for (int i = 0; i < SETS.length; i++) {
			head[i].append("set ");
			head[i].append(SETS[i]);
			head[i].append(" := ");
		}
	}
	
}
