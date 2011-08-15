package drivers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import options.Options;

import qa.*;
import utils.MyNLP;
import ml.*;
import ml.perceptron.*;
import ml.utils.*;

public class QAParamTrainer {
	public static void main(String[] args) throws Exception {
		
	    if(args.length!=1) 
        { 
            System.err.println("Usage: java Trainer <.config> file"); 
            System.exit(0); 
        }
	     
	     System.err.println("Config path is: "+args[0]); 
         Options opts = new Options(args[0]);
         
		// Training data
		String qlistFile = opts.get("TRAIN"); // contains parses
		String qlistTestFile = opts.get("TEST"); // contains plain text
		String vecTAG = "original";
		if(opts.defined("VEC_TAG")){
			vecTAG = "."+opts.get("VEC_TAG"); // which vector file to load (MI, MT, GLS etc) 
		}

		int num_iter = opts.getInt("ITER");
		
		FeatureManager fm = new FeatureManager(opts.get("FEATURE_FREQ"));
		FeatureManager.feat_limit = opts.getInt("FEATURE_FREQ_LIMIT");
		System.err.println(FeatureManager.feat_limit); 
		MyNLP.stopwordFile = opts.get("STOPWORDS");
		MyNLP.load();
		
		//Add Customized FEATURES (to be handled cleaner)
		if(opts.defined("SGTLEX")){
			FeatureManager.loadSMTFeatures(opts.get("SGTLEX"), opts.get("TGSLEX"));
			FeatureManager.appendNewFeature("sgt.MT",new Double(0));
			FeatureManager.appendNewFeature("tgs.MT",new Double(0));
		}
		if(opts.defined("GLS_IN")){
			// Add GLS Based features
			FeatureManager.loadGLSFeatures(opts.get("GLS_IN"), opts.get("GLS_OUT"));
			FeatureManager.appendNewFeature("gls.MT", new Double(0));
		}
	
		if(opts.defined("MI_TABLE")){
			//Add MutualInfo based features 
			FeatureManager.loadMIFeatures(opts.get("MI_TABLE"));
			FeatureManager.appendNewFeature("mi.MT",new Double(0));
		}
		// Read in the training data
		// Mapping table from keys to Integers 
		Hashtable<String,Integer> keytable = new Hashtable<String, Integer>(10000);
		// All questions 
		Hashtable<Integer,Question> questionsData = new Hashtable<Integer,Question>(10000);
		// Corresponding references 
		Hashtable<Integer,Answer> answerRefData = new Hashtable<Integer,Answer>(10000);
		
		BufferedReader qlistr = new BufferedReader(new FileReader(qlistFile));
		
		String qfile = "";
		int qcount = 0;
		int acount = 0;
		int na = 0;
		int qfilecount = 0;
		while((qfile = qlistr.readLine()) != null){
			qfilecount++;
			if(qfilecount%100==0){
				System.err.println("Loaded:"+qfilecount);					
			}else{
				if(qcount%100==0){
					System.err.print(qcount+" ");
					//System.err.print("(weights:"+FeatureManager.feats.size()+")");
				}
			}
			// Load question: 
			BufferedReader qr = new BufferedReader(new FileReader(qfile));
			BufferedReader qrOriginal = new BufferedReader(new FileReader(qfile+".original"));
			String ques = "";String quesOriginal = "";

			// Question number starts with 0
			int qno=0;
			while((ques = qr.readLine()) != null){
				quesOriginal = qrOriginal.readLine();
				
				String key= qfile+":"+qno;
				keytable.put(key, qcount);
				questionsData.put(qcount,new Question(ques,quesOriginal,qfile,qno)); 
			qno++;
			qcount++; // Total q's in all websites
			}
			qr.close();
			qrOriginal.close();
			
			// Load features of Reference from Answer VEC files 
			try{ 
				String ansvec = qfile.replaceAll("\\.qlist",".vec"+vecTAG);		
				BufferedReader ansr = new BufferedReader(new FileReader(ansvec));
				String ans = "";
				while((ans = ansr.readLine()) != null){
					String[] arr = ans.split(":");
					int qnum = Integer.parseInt(arr[0]);
					// Only load the REF entry, answer ID and question ID shud match
					Pattern pattern = Pattern.compile("\\/a([0-9]+).txt");
					Matcher matcher = pattern.matcher(ans);
					
					if(matcher.find()) {
						int id = Integer.parseInt(matcher.group(1));
						if((qnum==id) && (id!=-1)) {
							// Create an answer
							Answer a = new Answer(ans);
							if(a.id!=-1) {
								FeatureManager.appendToModel(a.features);
							}
							
							String key= qfile+":"+qnum;
							int keyID = keytable.get(key);
							
							// Sets original answer string and uses it to compute MT features 
							a.doMore(qfile);
							a.computeExtraFeatures(questionsData.get(keyID).qOriginal,opts);
							
							answerRefData.put(keyID,a);
							acount++;
						}
					}
				}
				ansr.close();
			} catch (Exception e) { e.printStackTrace();}
		}
		System.err.println("Starting statistics:");
		System.err.println("Total Ques:"+qcount);
		System.err.println("Total Refs:"+acount);
		System.err.println("Refs uavailable:"+na);

		// Test one feature at a time  
		//FeatureManager.feats.clear();
		//FeatureManager.appendNewFeature("gls.MT", new Double(0));
		//FeatureManager.appendNewFeature("sgt.MT", new Double(0));
		//FeatureManager.appendNewFeature("tgs.MT", new Double(0));
		//FeatureManager.appendNewFeature("mi.MT", new Double(0));
		
		// Create a perceptron (Algorithm 1) for training and then set it to decoder
		// Initialization method
		QADecoder qdecoder = new QADecoder(qlistFile,FeatureManager.initializeWeights(),vecTAG,opts);
		//System.out.println("-----TESTING:-------");
		
		// Training method 
		Perceptron p = new Perceptron(qdecoder);
		p.trainAverage(questionsData,answerRefData, num_iter, qlistTestFile);

		//qdecoder.readWeights("vweights.txt.3");		
		qdecoder.decodeTest(qlistTestFile);
	}
}
