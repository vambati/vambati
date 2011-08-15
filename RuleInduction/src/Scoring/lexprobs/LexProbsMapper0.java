package Scoring.lexprobs;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Utils.MyUtils;

public class LexProbsMapper0 extends Mapper<LongWritable, Text, Text, Text> {

	@Override
	public void map(LongWritable pos, Text value, Context context) throws IOException, InterruptedException {
		
		Counter phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read");

		String[] fields = MyUtils.split(value.toString().trim(), " ||| ", Integer.MAX_VALUE);

		if (fields.length != 4) {
			throw new RuntimeException("Got " + fields.length
					+ " fields for line, but was expecting 4: " + value.toString().trim());
		}

		String categ = fields[0].trim();
		String srcPhr = fields[1].trim();
		String tgtPhr = fields[2].trim();
		String scores = fields[3].trim();

		phraseRecordsRead.increment(1);

		String[] srcTokens = MyUtils.tokenize(srcPhr, " ");
		String[] tgtTokens = MyUtils.tokenize(tgtPhr, " ");

		for (String tok : srcTokens) {
			context.write(new Text(tok), new Text(""));
		}
		for (String tok : tgtTokens) {
			context.write(new Text(tok), new Text(""));
		}
	}
}
