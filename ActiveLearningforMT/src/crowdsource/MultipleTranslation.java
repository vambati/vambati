package crowdsource;
/*
 * Mechanical Turk CSV file processing code
 * - Computes reliability scores for Oracles based on annotator agreement
 * - 
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import com.amazonaws.mturk.addon.*;


import utils.MyNLP;
import crowdsource.oracle.*;
import crowdsource.validate.*;

public class MultipleTranslation {
	public int rejected = 0; 
	public int total = 0;
	
	int SELECT_FROM = 10; 
	
	// optimal number of queries 
	public static int OPT_QUERY_COUNT = 0;
	
	public static Hashtable<String,Oracle>  oracles = null;
	public static Hashtable<String,HIT> hits = null;
	public static TranslationValidator validator = null;
	
	public MultipleTranslation(String rFile) throws Exception{
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

		if(args.length<2){
			System.err.println("Usage: java Mturk <file> ");
			System.err.println("Will be used as: java Mturk <file.csv> <file> <file.google> <file.ref>");
			System.err.println("Sel_type: vote, wvote,rel, vote-rel, rand, ie, none");
			System.exit(0);
		}
		
		String rFile = args[0];
		String inputFile = args[1];
		String goldFile = args[2];
		String sel_type = args[3];  
		
		MultipleTranslation mturk = new MultipleTranslation(rFile);
		int assignments = 3;

		for(String hitid:hits.keySet()){
			System.out.println(hits.get(hitid).ASSIGNMENT.toString());
		}
		
		System.exit(0);
		
		
		mturk.setValidator(inputFile,goldFile,goldFile);
		
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
		mturk.printHITStats();
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

		
		BufferedReader sr = new BufferedReader(new FileReader(file));
		String line="";
		int count=0,id=-1;
		while((line= sr.readLine()) != null){
			String lineArr[] = line.split("\\t");
  			// Fields in the CSV file from Mturk results
			
			String hitid = lineArr[0]; 
			String workerid = lineArr[1];
			String tgt = lineArr[2];
 			
			// HIT 
			if(!hits.containsKey(hitid)){
				HIT x = new HIT(hitid,hitid+"");
				hits.put(hitid,x);
			}
			
			// Only add a given number of assignments 
			if(hits.get(hitid).ASSIGNMENT.size()<SELECT_FROM){
				int assignmentid = hits.get(hitid).ASSIGNMENT.size()+1;
				hits.get(hitid).addAssignment(workerid,assignmentid+"",tgt);	
			}
			
			// Oracle 
			if(!oracles.containsKey(workerid)){
				Oracle x = new Oracle(workerid);
				oracles.put(workerid, x);
			}
			oracles.get(workerid).submitted++; 
		}
		System.err.println("--------------------");
	}
}