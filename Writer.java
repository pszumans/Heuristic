import java.io.File;
import java.util.List;

public class Writer implements Parser.AMPLWriter {

	public static final int DEMANDS_KEY = 0;
	public static final int DATA_SERVERS_KEY = 1;
	public static final int COMP_SERVERS_KEY = 2;
	public static final int RACKS_KEY = 3;
	public static final int LINKS_KEY = 4;
	public static final int ARCS_KEY = 5;
	
	private final String DEMANDS = "REQUESTS";
	private final String DATA_SERVERS = "DATA_SERVERS";
	private final String COMP_SERVERS = "COMPUTING_SERVERS";
	private final String RACKS = "RACKS";
	private final String LINKS = "LINKS";
	private final String ARCS = "ARCS";
	
	private File file;
	private StringBuilder[] sb;
	
	public Writer(File file) {
		sb = new StringBuilder[6];
	}
	
	public Writer() {
		sb = new StringBuilder[6];
		for (int i = 0; i < 6; i++)
			sb[i] = new StringBuilder();

	}

	private void loadDataA(Object[] data, int type) {
		
		StringBuilder sb = new StringBuilder();
		
		switch (type) {
		case 1:
			for (int i = 0; i < data.length; i++) {
				sb.append(data[i].toString());
				if (i != data.length)
					sb.append(", ");
			}
			sb.append(";");
			break;
		case 2:
			for (int i = 0; i < data.length; i+=2) {
				sb.append("(");
				sb.append(data[i].toString());
				sb.append(", ");
				sb.append(data[i+1].toString());
				sb.append(")");
				if (i != data.length)
					sb.append(", ");
			}
			sb.append(";");
			break;
		}
	}
	
	public void loadData(String data, int dataType) {
		sb[dataType].append(data);
		sb[dataType].append(", ");
	}
	
	public void loadData(String data1, String data2, int dataType) {
		sb[dataType].append("(" + data1 + ", " + data2 + ", ");		
		
		sb[dataType + 1].append("(" + data1 + ", " + data2 + ", ");	
		sb[dataType + 1].append("(" + data2 + ", " + data1 + ", ");	

	}

	public void write() {
		for (int i = 0; i < sb.length; i++) {
			int start = sb[i].length() - 2;
			int end = sb[i].length() - 1;
			sb[i].replace(start, end, ";");
			sb[i].toString();
		}
	}
	
	public void loadDataB(String data, int dataType) {

		StringBuilder sb = new StringBuilder();
		
		switch (dataType) {
		case 0: 
			sb.append(DEMANDS_KEY);
			break;
		case 1:
			sb.append(DATA_SERVERS_KEY);
			sb.append(data);
			break;
		case 2:
			sb.append(COMP_SERVERS_KEY);
			sb.append(data);
			break;
		case 3:
			sb.append(RACKS_KEY);
			sb.append(data);
			break;
		case 4:
			sb.append(LINKS_KEY);
			sb.append(data);
			break;
		case 5:
			sb.append(ARCS_KEY);
			sb.append(data);
			break;
		}
		
	}
	
}
