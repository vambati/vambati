package query.sentence;

import java.util.Random;

import data.*;
import query.*;

public class Rand implements QuerySelector {
	Random randomGenerator = null;
	UDS unlabeled = null; 
	public Rand(UDS unlabeled){
		randomGenerator= new Random();
		this.unlabeled = unlabeled;
	}
	
	/* Assign a random score to each of the sentences in a range of DATA SIZE  
	 * When scored and sorted, it emulates sorta random selection 
	 */
	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		int range = unlabeled.data.size(); 
	    e.score = randomGenerator.nextInt(range);
	    //System.err.println("Generated : "+e.score);	 
		return e.score;
	}
}