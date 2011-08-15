package query.sentence.density;

import java.util.Iterator;

import model.smt.PhraseTable;
import model.smt.TranslationLEXICON;

import data.*;


public class DDSPTABLE extends DensityQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	PhraseTable ptable = null;
	TranslationLEXICON lexicon = null;
	LDS L = null; 
	
	public DDSPTABLE(String ngFile,PhraseTable ptable, TranslationLEXICON lexicon, LDS labeled){
		super(ngFile);
		this.ptable = ptable;
		this.lexicon = lexicon; 
		this.L = labeled;
	}

 	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double dscore = 1/NGRAMS.size(); // BACKOFF . Should not be a need ideally !!! LM built using the same right
		double uscore = 0; // Not present in Labeled data
		double pscore = 0; // Not present in PhraseTable
		
		Iterator<String> iter = e.PHRASES.iterator(); 
		while(iter.hasNext()){
			String p = iter.next();

			double flag = 0;
			
			if(! L.NGRAMS_EXISTING.containsKey(p)){
				uscore++;
				flag = 1;
			}
			
			// Discount for existence in Labeled data
			double dim = 0;
			if(NGRAMS.containsKey(p)){
				// Original vote power
				double vote = NGRAMS.get(p) / TOTAL_NGRAMS;
				if(L.NGRAMS_EXISTING.containsKey(p)) {
					dim = L.NGRAMS_EXISTING.get(p);
				}
				vote*=Math.pow(2.7314,-1*dim);
				dscore+= vote;
			}else{
				// System.err.println("Not match:"+p);
			}
			
			// Model-based 
			if(ptable.containsSrc(p)) {
				double pentropy = ptable.computeEntropy(p);

				// Discount entropy as well for the BATCH !!! NEW
				// TODO: Replace this by an estimate of Entropy !!!!
				// TODO: conf = lexicon.getAlignmentProbability_SGT(p, t, true);
				
				pentropy*= Math.pow(2.7314,-1*dim);
				pscore+=pentropy;
			}else{
				pscore+= ptable.ENTROPY_BACKOFF; // Entropy is '1' only when absolutely not seen it	
			}
		}
		// Normalize for Number of phrases possible   
		dscore = dscore / (double)e.PHRASES.size();
		uscore = uscore / (double)e.PHRASES.size();
		pscore = pscore / (double)e.PHRASES.size();
		
		// Combining in different ways 
		// Geometric mean 
		// e.score =  dscore * uscore;
		// Harmonic mean (Weighted)
		double beta = 1.0; 
 
		// e.score = ((1+beta*beta) *dscore *pscore ) /((beta*beta) * dscore + pscore);
		
		e.score = pscore; 
		
		// e.score = dscore * pscore;
		
	//	System.out.println(e.source+" ||| U:"+uscore+" D:"+dscore+" P:"+pscore+" ||| "+e.score);
		
		return e.score;
 	}
}
