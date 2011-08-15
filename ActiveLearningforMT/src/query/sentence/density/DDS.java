package query.sentence.density;

import java.util.Iterator;

import data.*;


public class DDS extends DensityQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	
	public DDS(String ngFile,LDS l){
		super(ngFile);
		L = l;
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double score = 1/TOTAL_NGRAMS;
		
		Iterator<String> iter = e.PHRASES.iterator(); 
		while(iter.hasNext()){
			String p = iter.next();
			
			// Original vote power
			double vote = 1.0;
			if(NGRAMS.containsKey(p)){
				vote = NGRAMS.get(p) / TOTAL_NGRAMS;
				
				// Discount for existence in Labeled data
				double dim = 0;
				if(L.NGRAMS_EXISTING.containsKey(p)) {
					dim = L.NGRAMS_EXISTING.get(p);
				}
				vote = vote * Math.pow(2.7314,-1*dim);
			}
			score+= Math.log(vote);
		}
		// Normalize for Number of phrases possible   
		e.score = score / e.PHRASES.size();
		//System.err.println(" D:"+score);
		return e.score;
	}
}
