package model.smt;

import java.io.*;
import java.util.*;

import data.TranslationEntry;
import data.UDS;

public class TranslationLEXICON  {

    public static double BACKOFF = 0; // Will be set as 1/Number of Entries 
    public  static int sgt_backoff = 0;
    public static int tgs_backoff = 0;
    
 // TODO: Entropy of unknown words is HIGH or LOW
    public static double ENTROPY_BACKOFF = -1;
    
    public  HashMap<String,LexicalEntry> sgtLexicon = null;
    public  HashMap<String,LexicalEntry> tgsLexicon = null;

    public TranslationLEXICON(String sgtfile,String tgsfile) {
        try{
               sgtLexicon = load(sgtfile);  
               tgsLexicon = load(tgsfile); 
       }catch(Exception e){e.printStackTrace();}
    }
    
    public HashMap<String,LexicalEntry> load(String file) {
    	HashMap<String,LexicalEntry> lexicon = new HashMap<String, LexicalEntry>(1000);
        // System.err.println("Loading lexicon from "+file);
    	 try{
        BufferedReader corpusReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        
        String str = ""; 
        int i=0;
        while( (str = corpusReader.readLine())!=null)
        {
        	String[] tokens = str.split("\\s+");
            String src = tokens[0];
            String tgt = tokens[1];
            String score = tokens[2];	
            Double prob = Double.parseDouble(score);
            
    	        if(!lexicon.containsKey(src)){
    	        	LexicalEntry trans = new LexicalEntry(src);
    	        	 lexicon.put(src,trans);
    	        }
    			 	
    	    	lexicon.get(src).addTrans(tgt, prob);
    	        i++;
        }
        BACKOFF = 1.0/ (double) i;
        ENTROPY_BACKOFF = -1*BACKOFF * Math.log(BACKOFF);
         
        // System.err.println("Loaded with entries: "+ i);
        }catch(Exception e){System.out.println(e.toString());}
        return lexicon;
    }
       
    public  double getWordProbability_SGT(String src,String tgt)
    {
    	try{
    		return sgtLexicon.get(src).translations.get(tgt);
	    }catch(Exception e){
	    	sgt_backoff++;
	    		return BACKOFF;
	    }
    }
    
    public  double getWordProbability_TGS(String src,String tgt)
    {
        try{
        		return tgsLexicon.get(tgt).translations.get(src);
        }catch(Exception e){
        	tgs_backoff++;
        		return BACKOFF;
        }
    }
    
    // Return P(src / tgt) 
    public  double getPhraseProbability_TGS(String src,String tgt,boolean normalized)
    {
        String[] st = src.split("\\s+");
        String[] tt = tgt.split("\\s+");

        // Src given Target scoring for phrases
        // Implementation similar to PESA toolkit calculation of phrase scores
        int i = st.length; int j = tt.length;
        
        if(i==0 || j==0){
                return BACKOFF; 
        }
        
        double prob= 1;
        for(String t: tt) {
                double sum=BACKOFF;
                for(String s: st){
                        sum += getWordProbability_TGS(s,t);
                }
                if(normalized){
                        sum = sum / i;
                }
                prob = prob * sum;
        }
        if(normalized){
                prob = prob/ j;
        }
        return prob;
    }
    // Return P(src / tgt) 
    public  double getPhraseProbability_SGT(String src,String tgt,boolean normalized)
    {
        String[] st = src.split("\\s+");
        String[] tt = tgt.split("\\s+");

        // Src given Target scoring for phrases
        // Implementation similar to PESA toolkit calculation of phrase scores
        int i = st.length; int j = tt.length;
        
        if(i==0 || j==0){
                return BACKOFF; 
        }
        
        double prob= 1;
        for(String s: st) {
                double sum=BACKOFF;
                for(String t: tt){
                        sum += getWordProbability_SGT(s,t);
                }
                if(normalized) {
                        sum = sum / j;
                }
                prob = prob * sum;
        }
        if(normalized){
                prob = prob/ i;
        }
        return prob;
    }
    
    // Get Alignment score P(A/S,T)   
    public  double getAlignmentProbability_SGT(String src,String tgt,
    							boolean normalized,HashMap<Integer,HashMap<Integer,Double>> Alignment)
    {
        String[] st = src.split("\\s+");
        String[] tt = tgt.split("\\s+");

        // Src given Target scoring for phrases
        // Implementation similar to PESA toolkit calculation of phrase scores
        int i = st.length; int j = tt.length;
        
        if(i==0 || j==0){
                return BACKOFF; 
        }
        
        double prob= 1;
        for(String s: st) {
                double sum=BACKOFF;
                for(String t: tt){
                	// TODO
                        sum += getWordProbability_SGT(s,t);
                }
                if(normalized) {
                        sum = sum / j;
                }
                prob = prob * sum;
        }
        if(normalized){
                prob = prob/ i;
        }
        return prob;
    }
    
	public double computePerplexity(){
		return Math.pow(2, computeEntropy());
	}
	
   public double computeEntropy(){
	   double avg_entropy = 0;
	   for(String src: sgtLexicon.keySet()){
		   avg_entropy+=sgtLexicon.get(src).computeEntropy();
	   }
	   return (avg_entropy/sgtLexicon.size()); // Normalize ?
	   // return (avg_entropy);
   }
   
    public void computeEntropies(){
    	// One direction 
 	   for(String w: sgtLexicon.keySet()){
 		   sgtLexicon.get(w).computeEntropy();
 	   }
 	   // Second direction
 	  for(String w: tgsLexicon.keySet()){
		   tgsLexicon.get(w).computeEntropy();
	   } 	   
    }

    public double getSGTEntropy(String str){
     double entropy = 0;
     if(sgtLexicon.containsKey(str)){
    	 entropy = sgtLexicon.get(str).getEntropy();
     }else{
    	 entropy = ENTROPY_BACKOFF;
     }
     return entropy; 
    }
    
    public double getTGSEntropy(String str){
        double entropy = 0;
        if(tgsLexicon.containsKey(str)){
       	 entropy = tgsLexicon.get(str).getEntropy();
        }else{
       	 entropy = ENTROPY_BACKOFF;
        }
        return entropy; 
     }
    
    // Compute entropy for all the possible phrases in a given file 
    public double computeFileEntropy_SGT(HashMap<String,Integer> dlog){
    	double avg_entropy = 0.0;
    	double N = 0; 
		for(String sp: dlog.keySet()){
			avg_entropy+=getSGTEntropy(sp);
			N++;
		}
    	return avg_entropy/dlog.size();
    }
    
    // Compute entropy for all the possible phrases in a given file 
    public double computeFileEntropy_TGS(HashMap<String,Integer> dlog){
    	double avg_entropy = 0.0;
    	double N = 0; 
		for(String sp: dlog.keySet()){
			avg_entropy+=getTGSEntropy(sp);
			N++;
		}
    	return avg_entropy/dlog.size(); 
    }
}
