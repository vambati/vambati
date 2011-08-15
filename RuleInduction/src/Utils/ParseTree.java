package Utils;

import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Rule.FeatureSet;
import TreeParser.ParseTreeNode;
import Utils.MyUtils;

import TreeParser.*;
import mosesannotator.*;

public class ParseTree {

    public ParseTree() {
    }

    public static ParseTreeNode buildTree(String tree) 
    {
    	return buildTree(tree,0);
    }
    
    public static ParseTreeNode buildTree(String tree,int lexicalization_mode) 
    {
		StringReader sr = new StringReader(tree);
		ParseTreeNode ptn = null; 
		
		try {
			ParseTreeLexer ptl = new ParseTreeLexer(sr);
			ParseTreeParser ptp = new ParseTreeParser(ptl);
			
			ptn = new ParseTreeNode();
			ptp.setTopNode(ptn);
	
			// Set as Parent
			ptn.isRoot = true;
			ptn.isFrontier = true;
		
		    ptp.tree();
		}
		catch (Exception e) {
		    //e.printStackTrace();
		    System.err.println("Unable to create ParseTree from the sentence"+tree);
		}
		
		// Parse Tree modifications 
		if(lexicalization_mode==1)
		{
			ParseTree.headAnnotate(ptn);
		}else if(lexicalization_mode==2)
		{
			ParseTree.headTypeAnnotate(ptn);
		}
		
		// Annotate yield information
		annotateYield(ptn);
		
	return ptn;
    }
        
    public static ParseTreeNode buildLexTree(String tree,int lexicalization_mode) 
    {
		StringReader sr = new StringReader(tree);
		
		LexParseTreeLexer ptl = new LexParseTreeLexer(sr);
		LexParseTreeParser ptp = new LexParseTreeParser(ptl);
		
		ParseTreeNode ptn = new ParseTreeNode();
		ptp.setTopNode(ptn);

		// Set as Parent
		ptn.isRoot = true;
		ptn.isFrontier = true;
		
		try {
		    ptp.tree();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		
		//Annotate head information
		ParseTree.headPosition(ptn);
		
		// Parse Tree modifications 
		if(lexicalization_mode==1)
		{
			ParseTree.headAnnotate(ptn);
		}
		 else if(lexicalization_mode==2)
		{
			ParseTree.headTypeAnnotate(ptn);
		}
		
		// Annotate yield information
		annotateYield(ptn);
	return ptn;
    }

    // Annotate Contextual information for the source side of the tree 
    public static void annotateLeftContext(ParseTreeNode ptn)
    {
    	// Add common features 
    	ptn.fs.addFeatureValue("node_type", ptn.nodetype);
    	
    	if(ptn.isTerminal){
    		ptn.fs.addFeatureValue("start_word", ptn.yield);
    		ptn.fs.addFeatureValue("start_word_type", ptn.nodetype);
    		ptn.fs.addFeatureValue("end_word", ptn.yield);
    		ptn.fs.addFeatureValue("start_word_type", ptn.nodetype);
    		return;
    	}else{
    		int first = 0; int last = ptn.children.size()-1;
		  for (int i=0;i<ptn.children.size();i++) 
		  {
			  ptn.children.get(i).fs.addFeatureValue("grandparent_node_type", ptn.nodetype);
	  		  annotateLeftContext(ptn.children.elementAt(i));
	  		  // Add left  information
	  		  if(i>0){
	  			ptn.children.get(i).fs.addFeatureValue("left_node_type", ptn.children.get(i-1).nodetype);
	  			ptn.children.get(i).fs.addFeatureValue("left_boundary_word", ptn.children.get(i-1).fs.getFeatureValue("end_word"));
	  			ptn.children.get(i).fs.addFeatureValue("left_boundary_word_type", ptn.children.get(i-1).fs.getFeatureValue("end_word_type"));
	  			ptn.children.get(i).fs.addFeatureValue("left_headword", ptn.children.get(i-1).fs.getFeatureValue("headword"));
	  			ptn.children.get(i).fs.addFeatureValue("left_headword_type", ptn.children.get(i-1).fs.getFeatureValue("headword_type"));
	  		  }else{
	  			ptn.children.get(i).fs.addFeatureValue("left_type", "_NA");
	  			ptn.children.get(i).fs.addFeatureValue("left_boundary_word", "_NA");
	  			ptn.children.get(i).fs.addFeatureValue("left_boundary_word_type", "_NA");
	  			ptn.children.get(i).fs.addFeatureValue("left_headword", "_NA");
	  			ptn.children.get(i).fs.addFeatureValue("left_headword_type", "_NA");
	  		  }
		  }
  		  // Add start and end word information
		  ptn.fs.addFeatureValue("start_word", ptn.children.get(first).fs.getFeatureValue("start_word"));
		  ptn.fs.addFeatureValue("start_word_type", ptn.children.get(first).fs.getFeatureValue("start_word_type"));
		  
		  ptn.fs.addFeatureValue("end_word", ptn.children.get(last).fs.getFeatureValue("end_word"));
		  ptn.fs.addFeatureValue("end_word_type", ptn.children.get(last).fs.getFeatureValue("end_word_type"));
    	}
  }
    
    // Annotate Contextual information for the source side of the tree 
    public static void annotateRightContext(ParseTreeNode ptn)
    {
    	// Add common features 
    	ptn.fs.addFeatureValue("node_type", ptn.nodetype);

    	if(ptn.isTerminal){
    		ptn.fs.addFeatureValue("start_word", ptn.yield);
    		ptn.fs.addFeatureValue("start_word_type", ptn.nodetype);
    		ptn.fs.addFeatureValue("end_word", ptn.yield);
    		ptn.fs.addFeatureValue("start_word_type", ptn.nodetype);
    		return;
    	}else{
    		int first = 0; int last = ptn.children.size()-1;
		  for (int i=0;i<ptn.children.size();i++) 
		  {
			  ptn.children.get(i).fs.addFeatureValue("grandparent_node_type", ptn.nodetype);
	  		  annotateRightContext(ptn.children.elementAt(i));
	  		  
	  		  // Add right information
	  		  if(i<ptn.children.size()-1) {
	  			ptn.children.get(i).fs.addFeatureValue("right_node_type", ptn.children.get(i+1).nodetype);
	  			ptn.children.get(i).fs.addFeatureValue("right_boundary_word", ptn.children.get(i+1).fs.getFeatureValue("start_word"));
	  			ptn.children.get(i).fs.addFeatureValue("right_boundary_word_type", ptn.children.get(i+1).fs.getFeatureValue("start_word_type"));
	  			ptn.children.get(i).fs.addFeatureValue("right_headword", ptn.children.get(i+1).fs.getFeatureValue("headword"));
	  			ptn.children.get(i).fs.addFeatureValue("right_headword_type", ptn.children.get(i+1).fs.getFeatureValue("headword_type"));
	  		  }else{
	  			ptn.children.get(i).fs.addFeatureValue("right_node_type", "_NA");
	  			ptn.children.get(i).fs.addFeatureValue("right_boundary_word", "_NA");
	  			ptn.children.get(i).fs.addFeatureValue("right_boundary_word_type", "_NA");
	  			ptn.children.get(i).fs.addFeatureValue("right_headword", "_NA");
	  			ptn.children.get(i).fs.addFeatureValue("right_headword_type", "_NA");
	  		  }
		  }
  		  // Add start and end word information
		  ptn.fs.addFeatureValue("start_word", ptn.children.get(first).fs.getFeatureValue("start_word"));
		  ptn.fs.addFeatureValue("start_word_type", ptn.children.get(first).fs.getFeatureValue("start_word_type"));
		  
		  ptn.fs.addFeatureValue("end_word", ptn.children.get(last).fs.getFeatureValue("end_word"));
		  ptn.fs.addFeatureValue("end_word_type", ptn.children.get(last).fs.getFeatureValue("end_word_type"));    	}
  }


    public static void getSpans(ParseTreeNode ptn,Hashtable<String,String> nodeSpans,int count)
    {
    	int x = ptn.sStart -1;
    	int y = ptn.sEnd -1;
		String key = x+"_"+y;
		nodeSpans.put(key, ptn.nodetype);
		  for (int i=0;i<ptn.children.size();i++) {
			 getSpans(ptn.children.elementAt(i),nodeSpans,count++);
		    }
	return;
  }

    public static void annotateYield(ParseTreeNode ptn)
    {
    	if(ptn.isTerminal()){
    		// Lower case it 
    		//ptn.yield = ptn.getS().toLowerCase();
    		ptn.yield = ptn.getS();
    	}else{
    		//ptn.yield = MyUtils.rtrim(getString(ptn)).toLowerCase();
    		ptn.yield = MyUtils.rtrim(getString(ptn));
		  for (int i=0;i<ptn.children.size();i++) {
			 annotateYield(ptn.children.elementAt(i));
		    }
    	}
	return;
  }

    public static ParseTreeNode leftBinarize(ParseTreeNode ptn) {
    	if(ptn.isTerminal()){
			return ptn;
		}
    	else if(ptn.children.size() > 2){
    		ParseTreeNode newnode = new ParseTreeNode();
    		for(int i=0;i<ptn.children.size()-1;i++){
    			newnode.children.add(ptn.children.elementAt(i));
    		}
    		if(! ptn.nodetype.endsWith("BAR")){
    			newnode.nodetype=ptn.nodetype+"BAR";
    		}else {
    			newnode.nodetype=ptn.nodetype;
    		}
    		ParseTreeNode lastelement = ptn.children.elementAt(ptn.children.size()-1);
   		
    		ptn.children.clear();
    		ptn.children.add(newnode);
    		ptn.children.add(lastelement);
    		leftBinarize(ptn);
    	}
    	else {
    		for(int i=0;i<ptn.children.size();i++){
    			leftBinarize(ptn.children.elementAt(i));
    		}
    	}
    return ptn;
    }

    public static ParseTreeNode rightBinarize(ParseTreeNode ptn) {
    	if(ptn.isTerminal()){
			return ptn;
		}
    	else if(ptn.children.size() > 2){
    		ParseTreeNode newnode = new ParseTreeNode();
    		for(int i=1;i<ptn.children.size();i++){
    			newnode.children.add(ptn.children.elementAt(i));
    		}
    		if(! ptn.nodetype.endsWith("BAR")){
    			newnode.nodetype=ptn.nodetype+"BAR";
    		}else {
    			newnode.nodetype=ptn.nodetype;
    		}
    		ParseTreeNode firstelement = ptn.children.elementAt(0);
   		
    		ptn.children.clear();
    		ptn.children.add(firstelement);
    		ptn.children.add(newnode);
    		
    		rightBinarize(ptn);
    	}
    	else {
    		for(int i=0;i<ptn.children.size();i++){
    			rightBinarize(ptn.children.elementAt(i));
    		}
    	}
    return ptn;
    }
    
    public static ParseTreeNode collapseUnaryToLowest(ParseTreeNode ptn){
    	if(ptn.children.size() == 1){
    		// Unary rule
    		
    		// First check if the only children is terminal. In that case no collapse
    		if(ptn.children.elementAt(0).isTerminal()){
    			return ptn;
    		}
    		else{
    			// System.err.println("Collapsing node "+ptn.nodetype);
        		// else collapse
    			int parentIndex = ptn.parentIndex;
    			ptn = ptn.children.elementAt(0);
    			ptn.parent = ptn.parent.parent;
    			ptn.parentIndex = parentIndex;
    			// If this is not the root
    			if(ptn.parent != null){
    				ptn.parent.children.set(parentIndex, ptn);
    			}
    			else{
    				ptn.isRoot = true;
    			}
    		}
    	}

    	// recurse on children
    	for(ParseTreeNode child : ptn.children){
    		// no need to go to the children that are terminal
    		if(child.isTerminal())
    			continue;
    		child = collapseUnaryToLowest(child);
    	}
    	return ptn;
    }


    // Attach head position information onto each node in the lexicalized parse tree 
    public static ParseTreeNode headPosition(ParseTreeNode ptn) 
	   {
			if(ptn.isTerminal())
				ptn.head_position=ptn.sStart;		 
			
		  	if(!ptn.isTerminal()) {
			  for (int i=0;i<ptn.children.size();i++) {
				  if(ptn.children.elementAt(i).head.equals(ptn.head)){
					  ptn.head_position=ptn.children.elementAt(i).sStart;					  
				  }
				 headPosition(ptn.children.elementAt(i));
			    }
			 }
		  	return ptn;
	   }
    
    public static ParseTreeNode headAnnotate(ParseTreeNode ptn) 
	   {
			if(ptn.isTerminal())
				ptn.nodetype+="-"+ptn.head;		 
			
		  	if(!ptn.isTerminal()) {
		  		ptn.nodetype+="-"+ptn.head;
			  for (int i=0;i<ptn.children.size();i++) {		 
				 headAnnotate(ptn.children.elementAt(i));
			    }
			 }
		  	return ptn;
	   }
 
    public static ParseTreeNode punctMap(ParseTreeNode ptn) 
   {
		Pattern PUNCT =  Pattern.compile("^[,\\.\\:\\-]$");    	 		 
		Matcher lookup = PUNCT.matcher(ptn.nodetype);
		if(lookup.find()){
  			ptn.nodetype = "PUNCT";		  				
		}
		  for (int i=0;i<ptn.children.size();i++) {		 
			 punctMap(ptn.children.elementAt(i));
		    }
	  	return ptn;
   }
    
    public static ParseTreeNode headTypeAnnotate(ParseTreeNode ptn) 
   {
			if(ptn.isTerminal())
				ptn.nodetype+="-"+ptn.head_type;		 
			
		  	if(!ptn.isTerminal()) {
		  		ptn.nodetype+="-"+ptn.head_type;
			  for (int i=0;i<ptn.children.size();i++) {		 
				 headTypeAnnotate(ptn.children.elementAt(i));
			    }
			 }
		  	return ptn;
   }
    
    public static String ptbString2(ParseTreeNode ptn) 
	   {
			String str="";
			if(ptn.isTerminal())
				str += "["+ptn.nodetype +" "+ptn.sString.elementAt(0)+"]";		 
			
		  	if(!ptn.isTerminal()) {
		  		str+="["+ptn.nodetype +" ";
			  for (int i=0;i<ptn.children.size();i++) {		 
				 str+=ptbString2(ptn.children.elementAt(i));
			    }
			  str+="]";
			 }
		  	return str;
	   }
    
	public static String ptbString(ParseTreeNode ptn) 
	   {
			String str="";
			if(ptn.isTerminal()) {
				// If it is a lexicalized parse tree
				if(ptn.head!=""){
					str += "("+ptn.nodetype +"["+ptn.head+"] "+ptn.sString.elementAt(0)+")";
				}else {
					str += "("+ptn.nodetype +" "+ptn.sString.elementAt(0)+")";
				}
			}
			
		  	if(!ptn.isTerminal()) {
				if(ptn.head!=""){
					str += "("+ptn.nodetype +"["+ptn.head+"] ";
				}else {
					str+="("+ptn.nodetype +" ";
				}
		  		
			  for (int i=0;i<ptn.children.size();i++) {		 
				 str+=ptbString(ptn.children.elementAt(i));
			    }
			  str+=")";
			 }
		  	return str;
	   }
	
    public static String getCFG(ParseTreeNode ptn)
    {
		String str = "";
	  	if(!ptn.isTerminal()) {
	  		str+=ptn.nodetype+"->";
		  for (int i=0;i<ptn.children.size();i++) {
			  str+=(ptn.children.elementAt(i).nodetype+" ");
			  
			  if(ptn.nodetype.equals("-NONE-"))
				  continue;
			  
			  System.out.println(getCFG(ptn.children.elementAt(i)));
		    }
		 } 
	  	return str;
  }
    
    public static String getString(ParseTreeNode ptn)
    {
		String str="";
		
	  	if(!ptn.isTerminal()) {
		  for (int i=0;i<ptn.children.size();i++) {
			  if(ptn.nodetype.equals("-NONE-"))
				  continue;
			 str+=getString(ptn.children.elementAt(i));
		    }
		 }
	  	 else
		 {
			 return ptn.sString.elementAt(0)+" ";
		 }
	return str;
  }
    	
	public static String treeString(ParseTreeNode ptn) 
   {
		String str="";

		if(ptn.isFrontier() && ptn.tStart!=-1){
			str+="|Frontier";
		}
		// Only for cases for T2T 
		if( ! ptn.tnodetype.equals("")){
			str+="|Decomposition";
		}
		str+="("+ptn.sStart+" "+ptn.sEnd+") -> ("+ptn.tStart+" "+ptn.tEnd+") ";
		str+="("+ptn.fs.toString()+")";
		str+=ptn.id+":"+ptn.nodetype +"-"+ ptn.tnodetype+" ";
		str+=ptn.complementspan.toString();
		if(ptn.isTerminal())
			str += " " + ptn.getS();		 
		
		str+="\n";
	  	if(!ptn.isTerminal()) {
		  for (int i=0;i<ptn.children.size();i++) {		 
			 str+=treeString(ptn.children.elementAt(i));
		    }
		 }
	  	return str;
   }
	
	public static void tiburonString(ParseTreeNode ptn, StringBuilder str){
		if(ptn.parentIndex > 0)
			str.append(' ');
		str.append(ptn.nodetype);
		str.append('(');
		
		// Check if terminal
		if(ptn.isTerminal()){
			str.append(ptn.getS());
		}
		else{
			for(ParseTreeNode child : ptn.children){
				tiburonString(child,str);
			}
		}

		str.append(')');
	}
}
