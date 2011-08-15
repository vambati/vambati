package query.alignment;

import model.smt.TranslationLEXICON;
import data.*;
import query.*; 
import java.util.regex.*;

public  class LexicalWeight implements QuerySelector {

	TranslationLEXICON LEX = null; 
	
	// Score Pattern for GIZA files
	Pattern scorepattern  = Pattern.compile("score : ([0-9\\.e\\-]+)");
	
	public LexicalWeight(String sgtFile,String tgsFile){
		// Loading the translation lexicons 
		LEX = new TranslationLEXICON(sgtFile,tgsFile); 	
	}
	
	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e; 
		int sennum = ae.senid; 
		
		double sgt = LEX.getPhraseProbability_SGT(e.source, e.target, true); // Normalized
		double tgs = LEX.getPhraseProbability_TGS(e.source, e.target, true); // Normalized
		
		ae.score = (sgt+tgs)/2.0;
 		// System.err.println("s:"+sgt+",t:"+tgs+" Conf="+ae.score);

		return ae.score;
	}
}