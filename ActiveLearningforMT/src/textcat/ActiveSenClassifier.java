package textcat;
import java.io.*; 
import java.util.*; 
import java.util.regex.*;

import crowdsource.validate.TranslationValidator;

// WEKA 
import utils.MyNLP;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.FastVector;
import weka.core.Attribute;

import weka.core.Utils;
import weka.clusterers.DensityBasedClusterer;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.clusterers.ClusterEvaluation;
import weka.core.*;
import java.io.BufferedReader;
import java.io.FileReader;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;

public class ActiveSenClassifier extends RandSenClassifier
{
	public ActiveSenClassifier(String trainFile,String testFile)
	{
		super(trainFile,testFile);
	}
	public ActiveSenClassifier(Instances trainData, Instances testData) 
	{
		super(trainData,testData);
	}
	
  	// Train on a selected set of sentences   
	public void train(Instances selData)
	{		 
		try {
		    cls.buildClassifier(selData);
		}catch(Exception e){
			System.err.println(e.toString());
		}
  	}
	
  	// Test on a selected set of sentences   
	//Assumes that train was called before on the classifier  
	public void test(Instances selData)
	{
		try {		    
		    Evaluation eval = new Evaluation(selData);
		    eval.evaluateModel(cls, selData);
		    System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		}catch(Exception e){
			System.err.println(e.toString());
		}
  	}
	
	// Train on Unseen Data (Output predictions to a file) TODO
	public void trainUnseen(Instances selData, Instances unseen)
	{
		try {
		    Classifier clsCopy = Classifier.makeCopy(cls);
		    //crossValidate(selData,clsCopy);
		    
		    clsCopy.buildClassifier(selData);
		    Evaluation eval = new Evaluation(selData);
		    // eval.evaluateModel(clsCopy, testData);
		    for(int i=0;i<unseen.numInstances();i++){
		    	double score = eval.evaluateModelOnce(clsCopy, unseen.instance(i));
		    	System.out.println(score);
		    }
		    
		    System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		}catch(Exception e){
			System.err.println(e.toString());
		}
  	}


	public static void main(String args[]) throws Exception{
		if(args.length!=2){
			System.err.println("Usage: java <train.arff> <test.arff>\n");
			System.exit(0);
		}
		
		int ITERS = 10;
		int BATCH_SIZE = 100; 
		
		System.out.println("Running Iterative classifier Data... ");
		ActiveSenClassifier dc = new ActiveSenClassifier(args[0],args[1]);
		
        // randomize data
        int seed = ITERS * BATCH_SIZE;
		Random rand = new Random(seed);   // create seeded number generator  
		trainData.randomize(rand);         // randomize data with number generator
		
		// Create seed data and train Classifier 
	    Instances seedData = new Instances(trainData,0,BATCH_SIZE);
	    seedData.toSummaryString();
		dc.train(seedData);
		 
		// Create remaining data
		Instances newtrainData = new Instances(seedData);
		 for (int i = 1; i < ITERS; i++) {
			 System.out.println("========Iteration:"+i);
// 			 Create an Empty instance of SELECTED with similar headers as seedData 
//			 Instances selected = dc.selectSequence(newtrainData,BATCH_SIZE); 
//			 for(int p=0;p<selected.numInstances();p++){
//				 newtrainData.add(selected.instance(p));
//			 }
			 
			 Instances selected = dc.selectRandom(newtrainData,i*BATCH_SIZE);
			 selected.toSummaryString();
			 System.out.println("========Classification=======");
			 dc.crossValidate(selected, cls);
		 }
	}
	
	// Selection Techniques 
	public Instances selectSequence(Instances curData, int n) throws Exception {
		return new Instances(trainData,curData.numInstances(),n); 
	}
 
	public Instances selectRandom(Instances curData, int n) throws Exception {
		Random rand = new Random(1000);   // create seeded number generator  
		trainData.randomize(rand);         // randomize data with number generator
		return new Instances(trainData,0,n); 
	}
	// Uncertainty based: class margin based  
	public Instances selectMargin(Instances curData, int n) throws Exception {
		return null;
	}
}