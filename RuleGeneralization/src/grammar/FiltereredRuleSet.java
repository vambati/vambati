/*
 * Filter ruleset for the following criteria 
 * 1. Adjacent Non-terminals are not allowed
 * 2. Complete Abstract rules are not allowed 
 * 3. Recursive rules
 * 4. Rules Non-terminal count greater than 'N' 
 */

package grammar;
import java.util.*;
 
public class FiltereredRuleSet extends RuleSet implements RuleVisitor
{	
	// Counters
	int totalRules = 0;
	int pruneCount=0;
	
	int abstractCount=0;
	int ruleOrderCount = 0;
	int adjacentNTCount = 0; 
	int recursiveCount = 0; 
	int unaryCount = 0; 
	int ntIntroCount = 0; 
	
	// Option Flags 
	boolean pruneAbstract = false;
	boolean pruneNTIntro = true;
	boolean pruneAdjacentNT = false;
	boolean pruneUnary = true;
	boolean pruneRecursive = true;
	boolean pruneRuleOrder = false;
	
	public FiltereredRuleSet()
	{
		// may be set the below as options 
	}
	public  void filterRules (List<Rule> grammar)
	{
		Iterator<Rule> iter = grammar.iterator(); 
		while(iter.hasNext()){
			Rule r = iter.next();
			if(action(r) ){
				iter.remove();
			}
		}
	}
	public void printStats()
	{
		System.err.println("---------Pruning Statistics--------");
		System.err.println("Recursive rules removed.."+recursiveCount);
		System.err.println("Unary rules removed.."+unaryCount);
		System.err.println("Abstract rules removed.."+abstractCount);
		System.err.println("NT Introduction rules removed.."+ntIntroCount);
		System.err.println("Adjacent NT Count rules removed.."+adjacentNTCount);
		System.err.println("Rule with rank > 2 removed.."+ruleOrderCount);
		
		System.err.println("Total rules in grammar.."+totalRules);
		System.err.println("Total rules filtered.."+pruneCount);
		System.err.println("Remaining with....  " + (totalRules-pruneCount) + " rules.");
		System.err.println("------------------------------------");
	}
	
	public boolean action(Rule r)
	{	
		totalRules++;
		
		if (pruneAbstract && isAbstractRule(r)){
			pruneCount++; abstractCount++; return true;
		}else if (pruneRecursive && isRecursive(r)){
			pruneCount++; recursiveCount++; return true;
		}else if (pruneUnary && isUnary(r)){
			//System.err.println(r.toString());
			pruneCount++; unaryCount++; return true;
		}else if (pruneAdjacentNT && hasAdjacentNT(r)){
			pruneCount++; adjacentNTCount++; return true;
		}else if (pruneRuleOrder && filterNTCount(r,2)){
			pruneCount++; ruleOrderCount++; return true;
		}else if (pruneNTIntro && hasNTIntroduction(r)){
			pruneCount++; ntIntroCount++; return true;
		}
		// Add it now to the ruleList (super class)
		addRule(r);
		return false;
	}
	
	public boolean isAbstractRule (Rule r)
	{ 
			if(r.isAbstract()){
				return true;
			}
			return false;
	}
	
	public boolean hasAdjacentNT(Rule r)
	{
		List <Integer> sIndex = new ArrayList<Integer>(); 
		
		for(int i=0;i<r.getConstitMap().length;i++){
			sIndex.add(r.getConstitMap()[i]);
		}
		
		Collections.sort(sIndex);
		
		// After sorting, if two consecutive Non-Terminals exist, then both should have alignment links 
		int prev = -3;
		
		for(int i=0;i<sIndex.size();i++){
			int sI = sIndex.get(i);
			
			if(sI<0)
				continue;
			
			// Check if consecutive links (Adjacent Non-terminals) 
			if((prev+1)==sI){
					return true; 
				}
			prev = sI;
		}
		return false;
	}
	
	public boolean isRecursive(Rule r)
	{  
		if(r.sLHS().equals(r.sRHS())){
				return true;
			}
	 return false;
	}
	
	public boolean filterNTCount(Rule r, int order)
	{
		if(r.getSNTCount()>order){
			return false;
		}
		return false;
	}
	
	// Filter unary rules (length = 1 , non-terminal count =1 )
	public boolean isUnary(Rule r)
	{
		if(r.sLength==1 && r.getSNTCount()==1){
			return true;
		}
		return false;
	}
	
	// Filter introduction of Non-terminals on either sides (NTCount != TNTCount 
	public boolean hasNTIntroduction(Rule r)
	{
		if(r.getSNTCount()!=r.getTNTCount()){
			return true;
		}
		return false;
	}
}
