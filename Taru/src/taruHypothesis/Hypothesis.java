/**
 * 
 */
package taruHypothesis;

import taruDecoder.Scorer;
import ml.utils.SparseVector;

/**
 * @author abhayaa
 *
 */
public class Hypothesis {

	private String words;
	private int length;
	private SparseVector features;
	private double totalScore;
	
	// source side info 
	private String swords; 
	private int slength; 
	//private int sStart=-1;
	//private int sEnd=-1;
	
	// For use in kbest extraction
	public double hScore;
	public double lmscore;

	// back pointers
	int edgeIndex;
	int[] kindex = new int[2];
	
	public Hypothesis(String words, String swords, int edgeIndex, int[] kindex){
		this.words = words;
		this.length = words.split("\\s+").length;
		this.swords = swords;
		this.slength = swords.split("\\s+").length;
		this.totalScore = 0;
		this.lmscore = 0;
		this.features = new SparseVector();
		
		this.edgeIndex = edgeIndex;
		this.kindex[0] = kindex[0];
		this.kindex[1] = kindex[1];
	}
	
	public void addFeatures(SparseVector s){
		features.add(s);
	}
	
	public double getScore(){
		return totalScore;
	}
	
	public String getWords(){
		return words;
	}
	
	public String getSrcWords(){
		return swords;
	}
	
	public int getSrcLength(){
		return slength;
	}
	
	public int getLength(){
		return slength;
	}
	
	public int getEdgeId(){
		return edgeIndex;
	}
	
	public int[] getKIndex(){
		return kindex;
	}
	
	public SparseVector getFeatures(){
		return features;
	}
	
	public void setScore(double score){
		totalScore = score;
	}
	
	@Override
	public String toString() {
		String str = totalScore + " : " + lmscore + " : ";
		str += words + "\n";
		str += edgeIndex + " : [" + kindex[0] + " " + kindex[1] + "]\n";
		str += "Feature Key : " + Scorer.getScorer().getFeatureNameMap() + "\n";
		return str + "Features : " + features + "\n";
		
	}
	
	public String toOptNBestFormat() {
		String str = "";
		str += words + "\n";
		return str + features.toNBestFormat();
		
	}
}
