package Hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Rule.Rule;
import Rule.RuleLearnerException;
import RuleLearner.CorpusEntry;
import RuleLearner.IPrinter;
import RuleLearner.RuleLearner;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;

public class HadoopRuleLearningMapper extends Mapper<LongWritable, Text, Text, Text> {

	private RuleLearner rulelearner;
	private Counter sentences;
	private Counter phrases;
	private Counter grammarRules;
	private Counter lexRules;
	private Counter sentencesSkippedDueToEmptyParseTree;

	private int i = 0;

	@Override
	public void setup(final Context context) {

		String strAllowBugs = context.getConfiguration().get("ALLOW_BUGS");
		if (strAllowBugs == null) {
			throw new RuntimeException("Required option not set: ALLOW_BUGS");
		}
		ScoreableRule.allowBugs = Boolean.parseBoolean(strAllowBugs);
		System.err.println("allowBugs=" + ScoreableRule.allowBugs);

		sentences = context.getCounter("COUNT", "Sentences");
		phrases = context.getCounter("COUNT", "Phrase Instances");
		grammarRules = context.getCounter("COUNT", "Grammar Instances");
		lexRules = context.getCounter("COUNT", "Lexicon Instances");
		sentencesSkippedDueToEmptyParseTree =
				context.getCounter("COUNT", "Sentences Skipped Due to Empty Target Parse Tree");

		rulelearner = new RuleLearner(context.getConfiguration(), new IPrinter() {
			public void close() {
			}

			public void writeLexicon(Rule rl) throws IOException, InterruptedException,
					RuleException {

				ScoreableRule rule = rl.toScoreableRule();
				rule.type = Type.LEXICON;
				context.write(new Text(rule.toHadoopRecordString()), new Text());
				lexRules.increment(1);
			}

			public void writePhrase(Rule rl) throws IOException, InterruptedException,
					RuleException {

				ScoreableRule rule = rl.toScoreableRule();
				rule.type = Type.PHRASE;
				context.write(new Text(rule.toHadoopRecordString()), new Text());
				phrases.increment(1);
			}

			public void writeRule(Rule rl) throws IOException, InterruptedException, RuleException {

				ScoreableRule rule = rl.toScoreableRule();
				rule.type = Type.GRAMMAR;
				context.write(new Text(rule.toHadoopRecordString()), new Text());
				grammarRules.increment(1);
			}

			public void writeT2TRule(String type, int id, String stree, String ttree) {
				throw new Error("Unimplemented.");
			}

			public void writeTaruPhrase(String type, int id, String tree, String target) {
				throw new Error("Unimplemented.");
			}

			public void writeTaruRule(String type, int id, String tree, String target) {
				throw new Error("Unimplemented.");
			}

			public void writeTiburonTrainFile(String fileName, ArrayList<String> parseTreeMap) {
				throw new Error("Unimplemented.");
			}

			public void writeTiburonTransducerFile(String fileName,
					HashMap<String, Integer> ruleRepository) {
				throw new Error("Unimplemented.");
			}
		});
		System.out.println("Rule transduction starts...");
	}

	@Override
	public void map(LongWritable dummy, Text value, final Context context) throws IOException,
			InterruptedException {

		final String strValue = value.toString().trim();
		System.err.println("Processing record: " + strValue);
		String[] fields = strValue.split(" \\|\\|\\| ");

		if (fields.length < 4) {
			throw new RuntimeException("Less than 4 fields in entry: " + value.toString().trim());
		}

		String type = "";
		String sl = fields[0].trim();
		String tl = fields[1].trim();
		String sparsetree = fields[2].trim();
		String align = fields[3].trim();

		int openCount = 0;
		int closeCount = 0;
		for (int i = 0; i < sparsetree.length(); i++) {
			if (sparsetree.charAt(i) == '(') {
				openCount++;
			} else if (sparsetree.charAt(i) == ')') {
				closeCount++;
			}
		}
		if (openCount != closeCount) {
			context.getCounter("ERRORS", "Malformed trees").increment(1);
			return;
		}

		sentences.increment(1);

		try {
			if (rulelearner.input_mode.equalsIgnoreCase("T2S")) {
				CorpusEntry ce = new CorpusEntry(sl, tl, type, sparsetree, align);
				rulelearner.transduce_t2s(ce, i);
			} else {

				if (fields.length < 5) {
					throw new RuntimeException("Less than 5 fields in entry: "
							+ value.toString().trim());
				}

				String tparsetree = fields[4].trim();

				if (tparsetree.equals("()")) {
					sentencesSkippedDueToEmptyParseTree.increment(1);
				} else {

					CorpusEntry ce = new CorpusEntry(sl, tl, type, sparsetree, tparsetree, align);
					if (rulelearner.input_mode.equalsIgnoreCase("T2T")) {
						rulelearner.transduce_t2t(ce, i);
					} else if (rulelearner.input_mode.equalsIgnoreCase("T2TS")) {
						rulelearner.transduce_t2ts(ce, i);
					} else if (rulelearner.input_mode.equalsIgnoreCase("TS2TS")) {
						rulelearner.transduce_ts2ts(ce, i);
					} else {
						throw new RuntimeException("No input mode specified");
					}
				}
			}
		} catch (RuleException e) {
			throw new RuntimeException("Error processing record: " + strValue, e);
		} catch (RuleLearnerException e) {
			throw new RuntimeException("Error processing record: " + strValue, e);
		}

		i++;
	}
}
