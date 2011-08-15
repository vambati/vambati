package query.phrase;

import query.sentence.density.DensityQuerySelector;
import data.*;

public class DDWDS extends DensityQuerySelector {

	LDS L = null;
	public DDWDS(String ngFile,LDS l){
		super(ngFile);
		L = l; 
	}
	
	public double computeScore(Entry e) {
		TranslationEntry te = (TranslationEntry)e;
		
		double dscore = 1/NGRAMS.size();
		double uscore = 0;
		
		// Phrase is the entire string 
		String p = te.source;
		 
			if(! L.NGRAMS_EXISTING.containsKey(p)){
				uscore++;
			}
			
			if(NGRAMS.containsKey(p)){
				// Original vote power
				double vote = NGRAMS.get(p) / TOTAL_NGRAMS;
				// Discount for existence in Labeled data
				double dim = 0;
				if(L.NGRAMS_EXISTING.containsKey(p)) {
					dim = L.NGRAMS_EXISTING.get(p);
				}
				vote*=Math.pow(2.7314,-1*dim);
				dscore+= vote;
			}

			// Normalize for Number of phrases possible   
		dscore = dscore / (double)te.PHRASES.size();
		uscore = uscore / (double)te.PHRASES.size();
			
		double beta = 1; 
		double score = (1+beta*beta) *dscore *uscore /((beta*beta) * dscore + uscore);
		
		return score;
	}
}