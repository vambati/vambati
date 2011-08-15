package Drivers;

import TreeParser.*;
import Utils.ParseTree;

public class LexicalizedTreeDriver {
     public static void main(String[] argv) {

    	 //String test = "(S1 (S (NP (DT the) (JJ quick) (JJ brown) (NN fox)) (VP (NP-SBJ~4 (-NONE- *))(VBD jumped) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))";
    	 String test = "(ROOT[is/VBZ]  (S[is/VBZ]    (NP[This/DT] (DT[This/DT] This))    (VP[is/VBZ] (VBZ[is/VBZ] is)      (ADVP[just/RB] (RB[just/RB] just))      (NP[test/NN] (DT[a/DT] a) (NN[test/NN] test)))    (.[./.] .)))";

    	 // Annotated with position information of head
    	ParseTreeNode tree = ParseTree.buildLexTree(test,-1);
    	String treeStr = ParseTree.ptbString(tree);
    	System.out.println(treeStr);

   	 // Annotated with head
    	ParseTreeNode tree2 = ParseTree.buildLexTree(test,1);
    	String treeStr2 = ParseTree.ptbString(tree2);
		System.out.println(treeStr2);
		
   	 // Annotated with head type info
		ParseTreeNode tree3 = ParseTree.buildLexTree(test,2);
    	String treeStr3 = ParseTree.ptbString(tree3);
		System.out.println(treeStr3);
    }
}
