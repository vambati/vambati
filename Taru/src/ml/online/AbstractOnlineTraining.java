/**
 * 
 */
package ml.online;

import java.util.ArrayList;

import drivers.TaruDriver;

import taruDecoder.Scorer;
import ml.utils.SparseVector;

/**
 * @author abhayaa
 *
 */
public abstract class AbstractOnlineTraining {
	
	// Decoder for computing argmax
	protected Decoder d;
	
	// Model parameters
	protected SparseVector wts;
	
	abstract public void update(String src, String ref);
	
	// Train on the given data for these many iterations
	public void trainLast(String [] src, String [] ref, int iteration){
		for(int t = 0; t < iteration; t++){
			for(int i = 0; i < src.length; i++){
				update(src[i],ref[i]);
			}
		}
	}

	// Train on the given data for these many iterations and keep the average
	public void trainAverage(ArrayList<String> src, ArrayList<String> ref, int iteration){

		// Get the starting model that Scorer has
		this.wts = Scorer.getScorer().getModel();
		
		// Sum of the coefficients from all runs
		SparseVector sum = new SparseVector();
		
		for(int t = 1; t <= iteration; t++){
			System.out.println("Iteration #"+t);
			for(int i = 0; i < src.size(); i++){
				System.out.println("Training Example #"+i);
				update(src.get(i),ref.get(i));
				sum.add(wts);
			}
			sum.scale(t*src.size());
			
			// Decode using sum
			Scorer.getScorer().setModel(sum);
				TaruDriver.main(new String[2]);
			sum.scale(1/t*src.size());
			
			// Restore back the original model
			Scorer.getScorer().setModel(wts);
		}
		sum.scale(iteration*src.size());
		wts = sum;
		save();
	}

	public void save(){
		Scorer.getScorer().writeModel();
	}

}
