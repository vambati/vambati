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
import Scoring.mle.features.suffstat.SuffStatFactory;

public class SuffStatMapper extends Mapper<LongWritable, Text, Text, Text> {

	public static final String DELIM = " ||| ";
	private Counter phraseRecordsRead;

	private MLESuffStat stat;

	// public static final int CATEG_SRC_TGT_ID = 0;
	// public static final int SRC_TGT_ID = 1;
	// public static final int SRC_ID = 2;
	// public static final int TGT_ID = 3;
	// public static final int UNLABELED_SRC_ID = 4;
	// public static final int UNLABELED_TGT_ID = 5;
	// public static final int SRCCATEG_ID = 6;
	// public static final int TGTCATEG_ID = 7;
	// public static final int NONSYNTACTIC_SRC_TGT_ID = 8;
	// public static final int NONSYNTACTIC_SRC_ID = 9;
	// public static final int NONSYNTACTIC_TGT_ID = 10;
	// public static final int SYNTACTIC_CATEG_SRC_TGT_ID = 11;
	// public static final int SYNTACTIC_SRC_TGT_ID = 12;
	// public static final int SYNTACTIC_SRC_ID = 13;
	// public static final int SYNTACTIC_CATEG_SRC_ID = 14;
	// public static final int SYNTACTIC_TGT_ID = 15;
	// public static final int SYNTACTIC_CATEG_TGT_ID = 16;
	// public static final int SYNTACTIC_CATEG_ID = 17;
	// public static final int SYNTACTIC_LABEL_FOR_PAIR_EXISTS = 18;
	// public static final int NUM_SUFF_STATS = 19;

	public static final String SYNTACTIC_LABEL_FOR_PAIR_EXISTS = "SYNTACTIC_LABEL_FOR_PAIR_EXISTS";
	private static FeatureManager fman;

	@Override
	public void setup(Context context) {
		phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read");
		stat = getStatClass(context.getConfiguration());
	}

	public static MLESuffStat getStatClass(Configuration conf) {
		try {
			String stats = conf.get("serializedStats");
			String feats = conf.get("serializedFeatures");
			fman = new FeatureManager(stats, feats);
			String statClassName = conf.get("statClassName");
			return SuffStatFactory.get(statClassName, fman);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void map(LongWritable pos, Text value, Context context) throws IOException,
			InterruptedException {

		try {
			phraseRecordsRead.increment(1);
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(value.toString());

			String newKey = stat.getKey(rule);
			context.write(new Text(newKey), new Text(value.toString()));
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}
}
