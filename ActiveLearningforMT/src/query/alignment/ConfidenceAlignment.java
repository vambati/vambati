package query.alignment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import model.smt.TranslationLEXICON;
import data.*;
import query.*; 

public  class ConfidenceAlignment implements QuerySelector {

	Hashtable<Integer,Double> gizaSGT = new Hashtable<Integer,Double>();
	Hashtable<Integer,Double> gizaTGS = new Hashtable<Integer,Double>();
	
	// Score Pattern for GIZA files
	Pattern scorepattern  = Pattern.compile("score : ([0-9\\.e\\-]+)");
	
	// Lexicons Translation 
	TranslationLEXICON LEX = null; 
	
	// Load A3 files and LEXICON files (bidirectional) 
	public ConfidenceAlignment(String sgtFile,String tgsFile,String sgtLexFile,String tgsLexFile){
	
		LEX = new TranslationLEXICON(sgtLexFile,tgsLexFile);
		
		int sennum = 0;
		double score = 0.0;
		// Reading a GZIP file (SGT)  
		try { 
		BufferedReader br = new BufferedReader(new InputStreamReader
								(new GZIPInputStream(new FileInputStream(sgtFile)),"UTF8"));
		
		String one = ""; String two =""; String three=""; 
		while((one=br.readLine())!=null){
			two = br.readLine(); 
			three=br.readLine(); 

			Matcher m1 = scorepattern.matcher(one);
			if (m1.find()) 
			{
				score = Double.parseDouble(m1.group(1));
				//System.err.println("Score:"+score);
			}

			gizaSGT.put(sennum,score); 
			sennum++;
		}
		br.close();
		System.err.println("Loaded "+sennum+ " sens from SGT file:"+sgtFile);
		
		// Reading a GZIP file  (TGS)
		sennum = 0; 
		br = new BufferedReader(new InputStreamReader
								(new GZIPInputStream(new FileInputStream(tgsFile)),"UTF8")); 
		while((one=br.readLine())!=null){
			two = br.readLine(); 
			three=br.readLine(); 
			
			Matcher m1 = scorepattern.matcher(one);
			if (m1.find()) 
			{
				score = Double.parseDouble(m1.group(1));
				//System.err.println("Score:"+score);
			}
			gizaTGS.put(sennum,score); 
			sennum++;
		}
		br.close();		
		}catch(Exception e){}
		System.err.println("Loaded "+sennum+ " sens from SGT file:"+sgtFile);	
	}
	
	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e; 
		int sennum = e.senid; 
		
		double sgt = gizaSGT.get(sennum) / (double)ae.tLength; // Normalize 
		double tgs = gizaTGS.get(sennum) / (double)ae.sLength; // Normalize 
		 
		// Confidence formulation similar to Fei Huang 2008 ACL paper (IBM) 
		double marginalize_S = LEX.getPhraseProbability_SGT(e.source, e.target, false);
		double marginalize_T = LEX.getPhraseProbability_TGS(e.source, e.target, false); 
			
		double s2t = sgt / marginalize_S; 
		double t2s = tgs / marginalize_T;
		
		// ae.score = -1 * Math.sqrt(sgt * tgs); // Utility here NEG-SCORE or COST
		// ae.score = -1* Math.sqrt(s2t * t2s); // SCORE 
		 
		ae.score = -2*(sgt * tgs)/(sgt+tgs); // Utility here NEG-SCORE or COST
		
		//System.err.println("s:"+sgt+",t:"+tgs+" Conf="+ae.score);

		return ae.score;
	}
}