package Scoring.mle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

import Utils.MyUtils;

public class Reducer2 extends Reducer<Text, Text, Text, Text> {

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

		Counter phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read");
		Counter phraseInstancesRead = context.getCounter("COUNT", "Phrase Instances Read");

		// determine the total count
		long tgtCount = 0;
		List<String> myItemList = new ArrayList<String>();
		for (Text tValue : values) {
			String value = tValue.toString().trim();
			String[] fields =
					MyUtils.split(value.toString().trim(), SuffStatMapper.DELIM, Integer.MAX_VALUE);
			if (fields.length != 6) {
				throw new RuntimeException("Got " + fields.length
						+ " fields for line, but was expecting 6: " + value);
			}

			String type = fields[0];
			String srcCateg = fields[1];
			String tgtCateg = fields[2];
			String srcPhr = fields[3];
			String tgtPhr = fields[4];
			long pairCount = Long.parseLong(fields[5]);

			phraseInstancesRead.increment(pairCount);

			tgtCount += pairCount;
			myItemList.add(value);
		}

		phraseRecordsRead.increment(1);

		for (String value : myItemList) {
			context.write(new Text(value), new Text(tgtCount + ""));
		}

		// TODO: Pass along alignment here
	}
}
