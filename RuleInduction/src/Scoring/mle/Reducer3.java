package Scoring.mle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

import Utils.MyUtils;

public class Reducer3 extends Reducer<Text, Text, Text, Text> {

	private int smoothCount;

	@Override
	public void setup(Context context) {
		String strSmoothCount = context.getConfiguration().get("smoothCount");
		if (strSmoothCount == null) {
			throw new RuntimeException("Required param not found: strSmoothCount");
		}
		this.smoothCount = Integer.parseInt(strSmoothCount);
	}

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		
		Counter uniquePhrasePairs = context.getCounter("COUNT", "Unique Phrase Pairs");
		Counter phraseInstancesRead = context.getCounter("COUNT", "Phrase Instances Read");

		// determine the total count
		long srcCount = 0;
		List<String[]> myItemList = new ArrayList<String[]>();
		for(Text tValue : values) {
			String value = tValue.toString().trim();
			String[] fields = MyUtils.split(value, SuffStatMapper.DELIM, Integer.MAX_VALUE);
			if (fields.length != 7) {
				throw new RuntimeException("Got " + fields.length
						+ " fields for line, but was expecting 7: " + value);
			}

			String type = fields[0].trim();
			String srcCateg = fields[1].trim();
			String tgtCateg = fields[2].trim();
			String srcPhr = fields[3].trim();
			String tgtPhr = fields[4].trim();
			long pairCount = Long.parseLong(fields[5].trim());
			String tgtCount = fields[6].trim();

			srcCount += pairCount;
			myItemList.add(fields);
		}

		// global counter, ambiguityFactor, scalingFactors, filterPrune,
		// sourcePhrases
		
		uniquePhrasePairs.increment(1);
		phraseInstancesRead.increment(srcCount);

		for (String[] fields : myItemList) {
			String type = fields[0].trim();
			String srcCateg = fields[1].trim();
			String tgtCateg = fields[2].trim();
			String srcPhr = fields[3].trim();
			String tgtPhr = fields[4].trim();
			long pairCount = Long.parseLong(fields[5].trim());
			long tgtCount = Long.parseLong(fields[6].trim());
			// long srcCount = pair.snd;

			double sgtLogProb = -Math.log10((double) pairCount / (double) (tgtCount + smoothCount));
			double tgsLogProb = -Math.log10((double) pairCount / (double) (srcCount + smoothCount));

			String categ = "[" + srcCateg + "::" + tgtCateg + "]";

			// remove -0
			String strTgsLogProb = (tgsLogProb + "").replace("-0.0", "0.0");
			String strSgtLogProb = (sgtLogProb + "").replace("-0.0", "0.0");

			// phrase count feature
			String scores =
					MyUtils.untokenize(" ", strTgsLogProb + "", strSgtLogProb + "", "0.4343",
							pairCount + "");
			String entry = MyUtils.untokenize(SuffStatMapper.DELIM, type, categ, srcPhr, tgtPhr, scores);
			
			context.write(new Text(entry), new Text(""));
		}
		
		// TODO: Pass along alignment here
	}
}
