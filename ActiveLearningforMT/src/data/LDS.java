package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Iterator;

/*
 * Labeled Dataset
 */
public class LDS {

	public Hashtable<Integer,TranslationEntry> data; 
	
	public Hashtable<String,Integer> NGRAMS_EXISTING = null;
	public double TOTAL_NGRAMS_EXISTING = 0;
	
	
	public LDS(){
		data = new Hashtable<Integer,TranslationEntry>();
		NGRAMS_EXISTING = new Hashtable<String, Integer>();
	}
	
	public LDS(CorpusData cHandle,int round){
		data = new Hashtable<Integer,TranslationEntry>();
		data = cHandle.getLabeled(round);
		
		// Now load Existing Ngrams or Phrases
		NGRAMS_EXISTING = new Hashtable<String, Integer>();
		loadAll();
	}
	
	public void loadAll() {
		for(Integer i: data.keySet()){
			TranslationEntry e = data.get(i);
			Iterator<String> iter = e.PHRASES.iterator(); 
			while(iter.hasNext()){
				String p = iter.next();
				if(!NGRAMS_EXISTING.containsKey(p)){
					NGRAMS_EXISTING.put(p,0);
				}
				NGRAMS_EXISTING.put(p,(NGRAMS_EXISTING.get(p)+1));
				TOTAL_NGRAMS_EXISTING++;
			}
		}
		System.err.println("Loaded all existing ngrams:"+TOTAL_NGRAMS_EXISTING);
	}
	
	public void addEntry(TranslationEntry e){
		//Add to data 
		data.put(e.senid,e);
		
		// Add the phrases 
		Iterator<String> iter = e.PHRASES.iterator(); 
		while(iter.hasNext()){
			String p = iter.next();
			if(! NGRAMS_EXISTING.containsKey(p)){
				NGRAMS_EXISTING.put(p,0);
			}
			NGRAMS_EXISTING.put(p,(NGRAMS_EXISTING.get(p)+1));
			TOTAL_NGRAMS_EXISTING++;
		}
	}

	// Add a single phrase to this 
	public void addPhrase(String p){
			if(! NGRAMS_EXISTING.containsKey(p)){
				NGRAMS_EXISTING.put(p,0);
			}
			NGRAMS_EXISTING.put(p,(NGRAMS_EXISTING.get(p)+1));
			TOTAL_NGRAMS_EXISTING++;
	}
}
