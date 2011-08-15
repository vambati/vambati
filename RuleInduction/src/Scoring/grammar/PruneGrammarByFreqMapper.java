package Scoring.grammar;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;

public class PruneGrammarByFreqMapper extends Mapper<LongWritable, Text, Text, Text> {

	private float minFreq;
	private FeatureManager fman;

	@Override
	public void setup(Context context) {
		Configuration conf = context.getConfiguration();
		String stats = conf.get("serializedStats");
		String feats = conf.get("serializedFeatures");
		fman = new FeatureManager(stats, feats);
		
		minFreq = Float.parseFloat(context.getConfiguration().get("minFreq"));
	}

	@Override
	public void map(LongWritable pos, Text value, Context context) throws IOException, InterruptedException {

		try {
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(value.toString());
			float count = rule.getCount(fman);

			if (rule.type.equals(ScoreableRule.Type.GRAMMAR)) {
				if (count >= minFreq) {
					context.write(new Text(rule.toHadoopRecordString()), new Text(""));
				}
			}
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}
}
