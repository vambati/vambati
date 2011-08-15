package RuleLearner.aligner;

/**
 * Desc: Program Aligns a Source side Tree to a String (Source side syntax is a parse tree)
 * Author: Vamshi Ambati
 * 12 Mar 2008
 * Carnegie Mellon University
 */
import Rule.Alignment;
import TreeParser.*;

import java.util.*;

public class AlignerT2SBitSet {

	public static int rulecount_projected = 0;
    public AlignerT2SBitSet() {
    }
    
    // Not so efficient, but calculates all word-alignment-consistent segments first
    // Then it uses this info to calculate all the DECOMPOSITION points by checking in  Target Trees
    public static void calculateSpans(ParseTreeNode sptn,String sl,String tl,Alignment amap)
    {
    	calculateSSpans(sptn,sl,tl,amap);
    					
    	calculateComplementSpans(sptn);
    	
    	//String t1 = ParseTree.treeString(sptn);
		//System.out.println(t1);
    }
    
	public static void calculateSSpans(ParseTreeNode ptn,String sl,String tl,Alignment amap) 
	{
	  	if(ptn.isTerminal())
    	{
	  		int x = ptn.sStart;
	  		Vector<Integer> ypoints = amap.getAlignment(x);
	  		// Handle Unaligned later
	  		if(ypoints!=null) {
	  			// This sets the closure of span
	  			ptn.tStart = Collections.min(ypoints);
	  			ptn.tEnd = Collections.max(ypoints);
	  			
		  		// Also set the span in bitset
		  		for(Integer i : ypoints){
		  			ptn.span.set(i.intValue());
		  		}
	  		}
	  		
	  		//	  		System.out.println(ptn.tStart+" "+ptn.tEnd+" ");
	  		if((ptn.sStart==ptn.sEnd) && (ptn.tStart==ptn.tEnd) && (ptn.tStart!=-1))
	  		{
	  			int type = amap.getAlignmentType(x);
		  		// One-One Aligned is a frontier
	  			// System.out.println(" Type of node: "+type);
		  		if(type==1) {
		  			ptn.isFrontier = true;		
		  			ptn.tnodetype = ptn.nodetype;
		  			rulecount_projected++;
		  		}
		  		// One-Many Aligned is NOT a frontier (But Assume min to max contiguous span - should change)
		  		else {
		  			ptn.isFrontier = false;
	  			}
		  		// Many-Many Aligned is NOT a frontier (To handle later)
	  		}
	  		else
	  		{
		  		// Many-One Aligned is NOT a frontier
	  			ptn.isFrontier = false;
	  		}
	  		// System.out.println(ptn.nodetype + " "+ ptn.getS()+" Frontier: "+ptn.isFrontier());
	  	}
    	else
    	{
    		 for (int i=0;i<ptn.children.size(); i++){
    			 ParseTreeNode child = ptn.children.elementAt(i);
    			  calculateSSpans(child,sl,tl,amap);
    		       // Set Target Spans
    		       if(ptn.tStart==-1){
    		    	   ptn.tStart = child.tStart;
    		       }
    		       else if((child.tStart!=-1) && (child.tStart<=ptn.tStart)) {
    		    	   ptn.tStart = child.tStart;
    		       }
    		       
    		       if(child.tEnd>=ptn.tEnd){
    		    	   ptn.tEnd = child.tEnd;
    		       }
    		       // Set span in bitset
    		       ptn.span.or(child.span);
    		       
    		       // Set Source Spans 
    		       ptn.sEnd = child.sEnd;
    		       ptn.sString.add(child.sString+" ");
    		   }
    	}
   return;
	}
	
	public static void calculateComplementSpans(ParseTreeNode ptn) 
	{
	  	if(ptn.isTerminal()) {
	  		return;
    	}
	  	else {
	  		for (int i=0;i<ptn.children.size(); i++) {
	  			ParseTreeNode child = ptn.children.elementAt(i);   		       
	  			
	  			// if the child is a Terminal, we do not want to set it to forntier here
	  			// That was already decided on the basis of alignments earlier.
	  			// Also a node whose yield is all unaligned should not be a frontier.
	  			if(child.isTerminal() || child.tStart == -1)
	  				continue;
	  			
	  			// Create a bitset representing the closure of span of this node
	  			BitSet spanClosure = new BitSet(child.span.size());
	  			spanClosure.set(child.tStart, child.tEnd+1, true);
	  			
	  			// The complement span is the union of parent's cpan and siblings span
	  			child.cSpan.or(ptn.cSpan);
	  			for (int j=0;j<ptn.children.size(); j++) {
					   if(j==i) continue;
					   child.cSpan.or(ptn.children.elementAt(j).span);
	  			}
	  			// Check for conflict
	  			if(!spanClosure.intersects(child.cSpan)){
	  				child.isFrontier = true;
//	  				System.out.println("Here !");
	  				
	  				child.tnodetype = child.nodetype;
	  				rulecount_projected++;
	  			}
  				  calculateComplementSpans(child);
	    		}
	  		}
	  	return;
	}
}
