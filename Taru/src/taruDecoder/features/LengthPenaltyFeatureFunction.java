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
public class LengthPenaltyFeatureFunction {

	// Feature related info 
	public static double lengthratio = 0.0;
	
	private static HashMap<String, Double> features;
	static {
		features = new HashMap<String, Double>(1);
	}
	
	/* (non-Javadoc)
	 * @see taruDecoder.features.FeatureFunction#computeFeature(taruDecoder.Hypothesis, taruDecoder.Hypothesis, int)
	 */
	public static HashMap<String, Double> computeFeature(Hypothesis h, Hypothesis h1, Hypothesis h2,String ruleId) {
		features.clear();
		double lp = 0.0;
		lp = lengthPenalty(h.getSrcLength(), h.getLength());
		features.put("LP", lp);	
		return features;
	}
	
	public static double computeFeature(Hypothesis h) {
		double lp = 0.0;
		lp = lengthPenalty(h.getSrcLength(), h.getLength());		
		return lp;
	}
	
	public static double lengthPenalty(int srclength, int tgtlength){
	double lengthPenalty = 0;
	double lengthbonus = 0;
	double expectedratio = lengthratio;

		if (srclength == 0 || tgtlength == 0) {
			return -10;
		}
	
		double actualratio = (double) tgtlength / (double) srclength;
	
		// Length penalty
		if (actualratio == expectedratio) {
			lengthPenalty = 0;
		} else {
			if (actualratio > expectedratio) { 
				lengthbonus = Math.exp(expectedratio - actualratio);
			} else if (actualratio < expectedratio) {
				lengthbonus = Math.exp(actualratio - expectedratio);
			}
			
			if (lengthbonus > 0) {
				// Convert to log domain 
				lengthPenalty = Math.log10(lengthbonus);
			} else {
				lengthPenalty = -100;
			}
		}
	return lengthPenalty;
	}
}