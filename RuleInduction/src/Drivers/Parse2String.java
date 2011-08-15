package Drivers;
/**
 * Counts the distribution of node types for a given Parse File input
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import TreeParser.*;
import Utils.ParseTree;
import java.util.*;

public class Parse2String {
	
	Hashtable<String,Integer> nodecounter = null;
	static int lengthlimit = 15;
     public static void main(String[] args) throws Exception {

    	String test = "(S1 (S (NP (DT the) (JJ quick) (JJ brown) (NN fox)) (VP (NP-SBJ~4 (-NONE- *))(VBD jumped) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))";

   	 	if(args.length!=1){
   	 		System.out.println("java CLASS <parsetreesfile>");
   	 		System.exit(0);
   	 	}
   	 	countFile(args[0]);
    }
     
    public static void countFile(String fileReference) throws Exception
 	{
 		BufferedReader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileReference)));
 		String lineFile;
 		int cnt = 1;
 		
 		while ((lineFile = inputFile.readLine()) != null)
 		{
 			//System.out.println(cnt+":"+lineFile);
 			ParseTreeNode tree = ParseTree.buildTree(lineFile);
 			//calculateSourceSpans(tree);
 			System.out.println(ParseTree.getString(tree));
 		cnt++;
 		}
 		inputFile.close();
 		System.err.println("Processed:"+cnt+" sentences");
 	}
}
