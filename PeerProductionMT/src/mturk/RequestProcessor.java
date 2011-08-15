package mturk;
import java.net.*; 
import java.io.*; 


import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import channels.P2PServer;
import channels.mail.MailInterface;
import channels.tcp.TCPServer;


public class RequestProcessor extends Thread{
	// the socket where to listen/talk
	BufferedReader infromClient;
	PrintWriter outtoClient;
	
	// File to write this log to 
	public String xmlFile,bbnFile,inputCSVFile, successFile, failFile;
	BufferedWriter xmlW = null; 
	BufferedWriter csvW = null;
	BufferedWriter bbnW = null;

	// TO process all files coming from EMAIL channel
	public RequestProcessor(String dirPath,String filename) {
		xmlFile = dirPath+"/"+filename;  // Plain xml

		String identifier = filename.replaceAll(".xml$","");
		
		bbnFile = dirPath+"/"+identifier+".bbn"; // Input file to create the final response 
		inputCSVFile = dirPath+"/"+identifier+".csv"; // CSV Input file for MTurk 
		successFile = dirPath+"/"+identifier+".success"; // HIT ids
		failFile = dirPath+"/"+identifier+".failure"; // HIT ids
	}

	public void extractXMLtoCSV() {
		System.err.println("Extracting CSV from XML");
		try {
		// csvW = new BufferedWriter(new FileWriter(inputCSVFile,false)); // Dont append
		// bbnW = new BufferedWriter(new FileWriter(bbnFile,false)); //Dont append
		csvW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputCSVFile),"UTF8"));
		bbnW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bbnFile),"UTF8"));
			
		XMLReader xr = XMLReaderFactory.createXMLReader();
		XML2MTurkCSVCreator handler = new XML2MTurkCSVCreator(csvW,bbnW); // Handler to understand XML and write to MTURK CSV file
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
		
		System.out.println("---Parsing XML using XSD");
			FileReader r = new FileReader(xmlFile); // input XML
		    xr.parse(new InputSource(r));	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    // Post with GOLD standard traps in between (Phrasal translations)
	public void crowdSource(String goldSeedFile) {
		long delay =  3 * 60 * 1000; //  3 minutes per HIT
	    int batch_size = 3;
	    MturkPoster mpGold = new MturkPoster(inputCSVFile,goldSeedFile, successFile,failFile,delay, batch_size);
	    mpGold.start();
	}
	
	   // Post without GOLD standard
	public void crowdSource() {
	     // Step 3: Create images out of sentences before posting  
	    P2PServer.createImages(P2PServer.imagesDirPath,bbnFile);
	      
		//Step 4: Post this to Mturk , and save results to successFile
	    long delay =  3 * 60 * 1000; //  3 minutes per HIT
	    int batch_size = 3; 	     
	    MturkPoster mp = new MturkPoster(inputCSVFile,successFile,failFile,delay, batch_size);
	    
	    mp.start();
	}
	
	// TO process all files coming from direct TCP channel
	public RequestProcessor(Socket socket,String dirPath,String batchIdentifier) {
		xmlFile = dirPath+"/"+batchIdentifier+".xml"; // Plain xml
		inputCSVFile = dirPath+"/"+batchIdentifier+".csv"; // CSV Input file 
		successFile = dirPath+"/"+batchIdentifier+".success"; // HIT ids
		failFile = dirPath+"/"+batchIdentifier+".failure"; // HIT ids
		
		try {
		infromClient  = new  BufferedReader(new InputStreamReader(socket.getInputStream()));
		outtoClient = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		// STEP 0: 
		System.out.println("1. Receiving XML file from BBN");
		   int lines = receiveData(xmlFile);
	       if(lines!=-1){
	    	   System.err.println("Receipt of success"+lines);
	       }else{
	    	   System.err.println("ERROR ! lines"+lines);
	       }   
		}catch(Exception e){
			System.out.println("Exception reading/writing  Streams: " + e);
		}
		finally{
			try {
				socket.close(); // Nothing more to read here
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		
//		try { 
//			processRequest();
//		}catch(Exception e){System.err.println("ERROR: Ending thread ! ");}
	}
	
	// Only for the socket case 
	public int receiveData(String file){	
        int i=0;
		try
		{
			// xmlW = new BufferedWriter(new FileWriter(file));
			xmlW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF8"));
			System.out.println("---Thread waiting for a String from the Client");
	        String inpSentence="";
	        
	        if((inpSentence = infromClient.readLine()).startsWith(TCPServer.start)) 
	        {
				while(!(inpSentence = infromClient.readLine()).startsWith(TCPServer.end)) {
			           // System.err.println("Received: " + inpSentence);
			           xmlW.write(inpSentence+"\n");
			           i++;
				}
	        }
	        else
	        {
	        	System.err.println("---Sorry! Wrong Code Word.\n Closing Connection");
	        	outtoClient.println("---Sorry! Wrong Code Word.");
	        	return -1; 
	        }
			outtoClient.println("---Received "+i+" lines. Thanks");
			return i; 
		}
		catch (Exception e) {
			System.err.println("ERROR:Exception reading/writing  Streams: " + e);
			return -1;
		}
		finally 
		{
			try {
				xmlW.flush();
		        outtoClient.flush();
				infromClient.close();
				outtoClient.close();
				xmlW.close(); 
			}
			catch (Exception e) {
				System.out.println("---Closing all connections:" + e);
			}
		}
	}
}
