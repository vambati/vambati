package drivers; 
 
import labels.LabelFeatureComputer;
import features.RuleFeatureComputer;
import features.RuleGrouper;
import grammar.*;

/**
 * Rule Generalization Code 
 * @author Vamshi Ambati
 * 10 Dec 2008
 * Carnegie Mellon University
 *
 */

public class LabelDriver 
{
	public static void main(String args[]) throws Exception
	{
		if(args.length==1)
		{
		 	long sTime = System.currentTimeMillis();
		 	System.err.println("Reading rule file into TRS... ");
			SimpleRuleSet rsf = new SimpleRuleSet();
			
			GrammarReader.loadGrammarOneline(args[0],rsf); // ONELINE FORMAT
			//GrammarReader.loadGrammar(args[0],rsf); // AVENUE FORMAT
			rsf.printStats();
			
			// Compute Label features    
			LabelFeatureComputer lc = new LabelFeatureComputer(rsf.getGrammar());
			lc.compute();
			lc.printStats();
		 	
			long eTime = System.currentTimeMillis();
			double secs = (eTime - sTime) / 1000;
			System.err.println("Time Taken: "+secs);
		}
		else
		{
			System.err.println("Usage: java LabelDriver <Oneline format grammar file>");
			System.exit(0);
		}
	}
}