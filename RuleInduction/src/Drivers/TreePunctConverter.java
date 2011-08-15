package Drivers;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import TreeParser.*;
import Utils.ParseTree;

public class TreePunctConverter {
     public static void main(String[] args) throws Exception{

    	String test = "(S1 (S (NP (DT the) (JJ quick) (JJ brown) (NN fox)) (VP (NP-SBJ~4 (-NONE- *))(VBD jumped) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))";
    	//punct(test);
   	 	if(args.length!=1){
   	 		System.out.println("java CLASS <parsetreesfile>");
   	 		System.exit(0);
   	 	}else{
   	 		punctFile(args[0]);
   	 	}
    }

    public static void punct(String line) throws Exception
  	{
    	 	//System.err.println(line);
    	 	ParseTreeNode tree = ParseTree.buildTree(line); 
  			tree = ParseTree.punctMap(tree);
  			
  			String str = ParseTree.ptbString(tree);
  			System.out.println(str);
  	}
     
    public static void punctFile(String fileReference) throws Exception
 	{
 		BufferedReader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileReference)));
 		String lineFile;
 		int cnt = 0;
 		
 		while ((lineFile = inputFile.readLine()) != null)
 		{ 
 			punct(lineFile);
 			cnt++;
 			if(cnt%10000==0){
 				System.err.println("Converted:"+cnt+" sentences");
 			}
 		}
 		inputFile.close();
 		System.err.println("Converted:"+cnt+" sentences");
 	}
}