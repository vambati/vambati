package query.sentence.density;

import java.util.Iterator;

import data.*;


public class DS extends DensityQuerySelector {
	
	public DS(String ngFile){
		super(ngFile);
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double score = 1/ TOTAL_NGRAMS;  
		
		Iterator<String> iter = e.PHRASES.iterator();
		while(iter.hasNext()){
			String p = iter.next();
			if(NGRAMS.containsKey(p)){
				// Original vote power
				score+= Math.log( (double) NGRAMS.get(p) / TOTAL_NGRAMS );
			}
		}
		// Normalize   
		e.score = score / e.PHRASES.size();
		return e.score;
	}
}
