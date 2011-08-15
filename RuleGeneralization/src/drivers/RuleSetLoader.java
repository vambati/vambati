// TransferRuleSetTest.java
// (c) by Greg Hanneman.  Written for 10-701.
// Last modified October 23, 2008.


package drivers;

import grammar.GrammarReader;
import grammar.Rule;
import grammar.SimpleRuleSet;

import java.util.List;
import java.util.Random;

public class RuleSetLoader
{	
	public static void main(String[] args)
	{
		// Check usage:
		if(args.length != 1)
		{
			System.err.println("Usage: java RuleSetTest <rule-file>");
			return;
		}

		// You could just load the rules using the Aggregator Visitor 
		SimpleRuleSet ruleSet = new SimpleRuleSet();
		GrammarReader.loadGrammar(args[0],ruleSet);
		List<Rule> grammar = ruleSet.getGrammar();
		System.err.print("Loading....\n");
		int numRules = ruleSet.getNumRules();
		System.err.println("Done " + numRules + " rules.");
		System.out.println("Highest frequency count: " +
				           ruleSet.GetMaxRuleFreqCount() + "\n");
		
		// Sample some of the output:
		Random rand = new Random();
		for(int i = 0; i < 5; i++)
		{
			int r = rand.nextInt(numRules);
			Rule thisRule = ruleSet.getRuleAt(r);
			System.out.println("Rule " + (r+1) + ": ------------------------");
			System.out.print("Src: " + thisRule.sLHS() + " -> ");
			for(String s : thisRule.sRHS())
				System.out.print(s + " ");
			System.out.print("\nTgt: " + thisRule.tLHS() + " -> ");
			for(String s : thisRule.tRHS())
				System.out.print(s + " ");
			System.out.println("\nScores: " + thisRule.GetSGTScore() +
					" and " + thisRule.GetTGSScore());
			System.out.println("Count: " + thisRule.GetFrequency());
			System.out.print("Constits: ");
			for(int m : thisRule.GetConstituentMap())
				System.out.print(m + " ");
			System.out.print("\n\n");
		}
		
	}


}
