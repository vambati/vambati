package crowdsource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import utils.MyNLP;

import crowdsource.oracle.Oracle;
import crowdsource.validate.*;

public class HITOutputSelector {
	
	public  Hashtable<String,Oracle>  oracles = null;
	public  Hashtable<String,HIT> hits = null;
	public Validator validator = null; 
	
	// Selects annotation for each hit in conjunction with the Oracle whose output 
	// needs to be taken from 
	public HITOutputSelector(Hashtable<String,HIT> hits, Hashtable<String,Oracle> oracles,Validator validator){
		this.hits = hits; 
		this.oracles = oracles; 
		this.validator = validator;
	}

	public void selectAnnotation(String inputFile,String type){
		int goldstandard_match=0;
		
		Hashtable<String,Integer> SEQ = new Hashtable<String,Integer>();
		try {
			BufferedReader or = new BufferedReader(new FileReader(inputFile));
			int i=0; 
			String src = "";
			while((src = or.readLine()) != null){
				String[] srcArr = src.split("\\t");
				//SEQ.put(MyNLP.removePunctuation(src),i);
				SEQ.put(srcArr[0], i);
				i++;
			}
			or.close();
		}catch(Exception e){System.err.println(e.toString());}
		
		// Make sentence selection and store in the order of the input
		// preserve PARALLELISM input and output coming from HITs
		Hashtable<Integer,String> output = new Hashtable<Integer,String>(1000);
		
		Random randomGenerator = new Random(System.currentTimeMillis());
		for(String hitid: hits.keySet()){
			HIT h = hits.get(hitid);
			
			String sel = "";
			if(type.equals("vote")){
				sel = majorityVote(h);
			}else if(type.equals("wvote")){
				sel = weightedMajorityVote(h);
			}else if(type.equals("rel")){
				sel = reliableVote(h);				
			}else if(type.equals("vote-rel")){
				sel = majorReliableVote(h);				
			}else if(type.equals("rand")){
				sel = randomVote(h,randomGenerator);
			}else if(type.equals("all")){
				sel = pickAll(h,2);
			}else{
				System.err.println("Wrong selection criteria::"+type);
				System.exit(0);
			}
			
			if(SEQ.containsKey(h.input)){
				int pos = SEQ.get(h.input);
				output.put(pos,sel);
				
				// Check if this matches with GOLD standard 
//				if(validator.matchGold(h.input,sel)){
//					goldstandard_match++;
//				}
			}else{
				System.err.println("NOT PRESENT:"+h.input);
				System.exit(0);
			}
		}
		
		// Print in the sequence 
		for(int i=0;i<output.size();i++){
			String str = output.get(i);
			System.out.println(str);
		}
		System.err.println("Gold standard matches:"+goldstandard_match);
	}

	// Always pick annotation that agrees MAXIMUM with other annotations for the HIT  
	public String pickAll(HIT h,int k){
		
		String best_annotation = "";
		Object outputs[] = h.ASSIGNMENT.values().toArray();
		
		for(int i=0;i<k;i++){
			String annotation = "";
			try {
				annotation = (String)outputs[i];
			}catch(Exception e){
				annotation = "NOT_AVAILABLE";
			}
			best_annotation+=annotation+"\n";
		}
		return best_annotation.replaceAll("\\n$", ""); 
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
	
	// Always pick annotation from best ORACLE 
	public String reliableVote(HIT h){
		String best_annotation = "";
		double best_reliability = -1;
		for(String workerid: h.ASSIGNMENT.keySet()){
			
			String annotation = h.ASSIGNMENT.get(workerid);
			double reliability = oracles.get(workerid).getReliability();
			
			if(best_reliability <reliability){
				best_reliability = reliability; 
				best_annotation = annotation; 
			} 
		}
		return best_annotation;
	}
	
	// Always pick majority vote
	// Break ties by picking annotation from best ORACLE 
	public String weightedMajorityVote(HIT h){
		
		// recompute a weighted Agreement 
		h.computeWeightedAgreement(validator, oracles);
		
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
		
		if(best_vote>=h.ASSIGNMENT.size()/2.0){
			return best_annotation;			
		}else{
			// System.err.println("No consensus:"+best_vote);
			String best_annotation_rel = reliableVote(h);
			return best_annotation_rel; 
		}

	}
	
	// Always pick majority vote
	// Break ties by picking annotation from best ORACLE 
	public String majorReliableVote(HIT h){
		
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
		
		if(best_vote>=h.ASSIGNMENT.size()/2.0){
			return best_annotation;			
		}else{
			// System.err.println("No consensus:"+best_vote);
			String best_annotation_rel = reliableVote(h);
			return best_annotation_rel; 
		}
	}
	
	// Pick at RANDOM. for now pick first as MTurk returns them at random  
	// TODO: Pure random 
	public String randomVote(HIT h,Random randomGenerator){
		int x = randomGenerator.nextInt(h.ASSIGNMENT.size());
		
		 // Sort hashtable.
	    Vector<String> v = new Vector<String>(h.ASSIGNMENT.keySet());
	    Collections.sort(v);
	    
		System.err.println("Picking :"+x);
 
		String workerID = v.elementAt(x); 
		return	h.ASSIGNMENT.get(workerID); 
	}
}
