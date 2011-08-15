/**
 * 
 */
package drivers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import options.Options;

import taruDecoder.*; 
import taruDecoder.hyperGraph.*;
import taruHypothesis.Hypothesis;
import treeParser.*;
import utils.evaluation.BleuScore;
import utils.lm.VocabularyBackOffLM;

/**
 * This class implements a top down tree to tree decoder
 * 
 * @author abhayaa
 * 
 */
public class TaruDriver {
		
	static VocabularyBackOffLM lm;
	static ArrayList<String> refs;
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length==1)
		{
			String cfgfile = args[0];
			System.out.println("Config path is: "+cfgfile);
			Options opts = new Options(cfgfile); 

			String inputFile = opts.get("SOURCE");
			String refFile = opts.get("REFS");
						
			System.out.println("Source...."+inputFile);
			System.out.println("References...."+refFile);
						
		 	long sTime = System.currentTimeMillis();
		 	
		 	// Load All the References first (Will prune later to contain only those that are decoded)  
			refs = new ArrayList<String>(500);
			BufferedReader refFileReader;
			try {
				refFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(refFile)));
			
				String lineFile;
				while ((lineFile = refFileReader.readLine()) != null)
				{
						refs.add(lineFile);
						//refs.add(lineFile.toLowerCase());
				}
				refFileReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Now load Input from source file and decode one by one 
			try {
 				BufferedReader br = new BufferedReader(new FileReader(inputFile));
				 
				int sCount = 0;
				int total = -1;
			 	 
			 	ArrayList<String> hyps = new ArrayList<String>(500);
			 	ArrayList<String> shortRefs = new ArrayList<String>(500);
			 	HashMap<String,Integer> vocabulary = new HashMap<String, Integer>();
			 	ArrayList<String> inputParses = new ArrayList<String>(500);
			 	
				String input = "";
				while ((input = br.readLine()) != null) {
					total++;
					//if(TaruDriver.refs.get(total).split(" +").length > 25){
					//	continue;
					//}
					shortRefs.add(refs.get(total));
					inputParses.add(input);
					ParseTreeNode ptn = ParseTree.buildTree(input);
					// Add to known vocabulary 
					String str = ParseTree.getString(ptn);
					for(String w: str.split(" +")){
						vocabulary.put(w, 1);
						//vocabulary.put(w.toLowerCase(), 1);
					}
				}
				
				Taru decoder = new Taru(cfgfile,vocabulary);
				
				for(String parseTree: inputParses) {
					System.err.println("---------------------------");
					System.out.println("Decoding " + sCount);
					
					//List<Hypothesis> topHyps = decoder.decodeParseTree(parseTree,100);
					List<Hypothesis> topHyps = decoder.decodeParseTree(parseTree,1);
				
					int i = 0; int N=10;
					if(topHyps.size()<10){ 
						N = topHyps.size(); 
					} 
					//for(i = 0; i < N; i++){
					for(i = 1; i <= N; i++){
					//	System.out.println(HGUtils.extractParse(binHG, 0, i));
					//	System.out.println(HGUtils.extractOriginalParse(binHG, 0, i));
						System.err.println(topHyps.get(i).getWords());
						System.err.println(topHyps.get(i).getFeatures());
					}
					System.out.println("Done! Hypothesis count = " +i +"\n" );
					
					if(hyps.size() > 0){
						hyps.add(topHyps.get(1).getWords());
						System.err.println(topHyps.get(1).getFeatures());
						//System.err.println(HGUtils.extractOriginalParse(binHG, 0, 1));
						System.out.println(hyps.get(sCount));
						sCount++;
					}
					else{
						// To compute bleu
						hyps.add("");
					}
					System.err.println("---------------------------");
				}
			 	long eTime = System.currentTimeMillis();
				System.out.println("Successfully decoded " + sCount + " sentences out of " + (total+1));
				System.out.println("Time Taken: " + ((eTime - sTime) / 1000));
				System.out.println("Computing BLEU.");
				printBlue(hyps, shortRefs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		long eTime = System.currentTimeMillis();
		double secs = (eTime - sTime) / 1000;
		System.out.print("Time Taken: "+secs);
	}
	else
	{
		System.out.println("Usage: java TaruDriver <config file>");
		System.exit(0);
	}
	
	}
	
	static double printBlue(ArrayList<String> hyps, ArrayList<String> refs) {
		double bleu = 0.0;
		try{		
			if(refs.size() != hyps.size()){
				System.out.println(refs.size() + " " + hyps.size());
				throw new Error("Mismatch in hyps and ref number !");
			}
			
			BleuScore bs = new BleuScore(hyps,refs);
			bleu = bs.printStats();
		}catch(Exception e){e.printStackTrace();}
		return bleu;
	}
}
