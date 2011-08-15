package query.sentence.uncert;

import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;

import query.*; 

import data.*;


/*
 * Diminishing density weighted Uncertainty Sampling
 * Uncertainty : Viterbi score from Moses Translation system 
 */
public class US extends DecodingUncertaintyQuerySelector {

	// For purposes of diversity , also load Labeled data
	LDS L = null;
	
	public US(LDS l, String conf, int tag, String src, String tgt) {
		// Load the previous data and decode the previous batch 
		super(l, conf, tag, src, tgt);
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double uscore = 0;
		String desc = "Not found";
		Iterator<String> iter = e.PHRASES.iterator(); 
		 	
		if(MOSES.containsKey(e.position_unlabeled)){
			uscore = MOSES.get(e.position_unlabeled);
			desc = MOSESHYP.get(e.position_unlabeled);
		}
		
		// Normalize for Number of phrases possible   
		e.score  = uscore / e.sLength; // (double)e.PHRASES.size();
		e.desc = desc; 
		
		return e.score;
	}
}