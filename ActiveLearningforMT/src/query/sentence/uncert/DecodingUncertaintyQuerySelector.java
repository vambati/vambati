package query.sentence.uncert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import data.LDS;

import model.smt.*;
import query.*;
import utils.ExternalProcRunner;

public abstract class DecodingUncertaintyQuerySelector implements QuerySelector{

	// Uncertainty Scores for Unlabled data loaded from Moses output 
	protected Hashtable<Integer,Double> MOSES = null; 
	protected Hashtable<Integer,String> MOSESHYP = null; 
	
	String moses_viterbi = "/home/vamshi/code/scripts/mt-cluster/viterbi-mosesrun.pl";
	
	public DecodingUncertaintyQuerySelector(LDS l,String conf,int tag,String src,String tgt){
		MOSES = new Hashtable<Integer, Double>();
		MOSESHYP = new Hashtable<Integer, String>();
		
		loadMosesScores(conf,tag,src,tgt);
		
	}

	private void loadMosesScores(String config,int tag,String src,String tgt) {
	
		String mosesFile = src+".ul."+tag+".mosesout";
		
		// To have a different ID 
		String fixtag = System.currentTimeMillis()+"";
		String tmpfile = src+".vamshi.moses."+fixtag;
		
		File mosesOutput = new File(mosesFile);
		if(!mosesOutput.exists()){
			// Run moses and create output file first
			 
			String cmdarr[] = {"perl",moses_viterbi,config,tag+""};
			String cmd = ""; 
			for(String s: cmdarr){
				cmd+=s+" ";
			}
			int val = ExternalProcRunner.myCommand(cmd, tmpfile);
			System.err.println("Executed: "+cmd);
			System.err.println("Return val:"+val);
		}
		System.err.println("MOSES FILE should now exist!!!\n");
		System.err.println("Loading Moses file:"+mosesFile);
		try{
		BufferedReader nr = new BufferedReader(new FileReader(mosesFile));
		String line = "";
		int position_num = 1;
			while((line = nr.readLine()) != null){
				//System.err.println(line);

				// MOSES FORMAT 
				// 15 ||| which date is wimbledon ?  ||| d: 0 -1.16168 0 0 -0.987772 0 0 lm: -52.0956 tm: -1.98961 -5.18053 -0.251362 -4.08052 3.99959 w: -5 ||| -309.857
					
				String arr[] = line.split(" \\|\\|\\| ");
				
				// IMPORTANT - MOSES SENNUM starts from ZERO 
				String hyp = arr[1];
				int hyplen = hyp.split("\\s+").length;
				String weights = arr[2];
				double score = Double.parseDouble(arr[3]);
					
				// Normalize and change scale (Should we normalize by hypothesis length ?) 
				score = -1 * score;  
				
				if(!MOSES.containsKey(position_num)){
					MOSES.put(position_num, score);
					MOSESHYP.put(position_num, hyp);
				}
				position_num++;
			}
		}catch(Exception e){System.err.println(e.toString());}	
	}
}
