package crowdsource;
/*
 * Mechanical Turk CSV file processing code
 * - Computes reliability scores for Oracles based on annotator agreement
 * - 
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import com.amazonaws.mturk.addon.*;


import utils.MyNLP;
import crowdsource.oracle.*;
import crowdsource.validate.*;

public class MTurkTranslation {
	public int rejected = 0; 
	public int total = 0;
	
	int SELECT_FROM = 15; 
	
	// optimal number of queries 
	public static int OPT_QUERY_COUNT = 0;
	
	public static Hashtable<String,Oracle>  oracles = null;
	public static Hashtable<String,HIT> hits = null;
	public static TranslationValidator validator = null;
	
	public MTurkTranslation(String rFile) throws Exception{
		// Initialize 
		oracles = new Hashtable<String, Oracle>(); 
		hits = new Hashtable<String, HIT>();
		
		// Load the results 
		loadFile(rFile);
	}
	
	private void setValidator(String inputFile, String googFile, String goldFile) {
		// Task validator 
		validator = new TranslationValidator();
		validator.loadGoogle(inputFile,googFile);
		validator.loadGold(inputFile,goldFile);
		
		// Compute sufficient stats for Gold standard Matching
		for(String hitid: hits.keySet()){
			HIT h = hits.get(hitid);
			for(String workerid: h.ASSIGNMENT.keySet()){
				// input, output 
				if(validator.isValid(h.input, h.ASSIGNMENT.get(workerid))){ // Check data entry problems
					oracles.get(workerid).errors++;
				}
				if(validator.matchGoogle(h.input, h.ASSIGNMENT.get(workerid)) ) { // Check gaming issues - Google, Yahoo?
					oracles.get(workerid).googlematch++;
				}
				if(validator.matchGold(h.input, h.ASSIGNMENT.get(workerid)) ) { // Check gaming issues - Google, Yahoo?
					oracles.get(workerid).goldmatch++;
				}
			}
		}
		
		// Compute sufficient stats for Majority Voting Matching
		for(String hitid: hits.keySet()){
			HIT h = hits.get(hitid);
			// Compute agreement 
			h.computeAgreement(validator);
			
			// Propagate and update agreement values amongst the oracles 
			// that participated in this particular HIT 
			for(String workerid: h.ASSIGNMENT.keySet()){
				oracles.get(workerid).agreement += h.AGREEMENT.get(workerid); 
			}
		}
	}
	
	public static void main(String args[]) throws Exception {

		if(args.length!=2){
			System.err.println("Usage: java Mturk <file> ");
			System.err.println("Will be used as: java Mturk <file.csv> <file> <file.google> <file.ref>");
			System.err.println("Sel_type: vote, wvote,rel, vote-rel, rand, ie, none");
			System.exit(0);
		}
		String tag = args[0];
		String sel_type = args[1]; // majority, reliability, rand, iethreshold
		
		String rFile = tag+".csv"; 
		String inputFile = tag;
		String googFile = tag+".google";
		String goldFile = tag+".ref";
		
		MTurkTranslation mturk = new MTurkTranslation(rFile);
		mturk.printAllHITs();
		
		mturk.setValidator(inputFile,googFile,goldFile);
		System.exit(0);
		
		if(sel_type.equals("none")){
			
		}else if(sel_type.equals("ee")){
			// Perform joint selection of oracle and instance (Joint selection) 
			 ExpExpSelector selector = new ExpExpSelector(hits,oracles,validator);
				selector.selectAnnotation(inputFile,sel_type); // Make selection and print in the order of the input provided
		}else if(sel_type.equals("ie")){
			// Perform joint selection of oracle and instance (Joint selection) 
			 IEThresholdingSelector selector = new IEThresholdingSelector(hits,oracles,validator);
				selector.selectAnnotation(inputFile,sel_type); // Make selection and print in the order of the input provided
		}else{
			// Compute Reliability of Oracles
			Estimator reliabilityEstimator = null;
			//reliabilityEstimator = new GoldStandardEstimator(hits,validator);
			reliabilityEstimator = new MajorityVotingEstimator(hits,validator);
			for(String workerid:oracles.keySet()){
				reliabilityEstimator.computeScore(oracles.get(workerid));
			}
			// Compute reliabilities then Select and Print output data as well 
			HITOutputSelector selector = new HITOutputSelector(hits, oracles,validator);
			selector.selectAnnotation(inputFile,sel_type); // Make selection and print in the order of the input provided
		}
				
		// mturk.printOracleStats();
		
	}
	
	private void printAllHITs() {
		int count = 1; 
		for(String hitid:hits.keySet()) {
			int rank = 1; 
			 System.out.println("SrcSent "+count+"\t"+hits.get(hitid).input);
			for(String key: hits.get(hitid).ASSIGNMENT.keySet() ) {
			 System.out.println(count+" "+rank+"\t"+hits.get(hitid).ASSIGNMENT.get(key));
			 rank++;
			}
		count++;
		}
	}

	
	private void printHITStats() {
		int assignments = 0;
		int mvote_eixsts = 0;
		for(String hitid:hits.keySet()){
			assignments+=hits.get(hitid).ASSIGNMENT.keySet().size();
			//assignments+=hits.get(hitid).assignments;
			if(hits.get(hitid).majorityExists()){
				mvote_eixsts++;
			}
		}
		
		System.err.println("------------");
		System.err.println("Hits:"+hits.keySet().size());
		System.err.println("Majority Vote exists for :"+mvote_eixsts);
		System.err.println("Assignments completed:"+assignments);
		System.err.println("Optimal Set of Queries:"+OPT_QUERY_COUNT);
	}
	
	private void printOracleStats() {
		System.err.println("------------");
		int gmatches = 0;
		for(String wid:oracles.keySet()){
			gmatches+=oracles.get(wid).googlematch;
			System.err.println(oracles.get(wid).toString());
		}
		System.err.println("------------");
		System.err.println("Oracles:"+oracles.keySet().size());
		System.err.println("GoogleMatches:"+gmatches);
	}

	// Loading a CSV results file from Amazon MTurk 
	public void loadFile(String file) throws Exception{
//		
 		//String SRC_INDEX = "Input.phrase1";
 		//String TGT_INDEX = "Answer.tgt1";

		String SRC_INDEX = "Input.src";
		String TGT_INDEX = "Answer.tgt";

		
		Date firstSubmit = null;
		Date lastSubmit = null;
		double totalWorkTime = 0; 
		
		System.err.println("Loading from :"+file);
	      //Loads the .success file containing the HIT IDs and HIT Type IDs of HITs to be retrieved.
	      HITDataCSVReader csvFile = new HITDataCSVReader(file,',');

	      System.err.println("Total Rows in CSV file "+csvFile.getNumRows());
	      int i=0;
	      int rejected=0;
	      
	      for(i=1;i<csvFile.getNumRows();i++){
	    	  
	       Map<String,String> hitLine = csvFile.getRowAsMap(i); 
	       
			if(hitLine.size()!=27){
			}
			
			// Fields in the CSV file from Mturk results 
			String hitid = hitLine.get("HITId");
			String assignmentid = hitLine.get("AssignmentId");
			String workerid = hitLine.get("WorkerId");
			double worktime = Double.parseDouble(hitLine.get("WorkTimeInSeconds"));
			String dateString = hitLine.get("SubmitTime");
			String status = hitLine.get("AssignmentStatus");
			
			totalWorkTime += worktime;
			
			Date date = null;
			//Sun Feb 21 04:25:15 GMT 2010
			String pattern = "EEE MMM dd kk:mm:ss zzz yyyy";
		    SimpleDateFormat format = new SimpleDateFormat(pattern);
		    try {
		       date = format.parse(dateString);
		    } catch (ParseException pe) {
		      pe.printStackTrace();
		    }
		    			
		    if(firstSubmit==null){
		    	firstSubmit = date;
		    	lastSubmit = firstSubmit;
		    }
		    
			if(date.after(lastSubmit)){
				lastSubmit = date;
			}
			if(date.before(firstSubmit)){
				firstSubmit = date;
			}
			
			// Submitted, Rejected, Accepted
			if(status.equals("Rejected")){
				rejected++;
				continue; 
			}
			
			String src = MyNLP.removePunctuation(hitLine.get(SRC_INDEX));
			String tgt = MyNLP.removePunctuation(hitLine.get(TGT_INDEX));
			if(tgt.equals("")){
				System.err.println("EMPTY: line "+i+": Annotation can not be empty");
			}
			
			// HIT 
			if(!hits.containsKey(hitid)){
				HIT x = new HIT(hitid,src);
				hits.put(hitid,x);
			}
			
			// Only add a given number of assignments 
			if(hits.get(hitid).ASSIGNMENT.size()<SELECT_FROM){
				hits.get(hitid).addAssignment(workerid,assignmentid,tgt);	
			}
			
			// Oracle 
			if(!oracles.containsKey(workerid)){
				Oracle x = new Oracle(workerid);
				oracles.put(workerid, x);
			}
			oracles.get(workerid).submitted++; 
		}
		System.err.println("--------------------");
		System.err.println("Total:"+i);
		System.err.println("Rejected:"+rejected);
		System.err.println("Loaded:"+(i-rejected));
		System.err.println("FirstSubmitted:"+firstSubmit);
		System.err.println("LastSubmitted:"+lastSubmit);
		System.err.println("TotalTimeSpent mins:"+totalWorkTime/60);
		System.err.println("Avg secs per translation:"+totalWorkTime/i);
		System.err.println("Time to completion:");
	}
}