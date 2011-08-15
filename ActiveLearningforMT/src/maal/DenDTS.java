package maal;

import java.util.Iterator;

import query.sentence.density.*;

import data.*;
import query.*;
/*
 * Diminishing density weighted Uncertainty Sampling
 * Uncertainty : Viterbi score from Moses Translation system 
 */
public class DenDTS extends DTSUncertaintyQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	DDS densityQS = null; 
	
	public DenDTS(String ngFile,LDS l,String mosesFile,int tag,String src,String tgt){
		super(l, mosesFile, tag, src, tgt);
		L = l;
		densityQS = new DDS(ngFile, l); 
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		// Get the DiminishingDensity Score
		double dscore = densityQS.computeScore(e);
		
		// Compute the Uncertainty score using Moses scores 
		double uscore = 0;		
		if(DTSCORES.containsKey(e.senid)){
			uscore = DTSCORES.get(e.senid);
		}
 		 
		double beta = 1;
		e.score = (1+beta*beta) *dscore *uscore /((beta*beta) * dscore + uscore); 
		return e.score;
	}
}