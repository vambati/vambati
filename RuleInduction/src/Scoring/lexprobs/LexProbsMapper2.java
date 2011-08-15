package Scoring.lexprobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.RuleException;
import Scoring.ScoreableRule;

public class LexProbsMapper2 extends Mapper<LongWritable, Text, Text, Text> {

	private Map<String, Map<String, Float>> f2n = null;
	private Map<String, Map<String, Float>> n2f = null;
	private String direction;

	@Override
	public void setup(Context context) {
		
		direction = context.getConfiguration().get("direction");
		
		if (direction.equalsIgnoreCase("F2N") || direction.equalsIgnoreCase("BOTH")) {
			
			f2n = new TreeMap<String, Map<String, Float>>();
			String f2nPath = context.getConfiguration().get("filteredLexF2N");
			if (f2nPath == null) {
				throw new RuntimeException("Required param not found: f2nPath");
			}
			try {
				FileSystem fs = FileSystem.get(context.getConfiguration());

				context.setStatus("Loading F2N");
				loadProbs(f2nPath, fs, f2n);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		if (direction.equalsIgnoreCase("N2F") || direction.equalsIgnoreCase("BOTH")) {

			n2f = new TreeMap<String, Map<String, Float>>();
			String n2fPath = context.getConfiguration().get("filteredLexN2F");
			if (n2fPath == null) {
				throw new RuntimeException("Required param not found: f2nPath");
			}

			try {
				FileSystem fs = FileSystem.get(context.getConfiguration());

				context.setStatus("Loading N2F");
				loadProbs(n2fPath, fs, n2f);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static void loadProbs(String strPath, FileSystem fs,
			Map<String, Map<String, Float>> dict) throws IOException {

		Path path = new Path(strPath);
		if (fs.getFileStatus(path).isDir()) {
			FileStatus[] parts = fs.listStatus(path);
			for (FileStatus part : parts) {
				BufferedReader dictIn =
						new BufferedReader(new InputStreamReader(fs.open(part.getPath())));
//				AddLexicalProbsToJoshuaGrammar.loadDict(dictIn, dict);
				dictIn.close();
			}
		} else {
			BufferedReader dictIn = new BufferedReader(new InputStreamReader(fs.open(path)));
//			AddLexicalProbsToJoshuaGrammar.loadDict(dictIn, dict);
			dictIn.close();
		}
		throw new Error("Needs updating.");
	}

	@Override
	public void map(LongWritable pos, Text value, Context context) throws IOException,
			InterruptedException {

		Counter phraseRecordsRead = context.getCounter("COUNT", "Phrase Records Read");

		try {
			phraseRecordsRead.increment(1);
			ScoreableRule rule = ScoreableRule.parseJoshuaRecord(value.toString().trim());

//			FeatureManager.addLexicalProbabilities(rule, f2n, n2f, false, false);

			// use joshua since we don't include type here
			context.write(new Text(rule.toJoshuaString()), new Text(""));
			
			throw new Error("Needs updating.");
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}

	}
}
