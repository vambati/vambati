package query.sentence.density;

import java.util.Iterator;

import data.*;


/*
 * Mathias Eck , Stephan Vogel - 2005 IWSLT method
 */


public class MatEck extends DensityQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	
	public MatEck(String ngFile,LDS l){
		super(ngFile);
		L = l;
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double score = 0;
		Iterator<String> iter = e.PHRASES.iterator(); 
		while(iter.hasNext()){
			String p = iter.next();
			
			if(NGRAMS.containsKey(p) && !L.NGRAMS_EXISTING.containsKey(p)){
				score+= NGRAMS.get(p);;
			}
		}
		// Normalize for Number of phrases possible   
		e.score = score / e.PHRASES.size();
		return e.score;
	}
}
