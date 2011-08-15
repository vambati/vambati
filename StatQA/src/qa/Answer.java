package qa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.*;

import options.Options;

import utils.MyNLP;
import utils.StringUtils;

import features.GLSFeatureFunction;
import features.LexicalWeightFeatureFunction;
import features.MIFeatureFunction;
import ml.utils.FeatureManager;
import ml.utils.SparseVector;

public class Answer {

	// whatever comes from the VEC files
	public String ans = ""; 
	public int id = -1;
	public double score = 0.0;
	
	// Actual TEXT in the answer 
	public String aString = ""; 
	
	public String apath = "";
	public SparseVector features = null; 
	
	public Answer(String ref) {
		ans = ref;
	
		String[] arr = ans.split(" +");
		String[] firstbit = arr[0].split(":");
		int qnum = Integer.parseInt(firstbit[0]);
		apath = firstbit[1];
		
		Pattern pattern = Pattern.compile("\\/a([0-9]+).txt");
		Matcher matcher = pattern.matcher(apath);
		
		if(matcher.find()) {
			id = Integer.parseInt(matcher.group(1));
		}
		// Load features from the VEC file
		features = FeatureManager.loadFeatures(ref);
	}
	
	public void doMore(String qfile) throws Exception
	{
		// If id is -1 can't do much more, as we dont have a path to it
		if(id==-1){
			return; 
		}
		// Set the original string 	
		String ansorigFile = qfile.replaceAll(".qlist","\\/a"+id+".txt");
		
		BufferedReader ansorigr = new BufferedReader(new FileReader(ansorigFile));
		// Read all lines 
		String ansOrig = ""; String line="";
		while((line=ansorigr.readLine())!=null){
			ansOrig+=line+" ";
		}
		aString = ansOrig;
		aString = StringUtils.trim(aString);
		
		ansorigr.close();
	}
		
	// Compute extra features that are not loaded from VEC file above.Should be done after ansOrig is computed 
	public void computeExtraFeatures(String qOrig,Options opts){

		if(opts.defined("SGTLEX")){// Machine Translation features - IBM Model 1 
			double sgt = LexicalWeightFeatureFunction.getPhraseProbability_SGT(qOrig,this.aString, false);
			features.set("sgt.MT",sgt);		
			double tgs = LexicalWeightFeatureFunction.getPhraseProbability_TGS(qOrig,this.aString, false);
			features.set("tgs.MT",tgs);
		}
		if(opts.defined("GLS_IN")){ // GLS - global lexical selection
			String qProcess = MyNLP.processString(qOrig);
			double gls = GLSFeatureFunction.getGLSScore(qProcess,this.aString, true);
			features.set("gls.MT",gls);
		}
		if(opts.defined("MI_TABLE")){ // Mutual Info
			double miscore = MIFeatureFunction.getMIScore(qOrig,this.aString, true);
			features.set("mi.MT",miscore);
		}
 	}
	
	public String toString(){
		return this.aString +" Score:"+score+"\n"; 
	}
}

