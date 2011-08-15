/**
 * 
 */
package taruDecoder.features;

import java.util.HashMap;

import taruHypothesis.Hypothesis;
import utils.IOTools;
import utils.lm.LMTools;
import utils.lm.VocabularyBackOffLM;
import java.io.*;

/**
 * @author abhayaa
 *
 */
public class LMFeatureFunction {

	// These need to be set as global variables too
	public static String lmFile = "";
	public static String arpaLmFile = ""; 
	
	private static VocabularyBackOffLM lm;
	private static HashMap<String, Double> lmCache; 

//	private static final double oovLogProbability = -15;
//	private static final double minLogProbability = -15;

	private static HashMap<String, Double> features;
	
	private static int cacheMiss;
	private static int totalCalls;
	
//	static {
//		loadLM();
//	}
	
	public static void loadLM() {
		features = new HashMap<String, Double>(1);
		lm = new VocabularyBackOffLM(false);
		try{
			System.err.println("Loading from "+arpaLmFile);
			lm.loadArpa((new InputStreamReader(IOTools.getInputStream(arpaLmFile))));
			//lm.saveBinary(IOTools.getOutputStream(lmFile), 0);
			 //lm.loadBinary(IOTools.getInputStream(lmFile), 0);
			
		}catch(Exception e){e.printStackTrace();}
		System.err.println("LM Loaded.");
		lmCache = new HashMap<String, Double>();		
	}

	/* (non-Javadoc)
	 * @see taruDecoder.features.FeatureFunction#computeFeature(taruDecoder.Hypothesis, taruDecoder.Hypothesis, int)
	 */
	public static HashMap<String, Double> computeFeature(Hypothesis h, Hypothesis h1, Hypothesis h2, String edgeId) {
		//System.err.println("Called LMFeature function - 2");
		totalCalls++;
		features.clear();
		
		String lStr = h1.getWords();
		String rStr = h2.getWords();
		
		String [] lContext = lStr.split(" +");
		String [] rContext = rStr.split(" +");
		
		String extraNGrams = "";
		String oldBiGram = "";

		int contextSize = 0;
		
		if(lContext.length > 2){
			extraNGrams += lContext[lContext.length-2] + " " + lContext[lContext.length-1];
			contextSize = 2;
		}
		else{
			extraNGrams = lStr;
			contextSize = 1;	
		}
		
		if(rContext.length > 2){
			oldBiGram = rContext[0] + " " + rContext[1];
		}
		else{
			oldBiGram = rStr;
		}
		extraNGrams += " " + oldBiGram;

		
		if(!lmCache.containsKey(extraNGrams+contextSize)){
			cacheMiss++;
			
			if(lmCache.size() > 1000000)
				lmCache.clear();
			
			double score = LMTools.getSentenceLogProbability(extraNGrams, lm, contextSize);
			
//			System.out.println(extraNGrams + " " + score);

			lmCache.put(extraNGrams+contextSize, new Double(score));			
		}

		if(!lmCache.containsKey(oldBiGram+"0")){
			double score = LMTools.getSentenceLogProbability(oldBiGram, lm, 0);
			lmCache.put(oldBiGram+"0", new Double(score));
//			System.out.println(oldBiGram + " " + score);
		}
		
		if(lmCache.size() % 100000 == 0)
			System.err.println("Cache Size: " + lmCache.size());
		
		features.put("LM3GRAM", lmCache.get(extraNGrams+contextSize).doubleValue() - lmCache.get(oldBiGram+"0").doubleValue());
		
		
//		System.out.println(features);
//		features.put("LM3GM", 0.0);
		return features;
	}
	
	public HashMap<String,Double> computeFeature(Hypothesis h, Hypothesis h1, String edgeid) {
		//System.err.println("Called LMFeature function - 1");
		features.clear();
		String words = h1.getWords();
		
		if(!lmCache.containsKey(words+"0")){
			if(lmCache.size() > 1000000)
				lmCache.clear();
			double score = LMTools.getSentenceLogProbability(words, lm, 0);
			lmCache.put(words+"0", new Double(score));
		}
		else{
//			System.out.println("Found in cache");
		}
		features.put("LM3GRAM", lmCache.get(words+"0").doubleValue());
		return features; 
	}


	public static double computeFeature(Hypothesis h) {
		// System.err.println("Called LMFeature function - 1");
		String words = h.getWords();
		if(!lmCache.containsKey(words+"0")){
			if(lmCache.size() > 1000000)
				lmCache.clear();
			double score = LMTools.getSentenceLogProbability(words, lm, 0);
//			System.out.println(words + " " + score);
			lmCache.put(words+"0", new Double(score));
//			System.out.println("Cache Size: " + lmCache.size());
		}
		else{
//			System.out.println("Found in cache");
		}
		
		return lmCache.get(words+"0").doubleValue();
	}

	
}
