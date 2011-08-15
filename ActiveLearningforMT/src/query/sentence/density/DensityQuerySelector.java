package query.sentence.density;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;

import query.SentenceSelection;
import query.*; 

import utils.MyNLP;

public abstract class DensityQuerySelector implements QuerySelector{

	// Density list of Ngrams from monolingual data
	public Hashtable<String,Integer> NGRAMS = null;
	public double TOTAL_NGRAMS = 0;
	
	public static String stopwordsFile = "";
	
	public DensityQuerySelector(String ngFile){
		NGRAMS = new Hashtable<String,Integer>();
		load(ngFile,stopwordsFile);
	}
	
	public void load(String ngFile,String stopwordsFile){
		System.err.println("Loading Ngram file:"+ngFile);
		MyNLP mynlp = new MyNLP(stopwordsFile);
		try{
		BufferedReader nr = new BufferedReader(new FileReader(ngFile));
		String line = "";
		int i=0;
			while((line = nr.readLine()) != null){
				String arr[] = line.split("\\t", 2);
				int count = Integer.parseInt(arr[0]);
				
				String ngram = arr[1];
				int len = ngram.split("\\s+").length;
				
				// Load only phrases of the MAX LENGTH we use
				//if(len<=SentenceSelection.PHRASE_MAX_LENGTH)
				{
					// Skip stop words for this language
					//if(! mynlp.isStopWord(ngram))
					{
						NGRAMS.put(ngram,count);
						TOTAL_NGRAMS+=count;
						i++;
					}
					//else{
					//	System.err.println("Skipping:"+ngram);
					//}
				}
			}
		nr.close();
		System.err.println("Loaded Ngrams data: "+i);
		}catch(Exception e){System.err.println(e.toString());}
	}
}

