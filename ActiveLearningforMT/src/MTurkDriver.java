
import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.addon.HITDataInput;

import crowdsource.mturk.*;
public class MTurkDriver {
	// Windows 
static String propertiesFile = "G:/mturk/vamshi.properties";

// static String xmlFile =   "G:/workspace-eclipse/ActiveLearningforMT/data/mturk/translation.xml";
// static String hitPropertiesFile = "G:/workspace-eclipse/ActiveLearningforMT/data/mturk/translation.properties";

static String xmlFile = "G:/workspace-eclipse/ActiveLearningforMT/data/mturk/phrase.xml";
static String hitPropertiesFile = "G:/workspace-eclipse/ActiveLearningforMT/data/mturk/phrase.properties"; 

// static String xmlFile = "G:/workspace-eclipse/ActiveLearningforMT/data/mturk/phrase_context.xml";
// static String hitPropertiesFile = "G:/workspace-eclipse/ActiveLearningforMT/data/mturk/phrase_context.properties"; 

	public static void main(String[] args) throws Exception {
		String dataFile = args[0];
		if(args.length!=2){
			System.err.println("java MTurkDriver FILE 0:translate|1:review");
		}
		if(args[1].equals("1")){
			review(dataFile);	
		}else{
			postHITs(dataFile);
		}
	}
	public static void review(String file) throws Exception {
		String successFile = file+".success"; 
		String outputFile = file+".csv";
		
		// TranslateReviewer r = new TranslateReviewer(propertiesFile);
		PhraseTranslateReviewer r = new PhraseTranslateReviewer(propertiesFile);
		
		HITDataInput success = new HITDataCSVReader(successFile);
		r.getResults(successFile, outputFile);
		  
		for(int i=1;i<success.getNumRows();i++){
			String hitid = success.getRowAsMap(i).get("hitid");
			try{
				r.reviewAnswers(hitid);
			}catch(Exception e){System.err.println(e.toString());}
		}
	}
		public static void postHITs(String dataFile){
	    MyRequester app = new MyRequester(propertiesFile);
	    
	    if(app!=null){
		    if (app.hasEnoughFund()) {
		      app.fromFile(hitPropertiesFile,xmlFile,dataFile,dataFile+".success",dataFile+".failure");
		      System.err.println("Success.");
		    } else {
		      System.err.println("You do not have enough funds to create the HIT.");
		    }
		  }
	    else{
		  System.err.println("Could not create APP!! Exit");
	    }
	 }
}