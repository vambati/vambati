package ml;

import java.util.HashSet;
import java.util.Iterator;

import qa.*;
import ml.utils.LossInterface;


/**
 * LossInterface is an interface for computing loss between the right
 * label, the classified label and the input.
 */
public class QALossEditDistance implements LossInterface {

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
			return stringOverlap(yRight.aString, yLearned.aString);
		}
		return -1;
	}

	private double stringOverlap(String refString, String hypString) {
		// Compute edit distance 
		String[] refArr = refString.split("\\s+");
		String[] learnedArr = hypString.split("\\s+");
		HashSet<String> learnedHash = new HashSet<String>();
		HashSet<String> refHash = new HashSet<String>();
		
		for(String rw: refArr){
			refHash.add(rw);
		}
		for(String lw: learnedArr){
			learnedHash.add(lw);
		}
		double overlap = 0;
		Iterator<String> rit = refHash.iterator();
		while(rit.hasNext()){
			String rw = rit.next(); 
			if(learnedHash.contains(rw))
				overlap++;
		}
//		System.err.println("Ref:"+refString);
//		System.err.println("Learned:"+hypString);
//		System.err.println("Overlap:"+overlap);

		return (refHash.size() - overlap);
	}
}
