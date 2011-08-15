package query;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import model.smt.PhraseTable;
import model.smt.TranslationLEXICON;

import options.Options;

import query.sentence.Diversity;
import query.sentence.OOV;
import query.sentence.Rand;
import query.sentence.density.*;
import query.sentence.hybrid.DUAL;
import query.sentence.hybrid.GraDUAL;
import query.sentence.uncert.*;

import data.*;

public class SentenceSelection {
	
	public static CorpusData cHandle = null; //  Main handler to manipulate with data. Acts as a Database  
	public static String CONFIG_FILE = ""; 
	
	public static String ngFile = ""; 
	public static String STOPWORDS = "";  
	public static String ptableFile = "",lex1="",lex2="";
	
	// Max length of phrases considered in all selections 
	public static int PHRASE_MAX_LENGTH = 3;
	// Similarity threshold for sentences selected in a particular batch 
	public static double SIM_THRESHOLD = 0.9;
	public static int BATCH_SIZE = 1000; 
	public static int MODE = 0;

	public UDS unlabeled = null;
	public LDS labeled = null;
	
	public static String queryType, s,t,sSD, tSD, corpusFile ;
	public static int tag;
	
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
			 		
			corpusFile = config.get("CORPUS_LOG");
			 sSD = s+".ssd."+tag;
			 tSD = t+".ssd."+tag;
			 
			 ptableFile = tag+"/working-dir/model/phrase-table.gz"; // New version of Moses doesnt have the 0-0 
			 lex1 =  tag+"/working-dir/model/lex.e2f";
			 lex2 =  tag+"/working-dir/model/lex.f2e";
				 
			SentenceSelection ss = new SentenceSelection();
			ss.selectSentences(queryType, corpusFile, sSD,tSD,BATCH_SIZE,tag);
		}
	
	public void selectSentences(String queryType,String corpusFile,String sSD,String tSD,int n,int tag){
		// Load corpus 
		cHandle = new CorpusData(corpusFile);
		// cHandle.clearRound();
		
		unlabeled= new UDS(cHandle,tag);
		labeled = new LDS(cHandle,tag);
		
		String curDir = System.getProperty("user.dir");
		System.err.println("Cur:"+curDir);
		
		// SMT Model 
		PhraseTable ptable = new PhraseTable(ptableFile);
		TranslationLEXICON lexicon = new TranslationLEXICON(lex1,lex2);
		
		QuerySelector qVisitor = null;
		String sUL = ""; // TODO : Lets handle this later ...18 April 2010 
		
		DensityQuerySelector.stopwordsFile = STOPWORDS;
		
		if(queryType.equalsIgnoreCase("rand")){
			qVisitor = new Rand(unlabeled);		// Random selection
			MODE = 1; // random can be BATCH selection 
		}
		/* Density Based methods */
		else if(queryType.equalsIgnoreCase("mateck")){
			qVisitor = new MatEck(ngFile,labeled);		// Mathias Eck Baseline (Eck 2005 paper)	
		}else if(queryType.equalsIgnoreCase("hafsarkar")){
			qVisitor = new HafSarkar(ngFile,labeled);	// Gholamreza Haffari and Maxim Roy and Anoop Sarkar 2008 Paper	
		}else if(queryType.equalsIgnoreCase("den")){
			System.err.println("Density Sampling");
			qVisitor = new DS(ngFile); 	// Just Density based 			
		}else if(queryType.equalsIgnoreCase("dden")){
			System.err.println("Diminshing Density Sampling");
			qVisitor = new DDS(ngFile,labeled); 	// Density Diminishing vote
		}else if(queryType.equalsIgnoreCase("dden-ptable")){
			System.err.println("Model based Density Sampling");
			qVisitor = new DDSPTABLE(ngFile,ptable, lexicon, labeled); 	// Density Diminishing vote 			
		}
		// Crazy lets try ! 
		else if(queryType.equalsIgnoreCase("dden-ptable-dual")){
			System.err.println("DUAL and Model based Density Sampling");
			qVisitor = new GRADUALPTABLE(ngFile,ptable, lexicon, labeled); 	// Density Diminishing vote 			
		}
		
		// New Methods !  
		else if(queryType.equalsIgnoreCase("kl-div")){
			System.err.println("KL Divergence approach (only looks at existing data!!! Can't use huge monolingual)");
			qVisitor = new KLDivergence(labeled,unlabeled); 	// Density Diminishing vote 			
		}
		
		/* Diversity based methods */
		else if(queryType.equalsIgnoreCase("oov")){
			System.err.println("OOV reduction Sampling");
			qVisitor = new OOV(labeled);  
		}
		else if(queryType.equalsIgnoreCase("div")){
			System.err.println("Diversity Sampling");
			qVisitor = new Diversity(labeled);
		}else if(queryType.equalsIgnoreCase("den-div")){
			System.err.println("Density Weighted Diversity Sampling");
			qVisitor = new DWDS(ngFile,labeled); 	// Density - Diversity (Vamshi)			
		}else if(queryType.equalsIgnoreCase("dden-div")){
			System.err.println("Diminshing Density Weighted Diversity Sampling");
			qVisitor = new DDWDS(ngFile,labeled); 	// Diminshing Density Weighted Diversity Sampling 			
		}
		
		/* Hybrid Methods */
		else if(queryType.equalsIgnoreCase("dual")){
			System.err.println("Dual approach to combining Div and Den as a function of Diversity and Density");
			qVisitor = new DUAL(ngFile,labeled); 	// Diminshing Density Weighted Diversity Sampling 			
		}else if(queryType.equalsIgnoreCase("gradual")){
			System.err.println("GraDual approach to combining Div and Den as a function of Diversity and Density");
			qVisitor = new GraDUAL(ngFile,labeled,tag); 	// Diminshing Density Weighted Diversity Sampling 			
		}
		
		/*
		 * Decoding based Selection Methods 
		 */
		else if(queryType.equalsIgnoreCase("dden-conf")){
			qVisitor = new DDWUS(ngFile,labeled,CONFIG_FILE, tag, s,t); 	// Density Weighted Confidence Sampling
		}else if(queryType.equalsIgnoreCase("conf")){
			qVisitor = new US(labeled,CONFIG_FILE,tag,s,t); 	// Confidence Score based Sampling (Uncertainty Sampling)
			MODE = 1; // Run it as batch
		}else if(queryType.equalsIgnoreCase("div-conf")){
			qVisitor = new DIVCONF(labeled,CONFIG_FILE,tag,s,t); 	// Confidence Score based Sampling (Uncertainty Sampling)
		}
		
		/* Uncertainty Model based methods */		
		else if(queryType.equalsIgnoreCase("cond-entropy")){
			PhraseEntropy.stopwordsFile = STOPWORDS;
			qVisitor = new PhraseEntropy(ptableFile,unlabeled,labeled); 	// Phrasal Entropy
			MODE = 1; // Run it as batch 
		}else if(queryType.equalsIgnoreCase("uncert2")){
			qVisitor = new PhraseIG(ptableFile); 			// Phrasal InformationGain Entropy 
		} else{
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
			unlabeled.computeCost(qVisitor);
	 
			// Pick top n entries as the selected batch (SSD)
			Vector<TranslationEntry> selected = new Vector<TranslationEntry>(n); 
			selected = unlabeled.getTopK(n);
			
			  for(int i=0;i<n;i++){
				  TranslationEntry e = selected.get(i);
				 selectedIds.add(e.senid); 
			  }
		}
		
		/* Selection of N entries one by one by Rescoring 
		 * TODO: Efficient way of re-computing for score updation*/
		public void selectOnebyOne(QuerySelector qVisitor,Vector<Integer> selectedIds, int n){
			 
			//Compute costs of the sentences using the Visitor Class
			unlabeled.computeCost(qVisitor);
			
			  for(int i=0;i<n;i++){
				  System.err.print(i+" ");
				  	  
				  // Sort and retrive top one
				  TranslationEntry e = unlabeled.getTop();
				  
				  // System.out.println(e.toString());
				  System.out.println("SELECTED:\t"+e.source);
				  
				  labeled.addEntry(e);		  
				  unlabeled.updateScore(e,qVisitor);
				  unlabeled.removeEntry(e);
				  selectedIds.add(e.senid);
			  }
			System.err.println("Extracted "+n);
		}
}
