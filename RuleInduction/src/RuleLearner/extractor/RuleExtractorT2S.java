package RuleLearner.extractor;

/**
 * Desc: Program Extracts Rules from a Source side parse Tree. All of the Target side information whether
 * 		 tree or string , is embedded inside the source parse tree nodes. So walking source side is sufficient 
 * 	
 * 		Rules extracted are FLATTENED and are not trees, so the program is called 'S 2 S' - String to String rules 
 *  
 * Author: Vamshi Ambati
 * 12 Mar 2008
 * Carnegie Mellon University
 */

/**
 * TODO: TARU style rules are obtained from this particular program
 * generalize it  
 */
import Rule.Constituent;
import RuleLearner.Comparer;
import RuleLearner.IPrinter;
import TreeParser.*;

import java.util.*;

public class RuleExtractorT2S {
     
	public static int getFrontier(ParseTreeNode ptn,StringBuffer tmpsource,Vector<Constituent> tmptarget,int counter)
	{
		tmpsource.append("("+ptn.nodetype);
		for (int i=0;i<ptn.children.size(); i++) {
			ParseTreeNode child = ptn.children.elementAt(i);
			
			
			if(child.isFrontier() && child.tStart>0 && child.tnodetype!=""){
				// Constituents or POS tags 'type=1'
    			//tmpsource.append("(x"+counter+":"+child.nodetype+")");
				//Taru does not require X format 
    			tmpsource.append("("+child.nodetype+")");
    			tmptarget.add(new Constituent(child.tStart,Constituent.POS,child.tnodetype,child.tStart,child.tEnd,counter));
    			counter++;
			}
			else{
				if(child.isTerminal())
				{
					tmpsource.append("( "+child.nodetype+" "+child.getS()+" )");
				}
				else {
					//tmpsource.append("("+child.nodetype);
					counter = getFrontier(child,tmpsource,tmptarget,counter);
					//tmpsource.append(")");
				}
			}
		}
		tmpsource.append(")");
		
		return counter;
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
	
	public static int extractRules(ParseTreeNode ptn,IPrinter writer,Vector<String> sSeq,Vector<String> tSeq,int sentId,int rulecounter)
	{
		if(!ptn.isTerminal())
		{
			for (int i=0;i<ptn.children.size(); i++) {
				ParseTreeNode child = ptn.children.elementAt(i);
				rulecounter = extractRules(child,writer,sSeq,tSeq,sentId,rulecounter);				
			}
		}
		/*else{
			// Let us get the lexical rule if this is a frontier node.
			if(ptn.isFrontier() && (ptn.tStart>0) && (ptn.tnodetype!="")){
				String src = "( "+ptn.nodetype+" "+ptn.getS()+" )";
				String tgt = "";
				for(int ti=ptn.tStart-1;ti<ptn.tEnd;ti++){
					tgt+=tSeq.elementAt(ti)+" "; 
				}
				//writer.writeT2SRule(ptn.nodetype,rulecounter,src,tgt);
				writer.writeTaruPhrase(ptn.nodetype,rulecounter,src,tgt);
			}
		}*/
		
		if( (ptn.isFrontier()) && (ptn.tStart>0) && (ptn.tnodetype!=""))
		{
			StringBuffer source = new StringBuffer(); 
			Vector<Constituent> tmptarget = new Vector<Constituent>();
			
			// First extract a complete syntactified phrasal rule
			StringBuilder src_phrase = new StringBuilder(); 
			StringBuilder tgt_phrase = new StringBuilder();

			// extractPhrasalRule(ptn, src_phrase);
			for(int si=ptn.sStart-1;si<ptn.sEnd;si++){
				src_phrase.append(sSeq.elementAt(si)+" "); 
			}
			
			for(int ti=ptn.tStart-1;ti<ptn.tEnd;ti++){
				tgt_phrase.append(tSeq.elementAt(ti)+" "); 
			}
			String src = src_phrase.toString();
			String tgt = tgt_phrase.toString();
			
			writer.writeTaruPhrase(ptn.nodetype,rulecounter,src,tgt);

			
			// Second extract the actual rule with possibly the VARIABLES inside 
			
			int counter=1;
			counter = getFrontier(ptn,source,tmptarget,counter);
			
	    	if(!tmptarget.isEmpty())
	    	{	
			String targetString = "";
			
			// Order in the sequence of the Target Spans
	    	Collections.sort(tmptarget,new Comparer());
			
				int tIndex = ptn.tStart-1;
				for(int j=0;j<tmptarget.size();j++)
				{
					int x = tmptarget.elementAt(j).start;
					int y = tmptarget.elementAt(j).end;
		
					if(tIndex==x-1)
					{
		    			targetString+= tmptarget.elementAt(j).match+":"+tmptarget.elementAt(j).word+" ";
		    			tIndex=y;
					}
		   			else if(tIndex<(x-1))
					{
						// Add the Lexical Items 
						for(;tIndex<x-1;tIndex++) {
							targetString+= tSeq.elementAt(tIndex)+" ";
						}
					j--;
					}
		    	}
				while(tIndex<ptn.tEnd){
					targetString+= tSeq.elementAt(tIndex)+" ";
					tIndex++;
				}

				//TODO: T2S rule (harcoded, relax it soon)
				writer.writeTaruRule(ptn.nodetype,rulecounter,source.toString(),targetString);
				rulecounter++;
	    	}
		}
   return rulecounter;
	}
}