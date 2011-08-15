package utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import qa.Answer;
import qa.Question;

public class MyNLP {
    public static String stopwordFile = "";
	static HashSet<String> stopwordSet = new HashSet<String>();
	
	public static void load(){
		int i=0;
		try {
			BufferedReader rbr = new BufferedReader(new FileReader(stopwordFile));
			String ref = "";
				while((ref = rbr.readLine()) != null){
					stopwordSet.add(ref.toLowerCase());
					i++;
				}
				rbr.close();
			}catch(Exception e){}
			System.err.println("Loaded stop words "+ i);	
	}

    public static boolean isStopWord(String word) {
        return stopwordSet.contains(word.toLowerCase());
    }
    
    public static String processString(String str){
    	// Lower case 
    	str = str.toLowerCase();
	
    	//Non-Ascii -> Remove punctuation 
    	str = str.replaceAll("[^\\p{L}]", " ");
    	
    	// Remove stop words and puncutation 
    	StringTokenizer st = new StringTokenizer(str);
    	String out = "";
    	while(st.hasMoreTokens()){
    		String tok = st.nextToken();
    		if(!isStopWord(tok)){
    				out+=tok+" ";
    		}
    	}
    	//System.err.println(str+"\n"+out+"\n");
    	return out;
    }
    
    // TODO
	public static boolean isPunctuation(String str) {
		return false;
	}
}
