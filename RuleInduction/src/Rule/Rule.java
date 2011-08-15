/*
 * Desc: Rule Learning using Seeded Version Spaces 
 *
 * Author: Vamshi Ambati 
 * Email: vamshi@cmu.edu 
 * Carnegie Mellon University 
 * Date: 27-Jan-2007
 */

package Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import Scoring.RuleException;
import Scoring.ScoreableRule;
import TreeParser.ParseTreeNode;

public class Rule implements Cloneable {
	// 1, 2 , 3
	public int ruleIndex;

	// NP, VP, S
	public String sType;
	public String tType;

	// Lexicalization (Introduced 3/Aug/2008)
	public String head = "";
	public String head_type = "";
	public String head_tgt = "";

	// Markovization
	public String parent_head = "";
	public String parent_type = "";

	// Features (22 February 2009)
	FeatureSet fs = null;

	// The Rule sequence could be -
	// a lEX RULE (all lexicals) - ["The" "red" "car"]
	// a FLAT RULE (lexicalized+POS) : ["The" ADJ N]
	// a FULL RULE (constituent level) : [DET ADJ N]
	public Vector<Constituent> slRuleSequence = new Vector<Constituent>();
	public Vector<Constituent> tlRuleSequence = new Vector<Constituent>();

	// 1. SL: The red car
	public Vector<String> slExamples = new Vector<String>();
	// 1. TL: lAal ghAdi
	public Vector<String> tlExamples = new Vector<String>();

	// ((1,1),(2,2)) - comes from Corpus as is
	public Alignment alignment;

	// Constructors
	public Rule(int id, ParseTreeNode ptn, Vector<Constituent> slV, Vector<Constituent> tlV,
			Alignment amap) throws RuleLearnerException {
		ruleIndex = id;
		sType = ptn.nodetype;
		tType = ptn.tnodetype;

		slExamples.add(ptn.getS().toString());
		tlExamples.add(ptn.getT().toString());

		alignment = amap;

		slRuleSequence = slV;
		tlRuleSequence = tlV;

		// Any other features accumulated as part of Rule Learning (Dynamically
		// stored into features)
		fs = new FeatureSet();
		// Copy over ?? Point to Context features for this Rule
		fs = ptn.fs;
		
		if(ScoreableRule.allowBugs == false && amap.AMap.size() == 0) {
			throw new RuleLearnerException("Rule must have at least one alignment link: " + toHashString());
		}
	}

	public Rule() {
		fs = new FeatureSet();
	}

	public Rule reverseClone() {
		Rule rev_rule = new Rule();
		// Start reverse cloning
		rev_rule.ruleIndex = ruleIndex;
		rev_rule.sType = tType;
		rev_rule.tType = sType;

		rev_rule.slRuleSequence = tlRuleSequence;
		rev_rule.tlRuleSequence = slRuleSequence;

		rev_rule.slExamples = tlExamples;
		rev_rule.tlExamples = slExamples;

		rev_rule.alignment = alignment.reverseClone();
		return rev_rule;
	}

	public String toConciseString(int FEATURES_FLAG) {
		String rule = "";
		rule += sType + "\t" + tType + "\t[";
		// SL Rule Sequence
		for (int i = 0; i < slRuleSequence.size(); i++) {
			rule += slRuleSequence.elementAt(i).toString() + " ";
		}
		rule += "]\t[";
		for (int i = 0; i < tlRuleSequence.size(); i++) {
			rule += tlRuleSequence.elementAt(i).toString() + " ";
		}
		// TL Rule Sequence
		rule += "]\t{";

		if (alignment != null) {
			rule += alignment.toConciseString();
		}
		rule += "}";

		if (FEATURES_FLAG == 1) {
			// if(head!=""){
			// rule += "\t("+head_type +" *"+ head +"*)";
			// rule += "\t("+head_type +" *"+ head_tgt +"*)";
			// }
			rule += "\t||| " + fs.toString();
		}
		rule += "\t||| " + slExamples.toString();

		rule += "\n";
		return rule;
	}

	public String toString() {
		String rule = "";
		rule += "{" + sType + "," + ruleIndex + "}\n";

		/*
		 * if(slExamples.size()>0) rule +=
		 * ";;SL: "+slExamples.elementAt(0)+"\n"; if(tlExamples.size()>0) rule
		 * += ";;TL: "+tlExamples.elementAt(0)+"\n";
		 */

		rule += sType + "::" + tType + "\t";
		// SL Rule Sequence
		rule += "[";
		for (int i = 0; i < slRuleSequence.size(); i++) {
			rule += slRuleSequence.elementAt(i).toString() + " ";
		}
		rule += "] -> [";
		for (int i = 0; i < tlRuleSequence.size(); i++) {
			rule += tlRuleSequence.elementAt(i).toString() + " ";
		}
		// TL Rule Sequence
		rule += "]\n";

		rule += "(\n";
		// Lexicalization related
		if (head != "") {
			rule += "(*head* " + head + ")\n";
		}
		if (head_type != "") {
			rule += "(*headtype* " + head_type + ")\n";
		}
		if (head_tgt != "") {
			rule += "(*tgthead* " + head_tgt + ")\n";
		}
		// Markovization related
		// if(parent_type!=""){
		// rule += "(*parent* "+ parent_type +")\n";
		// }
		// Other features TODO

		// Alignments as simple string
		if (alignment != null) {
			rule += alignment.toString();
		}
		rule += ")\n\n";
		return rule;
	}

	public String toHashString() {

		String[] src = new String[slRuleSequence.size()];
		String[] tgt = new String[tlRuleSequence.size()];
		int[] xAlign = new int[slRuleSequence.size()];
		int[] yAlign = new int[tlRuleSequence.size()];

		// put plain, unescaped words in src/tgt and -1 in alignment arrays
		initArrays(src, xAlign, slRuleSequence);
		initArrays(tgt, yAlign, tlRuleSequence);

		int nextAlignment = 1;
		for (Entry<Integer, Vector<Integer>> entry : alignment.AMap.entrySet()) {
			Integer x = entry.getKey();
			boolean isNonterm = (slRuleSequence.get(x - 1).type > 0);
			if (isNonterm) {
				for (Integer y : entry.getValue()) {
					xAlign[x - 1] = nextAlignment;
					yAlign[y - 1] = nextAlignment;
					nextAlignment++;
				}
			}
		}

		String srcStr = formatSourceRuleSequence(src, xAlign);
		String tgtStr = formatSourceRuleSequence(tgt, yAlign);

		// make sure hashes don't make it through
		sType = sType.replace("#", "-HASH-");
		tType = tType.replace("#", "-HASH-");
		srcStr = srcStr.replace("#", "-HASH-");
		tgtStr = tgtStr.replace("#", "-HASH-");

		// TODO: For lexical items alignment is null
		String astr = "";
		if (alignment != null) {
			astr = alignment.toSMTString();
		}

		String rule =
				ruleIndex + " # " + sType + " # " + tType + " # " + srcStr + " # " + tgtStr + " # "
						+ astr + "\n";
		return rule;
	}

	public ScoreableRule toScoreableRule() throws RuleException {

		String[] src = new String[slRuleSequence.size()];
		String[] tgt = new String[tlRuleSequence.size()];
		int[] xAlign = new int[slRuleSequence.size()];
		int[] yAlign = new int[tlRuleSequence.size()];

		// put plain, unescaped words in src/tgt and -1 in alignment arrays
		initArrays(src, xAlign, slRuleSequence);
		initArrays(tgt, yAlign, tlRuleSequence);

		int nextAlignment = 1;

		List<Entry<Integer, Vector<Integer>>> sortedReverseAlignment =
				new ArrayList<Entry<Integer, Vector<Integer>>>(alignment.reverseClone().AMap.entrySet());
		
		Collections.sort(sortedReverseAlignment, new Comparator<Entry<Integer, Vector<Integer>>>() {
			public int compare(Entry<Integer, Vector<Integer>> a,
					Entry<Integer, Vector<Integer>> b) {
				
				return a.getKey().compareTo(b.getKey());
			}
		});
		
		if(ScoreableRule.allowBugs == false && sortedReverseAlignment.size() == 0) {
			throw new RuntimeException("A rule must have at least one alignment link: " + this.toHashString());
		}

		// iterate in order on the rule learner "tgt" side (actually, the source translation side)
		// to assign non-terminal ID's sequentially on the source translation side
		for (Entry<Integer, Vector<Integer>> entry : sortedReverseAlignment) {
			Integer y = entry.getKey();
			Constituent tgtConstit = tlRuleSequence.get(y - 1);

			if (tgtConstit.isLexical() == false) {
				assert entry.getValue().size() == 1;
				int x = entry.getValue().get(0);

				// assign sequential IDs to aligned non-terminals
				xAlign[x - 1] = nextAlignment;
				yAlign[y - 1] = nextAlignment;
				nextAlignment++;

				// merge source and target non-terminals into a single joint
				// non-terminal
				String jointNonterm = tgt[y - 1] + "::" + src[x - 1];
				src[x - 1] = jointNonterm;
				tgt[y - 1] = jointNonterm;
			}
		}

		String srcStr = formatSourceRuleSequence(src, xAlign);
		String tgtStr = formatSourceRuleSequence(tgt, yAlign);

		String astr = "";
		if (alignment != null) {
			astr = alignment.reverseClone().toSMTString();
		}

		String consequent = "[" + tType + "::" + sType + "]";
		ScoreableRule rule = new ScoreableRule(null, consequent, tgtStr, srcStr, "", astr, "");
		rule.setCount(1);
		return rule;
	}

	private static void initArrays(String[] src, int[] xAlign, Vector<Constituent> slRuleSequence) {

		for (int i = 0; i < xAlign.length; i++) {

			// output just the plain word, no escaping
			src[i] = slRuleSequence.get(i).word;
			xAlign[i] = -1;
		}
	}

	private String formatSourceRuleSequence(String[] src, int[] xAlign) {
		StringBuilder srcBuilder = new StringBuilder(10 * src.length);
		for (int i = 0; i < src.length; i++) {
			if (xAlign[i] == -1) {
				srcBuilder.append(src[i] + " ");
			} else {
				srcBuilder.append("[" + src[i] + "," + xAlign[i] + "] ");
			}
		}
		return srcBuilder.toString().trim();
	}

	// TODO: Printing to SMT format phrase table
	public String toPtableString() {
		String rule = "";
		rule += sType + "\t" + tType + "\t";
		// SL Rule Sequence
		for (int i = 0; i < slRuleSequence.size(); i++) {
			rule += slRuleSequence.elementAt(i).toString() + " ";
		}
		rule += "\t";
		for (int i = 0; i < tlRuleSequence.size(); i++) {
			rule += tlRuleSequence.elementAt(i).toString() + " ";
		}
		rule += "\t";

		// TODO: For lexical items alignment is null
		if (alignment != null) {
			rule += alignment.toSMTString() + "\t";
		}
		// head annotation information -
		// Lexicalization related
		if (head_tgt != "") {
			rule += "\t" + head_tgt;
		}
		return rule + "\n";
	}

	public String toLexString() {
		String rule = "";
		rule += "{" + sType + "," + ruleIndex + "}\n";

		/*
		 * if(slExamples.size()>0) rule +=
		 * ";;SL: "+slExamples.elementAt(0)+"\n"; if(tlExamples.size()>0) rule
		 * += ";;TL: "+tlExamples.elementAt(0)+"\n";
		 */

		rule += sType + "::" + tType + " |: ";
		// SL Rule Sequence
		rule += "[";
		for (int i = 0; i < slRuleSequence.size(); i++) {
			rule += slRuleSequence.elementAt(i).toString() + " ";
		}
		rule += "] -> [";
		for (int i = 0; i < tlRuleSequence.size(); i++) {
			rule += tlRuleSequence.elementAt(i).toString() + " ";
		}
		// TL Rule Sequence
		rule += "]\n";

		rule += "(\n";
		// Alignments as simple string
		if (alignment != null) {
			rule += alignment.toString();
		}
		rule += ")\n\n";

		return rule;
	}

	public Object clone() {
		Rule r = null;
		try {
			r = (Rule) super.clone();
		} catch (Exception e) {
		}
		return r;
	}

	// Get methods
	public String getSLType() {
		return sType;
	}

	public String getTLType() {
		return tType;
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public Vector<Constituent> getSLRuleSequence() {
		return slRuleSequence;
	}

	public Vector<Constituent> getTLRuleSequence() {
		return tlRuleSequence;
	}

	// Set methods
	public void setTLType(String str) {
		tType = str;
	}

	public void setSLType(String str) {
		sType = str;
	}

	public void addSExample(String eg) {
		slExamples.add(eg);
	}

	public void setHead(String in) {
		head = in;
	}

	public void setTargetHead(String in) {
		head_tgt = in;
	}

	public void setHeadType(String in) {
		head_type = in;
	}

	public void setParentType(String in) {
		parent_type = in;
	}

	public void setParentHeadType(String in) {
		parent_head = in;
	}

	// TODO: get the type of the target head
	public void setType_tgtHead() {
	}

	public void addTExample(String eg) {
		tlExamples.add(eg);
	}

	// TODO: Not complete
	// public boolean equals(Rule r){
	// if(this.sType.equals(r.sType) &&
	// this.tType.equals(r.tType) &&
	// this.slRuleSequence.equals(r.slRuleSequence) &&
	// this.tlRuleSequence.equals(r.tlRuleSequence) &&
	// this.alignment.equals(r.alignment)
	// )
	// {
	// return true;
	// }
	// return false;
	// }

	// //This could be a good hash function
	// public int hashCode() {
	// String varstr = sType + tType + slRuleSequence.toString() +
	// tlRuleSequence.toString()+alignment.toSMTString();
	// return varstr.hashCode();
	// }

	// public boolean equals(Rule r)
	// {
	// if(this.hashCode()==r.hashCode()){
	// return true;
	// }
	// return false;
	// }

	public String getKey() {
		String key =
				sType + ":" + tType + ":" + slRuleSequence.toString() + ":"
						+ tlRuleSequence.toString() + ":" + alignment.toSMTString();
		return key;
	}
}
