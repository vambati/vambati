package utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class MyNLP {
	HashSet<String> stopwordSet = new HashSet<String>();
	String stopwordFile = "";
	public MyNLP(String file){
		stopwordFile = file;
		loadStopWords();
	}
	public void loadStopWords(){
		int i=0;
		try {
			System.err.println("Loading stopwords from:"+stopwordFile);
			BufferedReader rbr = new BufferedReader(new FileReader(stopwordFile));
			String ref = "";
				while((ref = rbr.readLine()) != null){
					stopwordSet.add(ref.toLowerCase());
					i++;
				}
				rbr.close();
			}catch(Exception e){System.err.println(e.toString());}
			System.err.println("Loaded stop words "+ i);	
	}

    public boolean isStopWord(String word) {
        return stopwordSet.contains(word.toLowerCase());
    }
    
    public static String removePunctuation(String str){
    	//Non-Ascii -> Remove punctuation 
    	str = str.replaceAll(";|,|\\.|%|#|\\?|\\)|\\(|\\@|\\!|\\&|>|<|'|\"", " ");
    	str = str.replaceAll("\\s+", " ");
    	str = str.replaceAll("\\s+$", "");
    	str = str.replaceAll("^\\s+", "");
    	return str;
    }
    
    public String processString(String str){
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
}
