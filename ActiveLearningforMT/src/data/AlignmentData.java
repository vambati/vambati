package data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.zip.GZIPInputStream;

import model.smt.GizaAlignment;

import query.QuerySelector;
/*
 * Labeled Dataset
 */
public class AlignmentData {

	public  Hashtable<Integer,AlignmentEntry> data; 
	// Priority Queue to help do the sorting 
	PriorityQueue<AlignmentEntry> pq = null;
	
	int sennum = 0;int links=0;
	
	public AlignmentData(String src,String tgt,String afile,int offset) {
		System.err.println("Loading GDF alignment data from: "+afile);
		data = new Hashtable<Integer,AlignmentEntry>();
		EntryCompare EC = new EntryCompare();
		pq = new PriorityQueue<AlignmentEntry>(1000,EC.new AlignmentEntryCompare());
		
		loadAlignmentGDA(src,tgt,afile,offset);
		System.err.println("Loaded sens:"+sennum);
		System.err.println("Loaded links:"+links);
	}
	
	public AlignmentData(String gizaFile) {
		System.err.println("Loading GIZA alignment data from: "+gizaFile);
		data = new Hashtable<Integer,AlignmentEntry>();
		EntryCompare EC = new EntryCompare();
		pq = new PriorityQueue<AlignmentEntry>(1000,EC.new AlignmentEntryCompare());
		
		loadAlignmentGIZA(gizaFile);
		
		System.err.println("Loaded sens:"+sennum);
		System.err.println("Loaded links:"+links);
	}
	
	public void loadAlignmentGIZA(String fname) {
		// Reading a GZIP file 
		try { 
		BufferedReader br = new BufferedReader(new InputStreamReader
								(new GZIPInputStream(new FileInputStream(fname)),"UTF8"));
		
		String one = ""; String two =""; String three=""; 
		while((one=br.readLine())!=null){
			two = br.readLine(); 
			three=br.readLine();
			String a = GizaAlignment.giza2gda(one, two, three,0);
			if(a.equals("")){
				System.err.println("WHAT !: Alignment empty ");
			}
			AlignmentEntry aobj = new AlignmentEntry(sennum,one,two,a,0);
			data.put(sennum, aobj);
			sennum++;
			links += aobj.linkcount;
		}
		br.close();
		}catch(Exception e){System.err.println(e.toString());}
	}

	
	public void loadAlignmentGDA(String sname,String tname,String aname,int offset) {
		try { 
		// Reading a GZIP file 
		BufferedReader sbr = new BufferedReader(new InputStreamReader(new FileInputStream(sname)));
		BufferedReader tbr = new BufferedReader(new InputStreamReader(new FileInputStream(tname)));
		BufferedReader abr = new BufferedReader(new InputStreamReader(new FileInputStream(aname)));
		
		String s = ""; String t =""; String a=""; 
		while((s=sbr.readLine())!=null){
			t = tbr.readLine(); 
			a = abr.readLine(); 
			
			AlignmentEntry aobj = new AlignmentEntry(sennum,s,t,a,offset);
			data.put(sennum,aobj);
			sennum++;
			links += aobj.linkcount;
		}
		sbr.close(); 
		tbr.close();
		abr.close();
		}catch(Exception e){System.err.println(e.toString());}
	}
	
	// Compute costs for all entries 
	public void computeCost(QuerySelector qVisitor){
		for(Integer i: data.keySet()){
			 
			if(i%10000==0)
			{
				System.err.print(i+" ");
			}
			try{
				AlignmentEntry ae = data.get(i);
				qVisitor.computeScore(ae);
			}catch(Exception e){
				System.err.println(e.toString());
				System.err.println("Error at:"+i);
				continue;
			}
		}
	}
		 
	public void addEntry(AlignmentEntry e){
		//Add to data 
		data.put(e.senid,e);
	}
	
	// TODO: Should be factored out 
	// Only common to UDS and not to LDS 
	public Integer getTop(){
		pq.clear(); 
		for(Integer i: data.keySet()){
			pq.add(data.get(i));
		}
		AlignmentEntry p = pq.poll();
		return p.senid;
	}
	
	/* Method to sort values in a hashtable */
	public HashSet<Integer> getTopK(int k) {
		pq.clear(); 
		for(Integer i: data.keySet()){
			pq.add(data.get(i));
		}
		 /* Print top k*/		
		 HashSet<Integer> results = new HashSet<Integer>(k);
		 while(k>0){
			 AlignmentEntry p = pq.poll();
			 System.err.println("Retrievining:"+p.score);
			 results.add(p.senid);
			 k--;
		 }
	return results;
	}
	
	public int size(){
		return data.size();
	}
}