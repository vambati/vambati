package query;

import java.util.*;

import model.smt.PhraseTable;

import options.Options;
import query.phrase.DDWDS;
import query.phrase.Diversity;
import query.sentence.density.DensityQuerySelector;
import utils.StringUtils;
import data.*;

public class PhraseSelection {
	// Path to Human alignment data 
	// Cn-En Parameters 
	// 2k Sentences Alignment
	public static String humanPhraseTable = "/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/expts/java/all/working-dir/model/phrase-table.0-0.gz"; // phrasetable trained on all data		 
	public static PhraseTable HUMAN = null; 
		
	// Similarity threshold for sentences selected in a particular batch 
	public static int BATCH_SIZE = 1000; 

	public UDS unlabeled = null;
	public LDS labeled = null;
		
	public static String queryType, corpusFile, sSD, tSD, sL , tL ;
	public static String s,t;
	public static int tag = -1;
	
	public static String ngFile,STOPWORDS;
	// Max length of phrases considered in all selections 
	public static int PHRASE_MAX_LENGTH = 3;
	// Similarity threshold for sentences selected in a particular batch 
	public static double SIM_THRESHOLD = 0.9; 
	public static int MODE = 0;
	 
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
		Options config = new Options(args[0]);
		
		// Usually TAG is the iteration number  
		tag = Integer.parseInt(args[1]);
		 
			 queryType = config.get("QUERY_TYPE");
			 s  = config.get("SOURCE_LABEL");
			 t  = config.get("TARGET_LABEL");
			 BATCH_SIZE = config.getInt("BATCH_SIZE"); 
				
			// Selection in one by one mode or batch mode 
			// one by one is slower  <0=single|1=batch>
			MODE = config.getInt("MODE");
			
			corpusFile = config.get("CORPUS_LOG");
			// Other parameters 
			ngFile = config.get("NGRAM_FILE");
			STOPWORDS = config.get("STOPWORD_FILE");
			PHRASE_MAX_LENGTH= config.getInt("PHRASE_MAX_LENGTH");
			 
			 sL = s+".l."+tag;
			 tL = t+".l."+tag;
	 
			 sSD = s+".ssd."+tag;
			 tSD = t+".ssd."+tag;
			 
		DensityQuerySelector.stopwordsFile = STOPWORDS;
		// Get it from param file 
		humanPhraseTable = config.get("PTABLE_FILE"); 
		HUMAN = new PhraseTable(humanPhraseTable);
		
		PhraseSelection ls = new PhraseSelection();
		ls.selectPhrases(queryType,BATCH_SIZE);
	}
	
	public void selectPhrases(String queryType,int n){
		// Load corpus 
		CorpusData cHandler = new CorpusData(corpusFile);
		labeled = new LDS(cHandler,tag);
		unlabeled = new UDS(cHandler,tag);	
		Hashtable<Integer,TranslationEntry> selectedData =  cHandler.getSelected(tag+1);
		
		// Score links to be selectively modified 
		Vector<TranslationEntry> pq = new Vector<TranslationEntry>(); 
		 
 		int nextCount = cHandler.data.size(); 
		
		for(Integer id: selectedData.keySet())
		{
			TranslationEntry ae = selectedData.get(id);

			System.err.println(ae.source+"\n");
			
			String[] arr = ae.source.split("\\s+");
			for(String w:arr){
				if(!labeled.NGRAMS_EXISTING.containsKey(w)){
					System.err.println("\t"+w);
					TranslationEntry pe = new TranslationEntry(nextCount++, w,w);
					pq.add(pe);
				}
			}	
		}
		
		// Clear existing round before starting to SAVE on phrases 
		// cHandler.clearRound(tag+1);
		
		Hashtable<String,Integer> selected = new Hashtable<String, Integer>(); 
		
		int count=0;
		for(TranslationEntry te: pq)
		{
			String trans = "NA";
			if(count>BATCH_SIZE){
				break;
			}
			if(selected.containsKey(te.source)){
				continue;
			}
			selected.put(te.source, 1);
			
			try{
				System.err.println(te.toString());
				if(HUMAN.PTABLE.containsKey(te.source)){
					trans = HUMAN.PTABLE.get(te.source).getTopTrans();
					te.target = trans; 
				}
			}catch(Exception e){
				System.err.println("Not found:"+te.source);
				System.err.println(e.toString());	
			}
			
			labeled.addEntry(te);
			cHandler.addEntry(te, tag+1); // Add to the Corpus Handler 
			unlabeled.removeEntry(te);
			count++;
		}
		cHandler.writeSelected(s, t, tag+1, pq); // Write for the previous round of sentences + some phrases
		cHandler.writeLabeled(s, t, tag+1,pq); // Write for the next round of training
		cHandler.writeUnLabeled(s, t, tag+1); // Write for the next round of training
		cHandler.updateLog(); // Write out the updated version with selected rounds etc back to CORPUS
	}
}