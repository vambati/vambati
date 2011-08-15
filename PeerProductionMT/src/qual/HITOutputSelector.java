package qual;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Random;

import utils.MyNLP;


public class HITOutputSelector {
	
	public  Hashtable<String,Oracle>  oracles = null;
	public  Hashtable<String,HIT> hits = null;
	public TranslationChecker validator = null; 
	
	// Selects annotation for each hit in conjunction with the Oracle whose output 
	// needs to be taken from 
	public HITOutputSelector(Hashtable<String,HIT> hits, Hashtable<String, qual.Oracle> oracles2,TranslationChecker validator2){
		this.hits = hits; 
		this.oracles = oracles2; 
		this.validator = validator2;
	}

	public void selectAnnotation(String inputFile,String outputFile, String type){
	 	
		try {
		BufferedWriter bwsrc = new BufferedWriter(new FileWriter(outputFile+".src"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		
		// bw.write("Urdu	English	1273597227\n");
		// Make sentence selection and store in the order of the input
		// preserve PARALLELISM input and output coming from HITs
		Hashtable<Integer,String> output = new Hashtable<Integer,String>(1000);
		 
		for(String hitid: hits.keySet()){
			HIT h = hits.get(hitid);
			
			String sel = "";
			if(type.equals("all")){
				sel = allScores(h);
				bwsrc.write(h.id+"\n");
				bw.write(sel);
				continue;
			}
			else if(type.equals("vote")){
				sel = majorityVote(h);
			}else if(type.equals("wvote")){
				sel = weightedMajorityVote(h);
			}else if(type.equals("rel")){
				sel = reliableVote(h);				
			}else if(type.equals("vote-rel")){
				sel = majorReliableVote(h);			
			}else if(type.equals("rand")){
				sel = randomVote(h);
			}else{
				System.err.println("Wrong selection criteria::"+type);
				System.exit(0);
			}
			//if(h.input.startsWith("cmu")){
				// Without scores 
			bw.write(sel+"\n");
			bwsrc.write(h.input+"\n");
			//}
		}
		bw.flush();
		bw.close();
		bwsrc.flush(); bwsrc.close(); 
		}catch(Exception e){
			System.err.println(e.toString());
		}
	}
	
	
	// Always pick annotation that agrees MAXIMUM with other annotations for the HIT  
	public String allScores(HIT h){
		
		String all = "";
		double best_vote = -1;
		for(String workerid: h.ASSIGNMENT.keySet()){
			
			String annotation = h.ASSIGNMENT.get(workerid);
			double vote = h.AGREEMENT.get(workerid);
			vote=vote/3.0;
			
			 all+=h.input+"\t"+annotation+"\t"+vote+"\n";  
		}
		return all; 
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
			return reliableVote(h); 
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
	public String randomVote(HIT h){
		Random randomGenerator = new Random();
		int x = randomGenerator.nextInt(h.ASSIGNMENT.size());
		// System.err.println("Selecting:"+x);
		
		String workerID = h.ASSIGNMENT.keys().nextElement(); 
		return	h.ASSIGNMENT.get(workerID); 
	}
}
