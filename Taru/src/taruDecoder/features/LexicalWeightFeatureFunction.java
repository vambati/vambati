/**
 * 
 */
package taruDecoder.features;

import java.util.HashMap;

import taruHypothesis.Hypothesis;
import utils.IOTools;
import utils.lm.LMTools;
import utils.lm.VocabularyBackOffLM;
import java.io.*;

/**
 * @author abhayaa
 *
 */
public class LexicalWeightFeatureFunction {

	// These need to be set as global variables too
	public static String sgtlexfile = "";
	public static String tgslexfile = "";
	
	public static double BACKOFF = 0.0001;
	
	public static HashMap<String,Double> sgtLexicon = null;
	public static HashMap<String,Double> tgsLexicon = null;
	
	private static HashMap<String, Double> features;
	
	
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
		
		features = new HashMap<String, Double>(2);
		 try{
			 sgtLexicon = new HashMap<String, Double>();
			 tgsLexicon = new HashMap<String, Double>();
			sgtLexicon = loadfromFile(sgtlexfile);
			tgsLexicon = loadfromFile(tgslexfile);
		}catch(Exception e){e.printStackTrace();}	
	}

	/* (non-Javadoc)
	 * @see taruDecoder.features.FeatureFunction#computeFeature(taruDecoder.Hypothesis, taruDecoder.Hypothesis, int)
	 */
	public static HashMap<String, Double> computeFeature(Hypothesis h, Hypothesis h1, Hypothesis h2, String edgeId) {
		//System.err.println("Called LexicalWeight function - 2");
		features.clear();

		// TODO: Need to be done more efficiently 
		double sgtscore = BACKOFF; 
		double tgsscore = BACKOFF;
		// sgtscore = LexicalWeightFeatureFunction.getPhraseProbability_SGT(h.getSrcWords(), h.getWords());  
		// tgsscore = LexicalWeightFeatureFunction.getPhraseProbability_TGS(h.getSrcWords(), h.getWords()); 
				
		features.put("SGTLEX", sgtscore);
		features.put("TGSLEX", tgsscore);
		
		return features;
	}

	public static HashMap<String, Double> computeFeature(Hypothesis h, Hypothesis h1, String edgeId) {
		//System.err.println("Called LexicalWeight function - 1");
		features.clear();
		
		double sgtscore = BACKOFF; 
		double tgsscore = BACKOFF;
		// sgtscore = LexicalWeightFeatureFunction.getPhraseProbability_SGT(h.getSrcWords(), h.getWords()); 
		// tgsscore = LexicalWeightFeatureFunction.getPhraseProbability_TGS(h.getSrcWords(), h.getWords()); 
				
		features.put("SGTLEX", sgtscore);
		features.put("TGSLEX", tgsscore);
		
		return features;
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
		// System.err.println("Phrase prob:"+src+":"+tgt);
		// Assuming phrases passed are separated by spaces 
		String[] st = src.split("\\s+");
		String[] tt = tgt.split("\\s+");

		// Src given Target scoring for phrases
		// Implementation similar to PESA toolkit calculation of phrase scores
		int i = st.length; int j = tt.length;
		
		if(i==0 || j==0){
			return BACKOFF; 
		}
		
		double prob= Math.log10(BACKOFF);
		for(String t: tt) {
			double sum=BACKOFF;
			for(String s: st){
				sum += getWordProbability_TGS(t,s);
			}
			if(normalized){
				sum = sum / i;
			}
			prob = prob + Math.log10(sum);
		}

		// Why will it be infinity ?
		if(prob==Double.NEGATIVE_INFINITY) 
			prob=-1 * BACKOFF;
		
		// Why will it be infinity ?
		if(prob==Double.POSITIVE_INFINITY) 
			prob= BACKOFF;
		
		return prob;
	}
	// Return P(src / tgt) 
	public static double getPhraseProbability_SGT(String src,String tgt,boolean normalized)
	{
		// System.err.println("Phrase prob:"+src+":"+tgt);
		// Assuming phrases passed are separated by spaces 
		String[] st = src.split("\\s+");
		String[] tt = tgt.split("\\s+");

		// Src given Target scoring for phrases
		// Implementation similar to PESA toolkit calculation of phrase scores
		int i = st.length; int j = tt.length;
		
		if(i==0 || j==0){
			return BACKOFF; 
		}
		
		double prob= Math.log10(BACKOFF);
		for(String t: tt) {
			double sum=BACKOFF;
			for(String s: st){
				sum += getWordProbability_SGT(t,s);
			}
			if(normalized) {
				sum = sum / j;
			}
			prob = prob + Math.log10(sum);
		}

		// Why will it be infinity ?
		if(prob==Double.NEGATIVE_INFINITY) 
			prob=-BACKOFF;

		// Why will it be infinity ?
		if(prob==Double.POSITIVE_INFINITY) 
			prob= BACKOFF;
		
		return prob;
	}
}