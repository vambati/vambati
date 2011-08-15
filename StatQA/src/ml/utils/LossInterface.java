package ml.utils;
import qa.*;


/**
 * LossInterface is an interface for computing loss between the right
 * label, the classified label and the input.
 */
public interface LossInterface {

	/**
	 * Method for calculating the loss
	 *
	 * @param x Input instance
	 * @param yRight correct output
	 * @param yLearned classified output
	 */
	public double loss(Answer yRight, Answer yLearned);
}
