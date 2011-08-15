package labels;

import java.util.*;

import utils.MyMath;
import grammar.*;
import features.*;

// This visitor class only loads all the rules and provides them as a 'List' which the user can then use in any way he wants
public class LabelFeatureComputer 
{
	// Compute features for each of the NODE LABELS that exist in the Rule 
	/*
	 * 1. Times it is the PARENT 
	 * - Is the target side PARENT also the same or different 
	 * - Do we want to keep track of the distribution of the target labels it rewrote itself as ?
	 * 2. Times it is in the CHILD LIST 
	 * 3. Relative Position in the Source Side Child List
	 * 4. Relative Position in the Source Target Child List
	 * 5. Was the rule monotonic ? Or non-monotonic 
	 * 6. Were there literal introductions in the rule? 
	 */
		
	List<Rule> grammar;
	Hashtable<String,NodeLabel> labelStats;
	
	public LabelFeatureComputer(List<Rule> grammar)
	{
		// Set params of which features to compute
		this.grammar = grammar;
		labelStats = new Hashtable<String, NodeLabel>();
	}
	
	public  void compute ()
	{
		Iterator<Rule> iter = grammar.iterator();
		while(iter.hasNext()){
			Rule r = iter.next();
			// Compute Features 
			
			String[] sLabels = r.sRHS();
			String[] tLabels = r.tRHS(); 
			
			// Label as Parent
			String plabel = r.sLHS;
			if(!labelStats.containsKey(plabel)){
				labelStats.put(plabel, new NodeLabel(plabel));
			}
			
			NodeLabel pnode = labelStats.get(plabel);
			pnode.isparent++;
			if(RuleFeatureComputer.isMonotonic(r)==1)
				pnode.isparent_monotone++; 
			else		
				pnode.isparent_reorder++; 
			
			// Label as Child 
			for(String childLabel: sLabels){
				// Only if it is a non-terminal in the child list
				if( ! childLabel.startsWith("\"")){		
					if(!labelStats.containsKey(childLabel)){
						labelStats.put(childLabel, new NodeLabel(childLabel));
					}
					NodeLabel cnode = labelStats.get(childLabel);
					cnode.ischild++;
					if(RuleFeatureComputer.isMonotonic(r)==1)
						cnode.ischild_monotone++; 
					else		
						cnode.ischild_reorder++;
				}
 			}
		}
		
		// Compute global stats 
		for(String type:labelStats.keySet()){
			NodeLabel node = labelStats.get(type);
			node.isparent_order_entropy = MyMath.entropy_binary(node.isparent_monotone,node.isparent);
			node.ischild_order_entropy = MyMath.entropy_binary(node.ischild_monotone,node.ischild);
		}
	}
	// Public functions: ////////////////////////////////////////////////////
	
	public void printStats(){
		System.out.println("Total labels:"+labelStats.size());
		for(String type:labelStats.keySet()){
			System.out.println("---------");
			System.out.println(labelStats.get(type).toString());
		}
	}
}