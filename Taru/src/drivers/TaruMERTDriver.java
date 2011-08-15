/**
 * 
 */
package drivers;

import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import options.Options;
import taruDecoder.*;
import taruHypothesis.Hypothesis;
import treeParser.ParseTree;
import treeParser.ParseTreeNode;
 
/**
 * This class implements a top down tree to tree decoder
 * 
 * @author abhayaa
 * 
 */
public class TaruMERTDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if(args.length==1)
		{
			String cfgfile = args[0];
			System.err.println("Config path is: "+cfgfile);
			Options opts = new Options(cfgfile); 

			String inputFile = opts.get("SOURCE");
			String refFile = opts.get("REFS");
						
			System.err.println("Source...."+inputFile);
			System.err.println("References...."+refFile);
						
		 	long sTime = System.currentTimeMillis();
			// Now load Input from source file and decode one by one 
 				BufferedReader br = new BufferedReader(new FileReader(inputFile));
				 
				int sCount = 0;
				int total = -1;
			 	 
			 	HashMap<String,Integer> vocabulary = new HashMap<String, Integer>();
			 	ArrayList<String> inputParses = new ArrayList<String>(500);

				String input = "";
				while ((input = br.readLine()) != null) {
					total++;
					inputParses.add(input);
					ParseTreeNode ptn = ParseTree.buildTree(input);
					// Add to known vocabulary 
					String str = ParseTree.getString(ptn);
					for(String w: str.split(" +")){
						vocabulary.put(w, 1);
						//vocabulary.put(w.toLowerCase(), 1);
					}
				}	
		 	
				total=0;
				TaruTrainingWrapper decoder = new TaruTrainingWrapper(args[0],vocabulary);
				Iterator<String> inps = inputParses.iterator();
				while ( inps.hasNext()) {
					String parseTree = inps.next();
					// Format for NBEST START 
					System.out.println("Decoding " + total);
					List<Hypothesis> hyps = decoder.decode(parseTree, 200);
					if(hyps.size() > 0){
						sCount++;
						for(Hypothesis h : hyps){
							System.out.println(h.toOptNBestFormat());
						}
					}
					else{
						System.out.println();
					}
					System.out.println();
					// Format for NBEST END
					total++;
				}
			 	long eTime = System.currentTimeMillis();
				System.err.println("Successfully decoded " + sCount + " sentences out of " + total);
				System.err.println("Time Taken: " + ((eTime - sTime) / 1000));
		}
	}
}