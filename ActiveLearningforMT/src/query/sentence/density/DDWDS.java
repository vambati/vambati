package query.sentence.density;

import java.util.Iterator;

import data.*;


public class DDWDS extends DensityQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	
	public DDWDS(String ngFile,LDS l){
		super(ngFile);
		L = l;
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double dscore = 1/NGRAMS.size(); // BACKOFF . Should not be a need ideally !!! LM built using the same right
		double uscore = 1;
		
		Iterator<String> iter = e.PHRASES.iterator();
		while(iter.hasNext()){
			String p = iter.next();
			
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
			}else{
				// System.err.println("Not match:"+p);
			}
		}
		// Normalize for Number of phrases possible   
		dscore = dscore / (double)e.PHRASES.size();
		uscore = uscore / (double)e.PHRASES.size();
		
		// System.err.println(" U:"+uscore+" D:"+dscore);
		
		// Combining in different ways 
		// Geometric mean 
		// e.score =  dscore * uscore;
		// Harmonic mean (Weighted)
		double beta = 1; 
		e.score = ((1+beta*beta) *dscore *uscore ) /((beta*beta) * dscore + uscore);
		
	//	System.out.println("\t"+e.source+" ||| U:"+uscore+" D:"+dscore+" ||| "+e.score);
		
		return e.score;
	}
}
