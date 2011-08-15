package Scoring.mle;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;

public abstract class SuffStatBase extends Reducer<Text, Text, Text, Text> {

	private Counter phraseRecordsRead;
	private MLESuffStat stat;
	private FeatureManager fman;
	private List<ScoreableRule> outputRules = new ArrayList<ScoreableRule>(500000);

	@Override
	public void setup(Context context) {

		String stats = context.getConfiguration().get("serializedStats");
		String feats = context.getConfiguration().get("serializedFeatures");
		fman = new FeatureManager(stats, feats);

		phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read by Reducer");
		stat = SuffStatMapper.getStatClass(context.getConfiguration());
	}

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
			InterruptedException {

		try {
			outputRules.clear();
			long sum = 0;

			for (Text tValue : values) {
				phraseRecordsRead.increment(1);
				ScoreableRule rule = ScoreableRule.parseHadoopRecord(tValue.toString());

				if (stat.shouldCount(rule)) {
					long count = (long) rule.getCount(fman);
					sum += count;
				}

				if (stat.shouldAffect(rule)) {
					// we must wait until we know the count for this stat before
					// writing
					try {
						outputRules.add(rule);
					} catch (Throwable t) {
						// probably a memory error
						// make some room to report the error, then die
						int size = outputRules.size();
						outputRules.clear();
						StringWriter msg = new StringWriter();
						PrintWriter out = new PrintWriter(msg);
						out.println("Error processing key (" + size
								+ " output rules so far -- try aggregating a smaller set): "
								+ key.toString());
						t.printStackTrace(out);
						System.err.println(msg.toString());
						throw new RuntimeException(msg.toString());
					}

					final int WARNING_SIZE = 500 * 1000;
					if (outputRules.size() % WARNING_SIZE == 0) {
						String msg =
								"Count key of size " + outputRules.size() + ": " + key.toString();
						context.getCounter("WARNINGS", msg).increment(1);
						context.progress();
						System.err.println(msg);
					}
				} else {
					// don't store rules in memory that aren't affected by this
					// stat, just write them out directly
					yield(key.toString(), context, rule);
				}
			}

			for (ScoreableRule outputRule : outputRules) {
				if (stat.shouldAffect(outputRule)) {
					outputRule.setSufficientStat(stat.name, sum, fman);
				}
				yield(key.toString(), context, outputRule);
			}
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}

	public abstract void yield(String key, Context context, ScoreableRule outputRule)
			throws IOException, InterruptedException, RuleException;
}
