package query.sentence;

import query.*;
import data.*;


public class OOV implements QuerySelector {

	public LDS L = null; 
	
	public OOV(LDS l){
		L = l;
	}
	
	public double computeScore(Entry ex) {
		double score = 0; 
		
		TranslationEntry e = (TranslationEntry)ex;
 
		for(String p: e.PHRASES){
			if(! L.NGRAMS_EXISTING.containsKey(p)){
				score++; 
			}
		}
		e.score = score / e.PHRASES.size();
		  
		return e.score;
	}
}
