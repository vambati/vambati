package query.link;

import model.smt.TranslationLEXICON;
import data.*;
import query.*; 

public  class EntropyLink implements QuerySelector {
 
	TranslationLEXICON LEX = null; 
	 
	public EntropyLink(String sgtFile,String tgsFile){
		// Loading the translation lexicons 
		LEX = new TranslationLEXICON(sgtFile,tgsFile);
		LEX.computeEntropies();
	}
	
	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e; 
		
		String[] st = ae.source.split("\\s+");
        String[] tt = ae.target.split("\\s+");
        
		for(int x: ae.LINKS.keySet()){
			double entropy_sgt = LEX.getSGTEntropy(st[x]); 		
			for(int y: ae.LINKS.get(x).keySet()){
			double entropy_tgs = LEX.getTGSEntropy(tt[y]);
				
				double linkscore =  2 * (entropy_sgt * entropy_tgs) /(entropy_sgt + entropy_tgs); // Cost minimize it
				linkscore = entropy_sgt; 
				
				linkscore = 1.0 / linkscore; // pose as COST  
				ae.LINKS.get(x).put(y,linkscore);
			}
		}
		return 0.0;
	}
	
	public double computeLinkScore(String src,String tgt){ 
		double entropy_sgt = LEX.getSGTEntropy(src);
		double entropy_tgs = LEX.getTGSEntropy(tgt);
		
		double linkscore = 2 * (entropy_sgt * entropy_tgs) /(entropy_sgt + entropy_tgs); // Cost minimize it
		linkscore = 1.0 / linkscore; // pose as COST  
		return linkscore;  
	}
}