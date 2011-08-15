package query;
// Decoding based Phrase Selection
// First decode the selected set of sentences to see which of those are very low scoring 
// Get entire translations for low scoring hyps, and phrase translations for high scoring sentences 

import java.util.*;

import model.smt.PhraseTable;

import options.Options;
import query.phrase.DDWDS;
import query.phrase.Diversity;
import query.semisupervised.SelfTraining;
import query.sentence.density.DensityQuerySelector;
import query.sentence.uncert.DIVCONF;
import utils.StringUtils;
import data.*;

public class GranularitySelection {
	// Path to Human alignment data 
	// Cn-En Parameters 
	// 2k Sentences Alignment
	public static String humanPhraseTable = "/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/expts/java/all/working-dir/model/phrase-table.0-0.gz"; // phrasetable trained on all data		 
	public static PhraseTable HUMAN = null; 

	public static String CONFIG = "";
	
	// Similarity threshold for sentences selected in a particular batch 
	public static int BATCH_SIZE = 1000; 

	public UDS unlabeled = null;
	public LDS labeled = null;
		
	public static String queryType, corpusFile, sSD, tSD, sL , tL ;
	public static String s,t;
	public static int tag = -1;
	public static double budget_ratio = 1; 
	
	public static String ngFile,STOPWORDS;
	// Max length of phrases considered in all selections 
	public static int PHRASE_MAX_LENGTH = 3;
	// Similarity threshold for sentences selected in a particular batch 
	public static double SIM_THRESHOLD = 0.9; 
	public static int MODE = 0;
	
	// Budget Multi Annotation Learning 
	public static double BUDGET = 0;
	public static int ROUND = 1; // Which set of sentences to pick ? 
	
	 
	// To keep track of the count of times a link has been drawn froma  Sentence 
	Hashtable<Integer,Integer> sensModified = new Hashtable<Integer, Integer>();
	// Keep track of selected links 
	Hashtable<String,Integer> selectedLinks = new Hashtable<String, Integer>(); 
	Hashtable<String,Integer> errors = new Hashtable<String, Integer>();
	
	public static void main(String args[]) throws Exception{ 
		if(args.length!=2) 
	       { 
	           System.err.println("Usage: java ALDriver <CONFIG_FILE> <TAG>");
	           System.err.println("queryType = mateck, div, den, den-div,random, uncert 0");
	           System.exit(0); 
	       }
		CONFIG = args[0];
		Options config = new Options(CONFIG);
		
		// Usually TAG is the iteration number  
		tag = Integer.parseInt(args[1]);
		budget_ratio = tag;
		 
			 queryType = config.get("QUERY_TYPE");
			 s  = config.get("SOURCE_LABEL");
			 t  = config.get("TARGET_LABEL");
			 BATCH_SIZE = config.getInt("BATCH_SIZE");
			 
			 BUDGET = config.getDouble("BUDGET");
			 ROUND = config.getInt("ROUND");
				
			// Selection in one by one mode or batch mode 
			// one by one is slower  <0=single|1=batch>
			MODE = config.getInt("MODE");
			
			corpusFile = config.get("CORPUS_LOG");
			// Other parameters 
			ngFile = config.get("NGRAM_FILE");
			STOPWORDS = config.get("STOPWORD_FILE");
			PHRASE_MAX_LENGTH= config.getInt("PHRASE_MAX_LENGTH");
			 
		DensityQuerySelector.stopwordsFile = STOPWORDS;
		// Get it from param file 
		humanPhraseTable = config.get("PTABLE_FILE"); 
		HUMAN = new PhraseTable(humanPhraseTable);
		
		GranularitySelection mal = new GranularitySelection();
		mal.selectPhrases(queryType,BATCH_SIZE);
	}
	
	public void selectPhrases(String queryType,int n){
		
		System.err.println("Selecting Phrases in Ratio:"+budget_ratio);
		
		// Load corpus 
		CorpusData cHandler = new CorpusData(corpusFile);
		labeled = new LDS(cHandler,ROUND);
				
		double TOTAL_COST = 0.0; 
		double abudget1 = BUDGET*(1-budget_ratio/10); // Budget for first annotation  
		
		System.err.println("First Annotation BUDGET:"+abudget1);
		System.err.println("Second Annotation BUDGET:"+(BUDGET-abudget1));
		
		// Final data used 
		Vector<TranslationEntry> SELECTED = new Vector<TranslationEntry>();
		
		// This only keeps track of sentences added in this iteration 
		LDS budgetLabeled = new LDS(); // No data is labeled so far ! 
		
		// Sentence selection 
		for(Integer i: labeled.data.keySet()){
			if((TOTAL_COST >= abudget1)){
				System.err.println("Budget Exhausted for 1st Annotation:"+TOTAL_COST);
				break;				
			}
			// Alright, add this sentence 
			TranslationEntry sen = labeled.data.get(i); 
			SELECTED.add(sen);
			TOTAL_COST += sen.cost;

			budgetLabeled.addEntry(sen);
		}
		System.err.println("Budget Labeled Ngrams Loaded:"+budgetLabeled.TOTAL_NGRAMS_EXISTING);
		
		// Use only currently added sentences to score the phrases 
		QuerySelector qVisitor = null;
		// qVisitor = new query.phrase.DDWDS(ngFile, budgetLabeled); 
		qVisitor = new query.sentence.density.DDWDS(ngFile, budgetLabeled); 
		
		// If money still remains, get second annotation 
		if((BUDGET - abudget1)>0){
			// Pull out and score phrases
			EntryCompare EC = new EntryCompare();
			
			//PQ_PHRASES = new PriorityQueue<TranslationEntry>(1000,EC.new TranslationEntryCompare());
			
			UDS budgetUnlabeled = new UDS(cHandler); // Get all the data as unlabeled data ! 
			budgetUnlabeled.computeCost(qVisitor);

			// Phrase selection from all the DATA  
			Hashtable<String,Double> phrases = new Hashtable<String, Double>();
			int nextCount = cHandler.data.size();
			
			// Score phrases from all the sentences 
			for(Integer i: cHandler.data.keySet()){
				
				TranslationEntry ae = cHandler.data.get(i);
				// NGRAMS : may be overlapping and so less effective (TODO: Address that first!!!)
				for(String x: ae.PHRASES){
				
				// One-Word: non-overlapping, but no Context !!! 
				//for(String x: ae.source.split("\\s+")){
					
					if( ! phrases.containsKey(x)){
						TranslationEntry pe = new TranslationEntry(nextCount++, x,x);
						// pe.score = qVisitor.computeScore(pe); 
						phrases.put(x,1.0);
						// Just adding for now !!! 
						budgetUnlabeled.addEntry(pe);
					}
				}
			}

			System.err.println("Budget Loaded phrases from Unlabeled:"+budgetUnlabeled.data.size());

			//Create Index for fast retrieval 
			budgetUnlabeled.createIndex(); 
			budgetUnlabeled.computeCost(qVisitor);
					
			TranslationEntry phrase = null;
			while((phrase=budgetUnlabeled.getTop())!=null)
			{
			 	String trans = "NA";
				if(TOTAL_COST>=BUDGET) {
					System.err.println("Budget Exhausted for 2nd Annotation:"+TOTAL_COST);
					break;
				}
				try{
					// System.err.println(phrase.toString());
					if(HUMAN.PTABLE.containsKey(phrase.source)){
						trans = HUMAN.PTABLE.get(phrase.source).getTopTrans();
						phrase.target = trans;
						
						// Elicit phrase from human too
						TOTAL_COST += phrase.cost;
						SELECTED.add(phrase);
					}
				}catch(Exception e){
					System.err.println("Not found:"+phrase.source);
					// System.err.println(e.toString());
				}
				// This is also selected !!! 
				budgetLabeled.addEntry(phrase);
				// Update scores 
				budgetUnlabeled.updateScore(phrase,qVisitor);
				budgetUnlabeled.removeEntry(phrase);
			}
		}

		cHandler.writeLabeledBudget (s, t, tag+1,SELECTED); // Write for the next round of training
		cHandler.writeSelectedBudget(s, t, tag+1, SELECTED); // Write for the previous round of sentences + some phrases
		
		// cHandler.writeUnLabeled(s, t, tag +1); // Write for the next round of training
		// cHandler.updateLog(); // Write out the updated version with selected rounds etc back to CORPUS
	}
}