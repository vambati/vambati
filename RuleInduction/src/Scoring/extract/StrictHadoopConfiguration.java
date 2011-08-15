package Scoring.extract;

import org.apache.hadoop.conf.Configuration;

public class StrictHadoopConfiguration {
	private Configuration conf;

	public StrictHadoopConfiguration(Configuration conf) {
		this.conf = conf;
	}
	
	public int getInt(String name) {
		String str = get(name);
		int n = Integer.parseInt(str);
		return n;
	}
	
	public boolean getBoolean(String name) {
		String str = get(name);
		boolean b = Boolean.parseBoolean(str);
		return b;
	}
	
	public String get(String name) {
		String str = conf.get(name);
		if(str == null) {
			throw new RuntimeException("Required option maxPhraseLength not set");
		}
		return str;
	}
}
