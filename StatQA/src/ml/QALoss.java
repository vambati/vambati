package ml;

import qa.*;
import ml.utils.LossInterface;


/**
 * LossInterface is an interface for computing loss between the right
 * label, the classified label and the input.
 */
public class QALoss implements LossInterface {

	/**
	 * Method for calculating the loss
	 *
	 * @param x Input instance
	 * @param yRight correct output
	 * @param yLearned classified output
	 */
	// This is negative-LOSS = (Benefit)  
	public double loss(Answer yRight, Answer yLearned){
		// 0 1 - loss 
		if(yRight.id==-1 || yLearned.id==-1){
			// This can not be right
			System.err.print("NO..");
			System.exit(0);
		} else if(yRight.id == yLearned.id){
			return 0;
		}else{
			return 1;
		}
		
		return -1;
		// Rank difference (Hinge loss)
	}
}
