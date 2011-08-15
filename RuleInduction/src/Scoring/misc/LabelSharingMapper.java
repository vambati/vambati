package Scoring.misc;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.RuleException;
import Scoring.ScoreableRule;
import Utils.MyUtils;

public class LabelSharingMapper extends Mapper<LongWritable, Text, Text, Text> {

	private Counter phraseRecordsRead;

	@Override
	public void setup(Context context) {
		phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read");
	}

	@Override
	public void map(LongWritable pos, Text value, Context context) throws IOException,
			InterruptedException {

		try {
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(value.toString());
			phraseRecordsRead.increment(1);
			String newKey = MyUtils.untokenize(ScoreableRule.DELIM, rule.srcAntecedents, rule.tgtAntecedents);
			context.write(new Text(newKey), new Text(rule.toHadoopRecordString()));
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}
}
