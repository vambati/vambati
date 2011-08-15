package treeParser;

import java.util.ArrayList;
import java.util.Vector;

import utils.MyUtils;

public class ParseTreeNode {
	
	public int id = -1;
	
    public String nodetype ="";
    public String yield = "";
    
    public Vector<String> sString;
    public Vector<ParseTreeNode> children;

    public boolean isRoot;
    public boolean isTerminal;
    public boolean isFrontier;
    
    public int sStart=-1;
    public int sEnd=-1;

    // Abhaya's data    
    public ParseTreeNode parent;
    public int parentIndex;
    private String hashStr;
    public String spanString;
    
    public ArrayList<String> targetNodeTypes;
        
    public ParseTreeNode() {
	    isRoot = false;
	    isFrontier = false; 
	    isTerminal = false;
	    
	    sString = new Vector<String>(); 
		children = new Vector<ParseTreeNode>();
		parent = null;
		targetNodeTypes = new ArrayList<String>();
    }
    
    public String getS()
    {
    	String str="";
    	for(int i=0;i<sString.size();i++)
    		str+=sString.elementAt(i)+" ";
    	
    	return MyUtils.rtrim(str);
    }
    
    public boolean isTerminal()
    {
    	return isTerminal;
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