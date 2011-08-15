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
public class RuleScoreFeatureFunction {
	
	private static double BACKOFF = Math.log10(0.0001);

	private static HashMap<String, Double> features;
	static {
		features = new HashMap<String, Double>(1);
	}
	
	/* (non-Javadoc)
	 * @see taruDecoder.features.FeatureFunction#computeFeature(taruDecoder.Hypothesis, taruDecoder.Hypothesis, int)
	 */
	public static HashMap<String, Double> computeFeature(Hypothesis h, Hypothesis h1, Hypothesis h2,String ruleId) {
	//	System.err.println("Called RuleScore function - 2");
		features.clear();
		
		double sgt = BACKOFF;
		double tgs = BACKOFF;
		double rc = 0.0;
		if(ruleId.equalsIgnoreCase(Grammar.CONNECT_EDGE)){
			tgs = BACKOFF;
			sgt = BACKOFF;
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
		features.put("TGS", tgs);
		// SGT feature
		features.put("SGT", sgt);
		
		features.put("RC", rc);
		
		return features;
	}
	
	public static HashMap<String, Double> computeFeature(Hypothesis h, Hypothesis h1, String ruleId) {
		// System.err.println("Called RuleScore function - 1");
		features.clear();
		
		double sgt = BACKOFF;
		double tgs = BACKOFF;
		double rc = 0.0;
		if(ruleId.equalsIgnoreCase(Grammar.CONNECT_EDGE)){
			sgt = BACKOFF;
			tgs = BACKOFF;
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
		
		features.put("RC", rc);
		
		return features;
	}
}