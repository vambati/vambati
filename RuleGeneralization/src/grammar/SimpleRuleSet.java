// TransferRuleSet.java
// (c) by Greg Hanneman.  Written for 10-701.
// Last modified October 23, 2008.


package grammar;

// This visitor class only loads all the rules and provides them as a 'List' which the user can then use in any way he wants
public class SimpleRuleSet extends RuleSet implements RuleVisitor 
{
	int maxFreqCount;

	public boolean action(Rule r)
	{
		if(r.freq > maxFreqCount)
			maxFreqCount = r.freq; 
		
		addRule(r);
		return true;
	}
	
	// Public functions: ////////////////////////////////////////////////////
	
	public int GetMaxRuleFreqCount()
	{
		return maxFreqCount;
	}
	
	public void printStats(){
		System.err.println("Total rules loaded "+getNumRules());
	}
}