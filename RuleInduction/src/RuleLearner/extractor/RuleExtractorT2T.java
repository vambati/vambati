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

import RuleLearner.IPrinter;
import TreeParser.*;
import java.util.*;

public class RuleExtractorT2T {

	public static void getRuleAsTreeT(ParseTreeNode ptn,StringBuffer tmptarget)
	{
		tmptarget.append("("+ptn.nodetype);
		for (int i=0;i<ptn.children.size(); i++) {
			ParseTreeNode child = ptn.children.elementAt(i);
			
			// alignNum can not be 0 ....check again
			if(child.isFrontier() && child.alignNum!=0)
			{
					// Constituents or POS tags 'type=1' 
	    			tmptarget.append("(x"+child.alignNum+":"+child.nodetype+")");
			}
			else{
				if(child.isTerminal())
				{
					tmptarget.append("("+child.nodetype+" "+child.getS()+")");
				}
				else {
					//tmptarget.append("("+child.nodetype);
					getRuleAsTreeT(child,tmptarget);
					//tmptarget.append(")");
				}
			}
		}
		tmptarget.append(")");
	}
	
	public static int getRuleAsTreeS(ParseTreeNode ptn,StringBuffer tmpsource,int counter)
	{
		tmpsource.append("("+ptn.nodetype);
		for (int i=0;i<ptn.children.size(); i++) {
			ParseTreeNode child = ptn.children.elementAt(i);
						
			if(child.isFrontier() && child.tStart>0 && child.tnodetype!=""){
				// Constituents or POS tags 'type=1'
	    		tmpsource.append("(x"+counter+":"+child.nodetype+")");
	    			
	    		// Keep track of the alignment number for the NODE (ONLY FOR T2T case)
	    		// Only works for SOURCE , for target it is NULL 
	    		//System.out.println(child.nodetype+":"+child.targetnodePtr.nodetype);
	    		child.targetnodePtr.alignNum = counter;
    			counter++;
			}
			else{
				if(child.isTerminal())
				{
					tmpsource.append("("+child.nodetype+" "+child.getS()+")");
				}
				else {
					//tmpsource.append("("+child.nodetype);
					counter = getRuleAsTreeS(child,tmpsource,counter);
					//tmpsource.append(")");
				}
			}
		}
		tmpsource.append(")");
		
		return counter;
	}
	public static void getFrontier(ParseTreeNode sptn,StringBuffer tmpsource,ParseTreeNode tptn,StringBuffer tmptarget)
	{
		// Source 
		getRuleAsTreeS(sptn,tmpsource,1);
		// Target
		getRuleAsTreeT(tptn,tmptarget);
	}

	public static int extractRules(ParseTreeNode sptn,IPrinter writer,Vector<String> sSeq,Vector<String> tSeq,int sentId,int rulecounter) throws Exception
	{
		if(!sptn.isTerminal())
		{
			for (int i=0;i<sptn.children.size(); i++) {
				ParseTreeNode child = sptn.children.elementAt(i);
				rulecounter = extractRules(child,writer,sSeq,tSeq,sentId,rulecounter);				
			}
		
			if( (sptn.isFrontier()) && (sptn.tStart>0) && (sptn.tnodetype!=""))
			{
				StringBuffer source = new StringBuffer(); 
				StringBuffer target = new StringBuffer();
				
				ParseTreeNode tptn = sptn.targetnodePtr;
				
				// Check to prevent Node Aligning to Terminals 
				if(!tptn.isTerminal()) {
					getFrontier(sptn,source,tptn,target);
		
					writer.writeT2TRule(sptn.nodetype,rulecounter,source.toString(),target.toString());
					rulecounter++;
				}
		    }
		}
   return rulecounter;
	}
}