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

import Scoring.RuleException;
import TreeParser.*;

import java.io.IOException;
import java.util.*;

import Rule.Alignment;
import Rule.Constituent;
import Rule.Rule;
import Rule.RuleLearnerException;
import RuleLearner.Comparer;
import RuleLearner.IPrinter;

public class RuleExtractorS2S_Union {
     
	public static int getFrontier(ParseTreeNode ptn,Vector<Constituent> tmpsource,Vector<Constituent> tmptarget,int counter)
	{
		for (int i=0;i<ptn.children.size(); i++) {
			ParseTreeNode child = ptn.children.elementAt(i);
			
			if(child.isFrontier() && child.tStart>0 && child.tnodetype!=""){
				// Constituents or POS tags 'type=1'
    			tmpsource.add(new Constituent(child.sStart,Constituent.POS,child.nodetype,child.sStart,child.sEnd,counter));
    			tmptarget.add(new Constituent(child.tStart,Constituent.POS,child.tnodetype,child.tStart,child.tEnd,counter));
    			counter++;
			}
			else{
				counter = getFrontier(child,tmpsource,tmptarget,counter);					
			}
		}
		return counter;
	}

	public static int extractRules(ParseTreeNode ptn,IPrinter writer,Vector<String> sSeq,Vector<String> tSeq,int sentId,int rulecounter, Alignment amap,
			Hashtable<String, Rule> gra,Hashtable<String, Rule> phr,Hashtable<String, Rule> lex, boolean REVERSE_FLAG) throws RuleLearnerException
	{
		if(!ptn.isTerminal())
		{
			for (int i=0;i<ptn.children.size(); i++) {
				ParseTreeNode child = ptn.children.elementAt(i);
				rulecounter = extractRules(child,writer,sSeq,tSeq,sentId,rulecounter,amap,gra,phr,lex,REVERSE_FLAG);				
			}
		}
		
		if( (ptn.isFrontier()) && (ptn.tStart>0) && (ptn.tnodetype!=""))
		{
			Vector<Constituent> tmpsource = new Vector<Constituent>();
			Vector<Constituent> tmptarget = new Vector<Constituent>();
			int counter=1;
			counter = getFrontier(ptn,tmpsource,tmptarget,counter);
			
				// Print the Phrase for Syntactic Table (START)
				Vector<Constituent> xv = new Vector<Constituent>();
				Vector<Constituent> yv = new Vector<Constituent>();
								
				//System.out.println(ptn.getS());
				//System.out.println(ptn.sStart+"-"+ptn.sEnd+"::"+ptn.tStart+"-"+ptn.tEnd+" ");
				
				for(int i=ptn.sStart-1;i<ptn.sEnd;i++)
					xv.addElement(new Constituent(i,Constituent.LEXICAL,sSeq.elementAt(i),i+1,i+1,0));
				for(int i=ptn.tStart-1;i<ptn.tEnd;i++)
					yv.addElement(new Constituent(i,Constituent.LEXICAL,tSeq.elementAt(i),i+1,i+1,0));

				// Set tmpalignment to the Aligment for the Lexical Phrase
				// Finally Set the Alignment string for the lexical phrase rule (not required in Xfer, used in SMT)
				Alignment lexalignment = new Alignment(""); // Update this alignment later lexalignment.updateAlignmentStr()
				Hashtable<Integer,Vector<Integer>> tmplexalignment = new Hashtable<Integer,Vector<Integer>>();
				 
				for(int i=0;i<xv.size();i++){
					Vector<Integer> matches = amap.getAlignment(xv.elementAt(i).start);
			  		if(matches!=null) {
				  		for(Integer k : matches){
							int anchor = i+1;
							if(tmplexalignment.containsKey(k)) {
								tmplexalignment.get(k).add(new Integer(anchor));
							}else {
								Vector<Integer> tv = new Vector<Integer>();
								tv.add(new Integer(anchor));
								tmplexalignment.put(k,tv);
							}
				  		}
			  		}
				}
				for(int j=0;j<yv.size();j++){
					int y = yv.elementAt(j).start;
					Vector<Integer> xmatches = tmplexalignment.get(new Integer(y));
					if(xmatches!=null) {
						for(Integer x: xmatches){
							lexalignment.addLink(x,new Integer(j+1));
						}
					}
				}
				lexalignment.updateAlignmentStr();
				
				Rule rl1 = null; 
				Rule tmp_rl1 = new Rule(rulecounter,ptn,xv,yv,lexalignment);;
				if(REVERSE_FLAG){
					rl1 = tmp_rl1.reverseClone();
				}else{
					rl1 = tmp_rl1;
				}
				
				rulecounter++;
				// For lexicalization
				if(ptn.head!=""){
					rl1.setHead(ptn.head);
					rl1.setHeadType(ptn.head_type);
					if(ptn.head_tgt!=""){
						rl1.setTargetHead(ptn.head_tgt);
					}
				}
				
				if(ptn.isTerminal()){
					lex.put(rl1.getKey(),rl1);
				}
				else{
					phr.put(rl1.getKey(),rl1);
				}

	    	if(!tmptarget.isEmpty() && !tmpsource.isEmpty())
	    	{
			// Print the Generalized Rule		
	    	Vector<Constituent> source = new Vector<Constituent>();
			Vector<Constituent> target = new Vector<Constituent>();
			
	    	// Vector Starts at '0' index, but tStart is from '1'
	    	int sIndex=ptn.sStart-1;
			for(int j=0;j<tmpsource.size();j++)
			{
				int x = tmpsource.elementAt(j).start;
				int y = tmpsource.elementAt(j).end;
	
				if(sIndex==x-1){
	    			source.add(tmpsource.elementAt(j));
					sIndex=y;
				}
				else if(sIndex<(x-1)){
					// Add the Lexical Items 
					for(;sIndex<x-1;sIndex++) {
						source.add(new Constituent(sIndex,Constituent.LEXICAL,sSeq.elementAt(sIndex),sIndex,sIndex,0));
					}
				j--;
				}
			}
			while(sIndex<ptn.sEnd){
				source.add(new Constituent(sIndex,Constituent.LEXICAL,sSeq.elementAt(sIndex),sIndex,sIndex,0));
				sIndex++;
			}
			// Order in the sequence of the Target Spans
	    	Collections.sort(tmptarget,new Comparer());
			
				int tIndex = ptn.tStart-1;
				for(int j=0;j<tmptarget.size();j++)
				{
					int x = tmptarget.elementAt(j).start;
					int y = tmptarget.elementAt(j).end;
		
					if(tIndex==x-1) {
		    			target.add(tmptarget.elementAt(j));
		    			tIndex=y;
					}
		   			else if(tIndex<(x-1)) {
						// Add the Lexical Items 
						for(;tIndex<x-1;tIndex++) {
							target.add(new Constituent(tIndex,Constituent.LEXICAL,tSeq.elementAt(tIndex),tIndex,tIndex,0));
						}
					j--;
					}
		    	}
				while(tIndex<ptn.tEnd){
					target.add(new Constituent(tIndex,Constituent.LEXICAL,tSeq.elementAt(tIndex),tIndex,tIndex,0));
					tIndex++;
				}

				// Set tmpalignment to the Aligment for the rule
				Alignment alignment = new Alignment("");
				Hashtable<Integer,Integer> tmpalignment = new Hashtable<Integer,Integer>();
				
				// Finally Set the Alignment string for the rule 
				for(int i=0;i<source.size();i++){
					int match = source.elementAt(i).match;
					// Lexical items do not have alignment in the RULE 
					if(match!=0){
						int anchor = i+1;
						tmpalignment.put(new Integer(match), new Integer(anchor));
					}
				}

				for(int j=0;j<target.size();j++){
					int match = target.elementAt(j).match;
					// Lexical items do not have alignment in the RULE 
					if(match!=0){
						Integer x = tmpalignment.get(new Integer(match));
						Integer y = new Integer(j+1);
						alignment.addLink(x,y);
					}
				}
				alignment.updateAlignmentStr();

				Rule rl2 = null; 
				Rule tmp_rl2 = new Rule(rulecounter,ptn,source,target,alignment);
				if(REVERSE_FLAG){
					rl2 = tmp_rl2.reverseClone();
				}else{
					rl2 = tmp_rl2;
				}
			
				// For lexicalization
				if(ptn.head!=""){
					rl2.setHead(ptn.head);
					rl2.setHeadType(ptn.head_type);
					if(ptn.head_tgt!=""){
						rl2.setTargetHead(ptn.head_tgt);
					}
				}
				//For Markovization TODO
				//Other features  TODO
				gra.put(rl2.getKey(),rl2);
				rulecounter++;
	    	}
		}
   return rulecounter;
	}

	public static int flushRules(IPrinter mywriter, 
			Hashtable<String,Rule> gra, Hashtable<String,Rule> phr, Hashtable<String,Rule> lex) throws RuleException, IOException, InterruptedException 
	{
		int rulecounter_union = 0;
		// Print lexicon
		for(String r: lex.keySet()){
			mywriter.writeLexicon(lex.get(r));
			rulecounter_union++;
		}
		// Print phr table
		for(String r: phr.keySet()){
			mywriter.writePhrase(phr.get(r));
			rulecounter_union++;
		}
		// Print rules
		for(String r: gra.keySet()){
			mywriter.writeRule(gra.get(r));
			rulecounter_union++;
		}
		return rulecounter_union; 
	}
}
