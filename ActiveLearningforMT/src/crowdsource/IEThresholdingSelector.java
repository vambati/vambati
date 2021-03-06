package crowdsource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.PriorityQueue;

import utils.MyNLP;

import crowdsource.oracle.Oracle;
import crowdsource.validate.*;
import data.EntryCompare;

public class IEThresholdingSelector {
	
	public  Hashtable<String,Oracle>  oracles = null;
	public  Hashtable<String,HIT> hits = null;
	Validator validator = null;
	
	// Threshold for upperlimit 
	double EPSILON = 0.01;
	
	// total queries 
	public static int QUERY_COUNT = 0;
	
	// Selects annotation for each hit in conjunction with the Oracle whose output 
	// needs to be taken from 
	public IEThresholdingSelector(Hashtable<String,HIT> hits, Hashtable<String,Oracle> oracles, Validator validator){
		this.hits = hits; 
		this.oracles = oracles;
		this.validator = validator;
	}

	public void selectAnnotation(String inputFile,String type){
		Hashtable<String,Integer> SEQ = new Hashtable<String,Integer>();
		try {
			BufferedReader or = new BufferedReader(new FileReader(inputFile));
			int i=0; String src = "";
			while((src = or.readLine()) != null){
				SEQ.put(MyNLP.removePunctuation(src),i);
				i++;
			}
			or.close();
		}catch(Exception e){}
		
		// Make sentence selection and store in the order of the input
		// preserve PARALLELISM input and output coming from HITs
		Hashtable<Integer,String> output = new Hashtable<Integer,String>(1000); 
		for(String hitid: hits.keySet()){
			HIT h = hits.get(hitid);
			
			String sel = iethresholdVote(h);
 
			if(SEQ.containsKey(h.input)){
				int pos = SEQ.get(h.input);
				output.put(pos,sel);	
			}else{
				//System.err.println("NOT PRESENT:"+h.input);
				//System.exit(0);
			}
		}
		
		// Print in the sequence 
		for(int i=0;i<output.size();i++){
			System.out.println(output.get(i));
		}
	}
	private String iethresholdVote(HIT h) {
		Hashtable<String,Oracle> oracleset = sampleOracles(h);
		
		// Compute agreement among only the chosen Oracles for majority voting 
		 h.computeAgreement(validator,oracleset);
		// h.computeAgreement(validator);
		
		// Select majority vote from among these oracles 
		// Agreement count for Oracles not selected is ZERO , 
		 // so just find majority vote over all
		String best = majorityVote(h);
		
		// Update rewards and counts for the Oracles in this hit 
		for(String workerid: h.ASSIGNMENT.keySet()){
			// Rewards for not-selected oracles shud be ZERO
			 double reward = h.AGREEMENT.get(workerid);
			 oracles.get(workerid).addTask(reward);
		}
		return best;
	}
	
	// Select oracles that are within an EPSILON of the "Upper limit"
	private Hashtable<String,Oracle> sampleOracles(HIT h){
		EntryCompare EC = new EntryCompare(); 
		PriorityQueue<Double> limits = new PriorityQueue<Double>(100,EC.new DescendingCompare());
		
		 for(String workerid: h.ASSIGNMENT.keySet()){ // From all participant Oracles in this HIT 
			limits.add(oracles.get(workerid).getUpperInterval());
		}
		double upperInterval = limits.peek();
		
		// Select oracles above a threshold
		Hashtable<String,Oracle> oracleset = new Hashtable<String,Oracle>();
		
		for(String workerid: h.ASSIGNMENT.keySet()){
			double ui = oracles.get(workerid).getUpperInterval();
			if(upperInterval-ui <= EPSILON ){
				oracleset.put(workerid,oracles.get(workerid));
			}
		}
		//System.err.print(upperInterval+"("+oracleset.size()+")");
		if(oracleset.isEmpty()){
			QUERY_COUNT+=1;	
		}else{
			QUERY_COUNT+=oracleset.size();
		}
		
		System.err.print("("+oracleset.size()+")");
		return oracleset;  
	}
	
	// Always pick annotation that agrees MAXIMUM with other annotations for the HIT  
	public String majorityVote(HIT h){
		String best_annotation = "";
		double best_vote = -1;
		for(String workerid: h.ASSIGNMENT.keySet()){
			String annotation = h.ASSIGNMENT.get(workerid);
			double vote = h.AGREEMENT.get(workerid);
			
			if(best_vote<vote){
				best_vote = vote; 
				best_annotation = annotation; 
			}
		}
		return best_annotation; 
	}
}
