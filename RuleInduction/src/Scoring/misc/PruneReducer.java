package Scoring.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

import com.sun.tools.javac.util.Pair;

public class PruneReducer extends Reducer<Text, Text, Text, Text> {

	private int minCount;
	private float[] scalingFactors;
	private int ambiguityFactor;
	private Counter uniqueSourceSides;
	private Counter uniquePhrasePairs;
	private Counter phrasePairsAfterPruning;
	private Counter sourceSidesAfterPruning;
	private FeatureManager fman;
	private String[] sortFeatureNames;

	@Override
	public void setup(Context context) {

		Configuration conf = context.getConfiguration();
		String stats = conf.get("serializedStats");
		String feats = conf.get("serializedFeatures");
		fman = new FeatureManager(stats, feats);

		String strSortFeatureNames = context.getConfiguration().get("sortFeatureNames");
		sortFeatureNames = MyUtils.tokenize(strSortFeatureNames, " ");

		minCount = context.getConfiguration().getInt("minCount", 1);

		uniquePhrasePairs = context.getCounter("COUNT", "Unique Phrase Pairs");
		uniqueSourceSides = context.getCounter("COUNT", "Unique Source Sides");
		phrasePairsAfterPruning = context.getCounter("COUNT", "Phrase Pairs After Pruning");
		sourceSidesAfterPruning = context.getCounter("COUNT", "Source Sides After Pruning");

		String strScalingFactors = context.getConfiguration().get("scalingFactors");
		if (strScalingFactors == null) {
			throw new RuntimeException("Required param not found: scalingFactors");
		}
		this.scalingFactors = MyUtils.tokenizeFloats(strScalingFactors, " ");

		String strAmbiguityFactor = context.getConfiguration().get("ambiguityFactor");
		if (strAmbiguityFactor == null) {
			throw new RuntimeException("Required param not found: ambiguityFactor");
		}
		this.ambiguityFactor = Integer.parseInt(strAmbiguityFactor);
	}

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
			InterruptedException {

		try {
			Map<String, List<Pair<Double, ScoreableRule>>> outputItemsMap =
					new HashMap<String, List<Pair<Double, ScoreableRule>>>();

			uniqueSourceSides.increment(1);

			for (Text tValue : values) {
				uniquePhrasePairs.increment(1);

				String value = tValue.toString();
				ScoreableRule rule = ScoreableRule.parseHadoopRecord(value);

				if (rule.getCount(fman) >= minCount) {

					// float sgtLogProb =
					// rule.getFeature(FeatureManager.featureIndices.get(FeatureManager.SGT_PHRASE));
					// float tgsLogProb =
					// rule.getFeature(FeatureManager.featureIndices.get(FeatureManager.TGS_PHRASE));
					// double scoreTotal = scalingFactors[0] * sgtLogProb +
					// scalingFactors[1] * tgsLogProb;

					double scoreTotal = 0.0;
					for (int i = 0; i < sortFeatureNames.length; i++) {
						String name = sortFeatureNames[i];
						final float score;
						if (fman.featIndexManager.contains(name)) {
							score = rule.getFeature(name, fman);
						} else if (fman.statIndexManager.contains(name)) {
							score = rule.getSufficientStat(name, fman);
						} else {
							throw new RuntimeException("No feature or sufficient statistic named " + name);
						}
						scoreTotal += score * scalingFactors[i];
					}

					List<Pair<Double, ScoreableRule>> list =
							outputItemsMap.get(rule.getConsequent());
					if (list == null) {
						list = new ArrayList<Pair<Double, ScoreableRule>>();
						outputItemsMap.put(rule.getConsequent(), list);
					}
					list.add(new Pair<Double, ScoreableRule>(scoreTotal, rule));
				}
			}

			for (Entry<String, List<Pair<Double, ScoreableRule>>> entry : outputItemsMap.entrySet()) {
				List<Pair<Double, ScoreableRule>> list = entry.getValue();

				Collections.sort(list, new Comparator<Pair<Double, ScoreableRule>>() {
					public int compare(Pair<Double, ScoreableRule> o1,
							Pair<Double, ScoreableRule> o2) {
						// we're using negative log probs
						// smaller numbers (closer to zero) are better
						return Double.compare(o1.fst, o2.fst);
					}
				});

				// take only n-best translations of each srcSide
				if (list.size() > ambiguityFactor) {
					list = list.subList(0, ambiguityFactor);
				}

				sourceSidesAfterPruning.increment(1);

				for (Pair<Double, ScoreableRule> pair : list) {
					phrasePairsAfterPruning.increment(1);
					context.write(new Text(pair.snd.toHadoopRecordString()), new Text(""));
				}
			}
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}
}
