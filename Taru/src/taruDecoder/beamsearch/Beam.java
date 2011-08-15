package taruDecoder.beamsearch;

import java.util.*;

import taruHypothesis.*;

public class Beam {

	private int id = -1;
	private HashSet<String> uniqueKBest;
	PriorityQueue<Hypothesis> kbest = null;
	
	int cur_size;
	int max_size = 1000;
		
	public Beam(int id){
		// Check: Make sure it is added in the Descending order
		this.id = id; 
		kbest = new PriorityQueue<Hypothesis>(max_size,new HypothesisScoreComparator());
		uniqueKBest = new HashSet<String>(10);
	}
	
	public boolean addKBestHypothesis(Hypothesis hyp){
		// Check if we have already seen this string
//		if(uniqueKBest.contains(hyp.getWords()))
//			return false;
		// Looks like a new one. Add to both kbest sorted list and unique hash
		if(!uniqueKBest.contains(hyp.getWords())){
			System.err.println("Something doesn't look right !");
			throw new Error("Adding to kbest without adding to unique first !");
		}
		kbest.add(hyp);
		uniqueKBest.add(hyp.getWords());
//		System.out.println("Adding new hyp: " + hyp);
		return true;
	}
	
	
	// [TODO] This method is used by the initial hypothesis being added. Not a good idea
	// and not correct since we may get the same hyp with better score which will be discarded
	public boolean addHypothesis(Hypothesis hyp){
		if(cur_size < max_size){
			// Check if we have already seen this string
			if(uniqueKBest.contains(hyp.getWords()))
				return false;
			else{
				kbest.add(hyp);
				uniqueKBest.add(hyp.getWords());
				System.out.println("Adding new hyp: " + hyp.getWords());
				return true;
			}
		}
		return false;
	}

	public boolean containsHyp(String hyp){
		return uniqueKBest.contains(hyp);
	}

	public int size(){
		return cur_size; 
	}
	
	public void setSize(int x){
		max_size = x;
	}
	
	public List<Hypothesis> getTopKHypothesis(int k){
//		System.out.println(k+" "+kbest.size());
		if(k <= kbest.size()){
			k = kbest.size();
		}
		ArrayList<Hypothesis> list = new ArrayList<Hypothesis>();
		for(int i=0;i<k;i++){
			list.add(kbest.poll());
		}
		return list;
	}
}
