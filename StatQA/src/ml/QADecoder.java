/**
 * 
 */
package ml;

import java.io.*;
import java.util.*;

import options.Options;

import ml.utils.FeatureManager;
import ml.utils.SparseVector;

import qa.*;
/**
 * @author Vamshi
 *
 */
public class QADecoder implements Decoder {
	
	int KBEST = 10;
	
	// Current Model Weights 
	public SparseVector model = null;
	
	// Indexer (Load answer vectors from File - features already computed, you only load the feat vec file) 
	public IRIndexer_Offline ir = null; 

	// TODO: Indexer (Need to retrieve results and then compute features as well) 
	// IRIndexer ir = null;
	Options opts = null;

	public QADecoder (String qlistfile, SparseVector weights,String vecextension,Options opts){
		// Initialize weights
		model = new SparseVector();
		this.setWeights(weights);
		// Initialize the IR idexer and load from file 
		ir = new IRIndexer_Offline(qlistfile);
		ir.vecTAG = vecextension; 
		
		this.opts = opts;  
	}
	

	public void decodeTest(String testqfile,String weightsfile) throws Exception {
		// Set Weights 
		readWeights(weightsfile);
		decodeTest(testqfile);
	}
	
	public void decodeTest(String testqfile) throws Exception {
		// Test file
		//System.err.println("Testing on file: "+testqfile);
		BufferedReader qlistr = new BufferedReader(new FileReader(testqfile));
		String qfile = "";
		double total= 0;
		double correct = 0;
		
		// Error log
		BufferedWriter writer = new BufferedWriter(new FileWriter("error.log"));
		while((qfile = qlistr.readLine()) != null){
			// Load question: 
			BufferedReader qr = new BufferedReader(new FileReader(qfile));
			BufferedReader qrOriginal = new BufferedReader(new FileReader(qfile+".original"));
			String quesOriginal = "";
			
			String ques = "";
			// Question number starts with 0
			int qno=0; 
			while((ques = qr.readLine()) != null)
			{
				quesOriginal = qrOriginal.readLine(); // Original question 
				Answer a = this.decode1best(new Question(ques,quesOriginal,qfile,qno));
				if(a!=null && a.id==qno){
					correct++;
					writer.write("1\n");
				}else{
					writer.write("0\n");
				}
			total++;
			qno++;
			}
			qr.close();
			qrOriginal.close();
		}
		writer.flush();
		writer.close();
		System.err.println("Test:: Correct/Total:"+correct+"/"+total+" ("+correct*100/total+"%) weights:"+model.size());
	}

	public List<Answer> rerank(Question q,List<Answer> answers) throws Exception {
		SparseVector mymodel = new SparseVector(); // What features are relevant for you 
		mymodel.set("gls.MT", 1);
		mymodel.set("sgt.MT", 1);
		mymodel.set("tgs.MT", 1);
		
//		System.err.println(q.id+":"+q.qOriginal);
//		System.err.println("Before Reranking");
//		System.err.println(answers);

		if(answers!=null && (!answers.isEmpty()) ){
			for (Answer a: answers){
				a.score = this.scoreIt(a, mymodel);
			}
			// Sort in Descending order
			Collections.sort(answers,new AnswerCompare());
		
//			System.err.println("After Reranking");
//			System.err.println(answers);
//			System.err.println("---------------");
			return answers; 
		}
		return null;
	}
		
	public List<Answer> decode(Question q, int k) throws Exception {
		// Make a call to Lucene and return results 
		// If it is present on DISK already, then just read and extract (XML file) 
		List<Answer> answers = ir.retrieveAnswers(q);
		
		if(answers!=null && (!answers.isEmpty()) ){
			for (Answer a: answers){
				// Compute extra features - GLOBAL ones 
				a.computeExtraFeatures(q.qOriginal,opts);
				a.score = this.scoreIt(a);
			}
			
			// Sort in Descending order
			Collections.sort(answers,new AnswerCompare());
			if(answers.size()<=k){
				k = answers.size();
			}
			// Reduce the space 
//			answers = answers.subList(0,k);
//			answers = rerank(q,answers);
			return answers;
		}
		return null;
	}

	public Answer decode1best (Question q) throws Exception{

		List<Answer> alist = decode(q,KBEST);
		if(alist!=null){
			return alist.get(0);
		}
		return null;
	}
	
	// All the trained weights will be scored as is, but untrained ones need to be re-initialized ?  
	public double scoreIt(Answer a){
		double score = 0;		
		// Compute score over the answer vector 
		for(String fn: a.features.getFeatures()){
			double wt = FeatureManager.word_weight_init;
				if(model.exists(fn)){
					wt = model.get(fn);
				}else{
					if(fn.contains("wbt2")){
						wt = FeatureManager.exp_weight_init;
					}else if(fn.contains("webtalk-")){ // Skip it
						wt = 0;
					}else if(fn.contains(".b")){ // Skip it
						wt = 0;
					}else if(fn.contains(".MT")){// Machine Translation features
						wt = 0;
					}
					else if(fn.contains(".p")){ // Skip it
						wt = 0;
					}
				}
			score+= wt * a.features.get(fn);	
		}
		return score; 
	}
	
	// Score with a passed model   
	public double scoreIt(Answer a,SparseVector mymodel){
		double score = 0;		
		// Compute score over the answer vector 
		for(String fn: a.features.getFeatures()){
			double wt = 0;
				if(mymodel.exists(fn)){
					wt = mymodel.get(fn);
				}
			score+= wt * a.features.get(fn);	
		}
		return score; 
	}
	
	// This function returns the feature vector towards which
	// update should be done. Since many different strategies
	// can be used for deciding the target, decoder should 
	// handle this.
	public SparseVector getTargetFeatures(Question src, Answer ref){
		return null;
	}

	public List<Answer> decodeAll(List<Question> q, int kbest) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	// Value	
	public void setWeights(SparseVector wts){
		for(String fn: wts.getFeatures()){
				model.set(fn,wts.get(fn));
		}
	}
	
	// Reference
	public SparseVector getWeights(){
		return model;
	}
	// Value	
	public SparseVector getWeightsCopy(){
		SparseVector vec = new SparseVector();
		for(String fn: model.getFeatures()){
			vec.set(fn,model.get(fn));
		}
		return vec;
	}
	
	
	public void saveWeights(String file,int iteration) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file+"."+iteration));
			Vector v = new Vector(model.getFeatures());
			Collections.sort(v);
			Iterator it = v.iterator();
			while(it.hasNext()){
				String featname =  (String)it.next();
				writer.write(featname+"\t"+model.get(featname)+"\n");	
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readWeights(String weightfile) {
		try {
			BufferedReader fbr = new BufferedReader(new FileReader(weightfile));
			String str = "";
			while((str = fbr.readLine()) != null){
				String [] arr = str.split("\t");
				String name = arr[0];
				double weight = Double.parseDouble(arr[1]);
				model.set(name, weight);
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("Loaded weights from:"+weightfile);
	}
}
