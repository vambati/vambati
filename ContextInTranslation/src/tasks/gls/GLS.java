package tasks.gls; 

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import classifiers.DataSet;
import classifiers.Entry;
import classifiers.MaxEntClassifier;
import options.*;

/**
 * Global Lexical Selection 
 * @author Vamshi Ambati
 * 1 Mar 2009
 * Carnegie Mellon University
 *
 */

public class GLS 
{
	public static String sourceFile;
	public static String targetFile;
	public static String modelDir;
	
	public static int stopat =-1;
	
	// Index for each source sentence bag  
	Hashtable<Integer,String[]> sourceSentences;
	// Inverted Index from targetword to source sentence indices 
	Hashtable<String,HashMap<Integer,Integer>> targetWordMap; 
	
	// Classifier interface
	MaxEntClassifier mxC = new MaxEntClassifier(); 
	
	public GLS(Options opts) throws Exception
	{	
		// CORPUS and Parses
		 sourceFile = opts.get("SOURCE_FILE");
		 targetFile = opts.get("TARGET_FILE");
		 modelDir = opts.get("MODEL_DIR");
		 
		 if(opts.defined("STOPAT")){
			 stopat = Integer.parseInt(opts.get("STOPAT")); 
		 }
		 
		 System.err.println("Source file:"+sourceFile);
		 System.err.println("Target file:"+targetFile);
		 System.err.println("Load until:"+stopat);
		 
		sourceSentences = new Hashtable<Integer, String[]>();
		targetWordMap = new Hashtable<String, HashMap<Integer,Integer>>();
	}
		
	public void start() throws Exception
	{
		System.err.println("Loading Corpus...");
		loadCorpus();
		System.err.println("Creating training sets and models per target word...");
		// Create sets which have atleast N entries in them 
		createTrainingSets(100);
		System.err.println("Completed.");
	}		
	
	// Create training sets for each of the words so that BINARY CLASSIFIERS can be training '1' or '0' 
	// Only create those which have atleast 'cutoff' entries in it
	public void createTrainingSets(int cutoff) throws Exception {
		
		// Restrict features size for each entry 
		int CONTEXT_SIZE = 10; 
		
		System.err.println("Creating training sets for the entire Vocabulary...");
		 for(String tword: targetWordMap.keySet()){
			 
			 if(tword.contains("/")){
				 continue;
			 }
			 // Training data set for each target word 
			 DataSet ds = new DataSet(tword);
			 HashMap<Integer,Integer> index = targetWordMap.get(tword);
			 
			 // Create negative and positive examples from the sentences 
			 for(Integer sid: sourceSentences.keySet()){
				 // What is the outcome 
				 String outcome = "";
				 // Create the context
				 ArrayList<String> context = new ArrayList<String>();
				 String[] bagofwords = sourceSentences.get(sid);
				 
					 int max = CONTEXT_SIZE; 
					 if(bagofwords.length<=CONTEXT_SIZE) { max = bagofwords.length; } 
					 for(int i=0;i<max;i++)
						 context.add(bagofwords[i]);
				 
				 if(index.containsKey(sid)){
					 // Positive Example
					 outcome = "1";
					 Entry e = new Entry(outcome,context);
					 ds.addPositiveEntry(e);
				 }else{
					 // Negative Example
					 outcome = "-1";
					 Entry e = new Entry(outcome,context);
					 ds.addNegativeEntry(e);
				 }
			 }
			 System.err.println("Created for "+tword+" positive:"+ds.positive+" negative:"+ds.negative);
			 // Max Ent training (only for those streams with greater than cutoff 
			 if(ds.positive>cutoff)
				 mxC.train(ds);
		 }
		 System.err.println("Creating training sets Done!");
	}

	public void loadCorpus () throws Exception
	{
	   	BufferedReader sourceReader = null ;
	   	BufferedReader targetReader = null ;
	   	
		try {	
		sourceReader= new BufferedReader(new InputStreamReader(new FileInputStream(GLS.sourceFile)));
		targetReader= new BufferedReader(new InputStreamReader(new FileInputStream(GLS.targetFile)));
		}catch(IOException ioe){}
	
		String sl ="",tl="";
		int sennum=1;
		int twordnum=1;
		while( (sl = sourceReader.readLine())!=null)
		{
			// Add this to source sentence hash 
			sourceSentences.put(sennum, sl.split("\\s+"));
			
			tl = targetReader.readLine();
			
			String[] tokens = tl.split("\\s+");			
			for(int i=0;i<tokens.length;i++){
				String tword = tokens[i]; 
				if(!targetWordMap.containsKey(tword)){
					targetWordMap.put(tword,new HashMap<Integer,Integer>());
					twordnum++;
				}
				targetWordMap.get(tword).put(sennum,1); 
			}
			sennum++;
			
			if(sennum==GLS.stopat){
				break;
			}
		}
		System.err.println("Loaded...");
		System.err.println("Source sentences count:"+sennum);
		System.err.println("Target Vocabulary Size:"+twordnum);
	}
}
