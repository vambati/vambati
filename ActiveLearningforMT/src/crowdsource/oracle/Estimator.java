package crowdsource.oracle;

/* Class to estimate the accuracy of an Oracle
 * 1. Match against some gold standard and compute  - BLEU ? precision or recall
 * 2. Majority Voting estimator
 * 3. IEThresholding Estimator
 */
public interface Estimator {
	// Computes Oracle's reliability 
	// Oracle contains the sufficient statistics required 
	public abstract void computeScore(Oracle oracle);
}
