/**
 * 
 */
package drivers;


import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;


import java.net.*;
import java.security.*;

import java.io.*;
 import java.util.*;

import options.Options;

import taruDecoder.*; 
import taruDecoder.hyperGraph.*;
import taruHypothesis.Hypothesis;
import treeParser.*;
import utils.evaluation.BleuScore;
import utils.lm.VocabularyBackOffLM;

/**
 * This class implements  a wrapper for Taru to act as a server
 * 
 * @author Vamshi
 * 
 */
public class TaruServer {
		
	static VocabularyBackOffLM lm;
	static ArrayList<String> refs;
	
	static Taru decoder = null;
	static LexicalizedParser lp = null; 
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length!=3)
		{
			System.out.println("Usage: java TaruServer <config file> <port_number> <Stanford eng-grammar.gz>");
			
			System.exit(0);
		}
		String cfgfile = args[0];
		int portnumber = Integer.parseInt(args[1]);
		String graPath = args[2];
		
		System.err.println("Config path is: "+cfgfile);
		Options opts = new Options(cfgfile); 
								
		int sCount = 0;
		int total = -1;
	 	 
		// Stanford parser 
	     lp = new LexicalizedParser(graPath);
	     lp.setOptionFlags(new String[]
	                               {"-maxLength", "80",
	    		 					"-outputFormat", "oneline" 
	    		 				//	"-escaper", "edu.stanford.nlp.process.PTBEscapingProcessor",
	    		 				//	"-tokenized"
	    		 					});
	     System.err.println("Stanford Parser loaded.");

	    // Taru server
	 	HashMap<String,Integer> vocabulary = null;  // LOAD everything 
		decoder = new Taru(cfgfile,vocabulary);
	    System.err.println("Taru Server loaded.");

		// Now start server socket and listen for connections 
			int maxConnections = 0; // Allow all
			int i=0;
		 	  try{
		 	      ServerSocket listener = new ServerSocket(portnumber);
		 	      Socket server;
		 	      
		 	     System.err.println("Waiting for connections...........");
		 	      while((i++ < maxConnections) || (maxConnections == 0)){
		 	        doTranslate connection;
		 	        server = listener.accept();
		 	        doTranslate conn_c= new doTranslate(server);
		 	        Thread t = new Thread(conn_c);
		 	        t.start();
		 	      }
		 	    } catch (IOException ioe) {
		 	      System.out.println("IOException on socket listen: " + ioe);
		 	      ioe.printStackTrace();
		 	    }
		}		
	}

	class doTranslate implements Runnable {
	    private Socket server;
	    private String line;
	
	    doTranslate(Socket server) {
	      this.server=server;
	    }
	
	    @SuppressWarnings("deprecation")
		public void run () {
	 
	      try {
	        // Get input from the client
	        BufferedReader in = new BufferedReader (new InputStreamReader(server.getInputStream()));
	        PrintStream out = new PrintStream(server.getOutputStream());
	
	        while((line = in.readLine()) != null && !line.equals(".")) {
	          String parseTree = stanfordParse(line);
	          out.println(parseTree);
	          String output = translate(parseTree);
	          out.println(output);
	          out.flush();
	        }
	        server.close();
	      } catch (IOException ioe) {
	        System.out.println("IOException on socket listen: " + ioe);
	        ioe.printStackTrace();
	      }
	    }
	    
	    // Returns a lexicalized stanford parser output 
	    public String stanfordParse(String input) {
	        String[] sent = input.split("\\s+"); 
	        Tree parse = (Tree) TaruServer.lp.apply(Arrays.asList(sent));
	        
	        //Strip the scores 
	        String scoredParse = parse.toString();  
	        String unscoredParse = scoredParse.replaceAll("\\[\\d+\\.\\d+\\]", "");
	        return unscoredParse;
	      }

	    // Take as input a parse tree and return the translation (Tree 2 String) 
	    public String translate (String parseTree){
	    	String output = "";
	 	      
	    	// Decode and extract only the top best 
	    	List<Hypothesis> hyps = TaruServer.decoder.decodeParseTree(parseTree,1);	
  
				if(hyps.size()>0){ 
					output = hyps.get(0).getWords();
				}else {
					//" UNKNOWN" - just return string as is 
					ParseTreeNode ptn = ParseTree.buildTree(parseTree);
					output = ParseTree.getString(ptn);
				}

		return output;
	    }
}