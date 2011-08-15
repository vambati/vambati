package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Vector;

public class CorpusData {

	public static String logFile = "";
	public Hashtable<Integer,TranslationEntry> data;
	
	public CorpusData(String corpusFile) {
		this.logFile = corpusFile; 
		
		data = new Hashtable<Integer, TranslationEntry>(10000); 
		
		// Load from the log file  
			System.err.println("Corpus File: "+corpusFile);
			try{ 		
				BufferedReader sr = new BufferedReader(new FileReader(corpusFile));
				String line="",src = "",tgt="";
				int count=0,id=-1;
				while((line= sr.readLine()) != null){
					// IMPORTANT 
					line = line.toLowerCase(); 
					
					String[] lineArr = line.split("\\t");
					id = Integer.parseInt(lineArr[0]);
					src = lineArr[1]; // Source sent
					tgt = lineArr[2];  // Target sent
					int round = Integer.parseInt(lineArr[3]); // Round stamp
					
					TranslationEntry e = new TranslationEntry(id,src,tgt);
					e.setRound(round);
					data.put(id,e);
					count++;
				}
				sr.close(); 
				System.err.println("Total Data:"+count);
			} catch (Exception e) { e.printStackTrace();}
	}
	
	public void addEntry(TranslationEntry e,int round){
		e.setRound(round);
		data.put(e.senid, e);
	}

	public Hashtable<Integer,TranslationEntry> getSelected(int round){
		Hashtable<Integer,TranslationEntry> subset = new Hashtable<Integer, TranslationEntry>();
		for(Integer i:data.keySet()){
			TranslationEntry te = data.get(i);
			if(te.round==round){
				subset.put(i, te);
			}
		}
		System.err.println("Selected Data:"+subset.size());
		return subset; 
	}

	
	public Hashtable<Integer,TranslationEntry> getLabeled(int round){
		Hashtable<Integer,TranslationEntry> subset = new Hashtable<Integer, TranslationEntry>();
		for(Integer i:data.keySet()){
			TranslationEntry te = data.get(i);
			if(te.round>=0 && te.round<=round){
				subset.put(i, te);
			}
		}
		System.err.println("Labeled Data:"+subset.size());
		return subset; 
	}

	public Hashtable<Integer,TranslationEntry> getUnLabeled(int round){
		Hashtable<Integer,TranslationEntry> subset = new Hashtable<Integer, TranslationEntry>();
		
		int pos = data.size(); 
		for(Integer i:data.keySet()){
			TranslationEntry te = data.get(i);
			if(te.round==-1){
				te.position_unlabeled = pos;
				pos--;
				subset.put(i, te);
			}
		}
		System.err.println("Unlabeled Data:"+subset.size());
		return subset;		
	}
	
	public void setRound(Vector<Integer> selectedIds, int round){
		for(Integer i:selectedIds){
			TranslationEntry te = data.get(i);
			if(te.round==-1){
				te.round = round;
			}else{
				System.err.println("Error: Round already set");
				//System.exit(0);
			}
	  }
	}

	// Only clear a particular round 
	public void clearRound(int tag){
		for(Integer i:data.keySet()){
			TranslationEntry te = data.get(i);
			if(te.round==tag){
				te.round = -1;
			}
	  }
	}

	public void clearRound(){
		for(Integer i:data.keySet()){
			TranslationEntry te = data.get(i);
				te.round = -1;
	  }
	}
	
	public void updateLog(){
		try {
			BufferedWriter cWriter = new BufferedWriter(new FileWriter(logFile));
			for(Integer i:data.keySet()){
				TranslationEntry te = data.get(i);
				cWriter.write(te.senid+"\t"+te.source+"\t"+te.target+"\t"+te.round+"\n");
			}
			cWriter.flush(); cWriter.close(); 
		}catch(Exception e){
			System.err.println(e.toString());
		}
	}

	public void writeLabeled(String stag, String ttag, int round,Vector<TranslationEntry> phrases){
		String sSD = stag+".l."+round;
		String tSD = ttag+".l."+round;

		int prev_round = round - 1; 
		
		int count=0;
		//Print to files
		try {
		BufferedWriter sWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sSD),"UTF-8")); 
		BufferedWriter tWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tSD),"UTF-8"));
		  
		// Write sentences till previous round 
		for(Integer i:data.keySet()){
			TranslationEntry te = data.get(i);
			if(te.round!=-1 && te.round<=prev_round){
				sWriter.write(te.source+"\n");
				tWriter.write(te.target+"\n");
				count++;
			}
		}
		// Write phrases for current round 
		for(TranslationEntry te: phrases){
			sWriter.write(te.source+"\n");
			tWriter.write(te.target+"\n");			
		}
		
		sWriter.flush(); sWriter.close();
		tWriter.flush(); tWriter.close();
		}catch(Exception e){}	
		System.err.println("Wrote labeled Data:"+count);
	}
	
	public void writeLabeledBudget(String stag,String ttag,int tag,Vector<TranslationEntry> pq){
		String sSD = stag+".l."+tag;
		String tSD = ttag+".l."+tag; 
		
		//Print to files
		try {
		BufferedWriter sWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sSD),"UTF-8")); 
		BufferedWriter tWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tSD),"UTF-8"));
		 
		
		for(TranslationEntry te: pq){
			sWriter.write(te.source+"\n");
			tWriter.write(te.target+"\n");			
		}
		
		sWriter.flush(); sWriter.close();
		tWriter.flush(); tWriter.close();
		}catch(Exception e){}	
	}
	
	public void writeLabeled(String stag, String ttag, int round){
		String sSD = stag+".l."+round;
		String tSD = ttag+".l."+round;

		int count=0;
		//Print to files
		try {
		BufferedWriter sWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sSD),"UTF-8")); 
		BufferedWriter tWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tSD),"UTF-8"));
		 
		 
		for(Integer i:data.keySet()){
			TranslationEntry te = data.get(i);
			if(te.round!=-1 && te.round<=round){
				sWriter.write(te.source+"\n");
				tWriter.write(te.target+"\n");
				count++;
			}
		}
		sWriter.flush(); sWriter.close();
		tWriter.flush(); tWriter.close();
		}catch(Exception e){}	
		System.err.println("Wrote labeled Data:"+count);
	}

	public void writeSelected(String stag,String ttag,int round,Vector<TranslationEntry> phrases){
		String sSD = stag+".ssd."+round;
		String tSD = ttag+".ssd."+round;

		int prev_round = round -1; 
		
		//Print to files
		try {
		BufferedWriter sWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sSD),"UTF-8")); 
		BufferedWriter tWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tSD),"UTF-8"));
		 
		String log = "selection-log."+round;
		BufferedWriter logWriter = new BufferedWriter(new FileWriter(log));

		for(Integer i: data.keySet()) {
			TranslationEntry te = data.get(i);
			if(te.round==prev_round){
				sWriter.write(te.source+"\n");
				tWriter.write(te.target+"\n");
				logWriter.write(te.senid+"\n");
			}
	  }
		
		for(TranslationEntry te: phrases){
			sWriter.write(te.source+"\n");
			tWriter.write(te.target+"\n");			
		}
		
		sWriter.flush(); sWriter.close();
		tWriter.flush(); tWriter.close();
		logWriter.flush(); logWriter.close();
		}catch(Exception e){}	
	}
	
	public void writeSelectedBudget(String stag,String ttag,int round,Vector<TranslationEntry> pq){
		String sSD = stag+".ssd."+round;
		String tSD = ttag+".ssd."+round;

		//Print to files
		try {

		BufferedWriter sWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sSD),"UTF-8")); 
		BufferedWriter tWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tSD),"UTF-8"));
		 
 
		for(TranslationEntry te: pq){
			sWriter.write(te.source+"\t"+te.score+"\n");
			tWriter.write(te.target+"\n");			
		}
		
		sWriter.flush(); sWriter.close();
		tWriter.flush(); tWriter.close();
		}catch(Exception e){}	
	}

	public void writeSelected(String stag,String ttag,int round){
		String sSD = stag+".ssd."+round;
		String tSD = ttag+".ssd."+round;

		//Print to files
		try {
		BufferedWriter sWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sSD),"UTF-8")); 
		BufferedWriter tWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tSD),"UTF-8"));
		 
		String log = "selection-log."+round;
		BufferedWriter logWriter = new BufferedWriter(new FileWriter(log));

		for(Integer i: data.keySet()) {
			TranslationEntry te = data.get(i);
			if(te.round==round){
				// sWriter.write(te.source+"\n");
				sWriter.write(te.source+"\t"+te.score+"\t"+te.desc+"\n");
				tWriter.write(te.target+"\n");
				logWriter.write(te.senid+"\n");
			}
	  }
		sWriter.flush(); sWriter.close();
		tWriter.flush(); tWriter.close();
		logWriter.flush(); logWriter.close();
		}catch(Exception e){}	
	}

	public void writeUnLabeled(String stag, String ttag, int round) {
		String sSD = stag+".ul."+round;
		String tSD = ttag+".ul."+round;

		int count=0;
		//Print to files
		try {
		BufferedWriter sWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sSD),"UTF-8")); 
		BufferedWriter tWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tSD),"UTF-8"));
		 
		for(Integer i:data.keySet()){
			TranslationEntry te = data.get(i);
			if(te.round==-1 || te.round>round){
				sWriter.write(te.source+"\n");
				tWriter.write(te.target+"\n");
				count++;
			}
		}
		sWriter.flush(); sWriter.close();
		tWriter.flush(); tWriter.close();
		}catch(Exception e){}	
		System.err.println("Wrote Unlabeled Data:"+count);
	}
}