package Scoring.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;

import com.google.common.base.Joiner;

public class FilterMapper extends Mapper<LongWritable, Text, Text, Text> {

	private HashSet<String> sourcePhrases = new HashSet<String>();
	private Counter phraseRecordsRead;
	private Counter phraseRecordsInTestSet;
	private Counter hieroRead;
	private Counter hieroInTestSet;

	@Override
	public void setup(Context context) {

		phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read");
		phraseRecordsInTestSet = context.getCounter("COUNT", "Phrase Records In Test Set");
		hieroRead = context.getCounter("COUNT", "Hiero Records Read");
		hieroInTestSet = context.getCounter("COUNT", "Hiero Records In Test Set");

		String srcPhrasesFile = context.getConfiguration().get("srcPhrases");
		if (srcPhrasesFile == null) {
			throw new RuntimeException("Required param not found: srcPhrases");
		}
		try {
			readHdfsFileIntoHashSet(FileSystem.get(context.getConfiguration()), srcPhrasesFile,
					sourcePhrases);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void readHdfsFileIntoHashSet(FileSystem fs, String srcFilename,
			Set<String> destinationSet) {

		try {
			Path path = new Path(srcFilename);
			if (fs.getFileStatus(path).isDir()) {
				FileStatus[] parts = fs.listStatus(path);
				for (FileStatus part : parts) {
					read(fs, destinationSet, part.getPath());
				}
			} else {
				read(fs, destinationSet, path);
			}
			System.out.println("Loaded " + destinationSet.size() + " source phrases.");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void read(FileSystem fs, Set<String> sourcePhrases, Path path)
			throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(fs.open(path)));
		String line;
		while ((line = in.readLine()) != null) {
			sourcePhrases.add(line.trim());
		}
		in.close();
	}

	@Override
	public void map(LongWritable pos, Text value, Context context) throws IOException,
			InterruptedException {

		try {
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(value.toString());

			if (rule.type == Type.PHRASE) {
				phraseRecordsRead.increment(1);
			} else {
				hieroRead.increment(1);
			}

			if (rule.type == Type.PHRASE) {
				for (String ant : rule.getSourceAntecedents()) {
					if (ScoreableRule.isNonterminal(ant)) {
						throw new RuntimeException(
								"This filterer is only intended for phrases. It is not capable of filtering grammars.");
					}
				}

				if (sourcePhrases.contains(rule.srcAntecedents)) {
					phraseRecordsInTestSet.increment(1);
					context.write(new Text(rule.toHadoopRecordString()), new Text(""));
				}
			} else {
				
				boolean match = true;
				List<String> contiguousPhrase = new ArrayList<String>();
				for (String ant : rule.getSourceAntecedents()) {
					if (ScoreableRule.isNonterminal(ant)) {
						String strContiguous = Joiner.on(" ").join(contiguousPhrase);
						if(sourcePhrases.contains(strContiguous) == false) {
							match = false;
							break;
						}
						contiguousPhrase.clear();
					} else {
						contiguousPhrase.add(ant);
					}
				}
				
				String strContiguous = Joiner.on(" ").join(contiguousPhrase);
				if(sourcePhrases.contains(strContiguous) == false) {
					match = false;
				}

				if (match) {
					hieroInTestSet.increment(1);
					context.write(new Text(rule.toHadoopRecordString()), new Text(""));
				}
			}
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}
}
