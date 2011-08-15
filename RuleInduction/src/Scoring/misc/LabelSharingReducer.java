package Scoring.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.mle.SuffStatMapper;

public class LabelSharingReducer extends Reducer<Text, Text, Text, Text> {

	private Counter uniquePhrasePairs;
	private Counter phrasePairsAfterSharing;
	private FeatureManager fman;

	@Override
	public void setup(Context context) {

		String stats = context.getConfiguration().get("serializedStats");
		String feats = context.getConfiguration().get("serializedFeatures");
		fman = new FeatureManager(stats, feats);

		uniquePhrasePairs = context.getCounter("COUNT", "Unique Phrase Pairs");
		phrasePairsAfterSharing = context.getCounter("COUNT", "Phrase Pairs After Sharing");
	}

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
			InterruptedException {

		// SUFFICIENT STATS:
		// 1) Extract sufficient stat for SRC and TGT counts that come from
		// PHR's

		try {
			List<ScoreableRule> syntactic = new ArrayList<ScoreableRule>();
			ScoreableRule nonsyntactic = null;

			for (Text tValue : values) {
				uniquePhrasePairs.increment(1);

				String value = tValue.toString();
				ScoreableRule rule = ScoreableRule.parseHadoopRecord(value);

				if (ScoreableRule.isSyntactic(rule) == false) {
					if (nonsyntactic == null) {
						nonsyntactic = rule;
					} else {
						throw new RuntimeException(
								"Got TWO nonsyntactic rules for phrase. Expected 0 or 1: "
										+ rule.toHadoopRecordString());
					}
				} else {
					syntactic.add(rule);
				}
			}

			if (nonsyntactic == null && syntactic.get(0).type == ScoreableRule.Type.PHRASE) {
				// throw new
				// RuntimeException("No non-syntactic rule found for phrase pair. We assume syntactic phrases are a subset of non-syntactic phrases: "
				// + syntactic.get(0));
				int n = syntactic.get(0).getSourceAntecedents().length;
				String length = n > 15 ? ">15" : n + "";
				context.getCounter(
						"WARNINGS",
						"Skipped phrase pairs of length " + length
								+ " without a non-syntactic equivalent").increment(1);
				return;
			}

			int exists = 0;
			if (syntactic.size() > 0) {
				exists = 1;
			}

			// write all rules
			for (ScoreableRule rule : syntactic) {
				rule.setSufficientStat(SuffStatMapper.SYNTACTIC_LABEL_FOR_PAIR_EXISTS, exists, fman);
				context.write(new Text(rule.toHadoopRecordString()), new Text(""));
				phrasePairsAfterSharing.increment(1);
			}
			if (nonsyntactic != null) {
				// can happen for grammar rules
				nonsyntactic.setSufficientStat(SuffStatMapper.SYNTACTIC_LABEL_FOR_PAIR_EXISTS,
						exists, fman);
				context.write(new Text(nonsyntactic.toHadoopRecordString()), new Text(""));
				phrasePairsAfterSharing.increment(1);
			}

		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}
}
