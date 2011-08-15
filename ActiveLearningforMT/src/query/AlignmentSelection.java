package query;

import java.io.*;
import java.util.*;
import query.alignment.*;
import utils.StringUtils;
import data.*;

public class AlignmentSelection {

	// Path to Human alignment data 
	// Cn-En Parameters 
	public static String aH = ""; // Alignment
	 
	public static String aSD; // Selected or New Data 
	
	// Similarity threshold for sentences selected in a particular batch 
	public static final int COOC_THRESHOLD = 10;
	public static int BATCH_SIZE = 0; 

	// Offset, starts with 0 or 1 
	int offset = 0;
	 
	public AlignmentData automatic = null; 
	public AlignmentData human = null;
	
	public static String queryType, mDir, sL , tL, aL;
	public static String gizaSGTLexFile, gizaTGSLexFile, gizaSGTFile,gizaTGSFile;
	
	public static void main(String args[]) throws Exception{ 
  	   if(args.length!=4) 
       {
           System.err.println("Usage: java AlignmentSelection <queryType> <MODEL_DIR> <HUMAN_ALIGNFILE> <N>");
           System.err.println("queryType = conf, div, den, seq,random,");
           System.exit(0); 
       }       
		 queryType = args[0];
		 mDir = args[1];
		 aH = args[2];
		 BATCH_SIZE = Integer.parseInt(args[3]);
		 
				
		 sL = mDir+"/model/aligned.0.fr";
		 tL = mDir+"/model/aligned.0.en";
		 aL = mDir+"/model/aligned.grow-diag-final-and";
		 
		 aSD = aL+".new";
		 
		 gizaSGTFile = mDir+"/giza.en-fr/en-fr.A3.final.gz";
		 gizaTGSFile = mDir+"/giza.fr-en/fr-en.A3.final.gz";
		 
		 gizaSGTLexFile = mDir+"/model/lex.0-0.n2f";
		 gizaTGSLexFile = mDir+"/model/lex.0-0.f2n";
		 
		AlignmentSelection ss = new AlignmentSelection();
		ss.selectSentences(queryType,sL, tL,aL);
	}
	
	public void selectSentences(String queryType,String sL,String tL,String aL){
		// Load corpus 
		automatic = new AlignmentData(sL,tL,aL,offset);
		human = new AlignmentData(sL,tL,aH,offset);
			
		QuerySelector qVisitor = null;
				
		// Confidence formulation similar to Fei Huang (ACL 2008 paper) 
		if(queryType.equals("conf")){
			qVisitor = new ConfidenceAlignment(gizaSGTFile,gizaTGSFile,gizaSGTLexFile,gizaTGSLexFile);		 
		}
		// Confidence formulation using Lexicon instead of GIZA scores 
		else if(queryType.equals("lex")){
			qVisitor = new LexicalWeight(gizaSGTLexFile,gizaTGSLexFile);
		}
		// Sum of differences between SECOND best and FIRST best 
		else if(queryType.equals("margin")){
			qVisitor = new Margin(gizaSGTLexFile,gizaTGSLexFile);
		}
		replaceSelectedAlignment(qVisitor);
	}
	
	/* Selection in Batch Mode Active Learning */
	public void replaceSelectedAlignment(QuerySelector qVisitor) {
		
		automatic.computeCost(qVisitor);
		// Pick top n entries as the selected batch (SSD)
		HashSet<Integer> selected = new HashSet<Integer>(BATCH_SIZE); 
		selected = automatic.getTopK(BATCH_SIZE);
		
		//Print to files
		try{
		BufferedWriter aWriter = new BufferedWriter(new FileWriter(aSD));

		String log = "selection-log";
		BufferedWriter logWriter = new BufferedWriter(new FileWriter(log));

		int total =0;int k=0; 
		int hlinks=0; int links=0;
		//for(Integer id: automatic.data.keySet()){
		for(int id=0;id<automatic.data.size();id++) { 
		  AlignmentEntry ae = automatic.data.get(id);
		  AlignmentEntry aeHuman = human.data.get(id);
		  
		  String anew = ae.alignStr;
		  String a = anew;
		  
		  // Substitute it with human data if quality is low 
		  if(selected.contains(id) && (k<BATCH_SIZE)){
			  
			  anew = aeHuman.alignStr;
			  hlinks+= aeHuman.linkcount;
			  links+= ae.linkcount;
			  
			  logWriter.write(ae.senid+"\n");
			  System.err.println("Substituting:"+ae.score+"\n"+a+"\n"+anew+"\n");
			  k++;
		  }
		  aWriter.write(anew +"\n");
		  total++;
		}
		aWriter.close();
		logWriter.close();
		  
		  System.err.println("Total Sentences:"+total);
		  System.err.println("Substituted Alignments:"+k);
		  System.err.println("Automatic Link Count:"+links);
		  System.err.println("Human Link Count:"+hlinks);
		}catch(Exception e){System.err.println(e.toString());}
	}
			
	public void loadHumanData(String sH, String tH,String aH){
		int i=0;
		System.err.println("Loading Human Alignment data");
		try { 
			BufferedReader sbr = new BufferedReader(new InputStreamReader(new FileInputStream(sH)));
			BufferedReader tbr = new BufferedReader(new InputStreamReader(new FileInputStream(tH)));
			BufferedReader abr = new BufferedReader(new InputStreamReader(new FileInputStream(aH)));
			
			String s= ""; String t =""; String a=""; 
			while((s=sbr.readLine())!=null){
				t = tbr.readLine();
				a = abr.readLine();
				
				// Normalize to be similar with Moses formats 
				s = StringUtils.trim(s).toLowerCase();
				t = StringUtils.trim(t).toLowerCase();
				
				// String key = s+":"+t; 
				// human.put(key, a);
				i++;
			}
		}catch(Exception e){}
		System.err.println("Loading Human Alignment data");
	}
}