/**
 * 
 */
package ml;

import java.util.List;

import ml.utils.SparseVector;

import qa.*;
/**
 * @author Vamshi
 *
 */
public interface Decoder {
	// This function will return the feature vector
	// corresponding to top kbest hyps according to 
	// the given model
	public List<Answer> decode(Question q,int k) throws Exception;
	
	public Answer decode1best(Question q) throws Exception;
	
	// Decode a list of inputs at a time : And possibly save to some file as implemented
	public List<Answer> decodeAll(List<Question> q, int kbest) throws Exception;
	
	// This function returns the feature vector towards which
	// update should be done. Since many different strategies
	// can be used for deciding the target, decoder should 
	// handle this.
	public SparseVector getTargetFeatures(Question src, Answer ref) throws Exception;
	
	public void setWeights(SparseVector wts);
	
	public SparseVector getWeights();
}
