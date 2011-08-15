package p2p;

///////////////////////////


/// BROKEN CODE : Refer to P2PServer in PeerProductionMT project 


//////////////////////////
import java.io.*;
import java.net.*;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

public class P2PServer {
//	static String xsdFile = "/afs/cs/user/vamshi/workspace-eclipse/ActiveLearningforMT/data/p2p/translations.xsd";
//	public static String dirPath = "/afs/cs/user/vamshi/mturk-data/peerproduction-tmp/bbn";
	static int PORT_NUMBER = 6789;
	
	// Protocol strings 
	static String start = "BBN-CMU-START";
	static String end = "BBN-CMU-END";
	
	SchemaFactory factory = null;
	Validator validator = null; 
	
	public P2PServer(){
		try { 
	        // 1. Lookup a factory for the W3C XML Schema language
	         factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
	        
//	        // 2. Compile the schema. 
//	        // Here the schema is loaded from a java.io.File, but you could use 
//	        // a java.net.URL or a javax.xml.transform.Source instead.
//	        File schemaLocation = new File(xsdFile);
//	        Schema schema = factory.newSchema(schemaLocation);
//	    
//	        // 3. Get a validator from the schema.
//	         validator = schema.newValidator();
		}
		catch(Exception e){
		}
	}
	
    public static void main(String[] args)  {
    	if(args.length!=0){
    		System.err.println("Usage: java P2PServer");
    		System.exit(0);
    	}
    	P2PServer myserver = new P2PServer();
    	myserver.start(); 
    }
    
    public void start()  {
        try {
	        ServerSocket serverSocket = new ServerSocket(PORT_NUMBER); 
	        while(true)
	        {
	           Socket connectionSocket = serverSocket.accept();
	           
//	           System.out.println("New client asked for a connection");
//	           // Log the incoming data here 
//	   			String filename = System.currentTimeMillis()+".xml";
//	   			filename= dirPath+"/"+filename; 
//	   			
//	           CommunicatorThread t = new CommunicatorThread(connectionSocket,filename);    // make a thread of it
//	           System.err.println("Starting a thread for a new Client");
//	           int lines = t.receiveData();
//	           if(lines!=-1){
//	        	   System.err.println("Sending mail to BBN"+lines); 
//	           }else{
//	        	   System.err.println("ERROR ! lines"+lines);
//	           }
//	           
	        }
        }catch(Exception e){
        	System.err.println("Can not run server");
        	System.err.println(e.toString());
        }
    }
    
    public boolean validate(String xmlFile){
        // 4. Parse the document you want to check.
        Source source = new StreamSource(xmlFile);
        
        // 5. Check the document
        try {
            validator.validate(source);
            System.err.println(xmlFile + " is valid.");
        }
        catch (Exception ex) {
            System.err.println(xmlFile + " is not valid because ");
            System.err.println(ex.toString());
            return false; 
        }     	
        return true;
    }

}