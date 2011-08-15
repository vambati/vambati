package RuleLearner.extractor;

/**
 * Description: Program Extracts Rules from a Source side parse Tree. All of the Target side information whether
 * 		 tree or string , is embedded inside the source parse tree nodes. So walking source side is sufficient 
 * 	
 * 		Rules extracted are FLATTENED and are not trees, so the program is called 'S 2 S' - String to String rules 
 *  
 * Author: Vamshi Ambati
 * 12 Mar 2008
 * Carnegie Mellon University
 */

import java.util.Vector;

import RuleLearner.IPrinter;
import RuleLearner.RuleLearner;
import TreeParser.ParseTreeNode;

public class RuleExtractorT2TAbhaya {

	private static String SOURCE_OUTPUT_MODE = "tiburon";
	private static String TARGET_OUTPUT_MODE = "taru";
	private static int MAX_RULE_SIZE = 45;
	
	public static void getRuleAsTreeT(ParseTreeNode ptn,StringBuilder tmptarget)
	{
//		System.out.println(ptn.nodetype);
		if(TARGET_OUTPUT_MODE == "tiburon"){
			tmptarget.append(ptn.nodetype+"(");
		}
		else{
			tmptarget.append("("+ptn.nodetype);
		}
		
		for (int i=0;i<ptn.children.size(); i++) {
			ParseTreeNode child = ptn.children.elementAt(i);
			
			// alignNum can not be 0 ....check again
			if(child.isFrontier() && child.alignNum!=0)
			{
					// Constituents or POS tags 'type=1' 
				if(TARGET_OUTPUT_MODE == "tiburon"){
					if(i > 0)
						tmptarget.append(" ");
	    			tmptarget.append("q.x"+child.alignNum);
				}
				else{
	    			tmptarget.append("(q.x"+child.alignNum+":"+child.nodetype+")");
				}
			}
			else{
				if(child.isTerminal())
				{
					if(TARGET_OUTPUT_MODE == "tiburon"){
						if(i > 0)
							tmptarget.append(" ");
						if(child.getS() != ":"){
							tmptarget.append(child.nodetype+"("+child.getS()+")");
						}
						else{
							tmptarget.append(child.nodetype+"(COLON)");
						}
					}
					else{
						tmptarget.append("("+child.nodetype+" "+child.getS()+")");
					}
				}
				else {
					if(i > 0)
						tmptarget.append(" ");
					//tmptarget.append("("+child.nodetype);
					getRuleAsTreeT(child,tmptarget);
					//tmptarget.append(")");
				}
			}
		}
		tmptarget.append(")");
	}
	
	public static void getFlatTargetSide(ParseTreeNode ptn, StringBuilder tmptarget){

		if(tmptarget.length() == 0)
			tmptarget.append(ptn.nodetype+"(");
		
		for (int i=0;i<ptn.children.size(); i++) {
			ParseTreeNode child = ptn.children.elementAt(i);
			
			// alignNum can not be 0 ....check again
			if(child.isFrontier() && child.alignNum!=0)
			{
	    		tmptarget.append(" " + child.alignNum + ":" +child.nodetype);
			}
			else{
				if(child.isTerminal())
				{
					tmptarget.append(" " + child.getS());
				}
				else {
					tmptarget.append(" ");
					//tmptarget.append("("+child.nodetype);
					getFlatTargetSide(child, tmptarget);
					//tmptarget.append(")");
				}
			}
		}
	}
	
	public static int getRuleAsTreeS(ParseTreeNode ptn,StringBuilder tmpsource,int counter)
	{
		if(SOURCE_OUTPUT_MODE == "tiburon"){
			tmpsource.append(ptn.nodetype+"( ");
		}
		else{
			tmpsource.append("("+ptn.nodetype);
		}
		for (int i=0;i<ptn.children.size(); i++) {
			ParseTreeNode child = ptn.children.elementAt(i);
//			System.out.println(child.tStart);
			if(child.isFrontier() && child.tStart>0 && child.tnodetype!=""){
    			counter++;
				// Constituents or POS tags 'type=1'
				if(SOURCE_OUTPUT_MODE == "tiburon"){
					if(i > 0)
						tmpsource.append(" ");
					tmpsource.append("x"+counter+":"+child.nodetype);
				}
				else{
					tmpsource.append("(x"+counter+":"+child.nodetype+")");
				}
	    		// Keep track of the alignment number for the NODE (ONLY FOR T2T case)
	    		// Only works for SOURCE , for target it is NULL 
//	    		System.out.println(child.nodetype+":"+child.targetnodePtr.nodetype);
	    		child.targetnodePtr.alignNum = counter;

	    		if(counter > MAX_RULE_SIZE)
	    			return -1;
			}
			else{
				if(child.isTerminal())
				{
					if(SOURCE_OUTPUT_MODE == "tiburon"){
						if(i > 0)
							tmpsource.append(" ");
						if(child.getS() != ":"){
							tmpsource.append(child.nodetype+"("+child.getS()+")");
						}
						else{
							tmpsource.append(child.nodetype+"(COLON)");
						}
					}
					else{
						tmpsource.append("("+child.nodetype+" "+child.getS()+")");
					}
				}
				else {
					if(i > 0)
						tmpsource.append(" ");
					//tmpsource.append("("+child.nodetype);
					counter = getRuleAsTreeS(child,tmpsource,counter);
					if(counter == -1)
						return counter;
					//tmpsource.append(")");
				}
			}
		}
		tmpsource.append(")");
		
		return counter;
	}
	
	public static int extractMinimalRule(ParseTreeNode sptn,StringBuilder tmpsource,ParseTreeNode tptn,StringBuilder tmptarget)
	{
		int ruleSize = getRuleAsTreeS(sptn,tmpsource,0);
		if( ruleSize != -1){
			if(TARGET_OUTPUT_MODE.equalsIgnoreCase("taru")){
				getFlatTargetSide(tptn,tmptarget);
				tmptarget.append(")");
			}
			else{
				getRuleAsTreeT(tptn,tmptarget);
			}
		}
		else{
			return -1;
		}
		return ruleSize;
	}

	public static void extractPhrasalRule(ParseTreeNode ptn, StringBuilder rule){
		
		rule.append(ptn.nodetype+"(");
		
		if(ptn.isTerminal())
		{
			if(ptn.getS() != ":"){
				rule.append(ptn.getS());
			}
			else{
				rule.append("COLON");
			}
		}
		else{
			for (int i=0;i<ptn.children.size(); i++) {
				ParseTreeNode child = ptn.children.elementAt(i);
	
				if(i > 0)
					rule.append(" ");
				extractPhrasalRule(child,rule);
			}
		}
		rule.append(")");
	}
	
	public static void setMaxRuleSize(int ruleSize){
		MAX_RULE_SIZE = ruleSize;
	}
	
	public static int extractRules(ParseTreeNode sptn,IPrinter writer,Vector<String> sSeq,Vector<String> tSeq,int sentId,int rulecounter) 
	{
//		System.out.println(sptn.nodetype + " "+sptn.getS() +" Frontier: "+sptn.isFrontier());

		if(!sptn.isTerminal())
		{
			for (int i=0;i<sptn.children.size(); i++) {
				ParseTreeNode child = sptn.children.elementAt(i);
				rulecounter = extractRules(child,writer,sSeq,tSeq,sentId,rulecounter);				
			}
			if( (sptn.isFrontier()) && (sptn.tStart>0) && (sptn.tnodetype!=""))
			{

				StringBuilder source = new StringBuilder(); 
				StringBuilder target = new StringBuilder();
				
				ParseTreeNode tptn = sptn.targetnodePtr;
//				System.out.println(sptn.nodetype + " Aligned Node: "+tptn.nodetype);
				
				// Check to prevent Node Aligning to Terminals 
				if(!tptn.isTerminal()) {
					// Let us extract a minimal rule from this decomposition point
					int ruleSize = extractMinimalRule(sptn,source,tptn,target);
					if(ruleSize != -1){
						if(ruleSize > 0)
							writer.writeT2TRule(sptn.nodetype,rulecounter,source.toString(),target.toString());
						rulecounter++;
						RuleLearner.addRule("q."+source.toString() + " -> " + target.toString(),ruleSize);
					}
				}
		    }
		}
		else{
			// Let us get the lexical rule if this is a frontier node.
			if(sptn.isFrontier() && (sptn.tStart>0) && (sptn.tnodetype!="")){
				StringBuilder rule = new StringBuilder();			
				extractPhrasalRule(sptn, rule);
				rule.append(" -> ");
				extractPhrasalRule(sptn.targetnodePtr, rule);
				RuleLearner.addRule("q."+rule.toString(),0);
			}
		}
		return rulecounter;
	}
}