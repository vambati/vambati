package query.sentence.hybrid;

import query.*;
import java.util.Iterator;

import query.sentence.density.DensityQuerySelector;
import data.*;

/*
 * DUAL: Compute switching point between DWDS and DIV and switch over  
 */
public class DUAL extends DensityQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	
	// Flag that decides if switch has been reached 
	public boolean SWITCH = false;
	
	public DUAL(String ngFile,LDS l){
		super(ngFile);
		L = l;
		
		// Compute once at the beginning of the BATCH if switching point is reached
		computeSwitchPoint();
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		if(SWITCH){
			return computeDivScore(e);
		}else{
			return computeDWDScore(e);
		}
	}
	
	// Should we switch here ?  
	public void computeSwitchPoint(){
		SWITCH = false; 
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
