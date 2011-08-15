package drivers; 
 
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

public class SystemMain 
{
	public static void main(String args[]) throws Exception
	{
		if(args.length==1)
		{
			//System.out.println("Config path is: "+args[0]);
			//Options opts = new Options(args[0]); 

		 	long sTime = System.currentTimeMillis();
		 	//RuleGen rg = new RuleGen(opts);
			
			// Read the provided rule file into a TransferRuleSet:
			System.err.println("Reading rule file into TRS... ");
		
			// Or you could load and filter at the same time using Filterer Visitor,
			// Remaining rules are printed to Standard Output 
			SimpleRuleSet rsf = new SimpleRuleSet();
			//FiltereredRuleSet rsf = new FiltereredRuleSet();
			
			 GrammarReader.loadGrammarOneline(args[0],rsf); // Can also load with Context Based Features 
			// GrammarReader.loadGrammar(args[0],rsf); // Can not load with Context Based features (AVE format)
			
			rsf.printStats();
			//rsf.print();
			
			// Compute features   
			RuleFeatureComputer fc = new RuleFeatureComputer(rsf.getGrammar());
			fc.compute();
			//fc.print();
			
			// Perform Grouping of rules to get Statistics 
			RuleGrouper rgrouper = new RuleGrouper(rsf.getGrammar());
			rgrouper.print();
		 	
			long eTime = System.currentTimeMillis();
			double secs = (eTime - sTime) / 1000;
			System.err.println("Time Taken: "+secs);
		}
		else
		{
			System.err.println("Usage: java RuleGenDriver <.config file>");
			System.exit(0);
		}
	}
}