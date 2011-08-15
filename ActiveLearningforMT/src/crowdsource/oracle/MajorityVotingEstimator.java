package crowdsource.oracle;

import java.util.Hashtable;

import crowdsource.HIT;
import crowdsource.validate.*;

/* Class to estimate the accuracy of an Oracle
 * 1. Match against some gold standard and compute  - BLEU ? precision or recall
 * 2. 
 */
public class MajorityVotingEstimator implements Estimator{

	Hashtable<String,HIT> hits = null; 
	Validator validator = null;
	
	public MajorityVotingEstimator(Hashtable<String,HIT> hits,Validator validator){
		this.hits = hits;
		this.validator = validator;
	}
	
	public void computeScore(Oracle x){
		// Compute reliablity of oracles by Agreement 
		x.reliability = x.agreement / x.submitted;
	}
}
