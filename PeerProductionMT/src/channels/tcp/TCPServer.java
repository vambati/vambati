package channels.tcp;
import mturk.*; 
import java.io.*;
import java.net.*;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import channels.P2PServer;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import mturk.MturkInterface;
import mturk.RequestProcessor;

public class TCPServer extends P2PServer {

	public TCPServer(){
		try { 
	        // Lookup a factory for the W3C XML Schema language
	         factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
	        
	        // Compile the schema. 
	        // Here the schema is loaded from a java.io.File, but you could use 
	        // a java.net.URL or a javax.xml.transform.Source instead.
	        File schemaLocation = new File(xsdFile);
	        Schema schema = factory.newSchema(schemaLocation);
	    
	        // Get a validator from the schema.
	         validator = schema.newValidator();
		}
		catch(Exception e){
		}
	}
	
    public static void main(String[] args)  {
    	if(args.length!=0){
    		System.err.println("Usage: java P2PServer");
    		System.exit(0);
    	}
    	TCPServer myserver = new TCPServer();
    	myserver.start(); 
    }
    
    public void start()  {
    	System.out.println("Started PeerProduction Server");
        try {
	        ServerSocket serverSocket = new ServerSocket(PORT_NUMBER); 
	        while(true)
	        {
	           Socket connectionSocket = serverSocket.accept();
	           
	           System.out.println("New client asked for a connection");
	           // Log the incoming data here 
	   			String batchIdentifier = System.currentTimeMillis()+"";

	   		   System.err.println("Starting a thread to handle this File from Client");
	           RequestProcessor cT = new RequestProcessor(connectionSocket,dirPath,batchIdentifier); // make a thread of it
	        }
        }catch(Exception e){
        	System.err.println("Can not run server");
        	System.err.println(e.toString());
        }
    }
    
    public static boolean isValid(String xmlFile){
        // 4. Parse the document you want to check.
        Source source = new StreamSource(xmlFile);
        
        // 5. Check the document
        try {
            validator.validate(source);
            System.err.println("---"+ xmlFile + " is valid.");
        }
        catch (Exception ex) {
            System.err.println(xmlFile + " is not valid because ");
            System.err.println(ex.toString());
            return false; 
        }     	
        return true;
    }
}