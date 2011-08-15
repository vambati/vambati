package data;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

import query.*; 
import utils.*;

/*
 * Unlabeled dataset
 */
public class UDS {
	// Data containing all the Entries 
	public Hashtable<Integer,TranslationEntry> data;
	public Hashtable<Integer,Double> updatedScores;
	
	public HashSet<Integer> deletedData;
	
	// Mapping from Phrase to Sentence Ids 
	public Hashtable<String,HashSet<Integer>> invertedIndex = null;

	// Priority Queue to help do the sorting 
	PriorityQueue<TranslationEntry> pq = null;
	
	public Hashtable<String,Integer> NGRAMS_UNLABELED = null;
	public double TOTAL_NGRAMS_UNLABELED = 0;


	// Used to maintain vocabulary of sentences chosen in a particular batch
	// Useful in Batch mode only !!!
	HashSet<String> chosen = null;
	
	double max_score =0;
	int max_i = -1;
	boolean require_sort = true;
	private boolean add;
	
	public UDS(CorpusData cHandle){
		data = new Hashtable<Integer,TranslationEntry>();
		deletedData = new HashSet<Integer>();
		updatedScores = new Hashtable<Integer,Double>();
		invertedIndex = new Hashtable<String,HashSet<Integer>>();
		
		EntryCompare EC = new EntryCompare();
		pq = new PriorityQueue<TranslationEntry>(1000,EC.new TranslationEntryCompare());
		chosen = new HashSet<String>();
		
		NGRAMS_UNLABELED = new Hashtable<String, Integer>();
	}
	
	public UDS(CorpusData cHandle,int round){
		data = new Hashtable<Integer,TranslationEntry>();
		deletedData = new HashSet<Integer>();
		updatedScores = new Hashtable<Integer,Double>();
		invertedIndex = new Hashtable<String,HashSet<Integer>>();
		
		EntryCompare EC = new EntryCompare();
		pq = new PriorityQueue<TranslationEntry>(1000,EC.new TranslationEntryCompare());
		chosen = new HashSet<String>();
		data = cHandle.getUnLabeled(round);
		

		NGRAMS_UNLABELED = new Hashtable<String, Integer>();
		
		// Create inverted Index 
		createIndex();
	}
	
	public void createIndex(){
		for(Integer i: data.keySet()){
			TranslationEntry e = data.get(i); 
			for(String p: e.PHRASES){
				// Only an index 
				if(!invertedIndex.containsKey(p)){
					HashSet<Integer> tmp = new HashSet<Integer>();	
					invertedIndex.put(p,tmp);
				}
				invertedIndex.get(p).add(e.senid);
				
				// Need for computation within this dataset 
//				if(!NGRAMS_UNLABELED.containsKey(p)){
//					NGRAMS_UNLABELED.put(p,0);
//				}
//				NGRAMS_UNLABELED.put(p,(NGRAMS_UNLABELED.get(p)+1));
//				TOTAL_NGRAMS_UNLABELED++;
			}
		}
		//System.err.println("Created inverted index for "+invertedIndex.size() + " phrases");
	}
	
	public void addEntry(TranslationEntry e){
		//Add to data 
		data.put(e.senid,e);
		// Create index at the end. 
	}
	
	public void removeEntry(TranslationEntry e){
		//Remove from data
		deletedData.add(e.senid);
	}
	
	public void updateScore(TranslationEntry e, QuerySelector qVisitor){
		HashSet<Integer> affectedSens = new HashSet<Integer>();
		  
		for(String p:e.PHRASES){
			HashSet<Integer> tmp = invertedIndex.get(p);
			affectedSens.addAll(tmp);
		}
		computeCost(qVisitor,affectedSens);
	}
	
	/* Score computing Visitor  over only those sentences which matter */ 
	public void computeCost(QuerySelector qs,HashSet<Integer> idSet){
		for(Integer i: idSet){
			//System.err.print(i+" ");
			//System.err.println("Previous score:"+data.get(i).score);
			TranslationEntry e = data.get(i);
			double score = qs.computeScore(e);
			//System.err.println("Current score:"+data.get(i).score+"-----------\n");
			
			// Add another entry to reflect updated scores. 
			// Not deleting previous entry (DOES THIS WORK ? NO) 
			updatedScores.put(e.senid,score);
		}
	}
	
	/* Score computing Visitor */ 
	public void computeCost(QuerySelector qs){
		pq.clear();
		for(Integer i: data.keySet()){
			if(i%10000==0){
				System.err.print(i+" ");
			}
			TranslationEntry e = data.get(i);
			double score = qs.computeScore(e);
			
			// Insert into priority queue as well
			pq.add(e);
		}
	}
		 	
	public TranslationEntry getTop(){
 
		pq.clear(); 
		for(Integer i: data.keySet()){
			pq.add(data.get(i));
		}
 		 
		int flag = 1;
		while(flag==1){
			 TranslationEntry p = pq.poll();
			 //System.err.println("Top:"+p.toString());
			 // If sentence already seen, then ignore (could be book keeping)
			 if(deletedData.contains(p.senid)){
				 //System.err.println("Skip:"+p.toString());
				 continue;
			 }
			 else{
				flag = 0;
				return p;
			 }
		}
		return null;
	}
	
	 /* Sort phrase table */
	/* Method to sort values in a hashtable */
	public Vector<TranslationEntry> getTopK(int k) {
		 /* Print top k*/		
		
		 Vector<TranslationEntry> results = new Vector<TranslationEntry>(k);
		 while(k>0){
			 TranslationEntry p = pq.poll();
			 // If sentence already seen, then ignore (could be book keeping)
			 if(deletedData.contains(p.senid)){
				 continue;
			 }
			 if(p==null){
				 System.err.println("What the fk");
			 }
			 // Check if it is similar to batch collected so far
			 results.add(p);
			 
			 k--;
//			 double simscore = similarity(p.input);
//			 if(simscore<SentenceSelection.SIM_THRESHOLD){
//				 results.add(p);
//				 k--;
//			 }
		 }
	return results;
	}
	public double similarity(String str){
		// Remove Puncutation 
		str = MyNLP.removePunctuation(str);
		
		double simscore = 0; 
		String[] arr = str.split("\\s+");
		for(String x:arr){
			if(chosen.contains(x)){
				simscore++;
			}
		}
		simscore = simscore / arr.length;
		
		for(String x:arr){
			chosen.add(x);
		}
		return simscore; 
	}
}
