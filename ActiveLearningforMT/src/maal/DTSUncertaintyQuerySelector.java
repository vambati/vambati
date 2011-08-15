package maal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import data.LDS;
import dts.ActiveDTS;
import dts.DataCreator;

import model.smt.*;
import query.*;
import utils.ExternalProcRunner;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;

public abstract class DTSUncertaintyQuerySelector implements QuerySelector{

	// Uncertainty Scores for Unlabled data loaded from Moses output 
//	Hashtable<Integer,Double> MOSES = null; 
//	Hashtable<Integer,String> MOSESHYP = null; 
	
	// Classifier scores 
	protected Hashtable<Integer,Double> DTSCORES = null;
	
	String moses_viterbi = "/home/vamshi/code/scripts/mt-cluster/viterbi-mosesrun.pl";
	
	public DTSUncertaintyQuerySelector(LDS l,String conf,int tag,String src,String tgt){
//		MOSES = new Hashtable<Integer, Double>();
//		MOSESHYP = new Hashtable<Integer, String>();
	
		DTSCORES = new Hashtable<Integer, Double>();
		
		try {
			classifySens(conf,tag,src,tgt);
		} catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
		} 
	}
	
	private void classifySens(String config,int tag,String src,String tgt) throws Exception {
	
		String mosesFile = src+".ul."+tag+".mosesout";
		String refFile = tgt+".ul."+tag;
		
		// To have a different ID 
		String fixtag = System.currentTimeMillis()+"";
		String tmpfile = src+".moses."+fixtag;
		
		File mosesOutput = new File(mosesFile);
		if(!mosesOutput.exists()){
			// Run moses and create output file first
			System.err.println("Running moses to decode Unlabeled Data..... ");	 
			String cmdarr[] = {"perl",moses_viterbi,config,tag+""};
			String cmd = ""; 
			for(String s: cmdarr){
				cmd+=s+" ";
			}
			int val = ExternalProcRunner.myCommand(cmd, "/tmp/"+tmpfile);
			System.err.println("Executed: "+cmd);
			System.err.println("Return val:"+val);
		}
		
		// Select training data for DTS Classifier 
		System.err.println("Creating Training and Test Data... ");
		DataCreator dataloader = new DataCreator();

		String featfile = mosesFile+".arff";
		dataloader.loadData(mosesFile,refFile,"ARFF",true); // Normalized scores 
		dataloader.writeARFF(featfile);
		
		// Run DTS Classifier on all of the data  and score the sentences
		System.err.println("Running Classifier... ");
		ActiveDTS dts = new ActiveDTS(featfile,featfile); 
		Instances allSentences = new Instances(dts.trainData,0,dts.trainData.numInstances());
		
		// Train algorithm only on a subset of the data (SIMULATION)
		// You already have answers for all the data from Meteor computations 
		Instances selTrainData = dts.selectRandom(dts.trainData,MultiAnnotationSelection.BATCH_SIZE);
		//System.err.println(selTrainData.toString());
		
	    dts.train(selTrainData); // Build classifier
	    
		// Run algorithm on all of the trainingData then ! 
		try {     
		    for(int senid=0;senid<allSentences.numInstances(); senid++){
		    	double[] classProbs = dts.cls.distributionForInstance(allSentences.instance(senid));
		    	
		    	// System.err.println(senid+" :: +1="+classProbs[0]+" -1="+classProbs[1]);
				if(!DTSCORES.containsKey(senid)){
					DTSCORES.put(senid, classProbs[1]); // "Difficult to translate" posterior probability  
				}
		    }
		}catch(Exception e){
			System.err.println(e.toString());
		}	
	}
}
