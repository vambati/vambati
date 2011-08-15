
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Hashtable;

import crowdsource.validate.TranslationValidator;
import data.CorpusData;
import data.UDS;
import model.smt.Dictionary;
import model.smt.PhraseTable;
import model.smt.TranslationLEXICON;
import query.sentence.uncert.*;
import utils.MyNLP;
import utils.StringUtils;

public class Seedlexicon {

	public static int MAX_LENGTH = 3; 
	 
	public static String ptable = "/mnt/1tb/usr6/vamshi/Nist-Urdu09/BASELINES/moses-dict/working-dir/model/phrase-table.0-0.gz";
	  
	public static void main(String args[]) throws Exception {
	
		String file = args[0]; // CorpusData File for selecting most frequent words  
  		
		PhraseTable pt = new PhraseTable(ptable);
		
	    Hashtable<String,Integer> freq = new Hashtable<String, Integer>(); 
	    
			BufferedReader or = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = or.readLine()) != null){
				
				if(pt.PTABLE.containsKey(line)){
					String trans = pt.PTABLE.get(line).getTopTrans();
					System.err.println(line+"\t"+trans);	
				}
			}
	}
}
