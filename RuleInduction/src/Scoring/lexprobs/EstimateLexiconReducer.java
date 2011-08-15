package Scoring.lexprobs;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

import Utils.MyUtils;

public class EstimateLexiconReducer extends Reducer<Text, Text, Text, Text> {

	private Counter recordsRead;

	@Override
	public void setup(Context context) {
		recordsRead = context.getCounter("COUNT", "Records Read by Reducer");
	}

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
			InterruptedException {

		String[] columns = MyUtils.split(key.toString(), " ", Integer.MAX_VALUE);
		if(columns.length != 2) {
			throw new RuntimeException("Expected 2 columns, but got " + columns.length + ": " + key.toString());
		}
		
		String tgsOrSgt = columns[0];
		String denominatorWord = columns[1];
		
		Map<String, Float> numeratorPairs = new TreeMap<String, Float>();
		float sum = 0; 
		for (Text tValue : values) {
			recordsRead.increment(1);
			
			String[] cols = MyUtils.split(tValue.toString(), " ", Integer.MAX_VALUE);
			if(cols.length != 2) {
				throw new RuntimeException("Expected 2 columns, but got " + cols.length + ": " + tValue.toString());
			}
			
			String numeratorWord = cols[0];
			float occurrences = Float.parseFloat(cols[1]);
			
			increment(numeratorPairs, numeratorWord, occurrences);
			sum += occurrences;
		}

		for (Entry<String, Float> numeratorPair : numeratorPairs.entrySet()) {
			float prob = numeratorPair.getValue() / sum;
			String line = tgsOrSgt + "\t" + numeratorPair.getKey() + " " + denominatorWord + " " + prob;
			context.write(new Text(line), new Text());
		}
	}

	private static void increment(Map<String, Float> numeratorWords, String numeratorWord,
			float occurrences) {
		
		Float prevValue = numeratorWords.get(numeratorWord);
		if(prevValue == null) {
			prevValue = 0.0f;
		}
		numeratorWords.put(numeratorWord, prevValue + occurrences);
	}
}
