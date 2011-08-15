package query;

import java.io.*;
import java.util.*;

import query.alignment.ConfidenceAlignment;
import query.link.ConfidenceLink;
import data.*;

public class MultiAlignmentSelection {

	// Path to Human alignment data 
	// Cn-En Parameters 
	// 2k Sentences Alignment
	public static String humanAlignmentFile = "/mnt/1tb/usr6/vamshi/ActiveLearning/Cn-En/a.2k.human"; // Alignment
	// 5K sentences Alignment
	// public static String humanAlignmentFile = "/mnt/1tb/usr6/vamshi/ActiveLearning/Cn-En/a.2k.human"; // Alignment
	 
	public static String sSD, tSD,aSD; // Selected or New Data
	
	int offset= 0;

	// Similarity threshold for sentences selected in a particular batch 
	public static final int COOC_THRESHOLD = 10;
	public static int BATCH_SIZE = 1000; 

	public AlignmentData automatic = null;
	public AlignmentData human = null;
	
	// Key = Src+:+tgt , Value = Sentence ID   
	public Hashtable<String,Integer> humanCorpus = null;
	
	public static String queryType, mDir, sL , tL, aL; // Corpus 
	public static String gizaSGTLexFile, gizaTGSLexFile; // Lexicon files 
	public static String gizaSGTFile , gizaTGSFile; // A3 files 
	
	public static void main(String args[]) throws Exception{ 
  	   if(args.length!=4) 
       { 
           System.err.println("Usage: java AlignmentSelection <queryType> <MODEL_DIR> <HUMAN_ALIGN_FILE> <N>");
           System.err.println("queryType = conf, div, den, seq,random,");
           System.exit(0); 
       }       
		 queryType = args[0];
		 mDir = args[1];
		 humanAlignmentFile = args[2];
		 BATCH_SIZE = Integer.parseInt(args[3]);
				
		 sL = mDir+"/model/aligned.0.fr";
		 tL = mDir+"/model/aligned.0.en";
		 aL = mDir+"/model/aligned.grow-diag-final-and";
		 
// TEST
//		 sL = "1c";  
//		 tL = "1e";
//		 aL = "1a";
//		 humanAlignmentFile ="1h";
		 
		 sSD = sL+".new";
		 tSD = tL+".new";
		 aSD = aL+".new";
		
		 
		 gizaSGTFile = mDir+"/giza.en-fr/en-fr.A3.final.gz";
		 gizaTGSFile = mDir+"/giza.fr-en/fr-en.A3.final.gz";
		 
		 gizaSGTLexFile = mDir+"/model/lex.0-0.n2f";
		 gizaTGSLexFile = mDir+"/model/lex.0-0.f2n";
		 
		MultiAlignmentSelection ls = new MultiAlignmentSelection();
		ls.selectSentences(queryType,sL, tL,aL, BATCH_SIZE);
	}
	
	public void selectSentences(String queryType,String sL,String tL,String aL ,int n){
		// Load corpus 
		automatic = new AlignmentData(sL,tL,aL,offset);
		human = new AlignmentData(sL,tL,humanAlignmentFile,offset);
			
		// Compute Alignment scores first for each of the sentences
		QuerySelector qVisitor_FULL = null;
		qVisitor_FULL = new ConfidenceAlignment(gizaSGTFile,gizaTGSFile,gizaSGTLexFile,gizaTGSLexFile);
		automatic.computeCost(qVisitor_FULL);
		
		// Compute link level scores 
		QuerySelector qVisitor_LINK = null;
		qVisitor_LINK = new ConfidenceLink(gizaSGTLexFile,gizaTGSLexFile);		 
		automatic.computeCost(qVisitor_LINK);
		
		// Sort them by Alignment first 
		EntryCompare EC = new EntryCompare();
		PriorityQueue<AlignmentEntry> pqAlignment = null;
		pqAlignment = new PriorityQueue<AlignmentEntry>(1000,EC.new AlignmentEntryCompare());
		// Score links to be selectively modified
		PriorityQueue<LinkEntry> pqLink = null; 
		pqLink = new PriorityQueue<LinkEntry>(1000,EC.new LinkEntryCompare());
		
		int links=0;
		for(Integer i: automatic.data.keySet()){
			// Expand alignment and get each link out of it
			AlignmentEntry ae = automatic.data.get(i); 
			for(int x: ae.LINKS.keySet()){
				for(int y: ae.LINKS.get(x).keySet()){
					LinkEntry le = new LinkEntry(i,x,y);
					le.score = ae.LINKS.get(x).get(y);
					
					// Weight it with the Alignment scores so that Links are not coming from really bad Alignments
					// le.score = (le.score + ae.score)/2;
					
					pqLink.add(le);
					links++;
				}
			}
		}
		System.err.println("Total Links:"+links);
	
	/* Incorporating the alignment links from humans
	 * 1. Deleting links that humans disagree with
	 * 2. Add extra links that humans provide 
	 */ 
		//Print to files
		try{
		String log = "selection-log";
		BufferedWriter logWriter = new BufferedWriter(new FileWriter(log));
		
		Hashtable<Integer,Integer> sensModified = new Hashtable<Integer, Integer>();
		int k =0;int agree=0;int disagree=0;int notexist = 0;
		while(k<BATCH_SIZE && k<pqLink.size()){
			LinkEntry le = pqLink.poll();
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
		System.err.println("\n---------------------------");
		System.err.println("Total Alignments agreed:"+agree);
		System.err.println("Total Link Dropped (No Human Alignment NULL):"+notexist);
		System.err.println("Total Links Dropped (Incorrect):"+disagree);
		System.err.println("Total Sentences Modified:"+sensModified.size());
		System.err.println("---------------------------\n");
		
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
		}
		  sWriter.close();
		  tWriter.close();
		  aWriter.close();
		  logWriter.close();
		}catch(Exception e){System.err.println(e.toString());}
	}
}