package multidomain;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import model.smt.PhraseTable;
import model.smt.TranslationLEXICON;

import options.Options;

import query.QuerySelector;
import query.sentence.Diversity;
import query.sentence.Rand;
import query.sentence.density.*;
import query.sentence.hybrid.DUAL;
import query.sentence.hybrid.GraDUAL;
import query.sentence.uncert.*;

import data.*;

public class MultiDomainSelection {
	
  // Common across domains !
	public static String CONFIG_FILE = ""; 	 
	public static String STOPWORDS = "";  
	public static String ptableFile = "",lex1="",lex2=""; 
	public static int PHRASE_MAX_LENGTH = 3; 
	public static double SIM_THRESHOLD = 0.9;
	public static int BATCH_SIZE = 1000; 
	public static int MODE = 0;
	public static String queryType, s,t,sSD, tSD ;
	public static int tag;
	
	// Domain specific labels three domains ! 
	public static String ngFile = "", ngFile2="", ngFile3="";
	public UDS unlabeled1 = null, unlabeled2 = null, unlabeled3= null;
	public LDS labeled1 = null, labeled2 = null, labeled3= null;
	
	public static CorpusData cHandle = null; //  Main handler to manipulate with data. Acts as a Database
	public static String corpusFile1, corpusFile2, corpusFile3;
	
	
	public static void main(String args[]){
		if(args.length!=2) 
	       { 
	           System.err.println("Usage: java ALDriver <CONFIG_FILE> <TAG>");
	           System.err.println("queryType = mateck, div, den, den-div,random, uncert 0");
	           System.exit(0); 
	       }
		CONFIG_FILE = args[0]; 
		Options config = new Options(CONFIG_FILE);
		
		// Usually TAG is the iteration number  
		tag = Integer.parseInt(args[1]);
		 
			 queryType = config.get("QUERY_TYPE");
			 s  = config.get("SOURCE_LABEL");
			 t  = config.get("TARGET_LABEL");
			 BATCH_SIZE = config.getInt("BATCH_SIZE"); 
				
			// Selection in one by one mode or batch mode 
			// one by one is slower  <0=single|1=batch>
			MODE = config.getInt("MODE");
			
			// Other parameters 
			ngFile = config.get("NGRAM_FILE");
			STOPWORDS = config.get("STOPWORD_FILE");
			PHRASE_MAX_LENGTH= config.getInt("PHRASE_MAX_LENGTH");
			SIM_THRESHOLD = config.getDouble("SIM_THRESHOLD");
			 		
			corpusFile1 = config.get("CORPUS_LOG1");
			corpusFile2 = config.get("CORPUS_LOG2");
			corpusFile3 = config.get("CORPUS_LOG3");
 
			// Also load a second domain data file
			String corpusFile2 = config.get("CORPUS_LOG_OOD"); // Second Domain Data
			
			 sSD = s+".ssd."+tag;
			 tSD = t+".ssd."+tag;
			 
			 ptableFile = tag+"/working-dir/model/phrase-table.gz"; // New version of Moses doesnt have the 0-0 
			 lex1 =  tag+"/working-dir/model/lex.e2f";
			 lex2 =  tag+"/working-dir/model/lex.f2e";
				 
			MultiDomainSelection ss = new MultiDomainSelection();
			ss.selectSentences(queryType,sSD,tSD,BATCH_SIZE,tag);
		}
	
	public void selectSentences(String queryType, String sSD,String tSD,int n,int tag){
		
		System.err.println("==========MULTI-ANNOTATION ACTIVE LEARNING==========");
		// Load corpus 
		cHandle = new CorpusData(corpusFile1);		
		unlabeled1 = new UDS(cHandle,tag);
		labeled1 = new LDS(cHandle,tag);
		
		// Load corpus (Domain 2)
		CorpusData cHandle2 = new CorpusData(corpusFile2);		
		unlabeled2= new UDS(cHandle,tag);
		labeled2 = new LDS(cHandle,tag);
		
		// Load corpus (Domain 3) 
		CorpusData cHandle3 = new CorpusData(corpusFile3);		
		unlabeled3 = new UDS(cHandle3,tag);
		labeled3 = new LDS(cHandle3,tag);
		 
		String curDir = System.getProperty("user.dir");
		System.err.println("Cur:"+curDir);
		
		// SMT Model 
		PhraseTable ptable = new PhraseTable(ptableFile);
		TranslationLEXICON lexicon = new TranslationLEXICON(lex1,lex2);
		
		QuerySelector qVisitor = null;
		String sUL = ""; // TODO : Lets handle this later ...18 April 2010 
		
		DensityQuerySelector.stopwordsFile = STOPWORDS;
		
		if(queryType.equalsIgnoreCase("rand")){
		}
		/* Density Weighted Multi-Annotation using Difficult to Translate Sentence Classifier (DTS) */
		else if(queryType.equalsIgnoreCase("den-dts")){ 
		}
		else{
			System.err.println("Query Selection Mode "+queryType+" not present");
			System.exit(0);
		}
		
		// Batch mode or single mode selection 
		// single or one by one requires 'resorting' which is slow
		Vector<Integer> selectedIds = new Vector<Integer>();
		if(MODE==1){
			selectBatch(qVisitor,selectedIds,n);
		}else if(MODE==0) {
			selectOnebyOne(qVisitor,selectedIds,n);
		}
		
		cHandle.setRound(selectedIds, tag);
		cHandle.writeSelected(s, t, tag);
		cHandle.writeLabeled(s, t, tag+1); // Write for the next round of training
		cHandle.writeUnLabeled(s, t, tag+1); // Write for the next round of training
		cHandle.updateLog(); // Write out the updated version with selected rounds etc back to CORPUS 
	}
	
		/* Selection in Batch Mode Active Learning 
		 * TODO: MMR based approach to have diversity in batch */
		public void selectBatch(QuerySelector qVisitor,Vector<Integer> selectedIds, int n) {
		
			// Compute costs of the sentences using the Visitor Class
			unlabeled1.computeCost(qVisitor);
			unlabeled2.computeCost(qVisitor);
			unlabeled3.computeCost(qVisitor);
			
			// Pick top n entries as the selected batch (SSD)
			
		}
		
		/* Selection of N entries one by one by Rescoring 
		 * TODO: Efficient way of re-computing for score updation*/
		public void selectOnebyOne(QuerySelector qVisitor,Vector<Integer> selectedIds, int n){

		}
}
