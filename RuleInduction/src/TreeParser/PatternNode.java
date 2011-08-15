/**
 * 
 */
package TreeParser;

import java.util.Vector;

import RuleLearner.OrderPair;
import Utils.MyUtils;

/**
 * @author abhayaa
 *
 */
public class PatternNode {
	public int id = -1;
    public String nodetype ="";
    public Vector<PatternNode> children;

    private boolean isRoot;
    private boolean isTerminal;
    private boolean isFrontier;
    
    public int sStart=-1;
    public int sEnd=-1;
    
    private String text = "";
    // Only adjusted when we have a Target Parse tree
    public int tnodeid = -1;
    public String tnodetype="";
    
    public int alignNum = -1;
    
// Abhaya's data    
    public PatternNode parent;
    public int parentIndex;    
    
    public PatternNode() {
	    isRoot = false;
	    isFrontier = false; 
	    isTerminal = false;
	    
		children = new Vector<PatternNode>();
		parent = null;
    }
    
    public String getS()
    {
    	return text;
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
    
    public boolean setRoot(){
    	return isRoot = true;
    }
    
    public boolean setFrontier(){
    	return isFrontier = true;
    }
    
    public boolean setTerminal(){
    	return isTerminal = true;
    }
    
    public void setS(String text){
    	this.text = text;
    }
}
