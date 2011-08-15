package Scoring.misc;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;

// finds grammar rules (with non-terminals) that apply only in a specific context:
// i.e. they contain non-stopword terminals
public class IdentifySpecificContextRulesMapper extends Mapper<LongWritable, Text, Text, Text> {

	private Counter recordsRead;
	private Counter specificContextRules;
	private Counter grammarRulesWithoutSpecificContext;
	private Counter hieroRules;
	private Counter otherRules;
	private Set<String> stopwords = new HashSet<String>();

	@Override
	public void setup(Context context) {
		
		recordsRead = context.getCounter("COUNT", "Records Read");
		specificContextRules = context.getCounter("COUNT", "Grammar Rules with Specific Context");
		grammarRulesWithoutSpecificContext =
				context.getCounter("COUNT", "Grammar Rules without Specific Context");
		hieroRules = context.getCounter("COUNT", "Hiero Rules");
		otherRules = context.getCounter("COUNT", "Other Rules");
		
		String stopwordsFile = context.getConfiguration().get("stopwordsFile");
		if (stopwordsFile == null) {
			throw new RuntimeException("Required param not found: stopwordsFile");
		}
		try {
			FilterMapper.readHdfsFileIntoHashSet(FileSystem.get(context.getConfiguration()), stopwordsFile,
					stopwords);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	static int i = 0;

	@Override
	public void map(LongWritable pos, Text value, Context context) throws IOException,
			InterruptedException {

		try {
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(value.toString());
			recordsRead.increment(1);

			if (rule.type == Type.GRAMMAR) {
				boolean hasSpecificContext = false;
				boolean hasNonterminal = false;
				for (String src : rule.getSourceAntecedents()) {
					if (!ScoreableRule.isNonterminal(src) && !stopwords.contains(src)) {
						hasSpecificContext = true;
					}
					if(ScoreableRule.isNonterminal(src)) {
						hasNonterminal = true;
					}
				}
				
				// if it has a specific context, then we can filter it like a hiero rule
				if (hasNonterminal && hasSpecificContext) {
					// use HIERO tag as meaning context-specific
					rule.type = Type.HIERO;
					specificContextRules.increment(1);
					
					if(i < 10) {
						System.out.println("Specific context rule (tagged as hiero): " + rule.toHadoopRecordString());
					}
					i++;
					
				} else {
					grammarRulesWithoutSpecificContext.increment(1);
				}
			} else {
				if (rule.type == Type.HIERO) {
					hieroRules.increment(1);
				} else {
					otherRules.increment(1);
				}
			}
			context.write(new Text(rule.toHadoopRecordString()), new Text(""));
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}
}
