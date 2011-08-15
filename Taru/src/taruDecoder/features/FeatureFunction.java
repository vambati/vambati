/**
 * 
 */
package taruDecoder.features;

import java.util.HashMap;

import taruHypothesis.Hypothesis;

/**
 * @author abhayaa
 *
 */
public interface FeatureFunction {
	public HashMap<String, Double> computeFeature(Hypothesis h1, Hypothesis h2, int edgeId);
	public HashMap<String, Double> computeFeature(Hypothesis h1, int edgeId);
}
