package Scoring.misc;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;
import Scoring.mle.SuffStatMapper;
import Utils.MyUtils;

public class CombineCountsMapper extends Mapper<LongWritable, Text, Text, Text> {

	private FeatureManager fman;

	@Override
	public void setup(Context context) {
		Configuration conf = context.getConfiguration();
		String stats = conf.get("serializedStats");
		String feats = conf.get("serializedFeatures");
		if (stats == null) {
			throw new RuntimeException("Required configuration entry not found: serializedStats="
					+ stats);
		}
		fman = new FeatureManager(stats, feats);
	}

	// Combine Moses Phrase table, and Xfer phrase pairs
	// Dictionary entries currently disabled
	// ! " " me ||| chinese ||| 6-3/2 0-0/2 1-0/2 2-1/2 3-2/2 ||| m/2 p/2 ||| 2
	// 2
	// chinese ||| group member to see ||| (0) ||| (0) ||| 1e-5 1e-5 1e-5 1e-5
	// ||| 0.333 0.333 0.333 0.333 0.333 0.333
	// S # nn # nn # visit # chinese # align # 2
	@Override
	public void map(LongWritable offset, Text keyValue, Context context) throws IOException,
			InterruptedException {

		Counter nonSyntacticPhraseRecords =
				context.getCounter("COUNT", "NonSyntactic Phrase Records");
		Counter nonSyntacticPhraseInstances =
				context.getCounter("COUNT", "NonSyntactic Phrase Instances");

		Counter hieroPhraseRecords = context.getCounter("COUNT", "Hiero Rule Records");
		Counter hieroPhraseInstances = context.getCounter("COUNT", "Hiero Rule Instances");
		Counter syntacticPhraseRecords = context.getCounter("COUNT", "Syntactic Phrase Records");
		Counter syntacticPhraseInstances =
				context.getCounter("COUNT", "Syntactic Phrase Instances");
		Counter syntacticGrammarRecords = context.getCounter("COUNT", "Syntactic Grammar Records");
		Counter syntacticGrammarInstances =
				context.getCounter("COUNT", "Syntactic Grammar Instances");
		Counter notskippedLexiconEntries = context.getCounter("COUNT", "NOT Skipped Lexicon Entries");
		Counter totalRecordsWritten = context.getCounter("COUNT", "Total Records Written");
		Counter totalInstancesWritten = context.getCounter("COUNT", "Total Instances Written");

		if (keyValue.toString().trim().equals("")) {
			return;
		}

		try {
			boolean skip = false;
			ScoreableRule rule = null;

			String[] arrKeyValue = MyUtils.split(keyValue.toString().trim(), "\t", 2);

			if (arrKeyValue.length == 2) {
				// non-syntactic phrase, but a phrase nonetheless

				String key = arrKeyValue[0];
				String value = arrKeyValue[1];

				String[] fields =
						MyUtils.split(value.trim(), SuffStatMapper.DELIM, Integer.MAX_VALUE);

				String type = "P";
				String categ = "[PHR::PHR]";
				String tgtPhr = key;
				String srcPhr = fields[0];
				String align = fields[1];
				String reordering = fields[2];
				String pairCount = "1";

				nonSyntacticPhraseRecords.increment(1);
				nonSyntacticPhraseInstances.increment(Long.parseLong(pairCount));

				rule = new ScoreableRule(type, categ, srcPhr, tgtPhr, "", align, "");
				rule.setCount(Long.parseLong(pairCount), fman);
			} else {

				rule = ScoreableRule.parseHadoopRecord(keyValue.toString());
				float pairCount = rule.getCount(fman);

				Type type = rule.type;
				if (type.equals(Type.PHRASE)) {
					syntacticPhraseRecords.increment(1);
					syntacticPhraseInstances.increment((long) pairCount);
				} else if (type.equals(Type.HIERO)) {
					hieroPhraseRecords.increment(1);
					hieroPhraseInstances.increment((long) pairCount);
				} else if (type.equals(Type.GRAMMAR)) {
					syntacticGrammarRecords.increment(1);
					syntacticGrammarInstances.increment((long) pairCount);
				} else if (type.equals(Type.LEXICON)) {
					rule.type = Type.PHRASE;
					notskippedLexiconEntries.increment(1);
					skip = false;
				} else {
					throw new Error("Unrecognized type: " + rule.type);
				}
			}
			if (skip == false) {
				long longPairCount = (long) rule.getCount(fman);

				if (longPairCount < 1) {
					throw new RuntimeException("Count should be at least 1: "
							+ rule.toHadoopRecordString());
				}

				totalRecordsWritten.increment(1);
				totalInstancesWritten.increment(longPairCount);

				String newKey =
						MyUtils.untokenize(SuffStatMapper.DELIM, rule.consequent,
								rule.srcAntecedents, rule.tgtAntecedents);
				String newValue = rule.toHadoopRecordString();
				if(rule.type == Type.LEXICON) {
					throw new RuntimeException("Lexicon entries are no longer kosher.");
				}
				context.write(new Text(newKey), new Text(newValue));
			}
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}
}
