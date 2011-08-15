package drivers;


import treeParser.*;

public class TreeDriver {
     public static void main(String[] argv) {

    	 String test = "(S1 (S (NP (DT the) (JJ quick) (JJ brown) (NN fox)) (VP (NP-SBJ~4 (-NONE- *))(VBD jumped) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))";

    	 ParseTreeNode tree = ParseTree.buildTree(test);
    	
    	String treeStr = ParseTree.treeString(tree);
    	System.out.println(treeStr);
    }
}
