package model.smt;

import java.io.*;
import java.util.*;

public class Dictionary {
    public static double BACKOFF = -1;

    public static HashMap<String,HashMap<String,Double>> dict = null;
 
    public Dictionary(String file) {
        try{
               dict = new HashMap<String, HashMap<String,Double>>();
               dict = loadfromFile(file);
       }catch(Exception e){e.printStackTrace();}
    }
    
    public static HashMap<String,HashMap<String,Double>> loadfromFile(String file) throws IOException {
	    int i=0;
	    HashMap<String,HashMap<String,Double>> lexicon = new HashMap<String, HashMap<String,Double>>();
	    System.err.println("Loading dictionary from: "+file);
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

	        if(!lexicon.containsKey(src)){
	        	HashMap<String,Double> tmp = new HashMap<String, Double>(5);
	        	lexicon.put(src, tmp);
	        }
	        lexicon.get(src).put(tgt,score);
	        i++;
	    }
	    System.err.println("Loaded dictionary with entries: "+ i);
	    BACKOFF = 1.0/(double)i;
	    
	    return lexicon;
    }

	public double getScore(String s, String t) {
		try{
			s = s.toLowerCase();
			t = t.toLowerCase();
			return dict.get(s).get(t);
		}catch(Exception e){
			return BACKOFF;
		}
	}
}