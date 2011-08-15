package query.sentence.density;

import java.util.*;

import data.*;


/*
 * Gholamreza Haffari , Maxim Roy and Anoop Sarkar 2008 - ACL
 * Geomarith method of selecting sentences
 * 
 * This is a slight variation of their model. Lets see how it works 
 */

public class HafSarkar extends DensityQuerySelector {	
	// For purposes of diversity , also load Labeled data
	LDS L = null;
	
	public HafSarkar(String ngFile,LDS l){
		super(ngFile);
		L = l;
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double ulscore = 0;
		double lscore = 0;
		Iterator<String> iter = e.PHRASES.iterator(); 
		while(iter.hasNext()){
			String p = iter.next();
			if(NGRAMS.containsKey(p)){
				ulscore+=NGRAMS.get(p) / TOTAL_NGRAMS;
			}
			if(L.NGRAMS_EXISTING.containsKey(p)){
				lscore+=L.NGRAMS_EXISTING.get(p) / L.TOTAL_NGRAMS_EXISTING;
			}
		}
		e.score = (ulscore - lscore) / e.PHRASES.size();   
		return e.score;
	}
}
