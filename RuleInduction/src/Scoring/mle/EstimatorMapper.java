package Scoring.mle;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.mle.features.MLEFeatureFactory;
import Utils.MyUtils;

// uses suff stats to estimate MLE features
public class EstimatorMapper extends Mapper<LongWritable, Text, Text, Text> {

	private Counter phraseRecordsRead;
	private MLEFeature[] features;
	private static FeatureManager fman;

	public static MLEFeature getFeatureClass(String featureClassName, Configuration conf) {
		try {
			String stats = conf.get("serializedStats");
			String feats = conf.get("serializedFeatures");
			fman = new FeatureManager(stats, feats);
			MLEFeature feat = MLEFeatureFactory.get(featureClassName, fman);
			return feat;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setup(Context context) {

		phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read");

		String[] featureClassNames =
				MyUtils.tokenize(context.getConfiguration().get("featureClassNames"), " ");
		features = new MLEFeature[featureClassNames.length];

		for (int i = 0; i < featureClassNames.length; i++) {
			features[i] = getFeatureClass(featureClassNames[i], context.getConfiguration());
		}
	}

	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException,
			InterruptedException {

		try {
			phraseRecordsRead.increment(1);
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(value.toString());
			for (MLEFeature feat : features) {

				if (feat.shouldAffect(rule)) {

					float logMle = feat.calculateLogFeature(rule, fman);

					rule.setFeature(feat.name, logMle, fman);

				} else {
					// if feature doesn't affect rule, just give it a zero
					// log-prob
					rule.setFeature(feat.name, 0.0f, fman);
				}
			}
			context.write(new Text(rule.toHadoopRecordString()), new Text(""));
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}
}
