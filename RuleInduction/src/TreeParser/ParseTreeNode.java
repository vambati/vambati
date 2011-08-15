package TreeParser;

import java.util.BitSet;
import java.util.Vector;

import Rule.FeatureSet;
import RuleLearner.*;
import Utils.MyUtils;

public class ParseTreeNode {
	public int id = -1;
    public String nodetype ="";
    
    public Vector<String> sString;
    public Vector<String> tString;
    public Vector<ParseTreeNode> children;
        
    public boolean isRoot;
    public boolean isTerminal;
    public boolean isPreterminal;
    public boolean isFrontier;
    
    public int sStart=-1;
    public int sEnd=-1;
    
    public String yield="";
    
    // Closure span is kept track of by just 2 points, 
    // Is difficult when 1 word aligns to disjoint phrases  
    public int tStart=-1;
    public int tEnd=-1;
    
    // Only adjusted when we have a Target Parse tree
    public int tnodeid = -1;
    public String tnodetype="";
        
    //Galley style
    public Vector<OrderPair> complementspan;
        
    // Abhaya galley span and complement span
    public BitSet span;
    public BitSet cSpan;
    
    
    // HACK HACK 
    // Pointer to the target side parse treenode . ONLY USED FOR (T2T) output format
    // Easier to have a direct pointer to the target parse tree node it is aligned to 
    public ParseTreeNode targetnodePtr = null;
    public int alignNum = 0;
    // HACK HACK 
    
// Abhaya's data    
    public ParseTreeNode parent;
    public int parentIndex;
    private String hashStr;
    
    // Lexicalization  
    public String head="";
    public int head_position = -1;
    public String head_type ="";
    public String head_tgt ="";
    
    // All other features (both SOURCE and TARGET node related)
    public FeatureSet fs = null;
    
    public ParseTreeNode() {
	    isRoot = false;
	    isPreterminal = false;
	    isFrontier = false; 
	    isTerminal = false;
	    
	    sString = new Vector<String>(); 
	    tString = new Vector<String>();
	    complementspan = new Vector<OrderPair>();
		children = new Vector<ParseTreeNode>();
		parent = null;
		span = new BitSet(64);
		cSpan = new BitSet(64);
		
		fs = new FeatureSet();
    }
    
    public String getS()
    {
    	String str="";
    	for(int i=0;i<sString.size();i++)
    		str+=sString.elementAt(i)+" ";
    	
    	//return MyUtils.rtrim(yield);
    	return MyUtils.rtrim(yield);
    }
    public String getT()
    {
    	String str="";
    	for(int i=0;i<tString.size();i++)
    		str+=tString.elementAt(i)+" ";
    	
    	return MyUtils.rtrim(str);
    }
    
    public boolean isTerminal()
    {
    	return isTerminal;
    }
    
    public boolean isPreterminal()
    {
    	return isPreterminal;
    }
    
    public boolean isRoot()
    {
    	return isRoot;
    }
    
    public boolean isFrontier()
    {
    	return isFrontier;
    }
    
    public String getHashString(){
    	if(hashStr == null){
    		hashStr = nodetype + "_";
    		for(ParseTreeNode ptn : children){
    			hashStr += ptn.nodetype + "_";
    		}
    	}
    	return hashStr;
    }
}