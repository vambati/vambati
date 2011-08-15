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
public class Perceptron extends AbstractOnlineTraining {

	public Perceptron(Decoder d){
		this.d = d;
	}
	/* (non-Javadoc)
	 * @see ml.online.AbstractOnlineTraining#update(java.lang.String, java.lang.String)
	 */
	@Override
	public void update(String src, String ref) {
		
		// Compute the argmax features using the current model
		List<Hypothesis> argMaxHyp = d.decode(src, 1);
		
		// If no hyps were generated, skip
		if(argMaxHyp.size() == 0)
			return;
		
		// Get the target feature vector
		// Determining the target is the responsibility of the decoder
		SparseVector targetFeat = d.getTargetFeatures(src, ref);
		
		targetFeat.subtract(argMaxHyp.get(0).getFeatures());
		targetFeat.scale(argMaxHyp.get(0).getWords().length());
		wts.add(targetFeat);
		System.out.println("Model updated to : " + wts + "\n");
	}
}
