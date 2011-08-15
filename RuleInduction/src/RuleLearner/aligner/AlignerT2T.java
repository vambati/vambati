package RuleLearner.aligner;

/**
 * Desc: Program Aligns a Source side Tree to a String (Source side syntax is a parse tree)
 * Author: Vamshi Ambati
 * 12 Mar 2008
 * Carnegie Mellon University
 */
import Rule.Alignment;
import RuleLearner.OrderPair;
import TreeParser.*;

import java.util.*;

public class AlignerT2T {

    public AlignerT2T() {
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
	  			ptn.tStart = Collections.min(ypoints);
	  			ptn.tEnd = Collections.max(ypoints);
	  		}
//	  		System.out.println(ptn.tStart+" "+ptn.tEnd+" ");
	  		if((ptn.sStart==ptn.sEnd) && (ptn.tStart==ptn.tEnd) && (ptn.tStart!=-1))
	  		{
	  			int type = amap.getAlignmentType(x);
		  		// One-One Aligned is a frontier
//	  			System.out.println(" Type of node: "+type);
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
//			System.out.println(ptn.nodetype + " "+ ptn.getS()+" Frontier: "+ptn.isFrontier());
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
	  			if(child.isTerminal())
	  				continue;

	  			// First check if there is any conflict with siblings
	  			boolean conflict_sibling = false;
	  			for (int j=0;j<ptn.children.size(); j++)
				{
				   if(j==i) continue;
				    
				   ParseTreeNode sibling = ptn.children.elementAt(j);
		    		if(((sibling.tEnd - child.tStart) * (sibling.tStart - child.tEnd)) <= 0)
    			 	{
    			 		conflict_sibling = true;
    			 	}
		    	  // Add each of the sibling spans to its complementspan
	    		  child.complementspan.add(new OrderPair(sibling.tStart,sibling.tEnd));
				}
	  			
	  			// Now let us check conflict with parents
				  boolean conflict_parent = false;
	  			
				// Parent is also frontier, then don't have to check - no conflicts can occur
				  if(ptn.isFrontier())
				  {
					  conflict_parent = false;
				  }
				  // Parent is not frontier, so check with its complement span before declaring frontier
				  else
				  {
					  for(int k=0;k<ptn.complementspan.size();k++)
					  {
						 OrderPair span = ptn.complementspan.elementAt(k);
						if(((span.y - child.tStart) * (span.x - child.tEnd)) <= 0)
					 	{
					 		conflict_parent = true;
					 	}
						child.complementspan.add(span);
					  }					  
					}
				  
				  // If no conflicts, declear frontier
				  if(conflict_parent == false && conflict_sibling == false)
				  {
					  child.complementspan.clear();
					  child.isFrontier = true;
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
