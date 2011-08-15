package Scoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import Utils.MyUtils;

public class IndexManager {
	public final List<String> indices = new ArrayList<String>();

	public static IndexManager readFile(File f) throws IOException {
		String line = MyUtils.readLine(f);
		return readLine(line);
	}

	public static IndexManager readLine(String serializedLine) {
		IndexManager man = new IndexManager();
		String[] features = MyUtils.tokenize(serializedLine, " ");
		for (String feat : features) {
			man.add(feat);
		}
		return man;
	}

	public void writeFile(File f) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(f);
		String line = toString();
		out.println(line);
		out.close();
	}

	public void add(String featureName) {
		indices.add(featureName);
	}
	
	public boolean contains(String name) {
		return indices.contains(name);
	}

	public int get(String name) {
		// this lookup is reasonably fast only since we should never have more
		// than about 100 features
		int i = indices.indexOf(name);
		if (i < 0) {
			throw new RuntimeException("No such index name: " + name);
		}
		return i;
	}

	public int size() {
		return indices.size();
	}

	public String toString() {
		return MyUtils.untokenize(" ", MyUtils.toArray(indices));
	}
}
