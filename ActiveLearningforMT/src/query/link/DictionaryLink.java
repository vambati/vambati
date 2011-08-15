package query.link;
import model.smt.Dictionary;
import data.*;
import query.*; 

public  class DictionaryLink implements QuerySelector {
 
	Dictionary MODEL1 = null; 
	 
	public DictionaryLink(String coocfile){
		// Loading the translation lexicons 
		MODEL1 = new Dictionary(coocfile);
	}
	
	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e; 
		
		String[] st = ae.source.split("\\s+");
        String[] tt = ae.target.split("\\s+");

		for(int x: ae.LINKS.keySet()){
			for(int y: ae.LINKS.get(x).keySet()){
				double score = MODEL1.getScore(st[x],tt[y]);				
				ae.LINKS.get(x).put(y,score);
			}
		}
		return 0.0;
	}

	public double computeLinkScore(String src,String tgt) {
		double score = MODEL1.getScore(src,tgt);				
		return score; 
	}
	
}