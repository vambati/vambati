package Scoring.mle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

import Utils.MyUtils;

import com.sun.tools.javac.util.Pair;

public class SyntaxPrioReducer extends Reducer<Text, Text, Text, Text> {

	private boolean doSyntaxPrio;
	private long minCount;

	@Override
	public void setup(Context context) {

		String strMinCount = context.getConfiguration().get("minCount");
		if (strMinCount == null) {
			throw new RuntimeException("Required param not found: minCount");
		}
		this.minCount = Long.parseLong(strMinCount);

		String strDoSyntaxPrio = context.getConfiguration().get("doSyntaxPrio");
		if (strDoSyntaxPrio == null) {
			throw new RuntimeException("Required param not found: doSyntaxPrio");
		}
		this.doSyntaxPrio = Boolean.parseBoolean(strDoSyntaxPrio);
	}

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		
		Counter lowCounts = context.getCounter("COUNT", "Low Count Phrase Pairs Kept for Coverage");
		Counter nonsyntacticRead = context.getCounter("COUNT", "Non-Syntactic Phrase Pairs Read");
		Counter syntacticRead = context.getCounter("COUNT", "Syntactic Phrase Pairs Read");


		// Determine if removing phrase pairs below the minCount threshold
		// would result in losing source coverage

		List<Pair<String, Long>> syntactic = new ArrayList<Pair<String, Long>>();
		List<Pair<String, Long>> nonsyntactic = new ArrayList<Pair<String, Long>>();

		boolean minCountWillReduceCoverage = true;

		for(Text tValue : values) {
			String value = tValue.toString().trim();
			String[] fields = MyUtils.split(value.toString().trim(), SuffStatMapper.DELIM, Integer.MAX_VALUE);
			if (fields.length != 6) {
				throw new RuntimeException("Got " + fields.length
						+ " fields for line, but was expecting 6: " + value);
			}

			String type = fields[0];
			String srcCateg = fields[1];
			String tgtCateg = fields[2];
			String srcPhr = fields[3];
			String tgtPhr = fields[4];
			long pairCount = Long.parseLong(fields[5]);

			if (pairCount >= minCount) {
				minCountWillReduceCoverage = false;
				lowCounts.increment(1);
			}

			if (srcCateg.equals("PHR")) {
				nonsyntactic.add(new Pair<String, Long>(value, pairCount));
				nonsyntacticRead.increment(1);
			} else {
				syntactic.add(new Pair<String, Long>(value, pairCount));
				syntacticRead.increment(1);
			}
		} // end read values

		for (Pair<String, Long> pair : syntactic) {
			String value = pair.fst;
			long pairCount = pair.snd;

			if (minCountWillReduceCoverage || pairCount >= minCount) {
				// pairCount already in value
				context.write(new Text(value), new Text(""));
			}
		}

		// If doing syntax prioritization, only output non-syntactic phrases
		// when there aren't any syntactic phrases
		if (doSyntaxPrio == false || syntactic.size() == 0) {
			for (Pair<String, Long> pair : nonsyntactic) {
				String value = pair.fst;
				long pairCount = pair.snd;

				// Don't do count pruning on non-syntactic phrases
				// pairCount already in value
				context.write(new Text(value), new Text(""));
			}
		}

		// TODO: Pass along alignment here
	}
}
