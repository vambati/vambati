package ml.utils;
import java.io.BufferedReader;

import java.io.FileReader;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import features.GLSFeatureFunction;
import features.LexicalWeightFeatureFunction;
import features.MIFeatureFunction;
import qa.*; 

public class FeatureManager {

	// Threshold for feature selection 
	public static int feat_limit = 200; 
	
	// Also load frequency of features to do FeatureSelection on the fly 
	public String featFile = "";
	public static Hashtable<String,Integer> featFreq = new Hashtable<String, Integer>();

	public static Hashtable<String,Double> feats = new Hashtable<String, Double>();
	public static Hashtable<String,Double> featCounts = new Hashtable<String, Double>();
	
	// Index mapper for feature strings
	public static Hashtable<String,Integer> featIndex = new Hashtable<String, Integer>();
	
	// Initiatlizations 
	public static double word_weight_init = 1;
	public static double ngram_weight_init = 1;
	public static double exp_weight_init = 0;
	public static double binary_weight_init = 0;
	public static double mt_weight_init = 0;
	
	// Other stuff
	public static double binary_value_feature = 0.3;
	public static double ngram_step = 0.5;
	
	// Initialize from a file of feature names 
	public FeatureManager(String featfile) {
		BufferedReader fbr;
		try {
			fbr = new BufferedReader(new FileReader(featfile));
			String str = "";
			while((str = fbr.readLine()) != null){
				String [] arr = str.split("\\s+");
				
				int freq = Integer.parseInt(arr[0]);
				String name = arr[1];
				if(freq>= feat_limit)
					featFreq.put(name,freq);	
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void loadGLSFeatures(String one,String two) throws Exception{	 
		GLSFeatureFunction.inputFile = one;
		GLSFeatureFunction.bowFile = two;
		GLSFeatureFunction.load();
	}
	
	public static void loadSMTFeatures(String one,String two){	 
		LexicalWeightFeatureFunction.sgtlexfile = one;
		LexicalWeightFeatureFunction.tgslexfile = two;
		LexicalWeightFeatureFunction.loadLex();
	}
	public static void loadMIFeatures(String one){	 
		MIFeatureFunction.miFile = one;
		MIFeatureFunction.load();
	}
		
	public static void appendNewFeature(String name, Double val){
		if(feats.containsKey(name)){
			System.err.println("Feature already exists"+name);
		}else{
			feats.put(name, val);
		}
	}
	
	public static void appendToModel(SparseVector vec){
		for(String fn: vec.getFeatures()) {
			if(featFreq.containsKey(fn)) {
				if(feats.containsKey(fn)){
					// Average it across all entries seen so far
					double value = feats.get(fn) + vec.get(fn);
					feats.put(fn,value);
					featCounts.put(fn,featCounts.get(fn)+1.0);
				}else{
					feats.put(fn,vec.get(fn));
					featCounts.put(fn,1.0);
				}
			}
		}
	}
	
	// Initialization function that returns average weights for starting point 
	// Does initialization matter in perceptron 
	public static SparseVector initializeAverageWeights(){
		SparseVector model = new SparseVector();
		for(String fn: feats.keySet()) {
			// Average it 
			model.set(fn, feats.get(fn)/featCounts.get(fn));
		}
		return model;
	}
	
	public static SparseVector initializeWeights(){
		SparseVector dummyweights = new SparseVector();
		for(String fn: feats.keySet()) {
			if(fn.contains("wbt2")){
				dummyweights.set(fn,exp_weight_init);
			}
			else if(fn.contains("webtalk-")){
				dummyweights.set(fn,ngram_weight_init);
			}else if(fn.contains(".b")){
				dummyweights.set(fn,binary_weight_init);	
			}else if(fn.contains(".p")){
				// TODO: Position features	
				dummyweights.set(fn,binary_weight_init);
			}else if(fn.contains(".MT")){
				// Machine Translation features 	
				dummyweights.set(fn,mt_weight_init);
			}else { 
				dummyweights.set(fn,word_weight_init);	
			}
		}
		return dummyweights;
	}
	
	// Load features from the VEC files
	// 0:ang-md.org/a8.txt FEAT:value FEAT:value FEAT:value
	
	// VEC file contains all features, load all of them, and only train on a few of them
	// Remaining features are used for scoring 
	public static SparseVector loadFeatures(String ans) {
		SparseVector fVector = new SparseVector();
		String[] arr = ans.split(" +");

		// Skip the answer path feature and read remaining 
		for(int i=1;i<arr.length; i++){
			String[] f = arr[i].split(":");
			String fname = f[0];
			double value = Double.parseDouble(f[1]);
			
			if((fname.contains(".p")) || (fname.contains("webtalk-"))){
				continue;
			}else{
				fVector.set(fname,value);
			}
		}
	
		// Now modify the above weights with position based features 
		for(int i=1;i<arr.length; i++){
			String[] f = arr[i].split(":");
			String fn = f[0];
			double value = Double.parseDouble(f[1]);
			
			// BUG fixed. String manipulation always returns a reference 
			String fname = "";
			fname = fn.replaceAll(".p$", "");
			fname = fname.replaceAll("^webtalk-", "");
			// System.err.println(fname+":"+fn);
			
			if(fVector.exists(fname)){
				double fvalue = fVector.get(fname);
				// Modify existing features based on these Bin and Pos features
				if(fn.contains(".p")){
					fvalue = fvalue * 1 / (1+ binary_value_feature * (value-1));
					fVector.set(fname,fvalue);
				}else if(fn.contains("webtalk-")){	
					fvalue  = fvalue * (1+ ngram_step * Math.log(value));
					fVector.set(fname,fvalue);
				}
			}
		}
		return fVector;
	}
}
