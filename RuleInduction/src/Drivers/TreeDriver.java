package Drivers;


import TreeParser.*;
import Utils.ParseTree;

public class TreeDriver {
     public static void main(String[] argv) {

    	 String test = "(S1 (S (NP (DT the) (JJ quick) (JJ brown) (NN fox)) (VP (NP-SBJ~4 (-NONE- *))(VBD jumped) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))";

    	 ParseTreeNode tree = ParseTree.buildTree(test);
    	 // Annotate context 
    	 ParseTree.annotateLeftContext(tree);
    	 ParseTree.annotateRightContext(tree);
    	 
    	String treeStr = ParseTree.treeString(tree);
    	
    	System.out.println(treeStr);
    	
    	String treeStr2 = ParseTree.ptbString(tree);
		System.out.println(treeStr2);
    }
}
