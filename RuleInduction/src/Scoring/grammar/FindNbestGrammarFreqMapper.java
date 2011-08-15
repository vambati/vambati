package Scoring.grammar;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Utils.Beam;

public class FindNbestGrammarFreqMapper extends Mapper<LongWritable, Text, Text, Text> {

	private Beam<Float> beam;
	private FeatureManager fman;

	@Override
	public void setup(Context context) {
		Configuration conf = context.getConfiguration();
		String stats = conf.get("serializedStats");
		String feats = conf.get("serializedFeatures");
		fman = new FeatureManager(stats, feats);
		
		int maxSize = Integer.parseInt(context.getConfiguration().get("nbest"));
		beam = new Beam<Float>(maxSize, true);
	}

	@Override
	public void map(LongWritable pos, Text value, Context context) {

		try {
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(value.toString());
			float count = rule.getCount(fman);

			if (rule.type.equals(ScoreableRule.Type.GRAMMAR)) {
				beam.add(count);
			}
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void cleanup(Context context) {
		try {
			for (float f : beam) {
				context.write(new Text(f + ""), new Text(""));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
