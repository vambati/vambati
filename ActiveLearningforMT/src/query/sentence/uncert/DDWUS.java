package query.sentence.uncert;

import java.util.Iterator;

import query.sentence.density.DDWDS;

import data.*;
import query.*;
/*
 * Diminishing density weighted Uncertainty Sampling
 * Uncertainty : Viterbi score from Moses Translation system 
 */
public class DDWUS extends DecodingUncertaintyQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	DDWDS densityQS = null; 
	
	public DDWUS(String ngFile,LDS l,String mosesFile,int tag,String src,String tgt){
		super(l, mosesFile, tag, src, tgt);
		L = l;
		densityQS = new DDWDS(ngFile, l); 
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		// Get the DiminishingDensity Score
		double dscore = densityQS.computeScore(e);
		// Compute the Uncertainty score using Moses scores 
		double uscore = 0;
		if(MOSES.containsKey(e.senid)){
			uscore = MOSES.get(e.senid);
		}
		
		// Normalize     
		uscore  = uscore / e.sLength;
		//System.err.println("D:"+dscore+"- Moses:"+uscore);
		
		double beta = 1; 
		e.score = (1+beta*beta) *dscore *uscore /((beta*beta) * dscore + uscore); 
		return e.score;
	}
}