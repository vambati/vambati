package query.link;

import model.smt.TranslationLEXICON;
import data.*;
import query.*; 
import java.util.*;

public  class MarginLink implements QuerySelector {

	TranslationLEXICON LEX = null; 
	Hashtable<String,Double> bests = null;
	
	public MarginLink(String sgtFile,String tgsFile){
		// Loading the translation lexicons 
		LEX = new TranslationLEXICON(sgtFile,tgsFile);
	}
	
	public void computeBests(AlignmentEntry e){
		bests = new Hashtable<String,Double>();
		
		String[] st = e.source.split("\\s+");
		String[] tt = e.target.split("\\s+");
		
		EntryCompare EC = new EntryCompare();
		PriorityQueue<Double> tmp = new PriorityQueue<Double>(100,EC.new DescendingCompare());
		 
		// Compute best translations for each source word
		for(String sw: st){
			tmp.clear(); 
			// Fill up Priority Queue for ordering
			for(String tw: tt){
				tmp.add(LEX.getWordProbability_SGT(sw,tw));
			}
			double first = 0;
			
			if(!tmp.isEmpty()){
				first = tmp.poll();
			}
			bests.put(sw, first);
		}	
	}
	
	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e; 
		String[] st = e.source.split("\\s+");
		String[] tt = e.target.split("\\s+");
		
		computeBests(ae);

		// Compute Margins
		for(int x: ae.LINKS.keySet()){
			for(int y: ae.LINKS.get(x).keySet()){
				double sgt = LEX.getWordProbability_SGT(st[x],tt[y]);
				double best = bests.get(st[x]);
				double margin = 1.0/(best - sgt);
				 
				//System.err.println(st[x]+"\t"+best+"\t"+sgt);
				ae.LINKS.get(x).put(y,margin);
			}
		}
		return 0;
	}

	public double computeLinkScore(String src, String tgt) {
		double sgt = LEX.getWordProbability_SGT(src,tgt);
		double best = bests.get(src);
		double margin = 1.0/(best - sgt);
		return margin;
	}
}