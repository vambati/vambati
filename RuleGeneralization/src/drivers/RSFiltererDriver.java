// TransferRuleSetTest.java
// (c) by Greg Hanneman.  Written for 10-701.
// Last modified October 23, 2008.


package drivers;

import grammar.GrammarReader;
import grammar.FiltereredRuleSet;

public class RSFiltererDriver
{	
	public static void main(String[] args)
	{
			// Check usage:
			if(args.length != 1)
			{
				System.err.println("Usage: java RuleSetFiltererDriver <rule-file>");
				return;
			}
			
			// Read the provided rule file into a TransferRuleSet:
			System.err.print("Reading rule file into TRS... ");
		
			// Or you could load and filter at the same time using Filterer Visitor,
			// Remaining rules are printed to Standard Output 
			System.err.println("Filtering rules");
			FiltereredRuleSet rsf = new FiltereredRuleSet();
			GrammarReader.loadGrammar(args[0],rsf);
			// Print remaining Grammar  to STDOUT
			rsf.print();
			// print stats to STDERR
			rsf.printStats();	
			
	}
}
