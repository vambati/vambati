package query.link;

import java.util.HashMap;
import java.util.Hashtable;

import model.smt.TranslationLEXICON;
import data.*;
import query.*; 

public  class PosteriorLink implements QuerySelector {
 
	TranslationLEXICON LEX = null;
	HashMap<String,Double> marginalSrc = null;
	HashMap<String,Double> marginalTgt = null;
	 
	public PosteriorLink(String sgtFile,String tgsFile){
		// Loading the translation lexicons 
		LEX = new TranslationLEXICON(sgtFile,tgsFile); 		
	}
	
	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e; 
		marginalSrc = new HashMap<String,Double>();
		marginalTgt = new HashMap<String,Double>();
		
		int sennum = ae.senid;
		String[] st = ae.source.split("\\s+");
        String[] tt = ae.target.split("\\s+");
        
		for(int x: ae.LINKS.keySet()){
			double marginalize_S = 0.0;
			double marginalize_T = 0.0; 
			for(String tw: tt){
				marginalize_T+= LEX.getWordProbability_TGS(st[x],tw);
			}
			
			for(int y: ae.LINKS.get(x).keySet()){
				// Fei Huang 2008 link confidence metric
				double sgt = LEX.getWordProbability_SGT(st[x],tt[y]);				
				double tgs = LEX.getWordProbability_TGS(st[x],tt[y]);
				
				marginalize_S = 0.0;
				for(String sw: st){
					marginalize_S+= LEX.getWordProbability_SGT(sw,tt[y]);
				}

				double s2t = sgt / marginalize_S; // P(s/t) / SIGMA_s' P(s'/t)
				double t2s = tgs / marginalize_T; // P(t/s) / SIGMA_t' P(t'/s)
				
				if(s2t==0 || t2s==0){
					System.err.println("What:"+tt[y]);
				}
				
				double linkscore = 2 * (s2t * t2s) /(s2t + t2s); // Cost minimize it
				ae.LINKS.get(x).put(y,linkscore);
				// Store for future use 
				marginalSrc.put(tt[y],marginalize_S);
				marginalTgt.put(st[x], marginalize_T);
			}
		}
		return 0.0;
	}

	public double computeLinkScore(String s, String t) {
				// Fei Huang 2008 link confidence metric
				double sgt = LEX.getWordProbability_SGT(s,t); 				
				double tgs = LEX.getWordProbability_TGS(s,t); 
				
				double marginalize_S = marginalSrc.get(t); 
				double marginalize_T = marginalTgt.get(s);
				
				double s2t = sgt / marginalize_S; // P(s/t) / SIGMA_s' P(s'/t)
				double t2s = tgs / marginalize_T; // P(t/s) / SIGMA_t' P(t'/s)
				
				if(s2t==0 || t2s==0){
					System.err.println("What:"+s2t+" : "+t2s); 
					// System.exit(0);
				}
				
				double linkscore = 2 * (s2t * t2s) /(s2t + t2s); // Cost minimize it
				return linkscore;
	}
}