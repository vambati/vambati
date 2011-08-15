package query.sentence;

import query.*;
import data.*;


public class Diversity implements QuerySelector {

	public LDS L = null; 
	
	public Diversity(LDS l){
		L = l;
	}
	
	public double computeScore(Entry ex) {
		double score = 1/L.TOTAL_NGRAMS_EXISTING;
		
		TranslationEntry e = (TranslationEntry)ex;
 
		for(String p: e.PHRASES){
			if( L.NGRAMS_EXISTING.containsKey(p)){
				score+= Math.log( L.NGRAMS_EXISTING.get(p) / L.TOTAL_NGRAMS_EXISTING ); 
			}
		}
		   
		e.score = -1 * score; // Novelty is the opposite 
		
		e.score = e.score / e.PHRASES.size();
		  
		return e.score;
	}
}
