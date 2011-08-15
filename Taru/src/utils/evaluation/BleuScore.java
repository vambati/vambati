package utils.evaluation;

import java.util.StringTokenizer;
import java.util.ArrayList;

/** Compute BleuR1N4 score
 * First param: hypothesis file
 * The rest of the params: the reference files
 *  */

public class BleuScore
{
	public BleuR1N4Evaluator evaluator ;
	public BleuSentenceErrorStatistics[] err;
	
	public BleuScore(ArrayList<String> hyps,ArrayList<String> refs) throws Exception
	{
//		System.out.println(hyps.get(0));
//		System.out.println(refs.get(0));
		
		evaluator = new BleuR1N4Evaluator();
		
		Item[][] ref = itemize(refs) ;
		Item[][] hypothesis = itemize(hyps);
		
		if (hypothesis.length != ref.length)
			throw new Exception("Different number of lines between hypothesis and reference");
		
		err = new BleuSentenceErrorStatistics[ref.length];
		for (int i = 0; i < hypothesis.length; i++)
			err[i] = evaluator.getErrorStatistics(ref[i] , hypothesis[i][0]);
	}
	
	public double getScore()
	{
		return evaluator.getScore(err);
	}

	public BleuScore(String hyp, String ref) throws Exception {
//		System.out.println(hyps.get(0));
//		System.out.println(refs.get(0));
		
		evaluator = new BleuR1N4Evaluator();
		
		Item[] refs = itemize(ref) ;
		Item[] hypothesis = itemize(hyp);
		
//		if (hypothesis.length != refs.length)
//			throw new Exception("Different number of lines between hypothesis and reference");
		
		err = new BleuSentenceErrorStatistics[1];
//		for (int i = 0; i < hypothesis.length; i++)
			err[0] = evaluator.getErrorStatistics(refs, hypothesis[0]);
	}

	public double printStats()
	{
		evaluator.printDebug = true;
		return evaluator.getScore(err);
	}
	
	public double getSmoothBleuKlien(){
		return evaluator.getSentenceLevelSmoothBleu(err[0]);
	}
	
	public Item[] itemize(String item) 
	{
		Item[] out = new Item[1];
		int cnt = 0;	
		StringTokenizer st = new StringTokenizer(item);
//		ESentence[] f = new ESentence[1];
		String[] k = new String[ st.countTokens() ];
		for (int i = 0; i < k.length; i++)
			k[i] = st.nextToken().intern();
		out[0] = new ESentence(k, k);
		cnt++;
		return out;
	}
	
	public Item[][] itemize(ArrayList<String> items) 
	{
		ArrayList<Item[]> out = new ArrayList<Item[]>();

		int cnt = 0;	
		for(String lineFile: items) 
		{
			StringTokenizer st = new StringTokenizer(lineFile);
			ESentence[] f = new ESentence[1];
			String[] k = new String[ st.countTokens() ];
			for (int i = 0; i < k.length; i++)
				k[i] = st.nextToken().intern();
			f[0] = new ESentence(k, k);
			out.add(f);
			cnt++;
		}
		return out.toArray(new Item[out.size()][]);
	}
}
