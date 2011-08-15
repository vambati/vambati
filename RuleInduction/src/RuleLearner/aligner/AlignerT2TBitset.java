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

public class AlignerT2TBitset {

	public static int rulecount_aligned = 0;
	
    public AlignerT2TBitset() {
    }
    
    // Not so efficient, but calculates all word-alignment-consistent segments first
    // Then it uses this info to calculate all the DECOMPOSITION points by checking in  Target Trees
    public static void calculateSpans(ParseTreeNode sptn,ParseTreeNode tptn,String sl,String tl,Alignment amap)
    {
    	calculateSSpans(sptn,sl,tl,amap);
    	
		calculateSSpans(tptn,sl,tl,amap);
		
    	calculateComplementSpans(sptn);
    	// In this function adjust the tStart and tEnd for SourceTree, by looking at valid tree nodes
    	// on the TargetTree
    	adjustTranslationEquivalents(sptn,tptn,amap);
    	
    	/*String t1 = ParseTree.treeString(sptn);
		System.out.println(t1);

    	 String t2 = ParseTree.treeString(tptn);
 		System.out.println(t2);*/
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
	  		
	  		if((ptn.sStart==ptn.sEnd) && (ptn.tStart==ptn.tEnd) && (ptn.tStart!=-1))
	  		{
	  			int type = amap.getAlignmentType(x);
		  		// One-One Aligned is a frontier
	  			// System.out.println(" Type of node: "+type);
		  		if(type==1) {
		  			ptn.isFrontier = true;		  			 			
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
	  	if(ptn.isTerminal())
    	{
	  		return;
    	}
	  	else
	  	{
	  		for (int i=0;i<ptn.children.size(); i++)
	    	{
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
	  			for (int j=0;j<ptn.children.size(); j++){
					   if(j==i) continue;
					   child.cSpan.or(ptn.children.elementAt(j).span);
	  			}
	  			// Check for conflict
	  			if(!spanClosure.intersects(child.cSpan)){
	  				child.isFrontier = true;
//	  				System.out.println("Here !");
	  			}
  				  calculateComplementSpans(child);
	    		}
	  		}
	  	return;
	}
	
	// TAIL Recursion ( Align LOWER NODE , rather than Upper node)
	public static void adjustTranslationEquivalents(ParseTreeNode sptn,ParseTreeNode tptn, Alignment amap) 
	{
//			System.out.println(sptn.getS() + " Forntier: " + sptn.isFrontier());

			for (int i=0;i<sptn.children.size(); i++)
	    	{
	  			ParseTreeNode child = sptn.children.elementAt(i); 
	  			adjustTranslationEquivalents(child,tptn,amap);
	    	}
	  		// If it is a frontier and the node is not already aligned to the target side 
	  		if(sptn.isFrontier() && sptn.tnodeid==-1)
	  		{
	  			readTargetDetail(sptn,tptn, amap);
	  		}
	}

	// TAIL Recursion ( Align LOWER NODE , rather than Upper node)
	public static void readTargetDetail(ParseTreeNode sptn,ParseTreeNode tptn, Alignment amap) 
	{
	  		for (int i=0;i<tptn.children.size(); i++)
	    	{
	  			ParseTreeNode child = tptn.children.elementAt(i); 
	  			readTargetDetail(sptn,child,amap);
	    	}
	  		// terminal nodes can only align to terminal nodes 
	  		if(!sptn.isTerminal() && tptn.isTerminal())
	  			return;
  			// Adjust the sptn.tnodetype
  			// Adjust the tStart and tEnd to correspond exactly to target tree
	  		if(checkAlignment(sptn, tptn, amap))
	  		{
	  			// If target node for this source node, not identified 
	  			if(tptn.tnodeid==-1 && sptn.tnodeid==-1)
	  			{
		  			//System.out.println(sptn.nodetype+":"+tptn.nodetype);
		  			// A non-empty 'tnodetype' means some alignment was found 
		  			sptn.tnodetype=tptn.nodetype;
		  			sptn.tnodeid = tptn.id;
		  			tptn.tnodeid = sptn.id;
		  			// Also set target side as a Frontier 
		  			tptn.isFrontier = true;
		  			// Keep a pointer to the node that it is aligned to (T2T case)
		  			// ONLY FOR SOURCE (for target nodes it is NULL)
		  			sptn.targetnodePtr=tptn;
		  			tptn.tnodetype=sptn.nodetype;
		  			
		  			// Set the target start and ends
		  			sptn.tStart = tptn.sStart;
		  			sptn.tEnd = tptn.sEnd;
		  			
		  			rulecount_aligned++;
		  		return;
	  			}
	  		}
		}
	
	public static boolean checkAlignment(ParseTreeNode sptn, ParseTreeNode tptn, Alignment amap){
		// If exact match, return true
		if((sptn.tStart == tptn.sStart) && (sptn.tEnd == tptn.sEnd))
			return true;
		
		// If crossing boundries, return false
		if((sptn.tStart < tptn.sStart) || (sptn.tEnd > tptn.sEnd))
			return false;
		
		// No crossing boundries, so check that all the other target words are unaligned
		for(int i = tptn.sStart; i < sptn.tStart; i++){
			// If any word is aligned, return false
			if(amap.getReverseAlignment(i) != null)
				return false;
		}
		
		for(int i = tptn.sEnd; i > sptn.tEnd; i--){
			// if any word is aligned, return false
			if(amap.getReverseAlignment(i) != null)
				return false;
		}
		// Everything looks fine, return true
		return true;
	}
}
