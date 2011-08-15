/**
 * 
 */
package treeParser;

import java.io.Serializable;
import java.util.Vector;
import java.util.ArrayList;;
/**
 * @author abhayaa
 *
 */
public class PatternNode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id = -1;
    public String nodetype ="";
    public Vector<PatternNode> children;

    private boolean isRoot;
    private boolean isTerminal;
    private boolean isFrontier;
    
    public int sStart=-1;
    public int sEnd=-1;
    
    public  String text = "";
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
    
    public static void getVocab(PatternNode ptn,ArrayList<String> vocab)
    {  
	  	if(!ptn.isTerminal()) {
		  for (int i=0;i<ptn.children.size();i++) {
			  if(ptn.nodetype.equals("-NONE-"))
				  continue;
			  getVocab(ptn.children.elementAt(i),vocab);
		    }
		 }
	  	 else
		 {
			 vocab.add(ptn.getS());
		 }
  }
    
    public static String getString(PatternNode ptn)
    {
		String str="";
		str += "( " + ptn.nodetype + " "; 
	  	if(!ptn.isTerminal()) {
		  for (int i=0;i<ptn.children.size();i++) {
			  if(ptn.nodetype.equals("-NONE-"))
				  continue;
			 str+= getString(ptn.children.elementAt(i));
		    }
		 }
	  	 else
		 {
			 return "( " + ptn.nodetype + " " + ptn.getS() + ") ";
		 }
	  	str += ") ";
	return str;
  }
}
