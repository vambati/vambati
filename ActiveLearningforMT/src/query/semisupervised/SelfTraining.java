package query.semisupervised;

import java.util.Iterator;

import query.sentence.Diversity;
import query.sentence.density.DDWDS;
import query.sentence.uncert.DecodingUncertaintyQuerySelector;

import data.*;
import query.*;
/*
 * Diminishing density weighted Uncertainty Sampling
 * Uncertainty : Viterbi score from Moses Translation system 
 */
public class SelfTraining extends DecodingUncertaintyQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	LDS L = null; 
	
	public SelfTraining(LDS l,String mosesFile,int tag,String src,String tgt){
		super(l, mosesFile, tag, src, tgt);
		L = l; 
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		// Compute the Uncertainty score using Moses scores 
		double uscore = 0;
		String selftraining_translation = "";
		
		if(MOSES.containsKey(e.position_unlabeled)){
			uscore = MOSES.get(e.position_unlabeled);
			selftraining_translation = MOSESHYP.get(e.position_unlabeled);
		}else{
			System.err.println("ERR: Moses translation not found!!!"+e.toString());
		}
		// Normalize     
		uscore  = uscore / e.sLength;
		//System.err.println("D:"+dscore+"- Moses:"+uscore);
		
  
		e.score = -1 * uscore; // We want to select translations with maximum uscore
		e.target = selftraining_translation; // We want to set this one instead 
		
		return e.score;
	}
}