package query.alignment;

import java.util.Random;
import data.*;
import query.*;

public class Rand implements QuerySelector {
	Random randomGenerator = null;
	AlignmentData unlabeled = null; 
	
	public Rand(AlignmentData unlabeled){
		randomGenerator= new Random();
		this.unlabeled = unlabeled;
	}
	
	/* Assign a random score to each of the sentences in a range of DATA SIZE  
	 * When scored and sorted, it emulates sorta random selection 
	 */
	public double computeScore(Entry e) {
		int range = unlabeled.size(); 
	    e.score = randomGenerator.nextInt(range);
	    //System.err.println("Generated : "+e.score);	 
		return e.score;
	}
}