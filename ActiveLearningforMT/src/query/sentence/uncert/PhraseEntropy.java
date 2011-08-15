package query.sentence.uncert;

import java.util.Iterator;

import utils.MyNLP;
import data.*;

public class PhraseEntropy extends UncertaintyQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	UDS UL = null;
	LDS L = null;
	
	public static String stopwordsFile = "";
	MyNLP mynlp = null;
	
	public PhraseEntropy(String ptableFile,UDS u, LDS l){
		super(ptableFile);
		
		mynlp = new MyNLP(stopwordsFile);
		
		UL = u; 
		L = l;
		
		// Compute entropy of all phrases and then start scoring
		double entropy = MODEL.computeEntropy();
		
		System.err.println("Entropy:"+entropy);
		maxEntropy();
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		// H(T|S) = sum_s { P(S=s) * sum_t H(S=s|T=t) }
		double cond_entropy = 0;
		for(String p: e.PHRASES)
		{
			// Skip punctuations etc 
			if(mynlp.isStopWord(p)){
				continue;
			}
			
			double entropy= MODEL.ENTROPY_BACKOFF;
			// Compute Entropy 
			if(MODEL.PTABLE.containsKey(p)){
				entropy = MODEL.PTABLE.get(p).getEntropy();
			}
			
			// Compute probability weight 
			double weight = 1  / UL.TOTAL_NGRAMS_UNLABELED; 
			if(UL.NGRAMS_UNLABELED.containsKey(p)){
				weight = UL.NGRAMS_UNLABELED.get(p)  / UL.TOTAL_NGRAMS_UNLABELED;
			}
			
			cond_entropy += weight * entropy;
		}
		// Normalize for Number of phrases possible   
		e.score = cond_entropy / e.PHRASES.size();

		return e.score;
	}
	
	// Functions over the model
	public void  maxEntropy(){
		String maxs = "";
		double maxe = 0;
		for(String s: MODEL.PTABLE.keySet()){
			double e= MODEL.PTABLE.get(s).getEntropy();
			if( e > maxe){
				maxs = s;
				maxe = e;
			}
		}
		System.err.println("Phrase:"+maxs);
		System.err.println("Max Entropy:"+maxe);
	}
}
