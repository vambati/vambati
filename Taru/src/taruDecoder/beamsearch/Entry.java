package taruDecoder.beamsearch; 

import java.util.*;

import taruDecoder.hyperGraph.HyperGraph;
import taruHypothesis.Hypothesis;
import taruHypothesis.HypothesisScoreComparator;

public class Entry
{
	public boolean visiting;
	
	public String type;
	private int start;
	private int end;

	private BitSet span;
	
	private ArrayList<Integer> inEdges;
	private ArrayList<Hypothesis> kbest;
	private HashSet<String> uniqueKBest;
		
	private boolean exhausted;
	
	public Entry(int start, int end, String type)
	{
		this.type = type;
		this.start = start;
		this.end = end;
		span = new BitSet();
		
		if(start != -1 && end != -1){
			span.set(start, end+1);
		}
		
		inEdges = new ArrayList<Integer>();
		kbest = new ArrayList<Hypothesis>();
		uniqueKBest = new HashSet<String>();
		
		exhausted = false;
	}

	public Entry(String type)
	{
		this.type = type;
		
		span = new BitSet();
		
		inEdges = new ArrayList<Integer>();
		kbest = new ArrayList<Hypothesis>();
		uniqueKBest = new HashSet<String>();
		
	}

	public void addSpan(BitSet span){
		this.span.or(span);
	}

	public void addInEdge(int edgeId){
		inEdges.add(new Integer(edgeId));
	}

	public boolean addUniqueKBest(String hyp){
		if(uniqueKBest.contains(hyp))
			return false;
		uniqueKBest.add(hyp);
		return true;
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
		// Check if we have already seen this string
		if(uniqueKBest.contains(hyp.getWords()))
			return false;
		// Looks like a new one. Add to both kbest sorted list and unique hash
		kbest.add(hyp);
//		Collections.sort(kbest, new HypothesisScoreComparator());
		uniqueKBest.add(hyp.getWords());
		// System.out.println("Adding new hyp: " + hyp.getWords());
		return true;
	}

	public boolean containsHyp(String hyp){
		return uniqueKBest.contains(hyp);
	}
	
	// This function is needed because we don't keep lexicon sorted.
	public void sortHyps(){
		if(kbest.size() > 1){
			Collections.sort(kbest, HyperGraph.getHypScoreComparator());
			
			if(kbest.size() > 20){
				ArrayList<Hypothesis> kbestCopy = new ArrayList<Hypothesis>();
				uniqueKBest.clear();
				for(int i = 0; i < 20; i++){
					kbestCopy.add(kbest.get(i));
					uniqueKBest.add(kbest.get(i).getWords());
				}
				kbest = kbestCopy;
			}
		}
	}
	
	public int availableKBest(){
		return kbest.size();
	}
	
	public Hypothesis getKthHypothesis(int k){
//		System.out.println(k+" "+kbest.size());
		if(k-1 < kbest.size())
			return kbest.get(k-1);
		return null;
	}
	
	public List<Hypothesis> getTopKHypothesis(int k){
//		System.out.println(k+" "+kbest.size());
		if(k >= kbest.size())
			return kbest;
		else{
			return kbest.subList(0, k);
		}
	}

	public void setExhausted(){
		exhausted = true;
	}
	
	public boolean isExhausted(){
		return exhausted;
	}
	
	public void flushInEdges(){
		inEdges.clear();
	}
	
	public void flushHypsWithEdgePointers(){
		// Only flush the hyps that have some back pointers
		ArrayList<Hypothesis> kbestCopy = new ArrayList<Hypothesis>();
		for(Hypothesis h : kbest){
			if(h.getEdgeId() == -1){
				kbestCopy.add(h);
				uniqueKBest.remove(h.getWords());
			}
		}
		kbest = kbestCopy;
	}
	
	public int getSourceStart(){
		return start;
	}
	
	public int getSourceEnd(){
		return end;
	}
	
	public String getType(){
		return type;
	}
	
	public BitSet getSpan(){
		return span;
	}
	
	public ArrayList<Integer> getInEdges(){
		return inEdges;
	}
		
	public String toString()
	{
		String str =  "{<"+type+","+span.toString()+">\n";
		str += "Hyps:\n";
		for(Hypothesis h : kbest){
			str += h.toString();
		}
		str += "In Edges: " + inEdges.toString() + "}\n";
		return str;
	}

}
