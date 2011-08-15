package Scoring.extract;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import com.google.common.base.Joiner;

import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;
import Utils.MyUtils;
import edu.stanford.nlp.mt.base.IString;
import edu.stanford.nlp.mt.syntax.ghkm.AlignmentGraph;
import edu.stanford.nlp.mt.syntax.ghkm.HackedAlignmentGraph;
import edu.stanford.nlp.mt.syntax.ghkm.Rule;
import edu.stanford.nlp.mt.syntax.ghkm.StringNumberer;

public class GhkmRuleExtractionMapper extends Mapper<LongWritable, Text, Text, Text> {

	private Counter skippedMaxLhs;
	private Counter skippedMaxRhs;
	private Counter sentenceCount;
	private Counter ruleCount;
	private Counter phraseCount;

	private int i = 0;
	private int maxLHS;
	private int maxRHS;

	@Override
	public void setup(final Context context) {

		StrictHadoopConfiguration conf = new StrictHadoopConfiguration(context.getConfiguration());
		int maxCompositions = conf.getInt("maxCompositions");
		maxLHS = conf.getInt("maxSrc");
		maxRHS = conf.getInt("maxTgt");

		AlignmentGraph.setMaxCompositions(maxCompositions);

		sentenceCount = context.getCounter("COUNT", "Sentences");
		ruleCount = context.getCounter("COUNT", "Grammar Rules");
		phraseCount = context.getCounter("COUNT", "Syntactic Phrases");
		skippedMaxLhs = context.getCounter("COUNT", "Skipped due to source tree too big");
		skippedMaxRhs = context.getCounter("COUNT", "Skipped due to target tree too big");
	}

	public static final Pattern uniDirAntecedentNontermPattern =
			Pattern.compile("\\[(.+),([0-9]+)\\]");

	public static String convertToBidirectionalAntececents(String antecedents) throws RuleException {
		String[] arr = MyUtils.tokenize(antecedents, " ");
		for (int i = 0; i < arr.length; i++) {
			arr[i] = convertToBidirectionalLabel(arr[i]);
		}
		return Joiner.on(" ").join(arr);
	}

	public static String convertToBidirectionalLabel(String ruleElement) throws RuleException {

		Matcher match = uniDirAntecedentNontermPattern.matcher(ruleElement);

		if (match.matches() == false) {
			// this is not a non-terminal. move on.
			return ruleElement;
		} else {

			if (match.groupCount() != 2) {
				throw new RuleException("Expected 2 groups: " + ruleElement);
			}

			String label = match.group(1);
			String index = match.group(2);

			String bidir = "[" + label + "::" + label + "," + index + "]";
			return bidir;
		}
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

		String eLine = fields[0].trim();
		String fLine = fields[1].trim();
		String eTreeLine = fields[2].trim();
		String align = fields[3].trim();
//		String fTreeLine = fields[4].trim();

		int openCount = 0;
		int closeCount = 0;
		for (int i = 0; i < eTreeLine.length(); i++) {
			if (eTreeLine.charAt(i) == '(') {
				openCount++;
			} else if (eTreeLine.charAt(i) == ')') {
				closeCount++;
			}
		}
		if (openCount != closeCount) {
			context.getCounter("ERRORS", "Malformed trees").increment(1);
			return;
		}

		// TODO: Convert alignment
		String aLine = convertAlignment(align, false);

		sentenceCount.increment(1);

		StringNumberer dummyNum = new StringNumberer();
		HackedAlignmentGraph ag = new HackedAlignmentGraph(aLine, fLine, eTreeLine, eLine);
		Set<Rule> rules = ag.extractRules(dummyNum);
		for (Rule r : rules) {
			String alignment = ag.getAlignments(r);
			ag.unindexifyRule(r);

			if (r.lhsLabels.length > maxLHS) {
				skippedMaxLhs.increment(1);
				continue;
			}
			if (r.rhsLabels.length > maxRHS) {
				skippedMaxRhs.increment(1);
				continue;
			}

			try {
				String strCons = IString.getString(r.lhsLabels[0]);
				String consequent = "[" + strCons + "::" + strCons + "]";
				String ghkmSrcAnts = r.toJoshuaLHS();
				String srcAntecedents = convertToBidirectionalAntececents(ghkmSrcAnts);
				String tgtAntecedents = convertToBidirectionalAntececents(r.toJoshuaRHS());

				final String type;
				if (isPhrase(ghkmSrcAnts)) {
					type = ScoreableRule.typeToString(Type.PHRASE);
					phraseCount.increment(1);
				} else {
					type = ScoreableRule.typeToString(Type.GRAMMAR);
					ruleCount.increment(1);
				}

				// it's okay for GHKM rules to contain no alignment data
				ScoreableRule.allowBugs = true;
				ScoreableRule scoreable =
						new ScoreableRule(type, consequent, srcAntecedents, tgtAntecedents, "",
								alignment, "");
				scoreable.setCount(1);
				context.write(new Text(scoreable.toHadoopRecordString()), new Text());
			} catch (RuleException e) {
				throw new RuntimeException(e);
			}
		}

		i++;
	}

	private boolean isPhrase(String ghkmSrcAnts) {
		for (String element : MyUtils.tokenize(ghkmSrcAnts, " ")) {
			if (uniDirAntecedentNontermPattern.matcher(element).matches()) {
				return false;
			}
		}
		return true;
	}

	public static String convertAlignment(String align, boolean reverseAlignment) {
		// format: ((x1,y1),(x2,y2)...)

		// remove ( and )
		String tmp = align.substring(1, align.length() - 1);
		StringBuilder builder = new StringBuilder();

		if (tmp.length() > 0) {

			// remove ( from first pair and ) from last pair
			tmp = tmp.substring(1, tmp.length() - 1);

			String[] split = tmp.split("\\),\\(");

			for (String pair : split) {
				// each strPair is (x,y)
				String[] xy = MyUtils.tokenize(pair, ",");
				int x = Integer.parseInt(xy[0]) - 1;
				int y = Integer.parseInt(xy[1]) - 1;

				if (reverseAlignment) {
					builder.append(y + "-" + x + " ");
				} else {
					// keep them reversed, but make them 0-based
					builder.append(x + "-" + y + " ");
				}
			}
		}
		return builder.toString().trim();
	}
}
