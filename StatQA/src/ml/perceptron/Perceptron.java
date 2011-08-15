/**
 * 
 */
package ml.perceptron;
import java.util.*;

import ml.QADecoder;
import ml.utils.*;
import qa.*;

public class Perceptron {
	
	// Decoder for computing argmax
	protected QADecoder d;
	 
	// Parameters 
	SparseVector parameters = null;
	
	// Evaluation
	public double train_correct = 0;
	public double train_total = 0;
	
	public Perceptron(QADecoder d){
		this.d = d;
		parameters = d.getWeightsCopy();
	}
	
	// Add to the weights, the delta component of MODEL best and REF
	public void update(Question q, Answer ref) throws Exception {
		train_total++;
 
		// Compute features for hyp inside decoder
		d.setWeights(parameters);
		Answer a = d.decode1best(q);
		
		if(a!=null && ref!=null) { // Only if such an answer exists and REFERENCE exists  
			if(a.id==ref.id){
				//System.err.println("Q:"+a.id+" A:"+ref.id);
				// Nothing to do | Right answer  
				train_correct++;
			}else {
				// Wrong answer | So update to right one . Where is the right one
				for(String fn : parameters.getFeatures()){
					double value = parameters.get(fn);			
					if(ref.features.exists(fn)){
						value = value + ref.features.get(fn);
					}
					if(a.features.exists(fn)){
						value = value - a.features.get(fn);
					}
					parameters.set(fn, value);
				}
			}
		}
	}
	
	// Train on the given data for these many iterations
	// Influenced by last seen entries .
	public void train(Hashtable<Integer,Question> questionData, Hashtable<Integer,Answer> answerRefData, int iteration, String testqfile) throws Exception{
		System.err.println("Training... ");
		
		for(int t = 1; t <= iteration; t++){
			parameters = d.getWeightsCopy();
			
			System.err.print("Iteration #"+t+" ");
			long start = System.currentTimeMillis();
			int n=0;
			for(Integer key: questionData.keySet()) {
				if(n%1000==0){System.err.print(n+" ");}
				update(questionData.get(key),answerRefData.get(key));
				n++;
			}
			long end = System.currentTimeMillis();
	        System.err.println("[Time:"+(end-start)+"]");

		this.trainingAccuracy();
		d.setWeights(parameters);
		// Test for fun
		d.decodeTest(testqfile);
		save("vweights.txt",t);
		}
	}
	
	//TODO: Train on the given data for these many iterations and keep the average
	public void trainAverage(Hashtable<Integer,Question> questionData,Hashtable<Integer,Answer> answerRefData, int iteration, String testqfile) throws Exception{
		System.err.println("Training... ");

		parameters = d.getWeightsCopy();
		
		save("vweights.txt",0);
		// Initialize parameters
		for(int t = 1; t <= iteration; t++){
		SparseVector sum = d.getWeightsCopy(); 
		System.err.print("Iteration #"+t+" ");
			long start = System.currentTimeMillis();
			for(Integer key: questionData.keySet()) {
				update(questionData.get(key),answerRefData.get(key));
			  	sum.add(parameters); // Add
			}
	        long end = System.currentTimeMillis();
	        System.err.println("[Time:"+(end-start)+"]");

		double scalefactor = questionData.size();
		if(scalefactor==0){
			scalefactor = 1;
		}
		sum.scale(1/scalefactor); // Normalize
		d.setWeights(sum);

		this.trainingAccuracy();
		d.decodeTest(testqfile);
		save("vweights.txt",t);
		}		
	}

	public void trainingAccuracy(){
		System.err.println("Train: Correct/Total:"+train_correct+"/"+train_total+" ("+train_correct*100/train_total+"%) weights:"+d.model.size());
		train_total = 0;
		train_correct = 0;
	}
	
	public void save(String file,int iteration){
		d.saveWeights(file, iteration);
	}
}