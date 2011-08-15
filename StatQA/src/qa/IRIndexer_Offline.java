package qa;
 
import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;

import features.LexicalWeightFeatureFunction;
 
public class IRIndexer_Offline {
	
	public String qlistFile = "";
	public String vecTAG = ""; // Which vector file to use to load answers (MI, MT etc)  
	
	public IRIndexer_Offline (String file) {
		file= qlistFile; 
	}
	
	public ArrayList<Answer> retrieveAnswers(Question q){		
		ArrayList<Answer> results = new ArrayList<Answer>(10);
		try{
			String qfile = q.qpath;
				
			String ansvec = qfile.replaceAll("\\.qlist",".vec"+vecTAG);		
			BufferedReader ansr = new BufferedReader(new FileReader(ansvec));
			
			String ans = "";
				while((ans = ansr.readLine()) != null){
					String[] arr = ans.split(":");
					int qnum = Integer.parseInt(arr[0]);
					
//						if(id==-1){
//							// Answer string could be NULL ... Check why ? 
//							//System.err.println("Why is this negative:"+ans);
//							continue;
//						}
						if(qnum==q.id) {	
							Answer aObj = new Answer(ans);
							// Sets original string and compute MT featuers  
							aObj.doMore(qfile);
							results.add(aObj);
						}
				}
			ansr.close();
		} catch (Exception e) { e.printStackTrace();}
		return results;
	}
	
	// Take input as question. This also contains path to the VEC file that contains all the answer entries 
	// We retrieve ALL answers that have a qnum thats the same as the question num
	
	// Very slow, as we read the FILE multiple times. 
	// Should only open once and work with it 
	Hashtable<String,ArrayList<Answer>> cacheResults = new Hashtable<String,ArrayList<Answer>>(10);
	
	public ArrayList<Answer> retrieveAnswers_withCache(Question q){		
		ArrayList<Answer> results = new ArrayList<Answer>(10);
		try{
			String qfile = q.qpath;
			String qOrig = q.qOriginal; 
				
			if(! cacheResults.containsKey(qfile)){
				String ansvec = qfile.replaceAll("\\.qlist",".vec"+vecTAG);		
				BufferedReader ansr = new BufferedReader(new FileReader(ansvec));
			
				ArrayList<Answer> allans = new ArrayList<Answer>();
					String ans = "";
					while((ans = ansr.readLine()) != null){
						Answer aObj = new Answer(ans);
						int id = aObj.id; 
 							if(id==-1){
								// Answer string could be NULL ... Check why ? 
 								//System.err.println("Why is this negative:"+ans);
 								continue;
							}

							// Sets original string and compute MT featuers  
							aObj.doMore(qfile);
							allans.add(aObj);
					}
				cacheResults.put(qfile,allans);
				ansr.close();
			}
			 
			for(Answer aObj: cacheResults.get(qfile)){
				String[] arr = aObj.ans.split(":");
				int qnum = Integer.parseInt(arr[0]);
				// Only load those entries from VEC file that start with question number 
				if(qnum==q.id) {	
					results.add(aObj);
				}
			}
		} catch (Exception e) { e.printStackTrace();}
		return results;
	}
	
	// Manual garbage collection 
	public void free(){
		cacheResults = null; 
	}
}