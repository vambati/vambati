package qual;

import java.util.Hashtable;

/* Class to estimate the accuracy of an Oracle
 * 1. Match against some gold standard and compute  - BLEU ? precision or recall
 * 2. 
 */
public class MajorityVotingEstimator {

	Hashtable<String,HIT> hits = null; 
	TranslationChecker validator = null;
	
	public MajorityVotingEstimator(Hashtable<String, qual.HIT> hits2,TranslationChecker validator2){
		this.hits = hits2;
		this.validator = validator2;
	}
	
	public void computeScore(Oracle x){
		// Compute reliablity of oracles by Agreement 
		x.reliability = x.agreement / x.submitted;
	}
}
