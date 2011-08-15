package Scoring.misc;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

public class UniqueReducer extends Reducer<Text, Text, Text, Text> {

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
			InterruptedException {

		Counter count = context.getCounter("COUNT", "Unique Records Read");
		count.increment(1);
		context.write(new Text(key), new Text(""));
	}
}
