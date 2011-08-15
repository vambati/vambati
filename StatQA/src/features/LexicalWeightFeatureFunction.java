/**
 * 
 */
package features;

import qa.*;
import java.util.HashMap;
import java.io.*;
 
public class LexicalWeightFeatureFunction {

	// These need to be set as global variables too
	public static String sgtlexfile = "";
	public static String tgslexfile = "";
	
	public static double BACKOFF = 0.00001;
	
	public static HashMap<String,Double> sgtLexicon = null;
	public static HashMap<String,Double> tgsLexicon = null;
	
	public static HashMap<String,Double> loadfromFile(String file) throws IOException {
		int i=0;
		HashMap<String,Double> lexicon = new HashMap<String, Double>();
		System.err.println("Loading lexicon from "+file);
        BufferedReader corpusReader = null ;
        try {
        corpusReader= new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        }catch(IOException ioe){System.err.println("Can not load "+file);}
        
        String[] tokens; 
        String str = "";
        while( (str = corpusReader.readLine())!=null)
        {
            tokens   = str.split("\\s+");
            
            if(tokens.length!=3)
            	continue;
            
            String src = tokens[0];
            String tgt = tokens[1];
            Double score = Double.parseDouble(tokens[2]);
            
            String key = src+":"+tgt;
            lexicon.put(key,score);
            i++;
        }
        System.err.println("Loaded lexicon with entries: "+ i);
        return lexicon;
	}

	public static void loadLex() {
		 try{
			 sgtLexicon = new HashMap<String, Double>();
			 tgsLexicon = new HashMap<String, Double>();
			sgtLexicon = loadfromFile(sgtlexfile);
			tgsLexicon = loadfromFile(tgslexfile);
		}catch(Exception e){e.printStackTrace();}	
	}
	
	public static double getWordProbability_SGT(String src,String tgt)
	{
		String key = tgt+":"+src;
		//System.err.println("SGT Checking:"+key);
        if(sgtLexicon.containsKey(key)) {
        	//System.err.println(key+"="+sgtLexicon.get(key));
            	return sgtLexicon.get(key);
        }
        // Backoff score for OOV words or translations
        return BACKOFF;
	}
	
	public static double getWordProbability_TGS(String src,String tgt)
	{
		String key = src+":"+tgt;
		//System.err.println("TGS Checking:"+key);
		if(tgsLexicon.containsKey(key)) {
        	//System.err.println(key+"="+tgsLexicon.get(key));
        	return tgsLexicon.get(key);
        }
        // Backoff score for OOV words or translations
        return BACKOFF;
	}
	
	// Return P(src / tgt) 
	public static double getPhraseProbability_TGS(String src,String tgt,boolean normalized)
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
				sum += getWordProbability_TGS(t,s);
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
	public static double getPhraseProbability_SGT(String src,String tgt,boolean normalized)
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
}