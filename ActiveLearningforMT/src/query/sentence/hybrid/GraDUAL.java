package query.sentence.hybrid;

import java.util.Iterator;

import query.sentence.density.DensityQuerySelector;

import data.*;

/*
 * DUAL: Compute switching point between DWDS and DIV and switch over  
 */
public class GraDUAL extends DensityQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	
	double RATIO = 0;
	
	public GraDUAL(String ngFile,LDS l,int tag){
		super(ngFile);
		L = l;
		
		// TODO: Hack - computing ratio based on round number (only for Sp-En ) 
		computeRatio(tag);
	}

	// Compute once at the beginning of the BATCH 
	public void computeRatio(int round){
		// HACK
		// These numbers are computed outside in Excel for Sp-En . Needed to be coded inside java program for other language pairs
		double beta = 0.6;
		if(round<4) 
			RATIO= 1;
		else if(round==4)
			RATIO= 0.95; // Math.exp(beta - 0.689);
		else if (round==5)
			RATIO= 0.90;
		else if (round==6)
			RATIO= 0.85;
		RATIO = 0;
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double score_div =  computeDivScore(e);
		double score_den = computeDWDScore(e);

		// Exponentiate to have a diminishing effect 
		return  (RATIO * score_den + (1-RATIO) * score_div);
	} 
	
	// Pure Diversity method 
	public double computeDivScore(TranslationEntry e) {
		double newphrases = 0;
		Iterator<String> iter = e.PHRASES.iterator(); 
		while(iter.hasNext()){
			String p = iter.next();
			if(! L.NGRAMS_EXISTING.containsKey(p)){
				newphrases++;
			}
			// Add them to the list (This is sub-optimal and greedy, but simulates select-one vs batch)
			// L.addPhrase(p);
		}
		// Novelty over phrases possible   
		e.score = newphrases / (double)e.PHRASES.size();
		return e.score;
	}
	
	// Diminishing Density Weighted Diversity Sampling (DWDS) 
	public double computeDWDScore(TranslationEntry e) {
		double dscore = 0; // Density score
		double uscore = 0; // Diversity score
		
		Iterator<String> iter = e.PHRASES.iterator(); 
		while(iter.hasNext()){
			String p = iter.next();
			if(! L.NGRAMS_EXISTING.containsKey(p)){
				uscore++;
			}
			if(NGRAMS.containsKey(p)){
				double ngramvote =((double)NGRAMS.get(p) / (double)TOTAL_NGRAMS);
				dscore+=ngramvote;
			}
		}
		// Normalize for Number of phrases possible
		dscore = dscore / (double)e.PHRASES.size();
		uscore = uscore / (double)e.PHRASES.size();
		
		// Gradual decay in combining as a weighted Harmonic Mean ..
		double BETA = 1; 
		e.score =  dscore * uscore / ( BETA * dscore + (1-BETA) * uscore );
		return e.score;
	}
}