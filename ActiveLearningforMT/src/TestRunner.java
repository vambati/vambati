import query.*; 

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import model.smt.PhraseTable;
import model.smt.TranslationLEXICON;

import options.Options;

import query.sentence.Diversity;
import query.sentence.Rand;
import query.sentence.density.*;
import query.sentence.hybrid.DUAL;
import query.sentence.hybrid.GraDUAL;
import query.sentence.uncert.*;

import data.*;

public class TestRunner {
	
	public static CorpusData cHandle = null; //  Main handler to manipulate with data. Acts as a Database  
	
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
		if(args.length<2) 
	       { 
	           System.err.println("Usage: java ALDriver <CONFIG_FILE> <TAG> [QTYPE]");
	           System.err.println("queryType = mateck, div, den, den-div,random, uncert 0");
	           System.exit(0); 
	       }
		Options config = new Options(args[0]);
		
		// Usually TAG is the iteration number  
		tag = Integer.parseInt(args[1]);
		
		queryType=args[2];
		
		if(queryType.equals("")){
			 queryType = config.get("QUERY_TYPE");	
		}	 
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
				 
//			 ptableFile = tag+"/working-dir/model/phrase-table.0-0.gz"; // New version of Moses doesnt have the 0-0 
//			 lex1 =  tag+"/working-dir/model/lex.0-0.n2f";
//			 lex2 =  tag+"/working-dir/model/lex.0-0.f2n";
			 
			TestRunner ss = new TestRunner();
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
		

//		QuerySelector rand	= new Rand(unlabeled);		// Random selection
//		QuerySelector den_div = new DWDS(ngFile,labeled); 	// Density - Diversity (Vamshi)			
//		QuerySelector dden_div = new DDWDS(ngFile,labeled); 	// Diminshing Density Weighted Diversity Sampling 			
//		QuerySelector dden_conf = new DDWUS(ngFile,labeled,sUL+".mosesout", tag, "s", "e"); 	// Density Weighted Confidence Sampling
//		QuerySelector conf = new US(labeled,sUL+".mosesout",tag,"s","e"); 	// Confidence Score based Sampling (Uncertainty Sampling)

		QuerySelector den = new DS(ngFile); 	// Just Density based
		QuerySelector dden = new DDS(ngFile,labeled); 	// Density Diminishing vote 			
		QuerySelector dden_ptable = new DDSPTABLE(ngFile,ptable, lexicon, labeled); 	// Density Diminishing vote 			
		QuerySelector div = new Diversity(labeled);		

		 
		/* Score computing Visitor */ 
			int count=1;
			for(Integer i: unlabeled.data.keySet()){
				count++;
				if(count==100){
					System.exit(0);
				}
				TranslationEntry e = unlabeled.data.get(i);
				
				// Raw scores 
				double dscore = den.computeScore(e);
				double ddscore = dden.computeScore(e);
				double uscore = div.computeScore(e);
				double pscore = dden_ptable.computeScore(e);
				
				// Combinations 
				double beta = 1;
				double du_score = ((1+beta*beta) *dscore *uscore ) /((beta*beta) * dscore + uscore);
				double dp_score = ((1+beta*beta) *dscore *pscore ) /((beta*beta) * dscore + pscore);
				
				System.err.println(e.toString());
				System.err.println(dscore+"\t"+ddscore+"\t"+uscore+"\t"+pscore+"\t"+du_score+"\t"+dp_score);
			}
		
//		Vector<Integer> selectedIds = new Vector<Integer>();
//		//selectBatch(qVisitor,selectedIds,BATCH_SIZE);
//		selectOnebyOne(qVisitor,selectedIds,BATCH_SIZE);
		
//		cHandle.setRound(selectedIds, tag);
//		cHandle.writeSelected(s, t, tag);
//		cHandle.writeLabeled(s, t, tag+1); // Write for the next round of training 
//		cHandle.updateLog(); // Write out the updated version with selected rounds etc back to CORPUS
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
				  System.err.println(e.toString());
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
				  
				  System.err.println(e.toString());
				  
				  labeled.addEntry(e);			  
				  unlabeled.updateScore(e,qVisitor);
				  unlabeled.removeEntry(e);
				  selectedIds.add(e.senid);
			  }
			System.err.println("Extracted "+n);
		}	 	
}
