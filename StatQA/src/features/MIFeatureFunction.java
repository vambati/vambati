/**
 * 
 */
package features;

import java.util.HashMap;
import java.io.*;

import qa.*;
 
public class MIFeatureFunction {

	// These need to be set as global variables too
	public static String miFile = "";
 	private static HashMap<String, Double> MITable;
	
 	static double BACKOFF = 0.00000001;
	
	public static HashMap<String,Double> loadfromFile(String file) throws IOException {
		int i=0;
		HashMap<String,Double> lexicon = new HashMap<String, Double>(100000);
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
            if(tokens.length==3){
	            String src = tokens[0];
	            String tgt = tokens[1];
	            Double score = Double.parseDouble(tokens[2]);
	            lexicon.put(src+":"+tgt,score);
	            i++;
            }
        }
        System.err.println("Loaded MI Table with entries: "+ i);
        return lexicon;
	}

 	public static void load() {
 		try{
			System.err.println("Loading from "+miFile);
			MITable = loadfromFile(miFile);
		}catch(Exception e){e.printStackTrace();}
		System.err.println("MI Loaded.");		
	}
		
	// Return P(src / tgt) 
	public static double getMIScore(String src,String tgt,boolean normalized)
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
				String key = t+":"+s;
				if(MITable.containsKey(key)){
					sum += MITable.get(key); 
				}else{
					sum += BACKOFF;
				}
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
}
