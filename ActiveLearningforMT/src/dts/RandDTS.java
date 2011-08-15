package dts;
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
import weka.core.Instances;
import java.io.BufferedReader;
import java.io.FileReader;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;

public class RandDTS
{	
	static public Classifier cls = null;
	static public Instances trainData = null; 
	static public Instances testData = null;
	
	public RandDTS(Instances trainData, Instances testData) {
		this.trainData = trainData; 
		this.testData = testData; 
		initialize();
	}
	
	public RandDTS(String trainfile, String testfile) {
		// Load Data
		try {
		 BufferedReader reader = new BufferedReader(new FileReader(trainfile));
		 trainData = new Instances(reader);
		 reader.close();
		 trainData.setClassIndex(trainData.numAttributes()-1);
		 
		 BufferedReader reader2 = new BufferedReader(new FileReader(testfile));
		 testData = new Instances(reader2);
		 reader2.close();
		 testData.setClassIndex(testData.numAttributes()-1);
		}catch(Exception e){
		 	System.err.println(e.toString());
		}
		initialize();
	}

	public void initialize() {
		 // Define Classifier 
		 cls = new Logistic();         // new instance of tree
		 String[] options = new String[1];
		 options[0] = "-U";            // unpruned tree
		// cls.setOptions(options);     // set the options
		 
		// Summarize some statistics
		System.err.println("==========Training Data Statistics===========");
		trainData.toSummaryString();
		// Summarize some statistics
		System.err.println("==========Test Data Statistics===========");
		testData.toSummaryString();
	}
	
	public void classify() throws Exception
	{
		 // Training 
		 cls.buildClassifier(trainData);   // build classifier
		 
		 // Cross-Validation (10-fold cross validation
		 // crossValidate(trainData,cls);
		 
		 // Testing Final (large data set- Which classifier to use?) 
		Evaluation evalFinal = new Evaluation(trainData);
		evalFinal.evaluateModel(cls, testData);
		System.out.println(evalFinal.toSummaryString("\nResults\n======\n", false));
	}
	
	public void crossValidate(Instances trainData, Classifier cls) throws Exception {
		 int runs = 10; 
		 int folds = 10;
		 for (int i = 0; i < runs; i++) {
        // randomize data
         int seed = i * 100;
		 Random rand = new Random(seed);   // create seeded number generator
		 Instances randData = new Instances(trainData);   // create copy of original data
		 randData.randomize(rand);         // randomize data with number generator
		 randData.stratify(folds);
		 
		 Evaluation eval = new Evaluation(randData);
		 for (int n = 0; n < folds; n++) {
			   Instances trainS = randData.trainCV(folds, n);
			   Instances testS = randData.testCV(folds, n);
			   
		      // build and evaluate classifier
		      Classifier clsCopy = Classifier.makeCopy(cls);
		      clsCopy.buildClassifier(trainS);
		      eval.evaluateModel(clsCopy, testS);
		 }
	      // output evaluation
	      System.out.println();
	      System.out.println("=== Setup run " + (i+1) + " ===");
	      System.out.println("Classifier: " + cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions()));
	      System.out.println("Dataset: " + randData.relationName());
	      System.out.println("Folds: " + folds);
	      System.out.println("Seed: " + seed);
	      System.out.println();
	      System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation run " + (i+1) + "===", false));
		 }
		 // Return the averaged model across runs ? 
		 // TODO
	}

	public static void main(String args[]) throws Exception{
		if(args.length!=2){
			System.err.println("Usage: java <train.arff> <test.arff>\n");
			System.exit(0);
		}
		System.out.println("Running classifier Data... ");
		RandDTS dc = new RandDTS(args[0],args[1]);
		dc.classify();
	}
}