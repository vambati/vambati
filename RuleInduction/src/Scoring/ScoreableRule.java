package Scoring;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Scoring.mle.SuffStatMapper;
import Utils.MyUtils;

public class ScoreableRule {

	public enum Type {
		LEXICON, PHRASE, GRAMMAR, HIERO
	};
	public Type type;

	public String consequent;
	public String srcAntecedents;
	public String tgtAntecedents;
	public String features;

	public String alignment;
	private String suffStats;

	private String[] srcAntecedentsArr;
	private String[] tgtAntecedentsArr;
	private float[] featuresArr;
	private float[] statsArr;

	private boolean changed = false;

	public static final Pattern lhsPattern = Pattern.compile("\\[(.+)::(.+)\\]");
	public static final Pattern antecedentNontermPattern =
			Pattern.compile("\\[(.+)::(.+),([0-9]+)\\]");

	private String srcConsequent;

	private String tgtConsequent;

	private WordAlignment alignmentMap;

	public static final String DELIM = " ||| ";

	public static final String COUNT = "COUNT";

	public static boolean allowBugs = false;

	public ScoreableRule(String type, String consequent, String srcAntecedents,
			String tgtAntecedents, String features, String alignment, String suffStats)
			throws RuleException {

		this.type = parseType(type);
		this.consequent = consequent;
		this.srcAntecedents = srcAntecedents;
		this.tgtAntecedents = tgtAntecedents;
		this.features = features;
		this.alignment = alignment;
		this.suffStats = suffStats;

		if (allowBugs == false && alignment.trim().equals("")) {
			throw new RuleException("Rule must contain some alignment data: "
					+ toHadoopRecordString());
		}
	}

	private ScoreableRule() {

	}

	public int hashCode() {
		try {
			return toHadoopRecordString().hashCode();
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof ScoreableRule) {
			try {
				ScoreableRule other = (ScoreableRule) obj;
				return this.toHadoopRecordString().equals(other.toHadoopRecordString());
			} catch (RuleException e) {
				throw new RuntimeException(e);
			}
		} else {
			return false;
		}
	}

	// type ||| [src::tgt]] ||| src phrase ||| tgt phrase ||| features
	public static ScoreableRule parseHadoopRecord(String str) throws RuleException {

		ScoreableRule rule = new ScoreableRule();
		String[] columns = MyUtils.split(str, DELIM, Integer.MAX_VALUE);

		if (columns.length != 7) {
			throw new RuleException("Expected 7 columns but found " + columns.length + " instead: "
					+ "\"" + str + "\"");
		}

		try {
			rule.type = parseType(columns[0]);
		} catch (RuleException e) {
			throw new RuleException("Rule exception for rule: " + str, e);
		}
		rule.consequent = columns[1].trim();
		rule.srcAntecedents = columns[2].trim();
		rule.tgtAntecedents = columns[3].trim();
		rule.features = columns[4].trim();
		rule.alignment = columns[5].trim();
		rule.suffStats = columns[6].trim();
		return rule;
	}

	public static ScoreableRule parseJoshuaRecord(String str) throws RuleException {

		ScoreableRule rule = new ScoreableRule();
		String[] columns = MyUtils.split(str, " ||| ", Integer.MAX_VALUE);

		if (columns.length != 4) {
			throw new RuleException("Rule has " + columns.length + " fields, but was expecting 4: "
					+ str);
		}

		// No type?
		rule.consequent = columns[0];
		rule.srcAntecedents = columns[1];
		rule.tgtAntecedents = columns[2];
		rule.features = columns[3];
		return rule;
	}

	private void cacheConsequents() throws RuleException {
		Matcher match = lhsPattern.matcher(consequent);

		if (match.matches() == false) {
			throw new RuleException("Badly formatted LHS label: " + consequent);
		}

		if (match.groupCount() != 2) {
			throw new RuleException("Expected 2 groups: " + consequent);
		}

		srcConsequent = match.group(1);
		tgtConsequent = match.group(2);
	}

	public static String getSourceLabel(String nonterm) throws RuleException {
		return getLabel(nonterm, 1);
	}

	public static String getTargetLabel(String nonterm) throws RuleException {
		return getLabel(nonterm, 2);
	}

	public static String getLabelAlignment(String nonterm) throws RuleException {
		return getLabel(nonterm, 3);
	}

	private static String getLabel(String nonterm, int group) throws RuleException {
		Matcher match = antecedentNontermPattern.matcher(nonterm);

		if (match.matches() == false) {
			throw new RuleException("Badly formatted LHS label: " + nonterm);
		}

		if (match.groupCount() != 3) {
			throw new RuleException("Expected 3 groups: " + nonterm);
		}

		return match.group(group);
	}

	public String getSourceConsequent() throws RuleException {
		if (srcConsequent == null) {
			cacheConsequents();
		}
		return srcConsequent;
	}

	public String getTargetConsequent() throws RuleException {
		if (tgtConsequent == null) {
			cacheConsequents();
		}
		return tgtConsequent;
	}

	public static float[] tokenizeFeatures(String str, String delims, int minArrSize) {

		StringTokenizer tok = new StringTokenizer(str, delims);
		final int nToks = tok.countTokens();

		int arrSize = Math.max(minArrSize, nToks);
		float[] toks = new float[arrSize];

		// if (nToks < arrSize) {
		// throw new
		// RuntimeException("Not enough features in rule. expected size is " +
		// arrSize
		// + "found " + nToks);
		// }
		for (int i = 0; i < nToks; i++) {
			toks[i] = Float.parseFloat(tok.nextToken());
		}
		return toks;
	}

	public String[] getSourceAntecedents() {
		if (srcAntecedentsArr == null)
			srcAntecedentsArr = MyUtils.tokenize(srcAntecedents, " ");
		return srcAntecedentsArr;
	}

	public String[] getTargetAntecedents() {
		if (tgtAntecedentsArr == null)
			tgtAntecedentsArr = MyUtils.tokenize(tgtAntecedents, " ");
		return tgtAntecedentsArr;
	}

	public float getFeature(String featName, FeatureManager fman) {
		int i = fman.featIndexManager.get(featName);
		cacheFeatures(fman);
		return featuresArr[i];
	}

	private void cacheFeatures(FeatureManager fman) {
		if (featuresArr != null && featuresArr.length < fman.featIndexManager.size()) {
			updateBeforeSerializing();
			featuresArr = null;
		}
		if (featuresArr == null) {
			featuresArr = tokenizeFeatures(features, " ", fman.featIndexManager.size());
		}
	}

	public float getSufficientStat(String statName, FeatureManager fman) {
		int i = fman.statIndexManager.get(statName);
		cacheStats(fman);
		return statsArr[i];
	}

	private void cacheStats(FeatureManager fman) {
		if (statsArr != null && statsArr.length < fman.statIndexManager.size()) {
			updateBeforeSerializing();
			statsArr = null;

			// // this can happen if we initialized just for counts
			// float[] bigger = new float[fman.statIndexManager.size()];
			// System.arraycopy(statsArr, 0, bigger, 0, statsArr.length);
			// statsArr = bigger;
		}

		if (statsArr == null) {
			statsArr = tokenizeFeatures(suffStats, " ", fman.statIndexManager.size());
		}
	}

	public float getCount(FeatureManager fman) {
		return getSufficientStat(COUNT, fman);
	}

	public void setCount(int i) {
		if (statsArr == null) {
			statsArr = new float[1];
		}
		statsArr[0] = i;
		changed = true;
	}

	public void setCount(float value, FeatureManager fman) {
		setSufficientStat(COUNT, value, fman);
	}

	public void setSufficientStat(String statName, float value, FeatureManager fman) {
		int i = fman.statIndexManager.get(statName);
		cacheStats(fman);
		statsArr[i] = value;
		changed = true;
	}

	public void setFeature(String featName, float value, FeatureManager fman) throws RuleException {
		int i = fman.featIndexManager.get(featName);
		cacheFeatures(fman);
		featuresArr[i] = value;
		changed = true;

		checkDoubleSanity(value);
	}

	public static void checkProbSanity(double value) throws RuleException {
		checkDoubleSanity(value);
		if (value == 0.0) {
			throw new RuleException(
					"Probability should not be 0.0 (due to bad interaction with the log function)");
		}
		if (value < 0.0) {
			throw new RuleException("Probability should not be more than 0.0");
		}
		if (value > 1.0) {
			throw new RuleException("Probability should not be greater than 1.0");
		}
	}

	public static void checkDoubleSanity(double value) throws RuleException {
		if (Double.isInfinite(value)) {
			throw new RuleException("Infinite value for feature value");
		}
		if (Double.isNaN(value)) {
			throw new RuleException("NaN value for feature value");
		}
	}

	public static boolean isNonterminal(String str) {
		if (str.length() < 3) {
			return false;
		} else {
			if (str.charAt(0) == '[' && str.charAt(str.length() - 1) == ']') {
				return true;
			} else {
				return false;
			}
		}
	}

	public static Type parseType(String str) throws RuleException {
		if (str == null) {
			return null;
		} else {
			str = str.toLowerCase();
			if (str.startsWith("g")) {
				return Type.GRAMMAR;
			} else if (str.startsWith("l")) {
				return Type.LEXICON;
			} else if (str.startsWith("p")) {
				return Type.PHRASE;
			} else if (str.startsWith("h")) {
				return Type.HIERO;
			} else {
				throw new RuleException("Unknown type: " + str);
			}
		}
	}

	public static String typeToString(Type t) throws RuleException {
		if (t.equals(Type.GRAMMAR)) {
			return "G";
		} else if (t.equals(Type.LEXICON)) {
			return "L";
		} else if (t.equals(Type.PHRASE)) {
			return "P";
		} else if (t.equals(Type.HIERO)) {
			return "H";
		} else {
			throw new RuleException("Unknown type: " + t);
		}
	}

	public String toJoshuaString() {
		updateBeforeSerializing();
		String str =
				MyUtils.untokenize(" ||| ", consequent, srcAntecedents, tgtAntecedents, features);
		return str;
	}

	public static String escapeConsequentForCdec(String nonterm) throws RuleException {
		return nonterm.replace(",", "COMMA").replace("*", "AST");
	}

	public static String escapeAntecedentForCdec(String nonterm) throws RuleException {

		// exclude the comma and alignment index from escaping
		int comma = nonterm.lastIndexOf(',');
		String before = nonterm.substring(0, comma);
		String commaAndAfter = nonterm.substring(comma);
		
		String escapedBefore = escapeConsequentForCdec(before);
		return escapedBefore + commaAndAfter;
	}

	private static String escapeAntecedentsForCdec(String strCon) throws RuleException {
		String[] cons = MyUtils.tokenize(strCon, " ");
		for (int i = 0; i < cons.length; i++) {
			if(isNonterminal(cons[i])) {
				cons[i] = escapeAntecedentForCdec(cons[i]);
			}
		}
		return MyUtils.untokenize(" ", cons);
	}

	public String toCdecString(FeatureManager fman) throws RuleException {
		updateBeforeSerializing();
		String cdecFeatures = getCdecFeatures(fman);
		String lhs = escapeConsequentForCdec(consequent);
		String src = escapeAntecedentsForCdec(srcAntecedents);
		String tgt = escapeAntecedentsForCdec(tgtAntecedents);
		String str = MyUtils.untokenize(" ||| ", lhs, src, tgt, cdecFeatures);
		return str;
	}

	public String toMosesString() {
		updateBeforeSerializing();
		String probSpaceFeatures = getProbSpaceFeatures();
		String str = MyUtils.untokenize(" ||| ", srcAntecedents, tgtAntecedents, probSpaceFeatures);
		return str;
	}

	private String getCdecFeatures(FeatureManager fman) {

		cacheFeatures(fman);

		// stringify them
		List<String> featureNames = fman.featIndexManager.indices;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < featureNames.size(); i++) {

			String featureName = featureNames.get(i);
			// make sure the feature name doesn't contain the special character
			// =
			featureName = featureName.replace('=', '_');

			builder.append(featureName + "=" + featuresArr[i]);
			if (i != featureNames.size() - 1) {
				builder.append(" ");
			}
		}
		String strFeatures = builder.toString();

		// we don't want -0.0
		strFeatures = replaceWeirdZeroVariants(strFeatures);

		return strFeatures;
	}

	private String getProbSpaceFeatures() {

		// first load in negative log space features
		float[] probSpaceFeatures = tokenizeFeatures(features, " ", 0);

		// now exponentiate them
		for (int i = 0; i < probSpaceFeatures.length; i++) {
			// features are stored as -log10, so just reverse the operations
			// for indicator features, they will now just be a different
			// constant so MERT can still scale them as befure
			probSpaceFeatures[i] = (float) Math.pow(10, -probSpaceFeatures[i]);
		}

		// stringify them
		String strFeatures = MyUtils.untokenize(" ", probSpaceFeatures);

		// we don't want -0.0
		strFeatures = replaceWeirdZeroVariants(strFeatures);

		return strFeatures;
	}

	public String toHadoopRecordString() throws RuleException {
		updateBeforeSerializing();

		if (type == null) {
			throw new RuleException("HadoopRecord format requires non-null type");
		}
		String str =
				MyUtils.untokenize(" ||| ", typeToString(type), consequent, srcAntecedents,
						tgtAntecedents, features, alignment, suffStats);
		return str;
	}

	private void updateBeforeSerializing() {
		if (changed) {
			if (srcAntecedentsArr != null) {
				srcAntecedents = MyUtils.untokenize(" ", srcAntecedentsArr);
			}
			if (tgtAntecedentsArr != null) {
				tgtAntecedents = MyUtils.untokenize(" ", tgtAntecedentsArr);
			}
			if (featuresArr != null) {
				features = MyUtils.untokenize(" ", featuresArr);

				// we don't want -0.0
				// some decoders (I'm looking at you Joshua)
				// crash in bizzare ways if it finds these
				features = replaceWeirdZeroVariants(features);
			}
			if (statsArr != null) {
				suffStats = MyUtils.untokenize(" ", statsArr);
			}

			if (alignmentMap != null) {
				alignment = alignmentMap.toString();
			}
			changed = false;
		}
	}

	public static String replaceWeirdZeroVariants(String features) {
		features = features.replace("-0.0", "0");
		features = features.replace("0E-0", "0");
		features = features.replace("0E0", "0");
		return features;
	}

	public String getConsequent() {
		return consequent;
	}

	public WordAlignment getAlignment() {
		if (alignmentMap == null) {
			alignmentMap = new WordAlignment(alignment);
		}
		return alignmentMap;
	}

	public void setAlignmentString(String alignment2) {
		this.alignment = alignment2;
		changed = true;
	}

	public String toString() {
		try {
			return toHadoopRecordString();
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getLabelfromAntecedent(String ant) throws RuleException {
		if (isNonterminal(ant) == false) {
			throw new RuleException("Not a nonterminal: " + ant);
		}
		int comma = ant.lastIndexOf(',');
		if (comma == -1) {
			throw new RuleException("Not an antecedent nonterminal: " + ant);
		}
		String label = ant.substring(1, comma);
		return label;
	}

	public static String getLabelfromConsequent(String con) throws RuleException {
		if (isNonterminal(con) == false) {
			throw new RuleException("Not a nonterminal: " + con);
		}
		if (con.contains(",")) {
			throw new RuleException("Not a consequent nonterminal: " + con);
		}
		String noBraces = con.substring(1, con.length() - 1);
		return noBraces;
	}

	public static boolean isSyntactic(ScoreableRule rule) {
		return !rule.consequent.equals("[PHR]") && !rule.consequent.equals("[PHR::PHR]")
				&& rule.type != Type.HIERO;
	}

	public static boolean syntacticLabelExists(ScoreableRule rule, FeatureManager fman) {
		int exists =
				(int) rule.getSufficientStat(SuffStatMapper.SYNTACTIC_LABEL_FOR_PAIR_EXISTS, fman);
		return (exists == 1);
	}

	public List<Integer> getNonterminalAlignment() throws RuleException {
		List<Integer> list = new ArrayList<Integer>();
		for (String symbol : getTargetAntecedents()) {
			if (isNonterminal(symbol)) {
				String who = getLabelAlignment(symbol);
				int n = Integer.parseInt(who);
				list.add(n);
			}
		}
		return list;
	}

	public int getNonterminalCount() {
		int nonterms = 0;
		for (String tok : getSourceAntecedents()) {
			if (isNonterminal(tok)) {
				nonterms++;
			}
		}
		return nonterms;
	}

	public static int countTerminals(String[] toks) {
		int n = 0;
		for (String tok : toks) {
			if (isNonterminal(tok) == false) {
				n++;
			}
		}
		return n;
	}

	public int getSourceTerminalCount() {
		return countTerminals(getSourceAntecedents());
	}

	public int getTargetTerminalCount() {
		return countTerminals(getTargetAntecedents());
	}

	public static void main(String[] args) throws Exception {
		System.out.println(escapeAntecedentsForCdec("[NN::NN,1] [PP::PP,2] [$,::,,2]"));
	}
}
