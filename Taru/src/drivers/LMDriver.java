package drivers;

import java.io.InputStreamReader;

import utils.IOTools;
import utils.lm.*;

/** 
 * First param: ARPA format language model file
 *  */

public class LMDriver
{
	public static void main(String[] args) throws Exception
	{
		//String lmFile = args[0];
		String lmFile = "/Users/vamshi/Desktop/europarl.srilm";
		String test = "The quick brown fox jumped over the lazy dog";
		
		VocabularyBackOffLM lm = new VocabularyBackOffLM(false);
		System.out.println("Loading LM...");
		lm.loadArpa(new InputStreamReader(IOTools.getInputStream(lmFile)));
		System.out.println("LM loaded");
		
		double prob = LMTools.getSentenceLogProbability(test,lm, 0);
		System.out.println("Score of "+test+ " is: "+prob);
	}
}
