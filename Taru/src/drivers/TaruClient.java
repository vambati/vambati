/**
 * 
 */
package drivers;
import java.net.*;
import java.security.*;

import java.io.*;
 import java.util.ArrayList;
import java.util.HashMap;

import options.Options;

import taruDecoder.*; 
import taruDecoder.hyperGraph.*;
import treeParser.*;
import utils.evaluation.BleuScore;
import utils.lm.VocabularyBackOffLM;

/**
 * This class implements  a wrapper for Taru to act as a Client
 * 
 * @author Vamshi
 * 
 */
public class TaruClient {
	 Socket clientSocket = null;
	 String host = "chicago.lti.cs.cmu.edu";
	 int port = 1234; 
	 //String test = "(S (NP (NN We)))";
	 String test = "I ate an apple";
	 
	    public TaruClient()  {
	    	System.err.println("Establishing connection with:"+host+":"+port);
	    	try {
	    		clientSocket = new Socket(host,port);
	    	}catch(Exception e){ System.err.println("Can not establish connection to host");}
	    }
	    
	    @SuppressWarnings("deprecation")
		public void test (){
	    	if (clientSocket == null) { // Check
			    return;
			}
	
			BufferedReader console   = new BufferedReader(new InputStreamReader(System.in));
			try {
		    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true /* autoFlush */);
	
		    //Test 
		    out.println(test);
		    String testparse  = in.readLine();
		    String testoutput = in.readLine();
	        System.err.println("Test Input:"+ test);
	        System.err.println("Test Parse:"+ testparse);
	        System.err.println("Test Translation:"+ testoutput);
	          
		    // Continue 
		    String line = "";
		    while ((line=console.readLine())!=null) {
		          out.println(line);
		          String parse = in.readLine();
		          String translation = in.readLine();
		          System.err.println("Input:"+ line);
		          System.err.println("Parse:"+ parse);
		          System.err.println("Translation:"+ translation);
		      }
			} catch (IOException e) {
				System.err.println("Error binding I/O of socket, " + e); 
	    	}
	    }
	    
	    public static void main(String args[]) {
	    	TaruClient tc = new TaruClient();
	    	tc.test();
	    	
	    }  
	}