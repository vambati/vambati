package query;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import options.Options;

import model.smt.TranslationLEXICON;
import query.link.*;
import data.*;

public class LinkSelection2 {

	// Path to Human alignment data 
	// Cn-En Parameters 
	// 2k Sentences Alignment
	public static String humanAlignmentFile = "";
	public static String coocfile = "/mnt/1tb/usr6/vamshi/ActiveLearning/Cn-En/model1/model1.lexicon";
	
	// Score links to be selectively modified 
	PriorityQueue<LinkEntry> pq = null; 
	
	// Similarity threshold for sentences selected in a particular batch 
	public static final int COOC_THRESHOLD = 10;
	public static int BATCH_SIZE = 1000; 

	public AlignmentData automatic = null;
	public AlignmentData human = null;
	
	// Assumes that alignment starts from 1 
	int OFFSET  = 1;
	
	// To keep track of the count of times a link has been drawn froma  Sentence 
	Hashtable<Integer,Integer> sensModified = new Hashtable<Integer, Integer>();
	// Keep track of selected links 
	Hashtable<String,Integer> selectedLinks = new Hashtable<String, Integer>(); 
	
	// Key = Src+:+tgt , Value = Sentence ID   
	public Hashtable<String,Integer> humanCorpus = null;
	
	public static String queryType, mDir, sL , tL, aL;
	public static String gizaSGTLexFile, gizaTGSLexFile,gizaTGSFile,gizaSGTFile;
	public static String sSD, tSD, aSD, srcFreqFile, tgtFreqFile; 
	
	public static void main(String args[]) throws Exception{
  	   if(args.length<3) 
       { 
           System.err.println("Usage: java LinkSelection2 <CONFIG> <queryType> <N>");  
           System.exit(0); 
       }       	 
			Options config = new Options(args[0]);
			// Usually TAG is the iteration number  	 
			queryType = config.get("QUERY_TYPE");
			BATCH_SIZE = config.getInt("BATCH_SIZE");
			// Override 
			queryType = args[1];
			BATCH_SIZE = Integer.parseInt(args[2]);
			  	 
			 sL = config.get("SOURCE"); 
			 tL = config.get("TARGET"); 
			 aL = config.get("AUTO_ALIGN"); 
			 humanAlignmentFile = config.get("HUMAN_ALIGN");

			 // Chinese to English:SGT
			 gizaSGTLexFile = config.get("SGT_LEX");
			 gizaTGSLexFile = config.get("TGS_LEX");
			 
			 // Chinese to English:SGT
			 gizaSGTFile = config.get("SGT_ALIGN");
			 gizaTGSFile = config.get("SGT_ALIGN");
			 
			 srcFreqFile =  config.get("SRC_FREQ");
			 tgtFreqFile = config.get("TGT_FREQ");
			 
			 sSD = "tmp.s.new";
			 tSD = "tmp.t.new";
			 aSD = "tmp.a.new";
 		 		 
		LinkSelection2 ls = new LinkSelection2();
		ls.selectSentences(queryType,sL, tL,aL, BATCH_SIZE);
	}
	
	public void selectSentences(String queryType,String sL,String tL,String aL ,int n){
		// Load corpus 
		automatic = new AlignmentData(sL,tL,aL,OFFSET);
		human = new AlignmentData(sL,tL,humanAlignmentFile,OFFSET);
			
		QuerySelector qVisitor = null;
				
		// Link Confidence formulation similar to Fei Huang (ACL 2008 paper) 
		if(queryType.equals("conf")){
			qVisitor = new ConfidenceLink(gizaSGTLexFile,gizaTGSLexFile);		 
		}else if(queryType.equals("posterior")){
			qVisitor = new PosteriorLink(gizaSGTLexFile,gizaTGSLexFile);		 
		}else if(queryType.equals("margin")){
			// qVisitor = new MarginLink(gizaSGTLexFile,gizaTGSLexFile);
			qVisitor = new MarginLink2(gizaSGTLexFile,gizaTGSLexFile);		 
		}else if(queryType.equals("cooc")){
			qVisitor = new DictionaryLink(coocfile);		 
		}else if(queryType.equals("entropy")){
			qVisitor = new EntropyLink(gizaSGTLexFile,gizaTGSLexFile);		 
		}else if (queryType.equals("intersect")){
			qVisitor = new Bidirectional(gizaSGTFile,gizaTGSFile,OFFSET);
		}else if (queryType.equals("hybrid")){
			qVisitor = new HybridLink(gizaSGTLexFile,gizaTGSLexFile,srcFreqFile,tgtFreqFile,gizaSGTFile,gizaTGSFile,coocfile);
		}else{
			System.err.println("Unsupported query method");
			System.exit(0);
		}

		// Score all the links inside each AlignmentEntry object
		automatic.computeCost(qVisitor);
		
		// Pretend to select to understand batch representativeness 
		selectLinks(qVisitor);
	}
	
	/* Selection in Batch Mode Active Learning */
	public void scoreLinks(QuerySelector qVisitor) {

		// Sort them 
		EntryCompare EC = new EntryCompare();
		pq = new PriorityQueue<LinkEntry>(1000,EC.new LinkEntryCompare());
		 
		int links=0;
		for(Integer i: automatic.data.keySet()){
			// Expand alignment and get each link out of it
			AlignmentEntry ae = automatic.data.get(i);
			String sw[]=ae.source.split("\\s+");
			String tw[]=ae.target.split("\\s+");
			
			for(int x: ae.LINKS.keySet()){
				for(int y: ae.LINKS.get(x).keySet()){
					try {
						LinkEntry le = new LinkEntry(i,x,y);
						String str = le.senid+":"+le.x+":"+le.y;
						
						// Skip if already selected
						if(selectedLinks.containsKey(str)){
							continue; 
						}
						
						if(sensModified.containsKey(le.senid)){ 
							double dim = 
								(double) sensModified.get(le.senid)/ (double)ae.LINKS.size();
							// le.score = ae.LINKS.get(x).get(y) * Math.pow(Math.E, -1*dim);
							le.score = ae.LINKS.get(x).get(y) * 1 / dim;
							
						}else{
							le.score = ae.LINKS.get(x).get(y);
						}
						le.set(sw[x],tw[y]);
						pq.add(le);
						links++;
					}catch(Exception e){
						System.err.println(i+":"+ae.target);
					}
				}	
			}
		}
		System.err.println("Scored Links:"+links);
		System.err.println("From sentences:"+sensModified.size());
	}
	 	
	public void selectLinks(QuerySelector qVisitor) {
		
		double RESET_THRESHOLD = BATCH_SIZE / 10.0; 
		
		System.err.println("Resets after:"+RESET_THRESHOLD);
		//Print to files
		try{
		Hashtable<Integer,String> anchorAlignment = new Hashtable<Integer, String>();
		Hashtable<String,Integer> errors = new Hashtable<String, Integer>();
		
		int k =0;int agree=0;int disagree=0;int notexist = 0;
		int links=0;
		while(k<BATCH_SIZE){
			//if(k%RESET_THRESHOLD==0){
		    if(k==0){
				// Rescore to distribute evenly across all sentences 
				scoreLinks(qVisitor);
			}
			
			LinkEntry le = pq.poll();		
			String linkID = le.senid+":"+le.x+":"+le.y;
			selectedLinks.put(linkID, 1);
			
			//System.err.println(le.senid+":"+le.x+"-"+le.y+" score: "+le.score);
			String linkstr = "";
			if(human.data.get(le.senid).LINKS.containsKey(le.x)){
				if(human.data.get(le.senid).LINKS.get(le.x).containsKey(le.y)){
					// Humans says OK 
					agree++;
				}else{					
					disagree++;
					// Keep track of errors
					if(errors.containsKey(le.t))
						errors.put(le.t,errors.get(le.t)+1);
					else
						errors.put(le.t,1);
				}
				int hx=le.x;
				// add human links 
				for(int hy:human.data.get(le.senid).LINKS.get(le.x).keySet()){
					linkstr+=(hx+OFFSET)+"-"+(hy+OFFSET)+" ";
					// Note this 
					linkID = le.senid+":"+hx+":"+hy;
					selectedLinks.put(linkID, 1);
				}
			}else{  
				linkstr+=(0)+"-"+(le.y+OFFSET)+" ";
				notexist++;
			}
			// Create anchor alignment string 
			String str = "";
			if(anchorAlignment.containsKey(le.senid)){
				str=anchorAlignment.get(le.senid);
			} 
			str+=linkstr;
			anchorAlignment.put(le.senid, str);
			
			// Keep track of sentences modified 
			int count=1;
			if(sensModified.containsKey(le.senid)){
				count+=sensModified.get(le.senid);
			} 
			sensModified.put(le.senid, count);
			
			k++; 
		}

		BufferedWriter sWriter = new BufferedWriter(new FileWriter(sSD));
		BufferedWriter tWriter = new BufferedWriter(new FileWriter(tSD));
		BufferedWriter aWriter = new BufferedWriter(new FileWriter(aSD));

//		for(String x: errors.keySet()){
//			if(errors.get(x)>5) {
//			//	System.err.println(x+"\t"+errors.get(x));
//			}
//		}
//
		// Output all the data 
		//for(Integer id: automatic.data.keySet()){
		for(int id=0;id<automatic.data.size();id++){
			if(anchorAlignment.containsKey(id)){
				System.out.println(anchorAlignment.get(id));
				//aWriter.write(anchorAlignment.get(id)+"\n");
			}else{
				System.out.println();
				//aWriter.write("\n");
			} 
		}
		aWriter.close(); 
		
		System.err.println("\n---------------------------");
		System.err.println("Total Alignments Agreed:"+agree);
		System.err.println("Total Non existent Links:"+notexist);
		System.err.println("Total Links Disagreed:"+disagree);
		System.err.println("Total Sentences Modified:"+sensModified.size());
		System.err.println("---------------------------");
		System.err.println("Total Final Links:"+links);
		System.err.println("backoff sgt:"+TranslationLEXICON.sgt_backoff);
		System.err.println("backoff tgs:"+TranslationLEXICON.tgs_backoff);
		System.err.println("---------------------------\n");
		}catch(Exception e){System.err.println(e.toString());}
	}
}