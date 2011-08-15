package treeParser;

import java.io.*;

import utils.MyUtils;

public class ParseTree {

    public ParseTree() {
    }

    public static ParseTreeNode buildTree(String tree) 
    {
		StringReader sr = new StringReader(tree);
		
		ParseTreeLexer ptl = new ParseTreeLexer(sr);
		ParseTreeParser ptp = new ParseTreeParser(ptl);
		
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
		
		//Annotate parse tree with yields 
		annotateNode(ptn);
		// Annotate parse tree with IDs in post order (Useful for the Tree2String decoding - Yang Liu style)
		annotateId_postorder(ptn,0);
	return ptn;
    }

    public static int annotateId_postorder(ParseTreeNode ptn,int id)
    {
    	if(ptn.isTerminal){
    		ptn.id = id;
    		return id;
    	}else{
		  for (int i=0;i<ptn.children.size();i++) {
			 id = annotateId_postorder(ptn.children.elementAt(i),id);
			 ptn.children.elementAt(i).id = id++;
		  }
		  ptn.id = id;
    	}
	return id;
  }
    
    public static void annotateNode(ParseTreeNode ptn)
    {
    	if(ptn.isTerminal()){
    		// Lower case it 
    		//ptn.yield = ptn.getS().toLowerCase();
    		ptn.yield = ptn.getS();
    	}else{
    		//ptn.yield = MyUtils.rtrim(getString(ptn)).toLowerCase();
    		ptn.yield = MyUtils.rtrim(getString(ptn));
		  for (int i=0;i<ptn.children.size();i++) {
			 annotateNode(ptn.children.elementAt(i));
		    }
    	}
	return;
  }

    public static ParseTreeNode collapseUnaryToLowest(ParseTreeNode ptn){
    	if(ptn.children.size() == 1){
    		// Unary rule
    		
    		// First check if the only children is terminal. In that case no collapse
    		if(ptn.children.elementAt(0).isTerminal()){
    			return ptn;
    		}
    		else{
//    			System.out.println("Collapsing node "+ptn.nodetype);
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
    				ptn.isFrontier = true;
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
    
    public static String getString(ParseTreeNode ptn)
    {
		String str="";
		
	  	if(!ptn.isTerminal()) {
		  for (int i=0;i<ptn.children.size();i++) {
			  if(ptn.nodetype.equals("-NONE-"))
				  continue;
			 str+=getString(ptn.children.elementAt(i));
		    }
		 } else {
			 return ptn.sString.elementAt(0)+" ";
		 }
	return str;
  }
    	
	public static String treeString(ParseTreeNode ptn) 
   {
		String str="";

		if(ptn.isFrontier()){
			str+="|Frontier";
		}
		str+="("+ptn.sStart+" "+ptn.sEnd+") ";
		str+=ptn.id+":"+ptn.nodetype + " ";
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
