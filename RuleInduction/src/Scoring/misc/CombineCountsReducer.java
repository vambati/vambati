package Scoring.misc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Utils.MyUtils;

public class CombineCountsReducer extends Reducer<Text, Text, Text, Text> {

	private Counter phraseRecordsRead;
	private Counter phraseRecordsWritten;
	private FeatureManager fman;

	@Override
	public void setup(Context context) {
		Configuration conf = context.getConfiguration();
		String stats = conf.get("serializedStats");
		String feats = conf.get("serializedFeatures");
		fman = new FeatureManager(stats, feats);
		
		phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read by Reducer");
		phraseRecordsWritten = context.getCounter("COUNT", "Phrase Records Written by Reducer");
	}

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
			InterruptedException {

		try {
			ScoreableRule outputRule = null;

			Map<String, Long> alignments = new HashMap<String, Long>();
			long sum = 0;
			for (Text tValue : values) {

				ScoreableRule rule = ScoreableRule.parseHadoopRecord(tValue.toString());
				if (outputRule == null) {
					outputRule = rule;
				}

				String[] links = MyUtils.tokenize(rule.alignment, " ");
				for(String link : links) {
					Long prevValue = alignments.get(link);
					if(prevValue == null) {
						alignments.put(link, (long) 1);
					} else {
						alignments.put(link, prevValue + 1);
					}
				}
				
				phraseRecordsRead.increment(1);
				long pairCount = (long) rule.getCount(fman);
				sum += pairCount;
			}
			
			String alignment = formatAlignment(alignments);
			outputRule.setAlignmentString(alignment);

			if (outputRule != null) {
				phraseRecordsWritten.increment(1);
				outputRule.setCount(sum, fman);
				context.write(new Text(outputRule.toHadoopRecordString()), new Text(""));
			}
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}

	public static String formatAlignment(Map<String, Long> alignments) {
		StringBuilder builder = new StringBuilder();
		for(Entry<String, Long> entry : alignments.entrySet()) {
			String link = entry.getKey();
			Long count = entry.getValue();
			builder.append(link + "/" + count + " ");
		}
		return builder.toString().trim();
	}
}
