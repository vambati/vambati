/**
 * 
 */
package features;

import utils.StringUtils;

import java.util.HashMap;
import java.util.Hashtable;
import java.io.*;
 
public class GLSFeatureFunction {

	// These need to be set as global variables too
	public static String inputFile = "";
	public static String bowFile = "";
	
	public static HashMap<String,Hashtable<String,Double>> GLS = new HashMap<String,Hashtable<String,Double>>(1000);
		
	public static void load() throws IOException  {
		int n=0;
		System.err.println("Loading inputs from "+inputFile);
		System.err.println("Loading BOW from "+bowFile);
        BufferedReader inputReader = null ;
        BufferedReader bowReader = null ;
        try {
        inputReader= new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        bowReader= new BufferedReader(new InputStreamReader(new FileInputStream(bowFile)));
        }catch(IOException ioe){System.err.println("Can not load ");}
        
        String inp = ""; String bow = "";
        while( (inp = inputReader.readLine())!=null)
        {
        		bow = bowReader.readLine();
        		if(inp.equals("") || bow.equals("")){
        			continue;
        		}
        		Hashtable<String,Double> distribution = new Hashtable<String,Double>();
        		bow = StringUtils.trim(bow);
        		String[] arr = bow.split("\\s+");
        		for(int i=0;i<arr.length;i=i+2){
        			distribution.put(arr[i], Double.parseDouble(arr[i+1]));
        		}
        		GLS.put(inp,distribution);
        		n++;
        }
        System.err.println("Loaded entries: "+ n);
	}

	// Question, Answer  
	public static double getGLSScore(String src,String tgt,boolean norm)
	{ 
		String[] st = src.split("\\s+");
		String[] tt = tgt.split("\\s+");
		
		double BACKOFF = 0.0001;
		
		int i = st.length; int j = tt.length;		
		if(i==0 || j==0){
			return BACKOFF;
		}
		
		double score = 1.0;
		if(GLS.containsKey(tgt)){
			// Get 'q' distribution for that Answer and score the given input question 
			Hashtable<String,Double> myD = GLS.get(tgt);
			for(String sw: st) {
				if(myD.containsKey(sw)){
					//score += Math.log(myD.get(sw));
					score *= myD.get(sw);
				}
				else{
					// Anything not in top 10000 is given a uniform probability mass
					score *= BACKOFF;
				}
			}
		}else{
			System.err.println("Not present:"+tgt);
			return -1;
		}
		// Should we normalize for question length ? 
		if(norm)
			score = score/st.length; 
		return score;
	}
}