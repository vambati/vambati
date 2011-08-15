package query.link;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import data.*;
import query.*; 

public  class HybridLink implements QuerySelector {
  
	Bidirectional bLink = null;
	PosteriorLink pLink = null; 
	EntropyLink eLink = null;
	ConfidenceLink cLink = null;
	MarginLink mLink = null;
	DictionaryLink coocLink = null;
	
	// Load frequency tables 
	public double TOTAL = 0;
	HashMap<String,Double> sFreq = null; double STOTAL=0;
	HashMap<String,Double> tFreq = null; double TTOTAL = 0;
	
	public HybridLink(String sgtFile,String tgsFile,String sfreqFile,String tfreqFile,String sAlign,String tAlign,String coocFile){
		
		int OFFSET = 1; 
		pLink = new PosteriorLink(sgtFile,tgsFile); // Translation Lex
		bLink = new Bidirectional(sAlign,tAlign,OFFSET); // Viterbi Alignments (Query by Committee)
		 //cLink = new ConfidenceLink(sgtFile,tgsFile);
		 //eLink = new EntropyLink(sgtFile,tgsFile);
		 //mLink = new MarginLink(sgtFile,tgsFile);
		 // coocLink = new DictionaryLink(coocFile); // Bidirectional probabilities 
		 //
		 sFreq = loadfromFile(sfreqFile);
		 STOTAL = TOTAL; 
		 TOTAL = 0;
		 
		 tFreq = loadfromFile(tfreqFile);
		 TTOTAL = TOTAL;
	}
	
	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e;
		
		//mLink.computeBests(ae); // Compute once for the entire sentence 
	
		pLink.computeScore(ae); 
		
		String[] st = ae.source.split("\\s+");
        String[] tt = ae.target.split("\\s+");
        
		for(int x: ae.LINKS.keySet()){ 
			for(int y: ae.LINKS.get(x).keySet()){
				
				double posterior  = pLink.computeLinkScore(st[x],tt[y]);
				double intersect = bLink.computeLinkScore(ae.senid, x,y);
				// double entropy = eLink.computeLinkScore(st[x],tt[y]); 
				// double conf = cLink.computeLinkScore(st[x],tt[y]);
				// double margin = mLink.computeLinkScore(st[x],tt[y]);
				// double cooc = coocLink.computeLinkScore(st[x],tt[y]);
				 
/*				// Density measures 
				double sprob = sFreq.get(st[x])/STOTAL;
				double tprob = tFreq.get(tt[y])/TTOTAL;
				tprob = 1; 
				
				double weight = 1.0 / (double) (sprob * tprob);
				double linkscore =  2 * weight * intersect /(intersect +weight );
*/
				
				double linkscore = 2 * intersect * posterior/(posterior+intersect);
				// System.err.println("score:"+linkscore +" W:"+cooc+" C:"+posterior);
				ae.LINKS.get(x).put(y,linkscore);
			}
		}
		return 0.0;
	}
	
	   public HashMap<String,Double> loadfromFile(String file) {
		   try{
		    int i=0;
		    HashMap<String,Double> lexicon = new HashMap<String, Double>();
		    System.err.println("Loading dictionary from: "+file);
		    BufferedReader corpusReader = null ;
		    corpusReader= new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		
		    String str = "";
		    while( (str = corpusReader.readLine())!=null)
		    {
		    	String[] tokens = str.split("\\t");
		        String src = tokens[0];
		        Double score = Double.parseDouble(tokens[1]);

		        lexicon.put(src,score);
		        i++;
		        TOTAL+=score; 
		    }
		    System.err.println("Loaded dictionary with entries: "+ i);
		    return lexicon;
	    }catch(Exception e){System.err.println(e.toString()); }
	    return null; 
	   }
}