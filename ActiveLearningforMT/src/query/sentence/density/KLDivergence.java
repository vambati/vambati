package query.sentence.density;

import java.util.Iterator;

import query.QuerySelector;

import data.*;


public class KLDivergence implements QuerySelector {
	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	UDS U = null; 
	
	public KLDivergence(LDS l, UDS u){
		L = l;
		U = u; 
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double kldiv = 0.0; 
		
		Iterator<String> iter = e.PHRASES.iterator();
		for(String x: e.PHRASES){
			// Unlabeled data computation
			double p = 0.5 / U.TOTAL_NGRAMS_UNLABELED; // stupid backoff
			if(U.NGRAMS_UNLABELED.containsKey(x)){
				p = U.NGRAMS_UNLABELED.get(x) / U.TOTAL_NGRAMS_UNLABELED;
			} 
			
			// Labeled Data computation
			double q = 0.5  / L.TOTAL_NGRAMS_EXISTING; // stupid backoff
			if(L.NGRAMS_EXISTING.containsKey(x)){
				q = L.NGRAMS_EXISTING.get(x)  / L.TOTAL_NGRAMS_EXISTING;
			} 
			
			kldiv += p * (Math.log (p) - Math.log(q));
		}

		e.score =  kldiv / e.PHRASES.size(); // Normalize this (should be done in the distribution, but factor it out
		
		return e.score;
	}
}
