package drivers;

import java.io.BufferedReader;
import java.io.FileReader;
import features.LexicalWeightFeatureFunction;
import options.Options;


public class FeatureDriver {
     public static void main(String[] args) throws Exception {
    	 
  	   if(args.length!=1) 
       { 
           System.err.println("Usage: java Trainer <.config> file"); 
           System.exit(0); 
       }       
	     
	     System.err.println("Config path is: "+args[0]); 
        Options opts = new Options(args[0]);
        
		// Training data
		String qFile= opts.get("QFILE");  
		String aFile= opts.get("AFILE");
		 
		LexicalWeightFeatureFunction.sgtlexfile = opts.get("SGTLEX");
		LexicalWeightFeatureFunction.tgslexfile = opts.get("TGSLEX");
		LexicalWeightFeatureFunction.loadLex(); 
		
		BufferedReader qFileR = new BufferedReader(new FileReader(qFile));
		BufferedReader aFileR = new BufferedReader(new FileReader(aFile));
		
		// Question number starts with 0
		 
		String ques=""; String ans="";
		while((ques = qFileR.readLine()) != null){
			ans = aFileR.readLine();
			double sgt = LexicalWeightFeatureFunction.getPhraseProbability_SGT(ques,ans, false);
			double tgs = LexicalWeightFeatureFunction.getPhraseProbability_TGS(ques,ans, false);
			System.out.println("Q:"+ques);
			System.out.println("A:"+ans);
			System.out.println("sgtlex:"+sgt+"\t"+"tgslex:"+tgs+"\n");
		}   	
    }
}