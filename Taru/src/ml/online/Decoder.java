/**
 * 
 */
package ml.online;

import java.util.List;

import ml.utils.SparseVector;


import taruHypothesis.Hypothesis;

/**
 * @author abhayaa
 *
 */
public interface Decoder {
	// This function will return the feature vector
	// corresponding to top kbest hyps according to 
	// the given model
	public List<Hypothesis> decode(String t, int kbest);
	
	// This function returns the feature vector towards which
	// update should be done. Since many different startegies
	// can be used for deciding the target, decoder should 
	// handle this.
	public SparseVector getTargetFeatures(String src, String ref);
}
