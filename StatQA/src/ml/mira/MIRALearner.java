package ml.mira;

import java.io.*;
import java.util.Hashtable;
import java.util.List;

import qa.*; 
import ml.QADecoder;
import ml.QALoss;
import ml.QALossEditDistance;
import ml.utils.LossInterface;
import ml.utils.SparseVector;

/**
 * MIRALearner is a class for using MIRA with structured classification,
 * and training a set of weights. The code is mainly taken from Ryan
 * Mcdonald's MST parser.
 */
public class MIRALearner implements Serializable {
	private static final long serialVersionUID = 1L;
	
	int kBest = 1; // Updating it to top 10 is alright
	public SparseVector parameters = null;
	public transient SparseVector total = null;

	private transient LossInterface loss;
			
	// Decoder for computing argmax
	protected QADecoder d;
	 
	// Evaluation
	public double train_correct = 0;
	public double train_total = 0;
	

	public MIRALearner(QADecoder d) {
		this.d = d;
		parameters = d.getWeightsCopy();
		total = d.getWeightsCopy();
		loss = new QALoss(); // 0 - 1 Loss
		//loss = new QALossEditDistance(); // overlap Loss
	}
		
	// Train on the given data for these many iterations
	// Influenced by last seen entries .
	public void train(Hashtable<Integer,Question> questionData, Hashtable<Integer,Answer> answerRefData, int iteration, String testqfile) throws Exception{
		
		System.err.println("Training... ");
		double numInstances = questionData.size();
		
		for(int t = 1; t <= iteration; t++){
	        System.out.print(" Iteration "+t+":");
	        long start = System.currentTimeMillis();
			int n=0;
			for(Integer key: questionData.keySet()) {
				if(n%1000==0){
					System.err.print(n+" ");
				}

				// Decode with new weights
				d.setWeights(parameters);
				List<Answer> kbestAnswers = d.decode(questionData.get(key),kBest);
				if(kbestAnswers!=null && (questionData.get(key).id==kbestAnswers.get(0).id)){
					train_correct++;
				}else{
					double upd = (double)(iteration*numInstances - (numInstances*(t-1)+(n+1)) + 1);
					upd = 1;
					update(answerRefData.get(key), kbestAnswers,upd);
					
					//Verification code 
					d.setWeights(parameters);
					List<Answer> nowkbestAnswers = d.decode(questionData.get(key),kBest);
					if(nowkbestAnswers!=null && (questionData.get(key).id==nowkbestAnswers.get(0).id)){
						//System.err.println("Got it now !");
					}else{
						//System.err.println("Still did not get it!");
					}
				}
				n++;
				train_total++;
			}
			long end = System.currentTimeMillis();
			System.out.println("[Time:"+(end-start)+"]");
			
		trainingAccuracy();
        // averageParams(numInstances);
		d.setWeights(parameters);
		// Test for fun
		d.decodeTest(testqfile);
		save("mweights.txt",t);
		//unaverageParams(numInstances);
		}
		System.err.println("FINAL: Test");
		d.decodeTest(testqfile);
	}
	
	public void update(Answer refOutput, List<Answer> kBestOutputs, double upd)
	{
		// This can't be null
		if(refOutput==null || kBestOutputs==null)
			return;
		 
		int K = kBestOutputs.size();
		
		double[] b = new double[K];
		double[] lam_dist = new double[K];
		SparseVector[] dist = new SparseVector[K];
		
		double refScore = d.scoreIt(refOutput);
		//double refScore = d.scoreIt(refOutput,parameters);
		double Zpartition = 0; 
		for(int k = 0; k < K; k++) {
			double kScore = d.scoreIt(kBestOutputs.get(k));

			// Partition summation over K (ideally should be done over all)
			Zpartition += kScore;  

			// Margin
			lam_dist[k] = refScore - kScore;			
			
			// Loss
			b[k] = (double)loss.loss(refOutput,kBestOutputs.get(k));	

			// Feature distances 
			dist[k] = getDistVector(refOutput.features,kBestOutputs.get(k).features);
		}
		
		// Compute linear constraints over margin and loss (For perceptron you just relax this constraint
		for(int k = 0; k < K; k++) {
			b[k] -= (lam_dist[k]);
			//b[k] -= (lam_dist[k]/Zpartition);
		}
		
	    double[] alpha = hildreth(dist,b);
		
		SparseVector fv  = null;
		for(int k = 0; k < K; k++) {
			fv = dist[k];
			for(String fname: fv.getFeatures()){
					double value = parameters.get(fname);
					value+= alpha[k] * fv.get(fname);
					//value+= b[k] * fv.get(fname); // Simulate Perceptron 
					parameters.set(fname,value);
					
//					total.add(parameters);
// 					double tvalue = total.get(fname);
// 					tvalue += upd * alpha[k] * fv.get(fname);
// 					total.set(fname,tvalue);
			}
			
			// Verification code
			double marginBefore = d.scoreIt(refOutput) - d.scoreIt(kBestOutputs.get(0));
			double lossBefore = (double)loss.loss(refOutput,kBestOutputs.get(0));
			d.setWeights(parameters);
			double marginAfter = d.scoreIt(refOutput) - d.scoreIt(kBestOutputs.get(0));
			double lossAfter = (double)loss.loss(refOutput,kBestOutputs.get(0));
			//System.err.println("Before Loss:"+lossBefore+" and Margin:"+marginBefore);
			//System.err.println("After Loss:"+lossAfter+" and Margin:"+marginAfter+"\n");
		}
	}

	private double[] hildreth(SparseVector[] a, double[] b) {
		int i;
		int max_iter = 10000;
		double eps = 0.00000001;
		double zero = 0.000000000001;

		double[] alpha = new double[b.length];

		double[] F = new double[b.length];
		double[] kkt = new double[b.length];
		double max_kkt = Double.NEGATIVE_INFINITY;

		int K = a.length;

		double[][] A = new double[K][K];
		boolean[] is_computed = new boolean[K];
		for(i = 0; i < K; i++) {
			A[i][i] = dotProduct(a[i],a[i]);
			is_computed[i] = false;
		}

		int max_kkt_i = -1;
		for(i = 0; i < F.length; i++) {
			F[i] = b[i];
			kkt[i] = F[i];
			if(kkt[i] > max_kkt) { max_kkt = kkt[i]; max_kkt_i = i; }
		}

		int iter = 0;
		double diff_alpha;
		double try_alpha;
		double add_alpha;

		while(max_kkt >= eps && iter < max_iter) {

			diff_alpha = A[max_kkt_i][max_kkt_i] <= zero ? 0.0 : F[max_kkt_i]/A[max_kkt_i][max_kkt_i];
			try_alpha = alpha[max_kkt_i] + diff_alpha;
			add_alpha = 0.0;

			if(try_alpha < 0.0)
				add_alpha = -1.0 * alpha[max_kkt_i];
			else
				add_alpha = diff_alpha;

			alpha[max_kkt_i] = alpha[max_kkt_i] + add_alpha;

			if (!is_computed[max_kkt_i]) {
				for(i = 0; i < K; i++) {
					A[i][max_kkt_i] = dotProduct(a[i],a[max_kkt_i]); // for version 1
					is_computed[max_kkt_i] = true;
				}
			}

			for(i = 0; i < F.length; i++) {
				F[i] -= add_alpha * A[i][max_kkt_i];
				kkt[i] = F[i];
				if(alpha[i] > zero)
					kkt[i] = Math.abs(F[i]);
			}

			max_kkt = Double.NEGATIVE_INFINITY;
			max_kkt_i = -1;
			for(i = 0; i < F.length; i++)
				if(kkt[i] > max_kkt) { max_kkt = kkt[i]; max_kkt_i = i; }

			iter++;
		}
		return alpha;
	}

	public String toString()
	{
		return parameters.toString();
	}
	
	// For MIRA 
    // fv1 - fv2 (Of only those which are relevant to parameter space)
    public SparseVector getDistVector(SparseVector fv1, SparseVector fv2) {
    	SparseVector result = new SparseVector(); 
    	// Only update those present in Model, coz others in answer need not be Optimized for 
		for(String fn : parameters.getFeatures()) {
			double value = 0;
			if(fv1.exists(fn))
				value = value + fv1.get(fn);	
			if(fv2.exists(fn))
				value = value - fv2.get(fn);
			
		result.set(fn,value);
		}
		return result;
    }
    // For MIRA
    // Dot product of only those features that are relevant to training in Parameters / Model  
	public double dotProduct(SparseVector fv1, SparseVector fv2){

		double prod = 0.0;
		for(String i : parameters.getFeatures()){
			if(fv1.exists(i) && fv2.exists(i))
				prod += fv1.get(i) * fv2.get(i);
		}
		return prod;
	}
	
	public void trainingAccuracy(){
		System.err.println("Train:: Correct/Total:"+train_correct+"/"+train_total+" ("+train_correct*100/train_total+"%) weights:"+d.model.size());
		train_total = 0;
		train_correct = 0;
	}
	
	public void save(String file,int iteration){
		d.saveWeights(file, iteration);
	}	
	
	public void averageParams(double avVal) {
		total.scale(1/avVal);
	}
	public void unaverageParams(double avVal) {
		total.scale(avVal);
	}	 
}
