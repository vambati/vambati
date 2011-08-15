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

public class TreeNodeCounter {
	
	Hashtable<String,Integer> nodecounter = null;
	static int lengthlimit = 15;
     public static void main(String[] args) throws Exception{

    	String test = "(S1 (S (NP (DT the) (JJ quick) (JJ brown) (NN fox)) (VP (NP-SBJ~4 (-NONE- *))(VBD jumped) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))";

    	TreeNodeCounter tnc = new TreeNodeCounter();
    	
   	 	if(args.length!=1){
   	 		System.out.println("java CLASS <parsetreesfile>");
   	 		System.exit(0);
   	 	}else if(args.length==2){
   	 		tnc.countFile(args[0]);
   	 		lengthlimit = Integer.parseInt(args[1]);
   	 	} else if(args.length==1){
	 		tnc.countFile(args[0]);
	 		lengthlimit = 1500; // Some large number
	 	}
   	 	tnc.printstats();
    }
     public TreeNodeCounter() {
    	 nodecounter = new Hashtable<String,Integer>();
     }
     
     public void printstats()
     {
    	 int total=0;
    	 for(String key: nodecounter.keySet()){
    		 int value = nodecounter.get(key).intValue();
    		 System.out.println(key+"\t"+value);
    		 total+=value;
    	 }
    	 System.out.println("-------------");
    	 System.out.println("Total\t"+total);
     }
      
     public void countNodes (ParseTreeNode ptn) {
     	if(ptn.isTerminal()){
 			return;
 		}
     	else {
     		if((ptn.sEnd-ptn.sStart)<lengthlimit) {
     			String nodetype = ptn.nodetype;
     			nodetype = nodetype.replaceAll("\\[.+\\]$", "");
     			
		     		if(nodecounter.get(nodetype)!=null) {
		     			nodecounter.put(nodetype,nodecounter.get(nodetype)+1);
		     		}else {
		     			nodecounter.put(nodetype,new Integer(1));
		     		}
     		}
     		for(int i=0;i<ptn.children.size();i++){
     			countNodes(ptn.children.elementAt(i));
     		}
     	}
     return;
     }
     
    public void countFile(String fileReference) throws Exception
 	{
 		BufferedReader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileReference)));
 		String lineFile;
 		int cnt = 0;
 		
 		while ((lineFile = inputFile.readLine()) != null)
 		{
 			ParseTreeNode tree = ParseTree.buildTree(lineFile);
 			//calculateSourceSpans(tree);
 			countNodes(tree);
 			cnt++;
 			if(cnt%100000==0){
 				System.err.println("Processed:"+cnt+" sentences");
 			}
 		}
 		inputFile.close();
 		System.err.println("Processed:"+cnt+" sentences");
 	}
}
