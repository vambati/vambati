/**
 * 
 */
package taruDecoder;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import options.Options;

import taruDecoder.features.LMFeatureFunction;
import taruDecoder.features.LengthPenaltyFeatureFunction;
import taruDecoder.features.LexicalWeightFeatureFunction;
import taruDecoder.hyperGraph.HyperGraph;
import taruHypothesis.Hypothesis;
import tarugrammar.Grammar;
import tarugrammar.GrammarRule;
import tarugrammar.LexicalRule;
import tarugrammar.PhraseRule;

import ml.utils.SparseVector;

/**
 * @author abhayaa
 * @author vamshi
 * 
 * A Singlton class holding all the feature information
 */
public class Scorer {
	
	HyperGraph hg;
	
	// This needs to be set by the System as a GLOBAL variable 
	public static String modelFile = "";
	
	private HashMap<String, Integer> featureNameMap;
	private HashSet<Method> featureClassSet;
	private SparseVector model;

	private HashMap<Integer, Double> edgeLowerBounds;
	
	private int featureCount;
	
	private int TGSId;
	private int SGTId;
	private int LMId; 
	private int TGSLEXId;
	private int SGTLEXId;
	private int WCId; 
	private int LPId;
	private int RCId;
	
	private static Scorer scorer;
	
	private Scorer(){
		featureNameMap = new HashMap<String, Integer>();
		featureClassSet = new HashSet<Method>();
		model = new SparseVector();
		loadModel();
		printModel();
		featureCount = -1;
	}
	
	private void printModel(){
		System.err.println("Current Model loaded is - ");
		for(String str: featureNameMap.keySet()){
			int i = featureNameMap.get(str);
			System.err.println(str+":"+i+" = "+model.get(i));
		}
	}
	
	private void loadModel(){
		try{
			BufferedReader br = new BufferedReader(new FileReader(Scorer.modelFile));
			String line = "";
			Class [] paramTypes = {Hypothesis.class, Hypothesis.class, Hypothesis.class, String.class};
			while((line = br.readLine()) != null){
				featureCount++;
				String [] tokens = line.split("\\s+");
				featureNameMap.put(tokens[0], new Integer(featureCount));
				
				if(tokens[0].equalsIgnoreCase("TGS"))
					this.TGSId = featureCount;
				else if(tokens[0].equalsIgnoreCase("LM3GRAM"))
					this.LMId = featureCount;
				else if(tokens[0].equalsIgnoreCase("SGT"))
					this.SGTId = featureCount;
				else if(tokens[0].equalsIgnoreCase("SGTLEX"))
					this.SGTLEXId = featureCount;
				else if(tokens[0].equalsIgnoreCase("TGSLEX"))
					this.TGSLEXId = featureCount;
				else if(tokens[0].equalsIgnoreCase("WC"))
					this.WCId = featureCount;
				else if(tokens[0].equalsIgnoreCase("RC"))
					this.RCId = featureCount;
				else if(tokens[0].equalsIgnoreCase("LP"))
					this.LPId = featureCount;
				
				model.set(featureCount, Double.parseDouble(tokens[1]));
				
				if(tokens.length > 2){
					try {
						featureClassSet.add(Class.forName(tokens[2]).getMethod("computeFeature", paramTypes));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
				else{
					// No function to compute this feature which means
					// that it is a local feature that completely factorizes
					// along the antecedents. Example - word count
				}
			}
		} catch (FileNotFoundException e){ e.printStackTrace();} 
		  catch (IOException e) { e.printStackTrace();}
	}

	public HashMap<String, Integer> getFeatureNameMap(){
		return featureNameMap;
	}
	
	public void writeModel(){
		Set<Integer> keys = model.getFeatureIds();
		for(Integer key : keys){
			System.out.println(featureNameMap.get(key) + " " + model.get(key));
		}
	}
	
	public static Scorer getScorer(){
		
		if(scorer == null){
			scorer = new Scorer();
			System.err.println("Model still is "+scorer.modelFile);
		}
		return scorer;
	}
	
	// Which hyper graph are we working on currently. May be required for computing some
	// features
	public void setHyperGraph(HyperGraph hg){
		this.hg = hg;
		edgeLowerBounds = new HashMap<Integer, Double>(hg.getEdgeSize());
	}
	
	// Update the model (feature weights)
	// Should put in some checks to make sure this doesn't 
	// happen in the middle of extraction
	public void setModel(SparseVector model){
		this.model = model;
	}
	
	// get current model
	public SparseVector getModel(){
		return this.model;
	}
	

	// For lexical rules
	public void initializeHypothesisFeatures(Hypothesis hyp, PhraseRule prule){

		//System.err.println("Setting phrasal for: "+hyp.getSrcWords()+"="+hyp.getWords());
		SparseVector s = new SparseVector();

		s.set(featureNameMap.get("WC") , prule.getTargetWord().split("\\s+").length -1);
		s.set(featureNameMap.get("RC") , 1);
		s.set(featureNameMap.get("LP") , LengthPenaltyFeatureFunction.computeFeature(hyp));
		
		double [] scores = Grammar.getRuleScores(prule.getId());

		
		// Set the TGS feature
		s.set(TGSId, scores[0]);
		// Set the SGT feature
		s.set(SGTId, scores[1]);
		// Set the TGS feature
		s.set(TGSLEXId, LexicalWeightFeatureFunction.getPhraseProbability_TGS(prule.getSourceWord(), prule.getTargetWord(),true));
		// Set the SGT feature
		s.set(SGTLEXId, LexicalWeightFeatureFunction.getPhraseProbability_SGT(prule.getSourceWord(), prule.getTargetWord(),true));

		// Set the languageModel feature
		s.set(LMId, LMFeatureFunction.computeFeature(hyp));
		
		// Set up all the features in the hyp
		hyp.addFeatures(s);
		
		// dot product with the model
		hyp.setScore(hyp.getFeatures().dotProduct(model));
		//System.err.println("Phrase rule:"+ hyp.getFeatures());
	}
	
	// For phrasal rules
	public void initializeHypothesisFeatures(Hypothesis hyp, GrammarRule grule){
		 
		SparseVector s = new SparseVector();

		double [] scores = Grammar.getRuleScores(grule.getId());
		// Set the TGS feature
		s.set(TGSId, scores[0]);
		// Set the SGT feature
		s.set(SGTId, scores[1]);
		// Set the TGS feature
		s.set(TGSLEXId, LexicalWeightFeatureFunction.BACKOFF);
		// Set the SGT feature
		s.set(SGTLEXId, LexicalWeightFeatureFunction.BACKOFF);
		
		s.set(LPId, LengthPenaltyFeatureFunction.computeFeature(hyp));
		s.set(featureNameMap.get("WC") , grule.getPhraseLength());
		s.set(featureNameMap.get("RC") , 1);
		
		// Set the languageModel feature
		s.set(LMId, LMFeatureFunction.computeFeature(hyp));
		
		// Set up all the features in the hyp
		hyp.addFeatures(s);
		
		// dot product with the model
		hyp.setScore(hyp.getFeatures().dotProduct(model));
		//System.err.println("Gra rule:"+ hyp.getFeatures());
	}

	// For OOV word hyps
	public void initializeHypothesisFeatures(Hypothesis hyp){
		
		SparseVector s = new SparseVector();

		// Set the SGT, TGS features
		s.set(TGSId, Grammar.OOV_RULE_SCORE);
		s.set(SGTId, Grammar.OOV_RULE_SCORE);
		s.set(TGSLEXId, LexicalWeightFeatureFunction.BACKOFF);
		s.set(SGTLEXId, LexicalWeightFeatureFunction.BACKOFF);
		
		s.set(featureNameMap.get("WC") , 1);
		s.set(LPId, LengthPenaltyFeatureFunction.computeFeature(hyp));
		s.set(featureNameMap.get("RC") , 1);
		s.set(LMId, LMFeatureFunction.computeFeature(hyp));
		
		// Set up all the features in the hyp
		hyp.addFeatures(s);
		
		// dot product with the model
		hyp.setScore(hyp.getFeatures().dotProduct(model));
		
		//System.err.println("Just hyp rule:"+ hyp.getFeatures());
	}

	// For Connecting edges
	public void initializeHypothesisFeatures(Hypothesis hyp, int phraseLength){
		 
		//System.err.println("Setting for phrase");
		SparseVector s = new SparseVector();

		// Set the word count feature
		s.set(featureNameMap.get("WC") , phraseLength);
		// Is this a rule application or a GLUE ? 
		s.set(featureNameMap.get("RC") , 1);
		s.set(LPId, LengthPenaltyFeatureFunction.computeFeature(hyp));
		s.set(TGSLEXId, LexicalWeightFeatureFunction.BACKOFF);
		s.set(SGTLEXId, LexicalWeightFeatureFunction.BACKOFF);
		
		// Set the languageModel feature
		s.set(LMId, LMFeatureFunction.computeFeature(hyp));
				
		// Set up all the features in the hyp
		hyp.addFeatures(s);
		
		// dot product with the model
		hyp.setScore(hyp.getFeatures().dotProduct(model));
		//System.err.println("Just phrase:"+ hyp.getFeatures());
	}

	// Compute the combination cost of a binary edge
	public double computeCombinationCostBinary(Hypothesis h, Hypothesis h1, Hypothesis h2, int edgeId, boolean bound){
		Object [] args = {h, h1, h2, hg.getEdge(edgeId).getRuleId()};
		double score = 0;
		double lmscore = 0.0;
		for(Method m : featureClassSet){
			
			//System.err.println(h.getSrcWords()+":"+h.getWords());
			//System.err.println("\t1"+h1.getSrcWords()+":"+h1.getWords());
			//System.err.println("\t2"+h2.getSrcWords()+":"+h2.getWords());
			
			try {
				HashMap<String, Double> features = (HashMap<String, Double>)m.invoke(null, args);
				for(String s : features.keySet()){
					int index = -1;
					if(featureNameMap.containsKey(s))
						index = featureNameMap.get(s);
					else {
						featureCount++;
						featureNameMap.put(s, new Integer(featureCount));
						model.set(featureCount, 1);
						index = featureCount;
					}
					score += features.get(s).doubleValue() * model.get(index); 					
					h.getFeatures().increment(index, features.get(s).doubleValue());
					
					if(index == LMId)
						lmscore = features.get(s).doubleValue();
				}
				//System.err.println("After 2:"+h.getFeatures());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		if(bound){
			if(edgeLowerBounds.containsKey(edgeId)){
				if(edgeLowerBounds.get(edgeId) < lmscore)
					edgeLowerBounds.put(edgeId, lmscore);
			}
			else{
				edgeLowerBounds.put(edgeId, lmscore);
			}
		}
		
		h.hScore = h1.getScore() + h2.getScore() + score - lmscore*model.get(LMId) + edgeLowerBounds.get(edgeId);
		return score;
	}
	
	// Compute the cost of the unary edge
	public double computeCombinationCostUnary(Hypothesis h, Hypothesis h1, int edgeId){
		
		Object [] args = {h, h1, hg.getEdge(edgeId).getRuleId()};
		
		String ruleId = hg.getEdge(edgeId).getRuleId();
		int index = -1;
		
		//System.err.println(h.getSrcWords()+":"+h.getWords());
		double score=0.0;
		if(featureNameMap.containsKey(ruleId)) {
			index = featureNameMap.get(ruleId);
		} else{
			featureCount++;
			featureNameMap.put(ruleId, new Integer(featureCount));
			model.set(featureCount, 1);
			index = featureCount;
		}

		double sgtscore = LexicalWeightFeatureFunction.getPhraseProbability_SGT(h1.getSrcWords(), h1.getWords(),true); 
		double tgsscore = LexicalWeightFeatureFunction.getPhraseProbability_TGS(h1.getSrcWords(), h1.getWords(),true); 
		double lengthpenalty = LengthPenaltyFeatureFunction.computeFeature(h1); 
		
		double [] feature = Grammar.getRuleScores(ruleId);
		double sgtrule = feature[0];
		double tgsrule = feature[1];
		
		score  =  tgsrule * model.get(TGSId);
		score +=  sgtrule * model.get(SGTId);
		score +=  sgtscore * model.get(SGTLEXId);
		score +=  tgsscore * model.get(TGSLEXId);
		score += lengthpenalty * model.get(LPId);
		score += 1.0 * model.get(index);
		
		h.getFeatures().increment(TGSId, tgsrule);
		h.getFeatures().increment(SGTId, sgtrule);
		h.getFeatures().increment(SGTLEXId, sgtscore);
		h.getFeatures().increment(TGSLEXId, tgsscore);
		h.getFeatures().increment(LPId, lengthpenalty);
		h.getFeatures().increment(WCId, 1);
		h.getFeatures().increment(RCId, 1); 
		
		
		//System.err.println("After 1:"+h.getFeatures());
		//h.getFeatures().increment(index, 1.0);

/*  	  for(Method m : featureClassSet){
			try {
				System.err.println("Trying to call "+ m.getDeclaringClass());
				HashMap<String, Double> features = (HashMap<String, Double>)m.invoke(null, args);
				for(String s : features.keySet()){
					index = featureNameMap.get(s);
					score += features.get(s).doubleValue() * model.get(index);
					h.getFeatures().increment(index, features.get(s).doubleValue());
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} */
		
		h.hScore = h1.getScore() + score;
		return score;
	}

}
