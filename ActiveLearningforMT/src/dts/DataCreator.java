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

public class DataCreator
{
	Hashtable<Integer,String> sens;
	TranslationValidator mtScorer = null; 

	public DataCreator()
	{
		sens = new Hashtable<Integer, String>(100);
		mtScorer = new TranslationValidator(); 	
	}


	// This method clusters the ruleset based only on the Constituent Sequence or POS sequence 
	public void cluster() throws Exception
	{
		// Creating Nominal Attributes (Number of clusters) 
		Attribute tm1 = new Attribute("tm1");
		Attribute tm2 = new Attribute("tm2"); 
		Attribute tm3 = new Attribute("tm3"); 
		Attribute tm4 = new Attribute("tm4");
		Attribute tm5 = new Attribute("tm5");

		Attribute lm = new Attribute("lm");
		
		Attribute dm1 = new Attribute("dm1"); 
		Attribute dm2 = new Attribute("dm2");
		Attribute dm3 = new Attribute("dm3");
		Attribute dm4 = new Attribute("dm4");
		Attribute dm5 = new Attribute("dm5");
		Attribute dm6 = new Attribute("dm6");
		Attribute dm7 = new Attribute("dm7");

		// Creating a Vector of Nominal Attributes 
		FastVector attrInfo = new FastVector();
		attrInfo.addElement(dm1); attrInfo.addElement(dm2); attrInfo.addElement(dm3); attrInfo.addElement(dm4);
		attrInfo.addElement(dm5); attrInfo.addElement(dm6); attrInfo.addElement(dm7);
		attrInfo.addElement(lm);
		attrInfo.addElement(tm1); attrInfo.addElement(tm2); attrInfo.addElement(tm3); attrInfo.addElement(tm4); attrInfo.addElement(tm5);

		// Creating an instance set of DATA 
		Instances data = new Instances("sens",attrInfo,13); 

		for (Integer key: sens.keySet()) 
		{
			String feats = sens.get(key);
			
			
			String[] tokens = feats.split("\\s+");

			// Create empty instance 
			Instance inst = new Instance(13);
			// Provide this instance access to the dataset 
			inst.setDataset(data); 

			for(int k=0;k<tokens.length;k++)
			{
				Double val = new Double(tokens[k]);
			
				Attribute attr = data.attribute(k);
				inst.setValue(attr,val);
			} 
			data.add(inst);
		}
		System.err.println("Created data set for clustering");

		// WEKA CLUSTERING 
		EM cAlgorithm;
		System.out.println("\n--> cluster");
		String[] options = new String[2];
		options[0] = "-I";                 // max. iterations
		options[1] = "100";
		cAlgorithm = new EM();
		cAlgorithm.setOptions(options);
		// Manually set cluster number from OPTIONS

		int numofClusters = 6;
		cAlgorithm.setNumClusters(numofClusters); 
		cAlgorithm.buildClusterer(data);

		ClusterEvaluation eval;
		eval = new ClusterEvaluation();
		eval.setClusterer(cAlgorithm);
		eval.evaluateClusterer(new Instances(data));
		int num = eval.getNumClusters(); 
		System.out.println (num+" clusters. Done <--\n");

		// Use these assignments and populate the Version Spaces now 
		double[] assignments = eval.getClusterAssignments();

		// From the cluster assignment add the Rules to each Cluster
		int dataIndex=0;
		for(Integer src:  sens.keySet()) 
		{
			double clusterID = assignments[dataIndex];
			System.out.println(clusterID+"\t"+src+"\t"+sens.get(src));
			dataIndex++;
		}
	}

	public void loadData(String filename, String goldstandard,String format,boolean normalized) {

		try {
			BufferedReader or = new BufferedReader(new FileReader(filename));
			BufferedReader gsr = new BufferedReader(new FileReader(goldstandard));
			  
			String trans=""; String tgt = "";
			int id=0;
			while((trans = or.readLine()) != null)
			{
				// Store all 9 features in these 
				String featString = ""; 

				// trans is from MOSES nbest fromat n=1
				String transArr[] = trans.split(" \\|\\|\\| ");
				String hyp="",featscore=""; 
				double hypscore = -1.0,pscore1=0,pscore2=0,lscore1 = 0,lscore2=0,lmscore=0;
				double length = 0; 

				if(transArr.length==4){
					hyp = transArr[1];
					featscore =transArr[2]; 
					hypscore = Double.parseDouble(transArr[3]);
					String[] scoreArr = featscore.split("(\\w)+: ");
					String d = scoreArr[1];

					String lm = scoreArr[2];

					String tm = scoreArr[3];
					featString = d+" "+lm+" "+tm; 

					String[] tmArr = tm.split("\\s+");
					// TM Scores individual 
					pscore1 = Double.parseDouble(tmArr[0]);
					pscore2 = Double.parseDouble(tmArr[2]);
					lscore1 = Double.parseDouble(tmArr[1]);
					lscore2 = Double.parseDouble(tmArr[3]);

					String len = scoreArr[4];
					length = Double.parseDouble(len);
					length = length *-1;
					
				}else{
					System.err.println("ERROR:"+trans);
					System.exit(0);
				}
				hyp = MyNLP.removePunctuation(hyp);
 	
				// Gold standard English translation
				// Only two classes 
				String classLabel = "";
				tgt = gsr.readLine(); 
				double score = mtScorer.score(hyp,tgt);
				
				if(score>0.85){
					classLabel = "+1";
				}else if(score>0.7 && score<=0.85){
					classLabel = "+1"; 
				}else if(score>0.5 && score<=0.7){
					classLabel = "+1";
				}else if(score>0.3 && score<=0.5){
					classLabel = "-1";
				}else if(score <= 0.3){
					classLabel = "-1";
				}
  				
				// Feature String 
				String featString_svm = "";
				String[] scoreArrNorm = featString.split("\\s+");
				
				for(int i=0;i<scoreArrNorm.length;i++){
					int index = i+1;
					String num = scoreArrNorm[i];
					double val = Double.parseDouble(num);
					// Normalization  
					if(normalized==true){
						val = val / (double)length; 
					}
					// Format
					if(format.equalsIgnoreCase("svm")){
						featString_svm+=index+":"+val+" ";
					}else if(format.equalsIgnoreCase("arff")){
						featString_svm+=val+",";
					}
				}
 				
				// Add class label to instance 
				if(format.equalsIgnoreCase("arff")){
					featString_svm+=classLabel;
				}else if(format.equalsIgnoreCase("svm")){
					featString_svm =classLabel+" "+featString_svm;
				}	
				
				sens.put(id,featString_svm);
				id++;
			}
			or.close();
			gsr.close(); 
		}catch(Exception e){
			System.err.println(e.toString());
		}
	}

	private void writeSVM(String file) throws Exception {
		BufferedWriter bw=null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			for(int i=0;i<sens.size();i++){
				bw.write(sens.get(i)+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		bw.flush();
		bw.close();
	}

	public void writeARFF(String file) throws Exception {
		BufferedWriter bw=null;
		bw = new BufferedWriter(new FileWriter(file));
 			
		bw.write("@RELATION DTS\n");
		bw.write("\n");
		bw.write("@ATTRIBUTE TM1 NUMERIC\n");
		bw.write("@ATTRIBUTE TM2 NUMERIC\n");
		bw.write("@ATTRIBUTE TM3 NUMERIC\n");
		bw.write("@ATTRIBUTE TM4 NUMERIC\n");
		bw.write("@ATTRIBUTE TM5 NUMERIC\n");
		
		bw.write("@ATTRIBUTE LM NUMERIC\n");
		
		bw.write("@ATTRIBUTE DM1 NUMERIC\n");
		bw.write("@ATTRIBUTE DM2 NUMERIC\n");
		bw.write("@ATTRIBUTE DM3 NUMERIC\n");
		bw.write("@ATTRIBUTE DM4 NUMERIC\n");
		bw.write("@ATTRIBUTE DM5 NUMERIC\n");
		bw.write("@ATTRIBUTE DM6 NUMERIC\n");
		bw.write("@ATTRIBUTE DM7 NUMERIC\n");
		bw.write("@ATTRIBUTE CLASS {+1,-1}\n");
		bw.write("\n");
		bw.write("@DATA\n");
 		
			for(int i=0;i<sens.size();i++){
				String feats = sens.get(i);
				feats.replaceAll("\\s+",",");
				bw.write(feats+"\n");
			}
 		bw.flush();
		bw.close();
	}
		
	public static void main(String args[]) throws Exception{
		if(args.length!=5){
			System.err.println("Usage: java <HYP> <REF> <format: SVM|ARFF> <OUT_FILE> <normalized:false|true> \n");
			System.exit(0);
		}
		System.out.println("Creating Data... ");
		DataCreator dataloader = new DataCreator();

		String hypfile = args[0];
		String reffile = args[1];
		String featfile = args[2];
		String format = args[3];
		boolean normalized = Boolean.parseBoolean(args[2]);
		
		dataloader.loadData(hypfile,reffile,format,normalized); // Normalized scores 
		
		if(format.equalsIgnoreCase("svm")){
			dataloader.writeSVM(featfile);			 
		}else if(format.equalsIgnoreCase("arff")){
			dataloader.writeARFF(featfile);	 
		}	
	}
}