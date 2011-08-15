package query;

import java.io.*;
import java.util.*;

import options.Options;
import query.link.*;
import data.*;

public class LinkSelection {

	// Path to Human alignment data 
	// Cn-En Parameters 
	// 2k Sentences Alignment
	public static String humanAlignmentFile = ""; // Alignment
	public static String coocfile = "/mnt/1tb/usr6/vamshi/ActiveLearning/Cn-En/model1/model1.lexicon";
	
	// 5K sentences Alignment
	// public static String humanAlignmentFile = "/mnt/1tb/usr6/vamshi/ActiveLearning/Cn-En/a.2k.human"; // Alignment
	 
	public static String sSD, tSD,aSD; // Selected or New Data 

	// Assumes that alignment starts from 1 
	int OFFSET  = 1;
	
	// Score links to be selectively modified 
	PriorityQueue<LinkEntry> pq = null; 
	
	// Similarity threshold for sentences selected in a particular batch 
	public static final int COOC_THRESHOLD = 10;
	public static int BATCH_SIZE = 1000; 

	public AlignmentData automatic = null;
	public AlignmentData human = null;
	
	// Key = Src+:+tgt , Value = Sentence ID   
	public Hashtable<String,Integer> humanCorpus = null;
	
	public static String queryType, mDir, sL , tL, aL;
	public static String gizaSGTLexFile, gizaTGSLexFile,gizaSGTFile,gizaTGSFile;
	public static String srcFreqFile, tgtFreqFile; 
	
	// To keep track of the count of times a link has been drawn froma  Sentence 
	Hashtable<Integer,Integer> sensModified = new Hashtable<Integer, Integer>();
	// Keep track of selected links 
	Hashtable<String,Integer> selectedLinks = new Hashtable<String, Integer>(); 
	Hashtable<String,Integer> errors = new Hashtable<String, Integer>();
	
	public static void main(String args[]) throws Exception{ 
  	   if(args.length!=3) 
       { 
           System.err.println("Usage: java AlignmentSelection <CONFIG> <queryType> <MODEL_DIR> <HUMAN_ALIGN_FILE> <N>");
           System.err.println("queryType = conf, div, den, seq,random,");
           System.exit(0); 
       }
		Options config = new Options(args[0]);
		// Usually TAG is the iteration number  	 
		queryType = config.get("QUERY_TYPE");
		BATCH_SIZE = config.getInt("BATCH_SIZE");
		
		queryType = args[1];
		BATCH_SIZE = Integer.parseInt(args[2]);
		  	 
		 sL = config.get("SOURCE"); 
		 tL = config.get("TARGET"); 
		 aL = config.get("AUTO_ALIGN"); 
		 humanAlignmentFile = config.get("HUMAN_ALIGN");
		 
		 sSD = "tmp.s.new";
		 tSD = "tmp.t.new";
		 aSD = "tmp.a.new";

		 // Chinese to English:SGT
		 gizaSGTLexFile = config.get("SGT_LEX");
		 gizaTGSLexFile = config.get("TGS_LEX");
		 
		 // Chinese to English:SGT
		 gizaSGTFile = config.get("SGT_ALIGN");
		 gizaTGSFile = config.get("SGT_ALIGN");
		 
		 srcFreqFile =  config.get("SRC_FREQ");
		 tgtFreqFile = config.get("TGT_FREQ");
			 
		LinkSelection ls = new LinkSelection();
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
		}else if(queryType.equals("margin")){
			qVisitor = new MarginLink(gizaSGTLexFile,gizaTGSLexFile);		 
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
		
		modifyAlignment(qVisitor);
	}
	
	/* Selection in Batch Mode Active Learning */
	public void scoreLinks(QuerySelector qVisitor) {
		
		// Score all the links inside each AlignmentEntry object
		automatic.computeCost(qVisitor);
		
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
					LinkEntry le = new LinkEntry(i,x,y);
					// Skip if already selected
					String str = le.senid+":"+le.x+":"+le.y;
					if(selectedLinks.containsKey(str)){
						continue; 
					}
					if(sensModified.containsKey(le.senid)){
						double dim = sensModified.get(le.senid)/ae.LINKS.size();
						le.score = ae.LINKS.get(x).get(y) * Math.pow(Math.E, dim);
					}else{
						le.score = ae.LINKS.get(x).get(y);
					}
					le.set(sw[x],tw[y]);
					le.score = ae.LINKS.get(x).get(y);
					pq.add(le);
					links++;
				}
			}
		}
		System.err.println("Total Initial Links:"+links);
	}
	
	/* Incorporating the alignment links from humans
	 * 1. Deleting links that humans disagree with
	 * 2. Add extra links that humans provide 
	 */
	public void modifyAlignment(QuerySelector qVisitor) {
		//Print to files
		try{
		String log = "selection-log";
		BufferedWriter logWriter = new BufferedWriter(new FileWriter(log));
		
		int k =0;int agree=0;int disagree=0;int notexist = 0;
		int links=0;
		while(k<BATCH_SIZE){
			
			if(k%1000==0){
			// if(k==0){
				// Rescore to distribute evenly across all sentences 
				scoreLinks(qVisitor);
			}
			
			LinkEntry le = pq.poll();
			String linkID = le.senid+":"+le.x+":"+le.y;
			selectedLinks.put(linkID, 1);
			
			sensModified.put(le.senid, 1);
			
			// Get the human link for this . Does it exist ? 
			if(human.data.get(le.senid).LINKS.containsKey(le.x)){
				if(human.data.get(le.senid).LINKS.get(le.x).containsKey(le.y)){
					// Humans says OK 
					agree++;
				}else{
					// DROP this Link now
					disagree++;
					AlignmentEntry ae = automatic.data.get(le.senid);
					// TODO: Drop existing link  
					ae.dropLink(le.x,le.y);
					// Add link from Human data Could be multiple links - handle that ??
					ae.addAll(le.x,human.data.get(le.senid).LINKS.get(le.x));
					
					// Keep track of errors
					if(errors.containsKey(le.t))
						errors.put(le.t,errors.get(le.t)+1);
					else
						errors.put(le.t,1);
				}
			}else{
				// Does not exist - perhaps ADD ?  
				notexist++;
				AlignmentEntry ae = automatic.data.get(le.senid);
				ae.dropAll(le.x);
			}
			logWriter.write(le.senid+"\n");
			k++; 
		}
		
		// Print Frequent Errors 
		for(String x: errors.keySet()){
			if(errors.get(x)>5){
				System.out.println(x+"\t"+errors.get(x));
			}
		}
		
		BufferedWriter sWriter = new BufferedWriter(new FileWriter(sSD));
		BufferedWriter tWriter = new BufferedWriter(new FileWriter(tSD));
		BufferedWriter aWriter = new BufferedWriter(new FileWriter(aSD));

		// Output all the data 
		//for(Integer id: automatic.data.keySet()){
		for(int id=0;id<automatic.data.size();id++){
			  AlignmentEntry ae = automatic.data.get(id);
			  sWriter.write(ae.source +"\n");
			  tWriter.write(ae.target +"\n");
			  aWriter.write(ae.toSMTString() +"\n");
			  links+=ae.linkcount;
		}
		System.err.println("\n---------------------------");
		System.err.println("Total Alignments agreed:"+agree);
		System.err.println("Total Link Dropped (No Human Alignment NULL):"+notexist);
		System.err.println("Total Links Dropped (Incorrect):"+disagree);
		System.err.println("Total Sentences Modified:"+sensModified.size());
		System.err.println("---------------------------");
		System.err.println("Total Final Links:"+links);
		System.err.println("---------------------------\n");
		
		  sWriter.close();
		  tWriter.close();
		  aWriter.close();
		  logWriter.close();
		}catch(Exception e){System.err.println(e.toString());}
	}
}