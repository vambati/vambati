/*
 * Filter ruleset for the following criteria 
 * 1. Adjacent Non-terminals are not allowed
 * 2. Complete Abstract rules are not allowed 
 * 3. Recursive rules
 * 4. Rules Non-terminal count greater than 'N' 
 */

package features;

import grammar.*;

import java.util.*;
 /**
  * @author Vamshi Ambati
  *
  * Binary Features to be computed are - 
  * - isAbstract ?
  * - isLexical ? 
  * - isMonotonic ? 
  * - same or different Entries (nonterminal / terminal) ? 
  * - introducesLexicalEntries? 
  * 
  * Real Features to be computed are - 
  * - P(s/t) 
  * - 
  */
public class RuleFeatureComputer
{	
	List<Rule> grammar;
	public RuleFeatureComputer(List<Rule> grammar)
	{
		// Set params of which features to compute
		this.grammar = grammar;
	}
	
	public  void compute ()
	{
		Iterator<Rule> iter = grammar.iterator(); 
		while(iter.hasNext()){
			Rule r = iter.next();
			// Compute Features 
			r.feats.addFeatureValue("isAbstract", isAbstract(r));
			r.feats.addFeatureValue("isMonotonic", isMonotonic(r));
			r.feats.addFeatureValue("hasTLiterals", hasTLiterals(r));
			r.feats.addFeatureValue("isTLonger", isTLonger(r));
		}
	}
	// Is it a completely abstract rule 
	public static int isAbstract(Rule r)
	{ 
			if(r.isAbstract()){
				return 1;
			}
			return 0;
	}
	// Is it a completely lexical rule (length of NT entries == number of literals )
	public static int isLexical(Rule r)
	{ 
			if(r.sLength==r.sLITCount){
				return 1;
			}
			return 0;
	}
	
	// Is TARGET side of the rule monotonic or re-ordered w.r.t source side  
	public static int isMonotonic(Rule r)
	{
		int[] sIndex = r.getConstitMap();
		
		int prev = -3; 
		for(int i=0;i<sIndex.length;i++){
			int sI = sIndex[i];
			if(sI==-1)
				continue;
			
			if(prev>sI){
				return 0;
			}
			prev = sI;
		}
		return 1;
	}
	
	// Are more literals introduced on TARGET side 
	public  static int isTLonger(Rule r)
	{
		if((r.tLITCount-r.sLITCount)>0) {
			return 1;
		}
		return 0;
	}
	// Does it have literals on TARGET side of rule 
	public static int hasTLiterals(Rule r)
	{
		if(r.tLITCount>0) {
			return 1;
		}
		return 0;
	}
	
	public void print()
	{
		int total=0;
		Iterator<Rule> iter = grammar.iterator(); 
		while(iter.hasNext()){
			Rule r = iter.next();
				System.out.println(r.toOneline());
				total++;
		}
		System.err.println("Total rules:"+total);
	}
}
