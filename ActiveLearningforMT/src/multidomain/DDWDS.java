package multidomain;

import java.util.Iterator;

import data.*;

// Multiple domain Sampler 
// Ideally an array of LDS should used

public class DDWDS extends DensityQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	LDS L2 = null;
	LDS L3 = null;
	
	// Vector<LDS> domains ; 
	
	public DDWDS(String ngFile,LDS l, LDS l2, LDS l3){
		super(ngFile);
		L = l;
		L2 = l2;
		L3 = l3;
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
				
				// Discount for existence in Labeled data (From both domains!) 
				double dim1 = 0, dim2 =0; 
				if(L.NGRAMS_EXISTING.containsKey(p)) {
					dim1 = L.NGRAMS_EXISTING.get(p);
				}
				if(L2.NGRAMS_EXISTING.containsKey(p)) {
					dim2 = L2.NGRAMS_EXISTING.get(p);
				}
				   
				// Discount differently for both domains . 
				// TODO: Ideally change the weight dynamically
				double dim = 0.75 * dim1 + 0.25 * dim2; 
				vote= vote * Math.pow(2.7314,-1*dim);
				
				
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
