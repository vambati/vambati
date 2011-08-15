package Scoring.lexprobs;

import java.io.IOException;
import java.util.HashSet;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.misc.FilterMapper;
import Utils.MyUtils;

public class LexProbsMapper1 extends Mapper<LongWritable, Text, Text, Text> {

	private final HashSet<String> vocab = new HashSet<String>();

	@Override
	public void setup(Context context) {
		String strVocabPath = context.getConfiguration().get("vocabPath");
		if (strVocabPath == null) {
			throw new RuntimeException("Required param not found: vocabPath");
		}
		try {
			FilterMapper.readHdfsFileIntoHashSet(FileSystem.get(context.getConfiguration()),
					strVocabPath, vocab);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void map(LongWritable pos, Text value, Context context) throws IOException, InterruptedException {

		Counter lexProbsRead = context.getCounter("COUNT", "Lex Probs Read");
		Counter lexProbsInVocab = context.getCounter("COUNT", "Lex Probs In Vocab");

		String[] columns = MyUtils.tokenize(value.toString().trim(), " \t");
		String word1 = columns[0];
		String word2 = columns[1];
		lexProbsRead.increment(1);
		if (vocab.contains(word1) && vocab.contains(word2)) {
			lexProbsInVocab.increment(1);
			context.write(value, new Text(""));
		}
	}
}
