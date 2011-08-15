package query.link;

import model.smt.TranslationLEXICON;
import data.*;
import data.EntryCompare.DescendingCompare;
import query.*; 
import java.util.*;

public  class MarginLink2 implements QuerySelector {

	TranslationLEXICON LEX = null;
	PosteriorLink pLink  = null;
	Hashtable<String,Double> bests  = null;
	
	public MarginLink2(String sgtFile,String tgsFile){
		// Loading the translation lexicons 
		// LEX = new TranslationLEXICON(sgtFile,tgsFile);
		pLink = new PosteriorLink(sgtFile,tgsFile); // Translation Lex
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
				// tmp.add(LEX.getWordProbability_SGT(sw,tw));
				tmp.add(pLink.computeLinkScore(sw,tw) ); 
			}
			double first = 0;
			
			if(!tmp.isEmpty()){
				first = tmp.poll();
			}
			bests.put(sw, first);
		}
	}
	
	
	public double computeScore(Entry e){
		AlignmentEntry ae = (AlignmentEntry) e; 
		String[] st = e.source.split("\\s+");
		String[] tt = e.target.split("\\s+");
		
		
		pLink.computeScore(ae);
		computeBests(ae); 

		// Compute Margins
		for(int x: ae.LINKS.keySet()){
			for(int y: ae.LINKS.get(x).keySet()){
				 
				double sgt = pLink.computeLinkScore(st[x],tt[y]);
				
				double best = bests.get(st[x]);
				double margin = 1/(best - sgt);
				 
				//System.err.println(st[x]+"\t"+best+"\t"+sgt);
				ae.LINKS.get(x).put(y,margin);
			}
		}
		return 0;
	}
	
/*	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e; 
		String[] st = e.source.split("\\s+");
		String[] tt = e.target.split("\\s+");
		
		computeBests(ae);
				
		// Compute Margins
		for(int x: ae.LINKS.keySet()){
			for(int y: ae.LINKS.get(x).keySet()){
				double sgt = LEX.getWordProbability_SGT(st[x],tt[y]);
				
				double sMargin = srcMargins.get(st[x]);
				double tMargin = tgtMargins.get(tt[y]);
				double score = 2*(sMargin * tMargin)/ (sMargin+tMargin);
				
				// System.err.println(tt[x]+"::"+ st[x]+"\t"+sMargin+"\t"+tMargin);
				ae.LINKS.get(x).put(y,score);
			}
		}
		return 0;
	}
	*/
}