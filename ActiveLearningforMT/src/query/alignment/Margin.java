package query.alignment;

import model.smt.TranslationLEXICON;
import data.*;
import query.*; 
import java.util.PriorityQueue;

public  class Margin implements QuerySelector {

	TranslationLEXICON LEX = null; 
	
	public Margin(String sgtFile,String tgsFile){
		// Loading the translation lexicons 
		LEX = new TranslationLEXICON(sgtFile,tgsFile); 	
	}
	
	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e; 
		String[] st = e.source.split("\\s+");
		String[] tt = e.target.split("\\s+");
		
		EntryCompare EC = new EntryCompare();
		PriorityQueue<Double> tmp = new PriorityQueue<Double>(100,EC.new DescendingCompare());
		double margin_sum = 0.0;
		for(String sw: st){
			tmp.clear();
			// Fill up Priority Queue for ordering
			for(String tw: tt){
				tmp.add(LEX.getWordProbability_SGT(sw,tw)); 		
			}
			double first = 0,second=0.0;
			
			if(!tmp.isEmpty()){
				first = tmp.poll();
			}
			if(!tmp.isEmpty()){
				second = tmp.poll();
			}
			
			double margin = first - second; 
			// System.err.println("1best:"+first+",2best:"+second+" Margin="+margin);
			margin_sum+=margin; 
		}		
		// Margin needs to be minimized to obtain most Uncertain examples 
		ae.score = margin_sum/st.length;
		return ae.score;
	}
}