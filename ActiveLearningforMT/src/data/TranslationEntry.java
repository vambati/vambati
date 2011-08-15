package data;

import java.util.*;

import query.SentenceSelection;

import utils.StringUtils;

public class TranslationEntry extends Entry {	
	// Existing phrases on the source side (UNIQUE Phrases - may be dangerous!!) 
	//public Hashtable<String,Integer> PHRASES = null;  

	// Existing phrases on the source side 
	public HashMap<String,Double> PHRASESCORES = null;
	public Vector<String> PHRASES = null;
	public int phrasecount = 0;
	 
	public TranslationEntry(int i, String src, String tgt) {
		
		super(i,src,tgt);
		// PHRASESCORES = StringUtils.allPhrases(source,SentenceSelection.PHRASE_MAX_LENGTH);
		PHRASES = StringUtils.allPhrases(source,SentenceSelection.PHRASE_MAX_LENGTH);
	}
	
	public String toString(){
		String str= senid+" ||| "+source+" ||| "+target+" ||| "+score+" ||| "+cost;
		//str+="\nPhrasecount:"+PHRASES.size()+"\n----\n";
		return str;
	}
}
