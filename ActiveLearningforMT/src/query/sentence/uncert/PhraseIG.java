package query.sentence.uncert;

import java.util.Iterator;

import data.*;
import model.smt.LexicalEntry;
import model.smt.PhraseTable;


public class PhraseIG extends UncertaintyQuerySelector {
	
	// For purposes of diversity , also load Labeled data
	UDS UL = null;
	LDS L = null;
	
	// Reverse model of phrase table for TGT based partitions 
	PhraseTable MODEL_TGT = null;

	public PhraseIG(String ptableFile){
		// Load ptable by SRC
		super(ptableFile);
		// Load ptable by TGT
		MODEL_TGT = new PhraseTable(ptableFile,true);
		computeIG();
		maxIG();
	}
	
	public void computeIG(){
		// Compute H(T) and H(t/s)
		// First compute entropies
		MODEL.computeEntropy();
		MODEL_TGT.computeEntropy();
				
		for(String src:MODEL.PTABLE.keySet()) {
			LexicalEntry pe = MODEL.PTABLE.get(src); 
			//TODO: Assuming only one target side exists 
			double total_entropy =  pe.getEntropy();
				
			// Compute H(T/s) = sigma_t in T [ freq(t) / #T ] * H(t/s)
			double conditional = 0;
			for(String tgt:pe.translations.keySet()){
				LexicalEntry pe2 = MODEL_TGT.PTABLE.get(tgt);
				//double entropy = pe2.getEntropy();
				double p = pe2.translations.get(src);
				double entropy = -1 * ( p * Math.log(p) + (1-p) * Math.log(1-p+0.00001) );
				//conditional = ((double)pe2.translations.size()/(double)MODEL_TGT.partitions()) * entropy;
				conditional += ((double)pe.translations.size()/(double)MODEL.partitions()) * entropy;
			}
		
			// Compute IG(T/s) = H(T) - H(T/s)
			double ig = total_entropy - conditional; 
			MODEL.PTABLE.get(src).setIG(ig);
			//System.out.println("IG:"+ig);
			
			// Calculate split info for IG Ratio
			double psplit = ((double)pe.translations.size()/MODEL.NUM_PHRASES);
			double split_info = -1 * psplit * Math.log (psplit);
			double ig_ratio = ig / split_info;
			MODEL.PTABLE.get(src).setIGRatio(ig_ratio);
		}
	}

	public double computeScore(Entry ex) {
		TranslationEntry e = (TranslationEntry)ex;
		
		double score = 0;
		Iterator<String> iter = e.PHRASES.iterator(); 
		while(iter.hasNext()){
			String p = iter.next();
			if(MODEL.PTABLE.containsKey(p)){
				score+= MODEL.PTABLE.get(p).getIG();
			}
		}
		// Normalize for Number of phrases possible   
		e.score = score / e.PHRASES.size();
		return e.score;
	}
	
	// Functions over the model
	public void  maxIG(){
		String maxs = "";
		double maxe = 0;
		for(String s: MODEL.PTABLE.keySet()){
			double e= MODEL.PTABLE.get(s).getIGRatio();
			if( e >= maxe){
				maxs = s;
				maxe = e;
			}
		}
		System.out.println("Phrase:"+maxs);
		System.out.println("Max InfoGain:"+maxe);
	}
}