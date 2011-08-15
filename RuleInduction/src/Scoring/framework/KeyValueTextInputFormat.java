package Scoring.framework;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

import Utils.MyUtils;

public class KeyValueTextInputFormat extends FileInputFormat<Text, Text> {

	@Override
	public RecordReader<Text, Text> createRecordReader(InputSplit split, TaskAttemptContext context) {
		return new RecordReader<Text, Text>() {
			
			LineRecordReader reader = new LineRecordReader();

			@Override
			public void close() throws IOException {
				reader.close();
			}

			@Override
			public Text getCurrentKey() throws IOException, InterruptedException {
				return new Text(MyUtils.split(reader.getCurrentValue().toString(), "\t", 2)[0]);
			}

			@Override
			public Text getCurrentValue() throws IOException, InterruptedException {
				return new Text(MyUtils.split(reader.getCurrentValue().toString(), "\t", 2)[1]);
			}

			@Override
			public float getProgress() throws IOException, InterruptedException {
				return reader.getProgress();
			}

			@Override
			public void initialize(InputSplit split, TaskAttemptContext context)
					throws IOException, InterruptedException {
				reader.initialize(split, context);
			}

			@Override
			public boolean nextKeyValue() throws IOException, InterruptedException {
				return reader.nextKeyValue();
			}

		};
	}

	@Override
	protected boolean isSplitable(JobContext context, Path file) {
		CompressionCodec codec =
				new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
		return codec == null;
	}

}
