package qual;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
 
import edu.cmu.meteor.scorer.MeteorConfiguration;
import edu.cmu.meteor.scorer.MeteorScorer;
import edu.cmu.meteor.scorer.MeteorStats;
import edu.cmu.meteor.util.Constants;

import utils.MyNLP;

/* How do we validate that this is not noise - Account for Human Errors
 * 1. Non blank
 * 2. Illegitimate characters
 * 3. Length ratios ? 
 * 4. Compare with Gold Standard 
 * 	- Lexical overlap, semantic variations ??  
 * 5. Google Translation API / Other automatic translation outputs ? 
 * */

public class TranslationChecker {
	
	public Hashtable<String,String> GOOG = null;
	public MeteorScorer meteor = null ; 
	
	// Using automatic metrics to do the matching (METEOR)
	
	double FUZZY_MATCH_THRESHOLD_METEOR = 0.7; 
	double FUZZY_MATCH_THRESHOLD_METEOR_FOR_GOOGLE = 0.8; // Be strict for google match 
	
	public TranslationChecker() {
		MeteorConfiguration config = new MeteorConfiguration();
		
		ArrayList<Double> params = new ArrayList<Double>(3);
		params.add(0.5); //  alpha 
		params.add(1.0);
		params.add(1.0);
		config.setParameters(params);
		
		// Only exact match (Simulate TER and BLEU)
//		ArrayList<Integer> mods = new ArrayList<Integer>(1);
//		mods.add(Constants.MODULE_EXACT);
//		config.setModules(mods);
		
		 meteor = new MeteorScorer(config);
		 GOOG = new Hashtable<String,String>();
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
	public void loadGoogle(String googfile) {
		System.err.println("Loading Google results from:"+googfile); 
		try {
			BufferedReader gr = new BufferedReader(new FileReader(googfile));
			int i=0; 
			String line = "";  
			while((line = gr.readLine()) != null){
				String[] arr = line.split("\\t");
				String id = arr[0];
				String gtrans = arr[1];
				GOOG.put(id, gtrans);
				i++;
			}
			gr.close();
		}catch(Exception e){
			System.err.println("No Google file:"+e.toString());
			System.exit(0);
		}
	}
	
	// Match two strings 
	 public boolean match(String one, String two){
		 
		one = MyNLP.removePunctuation(one);
		one = one.replaceAll("[^\\p{ASCII}]", "");
		
		two = MyNLP.removePunctuation(two);
		two = two.replaceAll("[^\\p{ASCII}]", "");
		
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
		 output = MyNLP.removePunctuation(output);
		 output = output.replaceAll("[^\\p{ASCII}]", "");
		 
		 if(GOOG.containsKey(input)) {
			 String gout = GOOG.get(input);
			 
			 gout = MyNLP.removePunctuation(gout);
			 gout = gout.replaceAll("[^\\p{ASCII}]", "");
				
			 if(gout.equalsIgnoreCase(output)){
				 System.err.println("YES:"+output+"\n"+gout+"\n");
				 return true;
			 }else{
				 // Approx matching using Meteor 
				 if(score(gout,output) >= FUZZY_MATCH_THRESHOLD_METEOR_FOR_GOOGLE){
					 System.err.println("YES:"+output+"\n"+gout+"\n");
					 return true; 
				 }
			 }
			 System.err.println("NO:"+output+"\n"+gout+"\n");
		 }else{
			// System.err.println("ERROR:Nomatch:"+input);
		 }
		 return false; 
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
