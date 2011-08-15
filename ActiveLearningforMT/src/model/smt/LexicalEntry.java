package model.smt;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.PriorityQueue;

import data.AlignmentEntry;
import data.EntryCompare;
import data.EntryCompare.DescendingCompare;

//TODO
public class LexicalEntry {
	
	public String src = "";
	
	private double entropy = 0;
	private double ig = 0;
	private double ig_ratio = 0;
	
	// Translations 
	public HashMap<String,Double> translations = null;
	
	LexicalEntry(String src){
		this.src = src;
		translations = new HashMap<String,Double>(); 
	}
	public void addTrans(String val,Double prob){
		if(!translations.containsKey(val)){
			translations.put(val,prob);
		}else{
			// Does not repeat for the same "SOURCE" side
			System.err.println("CAN NOT HAPPEN"+val);
		}
	}
	// Compute entropy for this particular entry 
	   public double computeEntropy(){
		   double e = 0;
		   for(String tgt: translations.keySet()){
			   double p = translations.get(tgt);
				   e+= p * (Math.log(p)/Math.log(2));	   
		   }
		   entropy = -1.0 * e;		   
		   return entropy;
	   }
	   
	   public double getEntropy(){
		   return entropy; 
	   }
	   public double getIG(){
		   return ig; 
	   }
	   public void setIG(double ig){
		   this.ig = ig;
	   }
	   public double getIGRatio(){
		   return ig_ratio; 
	   }
	   public void setIGRatio(double i){
		   this.ig_ratio = i;
	   }
	   
	   public String toString(){
		   String str = src+" (Entropy:)"+entropy+"\n";
		   for(String tgt: translations.keySet()){
			   double p = translations.get(tgt);
			   str+= "\t"+tgt+":"+p+"\n"; 
		   }
		   return str;
	   }
	public String getTopTrans() {
		String maxtrans = ""; 
		double maxscore = 0.0; 
		   for(String tgt: translations.keySet()){
			   if(translations.get(tgt) >= maxscore){
				 maxscore = translations.get(tgt);
			     maxtrans = tgt; 
			   } 
		   }
		   return maxtrans;
	}
	
	// Get first and second best margin 
	public double marginFirstSecond(){
		EntryCompare EC = new EntryCompare();
		PriorityQueue<Double> tmp = new PriorityQueue<Double>(100,EC.new DescendingCompare());
		 
		// Compute best translations for each source word
			for(String tgt: translations.keySet()){
				tmp.add(translations.get(tgt)); 
			}
			double first = 0;
			
			if(!tmp.isEmpty()){
				first = tmp.poll();
			}
			double second = 0; 
			if(!tmp.isEmpty()){
				second = tmp.poll();
			}
		return (first-second);		
	}
}
