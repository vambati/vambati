package RuleLearner.aligner;

/**
 * Desc: Program Aligns a Source side Tree to a Target side tree
 * Author: Vamshi Ambati
 * 12 Mar 2008
 * Carnegie Mellon University
 */

import Rule.Alignment;
import RuleLearner.OrderPair;
import TreeParser.*;
import java.util.*;

public class AlignerT2S {

    public AlignerT2S() {
    }
    
	public static void calculateSpans(ParseTreeNode ptn,String sl,String tl,Alignment amap) 
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
	  		
	  		if((ptn.sStart==ptn.sEnd) && (ptn.tStart==ptn.tEnd) && (ptn.tStart!=-1))
	  		{
	  			int type = amap.getAlignmentType(x);
		  		// One-One Aligned is a frontier
		  		if(type==1) {
		  			ptn.isFrontier = true;
		  			ptn.tnodetype = ptn.nodetype;
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
	  		}
	  		
	  	}
    	else
    	{
    		 for (int i=0;i<ptn.children.size(); i++){
    			 ParseTreeNode child = ptn.children.elementAt(i);
    			  calculateSpans(child,sl,tl,amap);
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
  				  if(conflict_sibling==false)
  				  {
  					  // Parent is also frontier, then don't have to check - child can also become frontier
  					  if(ptn.isFrontier())
  					  {
  						  child.complementspan.clear();
  						  child.isFrontier = true;
  						  // Dummy for this case
  						  child.tnodetype = child.nodetype;
  					  }
  					  // Parent is not frontier, so check with its complement span before declaring frontier
  					  else
  					  {
  						  boolean conflict_parent = false;
  						  for(int k=0;k<ptn.complementspan.size();k++)
  						  {
  							 OrderPair span = ptn.complementspan.elementAt(k);
  							if(((span.y - child.tStart) * (span.x - child.tEnd)) <= 0)
		    			 	{
		    			 		conflict_parent = true;
		    			 	}
  						  child.complementspan.add(span);
  						  }
  						  
  						  if(conflict_parent==false)
  		  				  {
  							  child.complementspan.clear();
  	  						  child.isFrontier = true;
  	  						  // Dummy for this case
  	  						  child.tnodetype = child.nodetype;
  		  				  }
  		  				}
  				  }
  				  calculateComplementSpans(child);
	    		}
	  		}
	  	return;
	}
}
