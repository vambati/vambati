package query.phrase;

import query.*;
import data.*;


public class Diversity implements QuerySelector {

	public LDS L = null;
	
	public Diversity(LDS l){
		L = l;
	}
	
	public double computeScore(Entry ex) {
		double newphrases = 0;
		
		TranslationEntry e = (TranslationEntry)ex;
				
			String p = e.source;
			if(! L.NGRAMS_EXISTING.containsKey(p)){
				newphrases++;
			}
		
		newphrases = newphrases/ e.PHRASES.size();
		
		// Novelty over phrases possible   
		e.score = newphrases; 
		
		return e.score;
	}
}
