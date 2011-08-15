/**
 * 
 */
package taruDecoder;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import taruDecoder.hyperGraph.HGUtils;
import taruDecoder.hyperGraph.HyperGraph;
import taruHypothesis.Hypothesis;
import utils.evaluation.BleuScore;


import ml.online.Decoder;
import ml.utils.SparseVector;

/**
 * @author abhayaa
 *
 */
public class TaruTrainingWrapper implements Decoder {
	
	// Base decoder to do the actual decoding
	private Taru baseDecoder;
	
	// Keep around as much stuff as we can from round to round
//	private HashMap<String, HyperGraph> forestCache;	

	private HyperGraph forest;
	
	public TaruTrainingWrapper(String configfile,HashMap<String,Integer> vocab){
		baseDecoder = new Taru(configfile,vocab);
//		forestCache = new HashMap<String, HyperGraph>();
	}
	
	public List<Hypothesis> decode(String src, int kbest){
//		HyperGraph forest;
			return baseDecoder.decodeParseTree(src,kbest);
	}
	
	
	public SparseVector getTargetFeatures(String src, String ref){
		
//		System.out.println(ref);
//		HyperGraph forest = forestCache.get(src);
		System.out.println(HGUtils.extractKBestHypothesisParse(forest,1));
		List<Hypothesis> hypList = forest.getVertex(0).getTopKHypothesis(100);
		Hypothesis maxHyp = null;
		double maxScore = -800;
		double count = 0;
		for(Hypothesis hyp :  hypList){
//			System.out.println(hyp);
//			hyps.add(hyp.getWords());
//			double score = computeSmoothBlueKlien(hyps, refs);
//			System.out.print("Evaluating " + hyp.getWords() + " ");
			double score = computeSmoothBlueKlien(hyp.getWords(), ref);
			if(count == 0)
				count = score;
			if(score > maxScore){
				maxHyp = hyp;
				maxScore = score;
//				System.out.println("Better Target String: " + hyp + " " + score);
			}
//			hyps.clear();
		}
		
		System.err.println("Reference: " + ref);
		System.err.println("Current Best: " + forest.getVertex(0).getKthHypothesis(1) + " " + count);
		System.err.println("Chosen Target String: " + maxHyp + " " + maxScore);
//		forestCache.clear();
		
		return maxHyp.getFeatures();
	}
	
	public double computeSmoothBlueKlien(ArrayList<String> hyps, ArrayList<String> refs) {
		double bleu = 0.0;
		try{
			BleuScore bs = new BleuScore(hyps,refs);
			bleu = bs.getSmoothBleuKlien();
		}catch(Exception e){e.printStackTrace();}
		return bleu;
	}

	public double computeSmoothBlueKlien(String hyps, String refs) {
		double bleu = 0.0;
		try{
			BleuScore bs = new BleuScore(hyps,refs);
			bleu = bs.getSmoothBleuKlien();
		}catch(Exception e){e.printStackTrace();}
		return bleu;
	}

}
