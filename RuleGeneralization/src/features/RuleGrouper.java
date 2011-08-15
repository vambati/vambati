
package features;

import grammar.Rule;

import java.util.*;

import utils.MyMath;
  
public class RuleGrouper
{	
	int flag = 4;
	// Grammar grouped by certain STRING - src, tgt etc
	Hashtable<String,List<Rule>> grammar;
	// Features for each of the group, co-indexed by the same STRING as above 
	Hashtable<String,Hashtable<String,Double>> groupFeats;
	
	public RuleGrouper(List<Rule> rules)
	{
		grammar = new Hashtable<String, List<Rule>>();
		groupFeats = new Hashtable<String,Hashtable<String,Double>>();
		// group the given rules into this 
		group(rules);
		computeFeatures();
	}
	
	// Take a ruleList and load it into a grammar by some criteria of GROUPING 
	public  void group (List<Rule> ruleList)
	{
		Iterator<Rule> iter = ruleList.iterator(); 
		while(iter.hasNext()){
			Rule r = iter.next();
			// Group now 
			String key = "";
			
			if(flag==1) {
				key = r.sLHS+"\t"+r.sRHS;  // Node type + Source side of rule
			}
			else if(flag==2) {
				key = r.sLHS+"\t"+r.tRHS;  // Node type + Target side of rule
			}else if(flag==3){
				key = r.sLHS;  // Node type 
			}else if (flag==4){
				key = r.sRHS;  // Source side of rule
			}else if (flag==5){
				key = r.tRHS;  // Target side of rule
			}
			
			if(!grammar.containsKey(key)) {
				grammar.put(key,new ArrayList<Rule>());
			}
			grammar.get(key).add(r);
		}
		System.err.println("Grouping of rules done");
	}
	
	public void computeFeatures(){
		for(String key: grammar.keySet()){
 
			if(!groupFeats.containsKey(key)){
				groupFeats.put(key, new Hashtable<String,Double>());
			}
			 // Compute count based features for entire Grammar 
		
			List<Rule> ruleList = grammar.get(key);
			
			double total = ruleList.size();
			double abstractCount = 0;
			double monotoneCount = 0;
			double literalIntroCount = 0;
			
			Iterator<Rule> iter = ruleList.iterator(); 
			while(iter.hasNext()){
				Rule r = iter.next();
				abstractCount+=Integer.parseInt(r.feats.getFeatureValue("isAbstract")); 
				monotoneCount+=Integer.parseInt(r.feats.getFeatureValue("isMonotonic")); 
				literalIntroCount+=Integer.parseInt(r.feats.getFeatureValue("hasTLiterals")); 
			}	
		 groupFeats.get(key).put("total",total);
		 groupFeats.get(key).put("monotonic",monotoneCount);
		 //groupFeats.get(key).put("literalIntro",literalIntroCount);

		// TODO: Compute Entropy - How do I combine both entropies via a JOINT entropy? 
		 // Are these independent or dependent features?
		 groupFeats.get(key).put("reordering_entropy",MyMath.entropy_binary(monotoneCount,total));
		 groupFeats.get(key).put("literalintro_entropy",MyMath.entropy_binary(literalIntroCount,total));
		}
		System.err.println("Cumulative feature compuation of Groups done");
	}

	// print the group in some meaningful Format 
	// Anything interesting ?? 
	public void print()
	{
		int total=grammar.keySet().size();
		System.out.println("Total groups:"+total);
		
		for(String key: grammar.keySet()){
			int size = grammar.get(key).size();
			if(size<2)
				continue; 
			System.out.print(key+":\t");
			// Print features about the Group
			Hashtable<String,Double> featTable = groupFeats.get(key);
			
			for(String featname:featTable.keySet()){
				System.out.print(featTable.get(featname)+"\t");
				//System.out.print(featname+":"+featTable.get(featname)+"\t");
				//System.out.print(featTable.get(featname)+"\t");
			}
			System.out.println("");
			 
			// Print all rules target sides contained in this GROUP 
			Iterator<Rule> iter = grammar.get(key).iterator();
				while(iter.hasNext()){
					Rule r = iter.next();
					System.out.println("\t"+r.id+"\t["+r.sRHS+"] -> ["+r.tRHS+"]\t"+r.feats.toString());
				}
		}
	}
}
