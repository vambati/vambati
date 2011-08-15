/**
 * 
 */
package taruDecoder.features;


import java.util.HashMap;

import taruHypothesis.Hypothesis;
import tarugrammar.Grammar;

/**
 * @author abhayaa
 *
 */
public class WordCountFeatureFunction {

	private static HashMap<String, Double> features;
	static {
		features = new HashMap<String, Double>(1);
	}
	
	/* (non-Javadoc)
	 * @see taruDecoder.features.FeatureFunction#computeFeature(taruDecoder.Hypothesis, taruDecoder.Hypothesis, int)
	 */
	public static HashMap<String, Double> computeFeature(Hypothesis h, Hypothesis h1, Hypothesis h2,String ruleId) {
		features.clear();
		
		double sgt = 0.0;
		double tgs = 0.0;
		double rc = 0.0;
		
		if(ruleId.equalsIgnoreCase(Grammar.CONNECT_EDGE)){
			tgs = 0.0;
			sgt = 0.0;
			rc = 0;
		}
		else {
			if(ruleId.equalsIgnoreCase(Grammar.GLUE_EDGE)) {
				tgs = Grammar.GLUE_RULE_SCORE;
				sgt = Grammar.GLUE_RULE_SCORE;
			}
			else {
				tgs = Grammar.getRuleScores(ruleId)[0];
				sgt = Grammar.getRuleScores(ruleId)[1];
			}
			// Only count rules that are not dummy connection edges
			rc = 1.0;
		}
		
		// TGS feature
		features.put("WC", tgs);
		
		return features;
	}
	
	public static HashMap<String, Double> computeFeature(Hypothesis h, Hypothesis h1, String ruleId) {
		// System.err.println("Called RuleScore function - 1");
		features.clear();
		
		double sgt = 0.0;
		double tgs = 0.0;
		if(ruleId.equalsIgnoreCase(Grammar.CONNECT_EDGE)){
			tgs = 0.0;
			sgt = 0.0;
		}
		else {
			if(ruleId.equalsIgnoreCase(Grammar.GLUE_EDGE)) {
				tgs = Grammar.GLUE_RULE_SCORE;
				sgt = Grammar.GLUE_RULE_SCORE;
			}
			else {
				tgs = Grammar.getRuleScores(ruleId)[0];
				sgt = Grammar.getRuleScores(ruleId)[1];
			}
		}
		
		// TGS feature
		features.put("TGS", tgs);
		
		// SGT feature
		features.put("SGT", sgt);
				
		return features;
	}
}
