package crowdsource;
/*
 * Mechanical Turk CSV file processing code
 * - Computes reliability scores for Oracles based on annotator agreement
 * - 
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;

import utils.MyNLP;
import crowdsource.oracle.*;
import crowdsource.validate.*;

public class MTurkEvaluation {
	public int rejected = 0; 
	public int total = 0; 
	
	public static Hashtable<String,Oracle>  oracles = null;
	public static Hashtable<String,HIT> hits = null;
	public static Validator validator = null;
	
	public MTurkEvaluation(String rFile) throws Exception{
		// Initialize 
		oracles = new Hashtable<String, Oracle>(); 
		hits = new Hashtable<String, HIT>(); 
		
		// Load the results 
		loadFile(rFile);
	}
	
	private void setValidator(String inputFile) {
		// Task validator 
		validator = new MTEvalValidator();
	
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
			System.err.println("Usage: java Mturk <CSV> <goldFile> <select_reliability>");
			System.exit(0);
		}
		String rFile = args[0]; 
		String goldFile = args[1];
		String sel_type = args[2]; // majority, reliability, rand, iethreshold
		
		MTurkEvaluation mturk = new MTurkEvaluation(rFile);
		mturk.setValidator(goldFile);
		
		if(sel_type.equals("ie")){
			// Perform joint selection of oracle and instance (Joint selection) 
			IEThresholdingSelector selector = new IEThresholdingSelector(hits,oracles,validator);
			selector.selectAnnotation(goldFile,sel_type); // Make selection and print in the order of the input provided
		}else{
			// Compute Reliability of Oracles
			Estimator reliabilityEstimator = null;
			reliabilityEstimator = new MajorityVotingEstimator(hits,validator);
			for(String workerid:oracles.keySet()){
				reliabilityEstimator.computeScore(oracles.get(workerid));
			}
			// Compute reliabilities then Select and Print output data as well 
			HITOutputSelector selector = new HITOutputSelector(hits, oracles,validator);
			selector.selectAnnotation(goldFile,sel_type); // Make selection and print in the order of the input provided
		}		
		//mturk.printOracleStats();
		mturk.printHITStats();
	}
	
	private void printHITStats() {
		int assignments = 0;
		for(String hitid:hits.keySet()){
			assignments+=hits.get(hitid).ASSIGNMENT.size();
		}
		
		System.err.println("------------");
		System.err.println("Hits:"+hits.keySet().size());
		System.err.println("Assignments completed:"+assignments);
		System.err.println("Optimal Set of Queries:"+IEThresholdingSelector.QUERY_COUNT);
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
	}

	// Loading a CSV results file from Amazon MTurk 
	public void loadFile(String file) throws Exception{
		
		int SRC_INDEX = 24; // Segment ID 
		int TGT_INDEX = 34; // Answer.score
		int FIELD_LIMIT = 35;
		
		System.err.println("Loading from :"+file);
		BufferedReader fr = new BufferedReader(new FileReader(file));
		int i=1; String line = "";
		while((line = fr.readLine()) != null){
			String[] toks = line.split("\",\"");
			
			if(toks.length<FIELD_LIMIT){
				System.err.println("ERROR: line "+i+": Can not have more fields:"+FIELD_LIMIT); 
				continue;
			}
			
			// Fields in the CSV file from Mturk results 
			String hitid = toks[0];
			String assignmentid = toks[14];
			String workerid = toks[15];
			String status = MyNLP.removePunctuation(toks[16]); // Submitted, Rejected, Accepted
			if(status.equals("Rejected")){
				rejected++;
				continue; 
			}
			
			String src = MyNLP.removePunctuation(toks[SRC_INDEX]);
			String tgt = MyNLP.removePunctuation(toks[TGT_INDEX]);
			
			// Checks 
			//System.err.println(tgt);
			if(tgt.equals("")){
				System.err.println("EMPTY: line "+i+": Annotation can not be empty"); 
			}
			
			// HIT 
			if(!hits.containsKey(hitid)){
				HIT x = new HIT(hitid,src);
				hits.put(hitid,x);
			}
			hits.get(hitid).addAssignment(workerid,assignmentid,tgt);
			
			// Oracle 
			if(!oracles.containsKey(workerid)){
				Oracle x = new Oracle(workerid);
				oracles.put(workerid, x);
			}
			oracles.get(workerid).submitted++; 
			i++;
		}
		System.err.println("--------------------");
		System.err.println("Total:"+i);
		System.err.println("Rejected:"+rejected);
		System.err.println("Loaded:"+(i-rejected));
	}
}