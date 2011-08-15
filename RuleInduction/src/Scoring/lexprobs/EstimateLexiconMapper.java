package Scoring.lexprobs;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.WordAlignment;
import Utils.MyUtils;
import chaski.utils.Pair;

public class EstimateLexiconMapper extends Mapper<LongWritable, Text, Text, Text> {

	public static final String NULL = "NULL";
	private Counter sentencesRead;

	@Override
	public void setup(Context context) {
		sentencesRead = context.getCounter("COUNT", "Sentences Read");
	}

	// c.p. get_lexicon, line 830 in train-factored-model.pl by Philipp Koehn
	@Override
	public void map(LongWritable dummy, Text textValue, Context context) throws IOException,
			InterruptedException {

		sentencesRead.increment(1);

		String[] columns = MyUtils.split(textValue.toString(), " {##} ", Integer.MAX_VALUE);
		if (columns.length != 3) {
			throw new RuntimeException("Badly formatted line. Expected 3 columns but got "
					+ columns.length + ": " + textValue.toString());
		}

		// aligned words
		String[] s = MyUtils.tokenize(columns[0], " \t");
		String[] t = MyUtils.tokenize(columns[1], " \t");
		WordAlignment a = new WordAlignment(columns[2]);

		for (Pair<Integer, Integer> pair : a.getAlignmentPairs()) {

			int i = pair.getFirst();
			int j = pair.getSecond();
			if (i < 0 || i >= s.length || j < 0 || j >= t.length) {
				throw new RuntimeException("Invalid alignment link: " + i + "-" + j
						+ " for s.length=" + s.length + " t.length=" + t.length + ". Record: "
						+ textValue.toString());
			}

			emitSgt(context, s[i], t[j]);
			emitTgs(context, s[i], t[j]);
		}

		// unaligned words
		//
		// we recreate the behavior of Moses, although we should probably ignore
		// NULL on the side being summed.
		boolean isSourceToTarget = true;
		for (int i = 0; i < s.length; i++) {
			if (a.getWordsAlignedTo(i, isSourceToTarget).size() == 0) {
				emitSgt(context, s[i], NULL); // unaligned
				emitTgs(context, s[i], NULL); // weird, but how moses does it
			}
		}

		isSourceToTarget = false;
		for (int i = 0; i < t.length; i++) {
			if (a.getWordsAlignedTo(i, isSourceToTarget).size() == 0) {
				emitSgt(context, NULL, t[i]); // weird, but how moses does it
				emitTgs(context, NULL, t[i]);
			}
		}
	}

	private void emitTgs(Context context, String srcWord, String tgtWord) throws IOException,
			InterruptedException {
		String key = MyUtils.untokenize(" ", "TGS", srcWord);
		String value = MyUtils.untokenize(" ", tgtWord, "1");
		context.write(new Text(key), new Text(value));
	}

	private void emitSgt(Context context, String srcWord, String tgtWord) throws IOException,
			InterruptedException {
		String key = MyUtils.untokenize(" ", "SGT", tgtWord);
		String value = MyUtils.untokenize(" ", srcWord, "1");
		context.write(new Text(key), new Text(value));
	}
}
