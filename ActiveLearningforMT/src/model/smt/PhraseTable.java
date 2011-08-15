package model.smt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import utils.StringUtils;

import data.TranslationEntry;
import data.UDS;

public class PhraseTable {
	public double BACKOFF = -1;
	public double ENTROPY_BACKOFF = -1;
	public double NUM_PHRASES=0;

	public HashMap<String,LexicalEntry> PTABLE = null;
	// To load ptable in src_given_target or target_given_source mode
	public boolean isReverse = false; 
	
	public PhraseTable (String path,boolean isReverse){
		PTABLE = new HashMap<String,LexicalEntry>();
		this.isReverse = isReverse; 
		load(path);
	}
	
	public int partitions(){
		return PTABLE.size();
	}
	
	public boolean containsSrc(String src){
		return PTABLE.containsKey(src);
	}
	
   public PhraseTable(String path) {
	   PTABLE = new HashMap<String,LexicalEntry>();
		this.isReverse = false; 
		load(path);
		
        BACKOFF = 1.0/ (double) PTABLE.size();
        ENTROPY_BACKOFF = -1.0 * BACKOFF * Math.log(BACKOFF);
	}

public void load(String file) {
	    System.err.println("Loading phrasetable from "+file);
	    if(isReverse==true){
	    	System.err.println(" in reverse Direction: by TGT for Information Gain stuff");
	    }
	    try{
	    GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(file));
	    BufferedReader corpusReader = new BufferedReader(new InputStreamReader(gzis));
	    
	    String[] tokens;
	    String str = "";
	    while( (str = corpusReader.readLine())!=null)
	    {
	        tokens   = str.split(" \\|\\|\\| ");
	
	        if(tokens.length!=5)
	            continue;
	
	        String src = tokens[0];
	        String tgt = tokens[1];
//	        String a = tokens[2];
//	        String b = tokens[3];
	        String scores = tokens[4];
	        String []probs = scores.split("\\s+");
	
	        if(isReverse==false){
		        LexicalEntry trans = null;
		        if(!PTABLE.containsKey(src)){
		        	trans = new LexicalEntry(src);
		        }else{
		        	trans = PTABLE.get(src);
		        }
				// 5 features from moses ptable 
				Double prob1 = Double.parseDouble(probs[0]);
		    	trans.addTrans(tgt, prob1);
		        PTABLE.put(src,trans);	   	 
		        
	        }else{
		        LexicalEntry trans = null;
		        if(!PTABLE.containsKey(tgt)){
		        	 trans = new LexicalEntry(src);
		        }else{
		        	trans = PTABLE.get(tgt);
		        }
				// 5 features from moses ptable 
				Double prob1 = Double.parseDouble(probs[2]);
		    	trans.addTrans(src, prob1);
		        PTABLE.put(tgt,trans);	   
	        }
	        
	        NUM_PHRASES++;
	    }
	   System.err.println("Loaded phrasetable with entries: "+ NUM_PHRASES);
	    }catch(Exception e){System.err.println(e.toString());}
    }
   
	public double computePerplexity(){
		return Math.pow(2, computeEntropy());
	}
	
	public double computeEntropy(String src){
		return PTABLE.get(src).computeEntropy();
	}
	
   public double computeEntropy(){
	   double avg_entropy = 0;
	   for(String src: PTABLE.keySet()){
		   avg_entropy+=PTABLE.get(src).computeEntropy();
	   }
	   return (avg_entropy/(double)PTABLE.size()); // Normalize ?
	   //return (avg_entropy);
   }
   
   /* Sort phrase table */
	/* Method to sort values in a hashtable */
	public void sortModel(int k,int type) {
		PriorityQueue<LexicalEntry> pq = null;
		LexicalEntryCompare PEC = new LexicalEntryCompare();
		
		if(type==0){
			System.out.println("Sorting based on ENTROPY");
		pq = new PriorityQueue<LexicalEntry>(1000,PEC.new EntropyCompare());
		}else if(type==1){
			System.out.println("Sorting based on IG");
			pq = new PriorityQueue<LexicalEntry>(1000,PEC.new IGCompare());
		}else if (type==2){
			System.out.println("Sorting based on IG Ratio");
			pq = new PriorityQueue<LexicalEntry>(1000,PEC.new IGRatioCompare());
		}
		
		 for(String src: PTABLE.keySet()){
			 pq.add(PTABLE.get(src));
		   }
		 /* Print top k*/
//		 for(int i=0;i<k;i++){
//			 LexicalEntry p = pq.poll();
//			 System.out.println(p.toString());
//		 }
	}

	    // Compute entropy for all the possible phrases in a given file 
    public double computeFileEntropy(HashMap<String,Integer> dlog){
    	double avg_entropy = 0.0;    	
    		for(String sp: dlog.keySet()){
    			if(PTABLE.containsKey(sp)){
	    			avg_entropy+=PTABLE.get(sp).computeEntropy();
    			}else{
    				avg_entropy+= ENTROPY_BACKOFF;
    			}
    		}
    	return avg_entropy/dlog.size(); 
    }

	public double targetEntriesOf(String p) {
		return PTABLE.get(p).translations.size();
	}

	public double typetok(String filename) {
		
		// Load from the log file  
		int MAX_LENGTH = 3;
		
		double count=0; 
		double totaltok=0; double tok=0;
		double totaltype=0; double type=0;
		HashMap<String,Integer> dlog = new HashMap<String, Integer>(1000); 
		
		try{ 		
			BufferedReader sr = new BufferedReader(new FileReader(filename));
			String line="";
			while((line= sr.readLine()) != null){
				// IMPORTANT 
				line = line.toLowerCase(); 
				Vector<String> phrases = StringUtils.allPhrases(line,MAX_LENGTH); 
				for(String p:phrases) {
					if(PTABLE.containsKey(p)){
						tok++; 
					}
					totaltok++;
					
					if(dlog.containsKey(p)){
						int c= dlog.get(p);
						dlog.put(p,c++);
					}else{
						dlog.put(p,1);
					}
				}
 				count++;
			}
			sr.close(); 
			
			for(String p: dlog.keySet()){
				if(PTABLE.containsKey(p)){
					type++;
				}
			}
			totaltype = dlog.size(); 
			
		} catch (Exception e) { e.printStackTrace();}
		
		double typeR = type/totaltype;
		double tokR = tok/totaltok;
		
		double ttr = 2*(tokR * typeR)/(typeR+tokR);
		
		//System.err.println(typeR+"\t"+tokR+"\t"+ttr);
		return ttr;
	}
}
