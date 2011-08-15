// TransferRuleSet.java
// (c) by Greg Hanneman.  Written for 10-701.
// Last modified October 23, 2008.


package grammar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RuleSet   
{
	private List<Rule> ruleList;

	public RuleSet()
	{
		ruleList = new ArrayList<Rule>();
	}
	
	public void setGrammar(List<Rule> grammar){
		ruleList = grammar; 
	}
	public List<Rule> getGrammar(){
		return ruleList; 
	}
	
	public boolean addRule(Rule r)
	{
		ruleList.add(r);
		return true;
	}
	
	public int getNumRules()
	{
		return ruleList.size();
	}
	
	public Rule getRuleAt(int index)
	{
		return ruleList.get(index);
	}
	
	public void print()
	{
		int total=0;
		Iterator<Rule> iter = ruleList.iterator(); 
		while(iter.hasNext()){
			Rule r = iter.next();
				System.out.println(r.toString());
				total++;
		}
		System.err.println("Total rules:"+total);
	}
}