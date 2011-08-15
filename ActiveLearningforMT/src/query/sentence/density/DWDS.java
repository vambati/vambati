package query.sentence.density;

import query.QuerySelector;
import query.sentence.Diversity;

import data.*;

/*
 * DWDS : Density Weighted Diversity Sampling  
 */
public class DWDS implements QuerySelector{
	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	Diversity diversitySelector = null;
	DS densitySelector = null;
	
	public DWDS(String ngFile,LDS l){
		densitySelector = new DS(ngFile);
		diversitySelector = new Diversity(l);
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double dscore = densitySelector.computeScore(e);
		double uscore = diversitySelector.computeScore(e);
		 		
		// Combine 
		e.score = dscore + uscore; // Combining LOG values (Geometric mean) 
		
		e.desc = "D: "+dscore+"  U:"+uscore; 
		return e.score;
	}
}