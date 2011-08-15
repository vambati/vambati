package dts;
import java.io.*; 
import java.util.*; 
import java.util.regex.*;

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

public class ClusterSen
{
	// Vector contains a <RuleSet> element
	private Vector<String> clusters;
	Hashtable<String,String> sens;

	private int clusterIndex = -1; 

	int threshold = 3; 

	public ClusterSen()
	{
		sens = new Hashtable<String, String>(100);
		clusters = new Vector<String>();
	}

	public void clear()
	{
		clusters.clear();
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

		for (String key: sens.keySet()) 
		{
			String feats = sens.get(key);
			double len = key.split("\\s+").length;
			
			String[] tokens = feats.split("\\s+");

			// Create empty instance 
			Instance inst = new Instance(13);
			// Provide this instance access to the dataset 
			inst.setDataset(data); 

			for(int k=0;k<tokens.length;k++)
			{
				Double val = new Double(tokens[k]);
				val = val/ len; 
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
		for(String src:  sens.keySet()) 
		{
			double clusterID = assignments[dataIndex];
			System.out.println(clusterID+"\t"+src+"\t"+sens.get(src));
			dataIndex++;
		}
	}

	public void loadData(String filename) {

		try {
			BufferedReader or = new BufferedReader(new FileReader(filename));
			int i=0,j=0; 
			String trans=""; String tgt = "";

			while((trans = or.readLine()) != null){
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
				//System.out.println(hyp+"\n"+featString+"\n---------");	

				boolean normalized = true;
				String featString_norm = "";
				String[] scoreArrNorm = featString.split("\\s+");
				if(normalized==true){
					for(String num: scoreArrNorm){
						double val = Double.parseDouble(num);
						featString_norm+=(val/length)+" ";
					}
					sens.put(hyp,featString_norm);
				}else{
					sens.put(hyp,featString);	
				} 
			}	
			or.close(); 
		}catch(Exception e){
			System.err.println(e.toString());
		}
	}


	public static void main(String args[]) throws Exception{
		System.out.println("Clustering the seeds... ");
		ClusterSen clusterRS = new ClusterSen();

		clusterRS.loadData(args[0]);
		clusterRS.cluster();

		System.out.println("Clustering complete "); 
	}
}