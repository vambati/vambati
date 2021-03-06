package Scoring.mle;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Utils.MyUtils;

// TODO: Combine mappers into abstract superclass
public class SyntaxPrioMapper extends Mapper<Text, Text, Text, Text> {

	@Override
	public void map(Text key, Text value, Context context) throws IOException, InterruptedException {

		Counter phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read");
		Counter phraseInstancesRead = context.getCounter("COUNT", "Phrase Instances Read");

		String[] fields = MyUtils.split(key.toString().trim(), SuffStatMapper.DELIM, Integer.MAX_VALUE);

		if (fields.length != 5) {
			throw new RuntimeException("Got " + fields.length
					+ " fields for line, but was expecting 5: " + key.toString().trim());
		}

		String type = fields[0].trim();
		String srcCateg = fields[1].trim();
		String tgtCateg = fields[2].trim();
		String srcPhr = fields[3].trim();
		String tgtPhr = fields[4].trim();
		String pairCount = value.toString().trim();

		phraseRecordsRead.increment(1);
		phraseInstancesRead.increment(Long.parseLong(pairCount));

		String newValue =
				MyUtils.untokenize(SuffStatMapper.DELIM, type, srcCateg, tgtCateg, srcPhr, tgtPhr,
						pairCount);
		context.write(new Text(srcPhr), new Text(newValue));
	}
}
