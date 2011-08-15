package Scoring.misc;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;

// filters out non-syntactic rules that already have syntactic labels
public class LabelSharingFilterMapper extends Mapper<LongWritable, Text, Text, Text> {

	private Counter phraseRecordsRead;
	private Counter phraseRecordsKept;
	private Counter phraseRecordsSkipped;
	private FeatureManager fman;

	@Override
	public void setup(Context context) {
		
		String stats = context.getConfiguration().get("serializedStats");
		String feats = context.getConfiguration().get("serializedFeatures");
		fman = new FeatureManager(stats, feats);
		
		phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read");
		phraseRecordsKept = context.getCounter("COUNT", "Phrase Records Kept");
		phraseRecordsSkipped = context.getCounter("COUNT", "Phrase Records Skipped");
	}

	@Override
	public void map(LongWritable pos, Text value, Context context) throws IOException,
			InterruptedException {

		try {
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(value.toString());
			phraseRecordsRead.increment(1);
			
			if(ScoreableRule.syntacticLabelExists(rule, fman) && ScoreableRule.isSyntactic(rule) == false) {
				phraseRecordsSkipped.increment(1);
			} else {
				phraseRecordsKept.increment(1);
				context.write(new Text(rule.toHadoopRecordString()), new Text(""));
			}
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}
}
