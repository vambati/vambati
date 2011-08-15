
package utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class StringUtils implements Cloneable
{
	
	/* remove leading whitespace */
    public static String lower(String source) {
        return source.toLowerCase();
    }
    
    /* remove leading whitespace */
    public static String ltrim(String source) {
        return source.replaceAll("^\\s+", "");
    }

    /* remove trailing whitespace */
    public static String rtrim(String source) {
        return source.replaceAll("\\s+$", "");
    }

    /* replace multiple whitespaces between words with single blank */
    public static String itrim(String source) {
           return source.replaceAll("\\s+", " ");
    }

    /* remove all superfluous whitespaces in source string */
    public static String trim(String source) {
    	//source.replaceAll("\\n", " ");
    	//source.replaceAll("\\r", " ");
        return rtrim(ltrim(itrim(source)));
    }

    /* Count number of tokens in a String */
    public static int wordCount(String source) {
        source = rtrim(ltrim(itrim(source)));
        StringTokenizer st = new StringTokenizer(source);
        return st.countTokens();
    }

	public static Vector<String> allPhrases(String input, int MAX_LENGTH){
		Vector<String> phrases = new Vector<String>();
		String[] arr = input.split("\\s+");
		for(int i=0;i<arr.length;i++) {
			for(int j=0;j<MAX_LENGTH && j+i<arr.length;j++) {
				String phrase = ""; 
			    for(int k=i;k<=i+j;k++) {
				    phrase+= arr[k]+" ";
			    }
				phrase = StringUtils.rtrim(phrase);
				phrases.add(phrase);
			}
		}
		return phrases;
	}
	public static HashMap<String,Double> allPhrases2(String input, int MAX_LENGTH){
		HashMap<String,Double> phrases = new HashMap<String,Double>();
		String[] arr = input.split("\\s+");
		for(int i=0;i<arr.length;i++) {
			for(int j=0;j<MAX_LENGTH && j+i<arr.length;j++) {
				String phrase = ""; 
			    for(int k=i;k<=i+j;k++) {
				    phrase+= arr[k]+" ";
			    }
				phrase = StringUtils.rtrim(phrase);
				phrases.put(phrase,1.0);
			}
		}
		return phrases;
	}
	
	public static HashMap<String,Integer> filePhrases(String filename, int MAX_LENGTH){
		
		HashMap<String,Integer> dlog = new HashMap<String, Integer>(1000);
		try {
		BufferedReader sr = new BufferedReader(new FileReader(filename));
		String line=""; int count=0;
		while((line= sr.readLine()) != null){
			// IMPORTANT 
			line = line.toLowerCase(); 
			Vector<String> phrases = StringUtils.allPhrases(line,MAX_LENGTH); 
			for(String p:phrases) {
			 	if(dlog.containsKey(p)){
					int c= dlog.get(p);
					dlog.put(p,c++);
				}else{
					dlog.put(p,1);
				}
			}
		count++;
		}
		sr.close();
//		System.err.println("Dev File: "+filename);
//		System.err.println("Total Sens:"+count);
		}catch(Exception e ){
			System.err.println(e.toString());
		}
		return dlog;
	}
}

