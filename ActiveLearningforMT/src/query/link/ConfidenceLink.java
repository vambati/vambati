package query.link;

import model.smt.TranslationLEXICON;
import data.*;
import query.*; 

public  class ConfidenceLink implements QuerySelector {
 
	TranslationLEXICON LEX = null; 
	 
	public ConfidenceLink(String sgtFile,String tgsFile){
		// Loading the translation lexicons 
		LEX = new TranslationLEXICON(sgtFile,tgsFile); 		
	}
	
	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e; 
		
		int sennum = ae.senid;
		String[] st = ae.source.split("\\s+");
        String[] tt = ae.target.split("\\s+");
        
		for(int x: ae.LINKS.keySet()){
			for(int y: ae.LINKS.get(x).keySet()){
				double sgt = LEX.getWordProbability_SGT(st[x],tt[y]);				
				double tgs = LEX.getWordProbability_TGS(st[x],tt[y]);
				
				double linkscore = 2 * (sgt * tgs) /(sgt + tgs); // Cost minimize it
				ae.LINKS.get(x).put(y,linkscore);
			}
		}
		return 0.0;
	}
	
	public double computeLinkScore(String src,String tgt){ 
		double sgt = LEX.getWordProbability_SGT(src,tgt);				
		double tgs = LEX.getWordProbability_TGS(src,tgt);
		
		double linkscore = 2 * (sgt * tgs) /(sgt + tgs); // Cost minimize it		return linkscore;
		return linkscore;
	}
}