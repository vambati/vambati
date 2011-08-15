package query.sentence.uncert;

import query.*;
import model.smt.PhraseTable;

public abstract class UncertaintyQuerySelector implements QuerySelector{

	// Density list of Ngrams from monolingual data
	public PhraseTable MODEL = null;
	
	public static String stopwordsFile = "";
		
	public UncertaintyQuerySelector(String ptableFile){
		try{
			MODEL = new PhraseTable(ptableFile);
		}catch(Exception e){}
	}
}
