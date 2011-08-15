package crowdsource.validate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

import edu.cmu.meteor.scorer.MeteorConfiguration;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;

import utils.MyNLP;

/* How do we validate that this is not noise - Account for Human Errors
 * 1. Non blank
 * 2. Illegitimate characters
 * 3. Length ratios ? 
 * 4. Compare with Gold Standard 
 * 	- Lexical overlap, semantic variations ??  
 * 5. Google Translation API / Other automatic translation outputs ? 
 * */

public class TranslationValidator implements Validator {
	
	public Hashtable<String,String> GOOG = null;
	public Hashtable<String,String> GOLD = null;
	public MeteorScorer meteor = null ; 
	
	// Using automatic metrics to do the matching (METEOR)
	double FUZZY_MATCH_THRESHOLD_METEOR = 0.9; 
	
	public TranslationValidator() {
		MeteorConfiguration config = new MeteorConfiguration();
		ArrayList<Double> params = new ArrayList<Double>(3);
		params.add(0.95); //  alpha 
		params.add(0.5);
		params.add(0.4);
		config.setParameters(params);
		 meteor = new MeteorScorer(config);
		 GOOG = new Hashtable<String,String>();
		 GOLD = new Hashtable<String,String>();
	}
	/*
	 * Score by Meteor 
	 */
	public double score(String hyp,String ref){
		if(isValid(hyp,ref)){
			// For English 
			MeteorStats stats = meteor.getMeteorStats(hyp,ref); 
			// System.err.println(hyp+"\n"+ref+"\n"+stats.score);
			
			return stats.score;
		}
		return 0;
	}
	
	/*
	 * Score by inter annotator agreement
	 */
	public double scoreTranslations(String src,String tgt,String ref){
		double score = 0;
		if(isValid(src,tgt)){
			
		}
		return score; 
	}
	
	/* Does this match with Google translation */
	public void loadGoogle(String origfile, String googfile) {
		try {
			BufferedReader or = new BufferedReader(new InputStreamReader(new FileInputStream(origfile),"UTF8"));
			BufferedReader gr = new BufferedReader(new InputStreamReader(new FileInputStream(googfile),"UTF8"));
		
			int i=0; String src = ""; String gtrans = "";
			while((src = or.readLine()) != null){
				gtrans = gr.readLine();
				GOOG.put(MyNLP.removePunctuation(src),
						MyNLP.removePunctuation(gtrans));
				i++;
			}
			or.close(); gr.close();
		}catch(Exception e){}
	}
	
	// Match two strings 
	 public boolean match(String one, String two){
		 if(one.equalsIgnoreCase(two)){
			 return true;
		 }else{
			 // Approx matching using Meteor 
			 if(score(one,two) >= FUZZY_MATCH_THRESHOLD_METEOR){
				 return true; 
			 }
		 }
		 return false;
	 }
	 
	 public boolean matchGoogle(String input, String output){
		 if(GOLD.containsKey(input)) {
			 String gout = GOOG.get(input);
			 return match(output,gout);
		 }else{
			 System.err.println("ERROR:Nomatch:"+input);
		 }
		 return false; 
	 }
	 public boolean matchGold(String input, String output) {
		 if(GOLD.containsKey(input)) {
			 String gout = GOLD.get(input);
			 return match(output,gout);
		 }else{
			 // System.err.println("ERROR:Nomatch:"+input);
		 }
		 return false;
	 }
		
	 // Match with Gold translations 
	public void loadGold(String origfile, String goldfile) {
		try {
			BufferedReader or = new BufferedReader(new InputStreamReader(new FileInputStream(origfile),"UTF8"));
			BufferedReader gr = new BufferedReader(new InputStreamReader(new FileInputStream(goldfile),"UTF8"));
		
			
			int i=0; String src = ""; String gtrans = "";
			while((src = or.readLine()) != null){
				gtrans = gr.readLine();
				GOLD.put(MyNLP.removePunctuation(src),
						MyNLP.removePunctuation(gtrans));
				i++;
			}
			or.close(); gr.close();
		}catch(Exception e){}
	}
	
	/* Is this a valid translation */
	 public boolean isValid(String input, String output){
		 if(output.equals("") && !input.equals("")){ // EMPTY
			 // System.err.println("EMPTY: Wrong annotation:"+output);
			 return false;
		 }else if(input.equalsIgnoreCase(output)){ // COPY
			// System.err.println("COPY: Wrong annotation:"+output);
			 return false;
		 }
		 return true;
	 }
}
