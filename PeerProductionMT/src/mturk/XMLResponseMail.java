package mturk;

// DOES NOT WORK - 

import org.w3c.dom.*;

import javax.xml.parsers.*; 
import javax.xml.transform.*; 
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 
import java.io.*;
import java.util.*;

public class XMLResponseMail {

	// Load the results into a Hashtable <req_id,target>  
	Hashtable<String,String> targets = null;  
	
	public XMLResponseMail(){
		targets = new Hashtable<String, String>();	
	}
	
	public static void createResponse(String resultsCSV,String reqXMLFile,String resXMLFile){
	// Load all results into Hashtable for respXML creation 
	loadHash(resultsCSV); 
	
	// Modify XML according to the Hashtable 
	try { 
	File file = new File(reqXMLFile);
	//Create instance of DocumentBuilderFactory
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	//Get the DocumentBuilder
	DocumentBuilder docBuilder = factory.newDocumentBuilder();
	//Parsing XML Document
	Document doc = docBuilder.parse(file); 
	//Pointing to document root element
	Node child = doc.getFirstChild();
	// Element root = doc.getDocumentElement();
 
	recursivelyUpdate(child);
	
	//Save it back to a new XML 
	// setting up a transformer
    TransformerFactory transfac = TransformerFactory.newInstance();
    Transformer trans = transfac.newTransformer();
 
    //generating string from xml tree
    StringWriter sw = new StringWriter();
    StreamResult result = new StreamResult(sw);
    DOMSource source = new DOMSource(doc);
    trans.transform(source, result);
    String xmlString = sw.toString();
 
    //Saving the XML content to File
    OutputStream f0 = new FileOutputStream(resXMLFile);
    byte buf[] = xmlString.getBytes();
    
    for(int i=0;i<buf .length;i++) {
    	f0.write(buf[i]);
    }
	f0.close();
	buf = null;
	}catch(Exception e){
		System.err.println(e.toString());
	}
	}
	private static void recursivelyUpdate(Node root) {
		if(!root.hasChildNodes())
		return;

		NodeList childrenList = root.getChildNodes();
		
		for(int i=0;i<childrenList.getLength();i++)  
		{
			Node child = childrenList.item(i);
			if(child.hasChildNodes())
			{
				recursivelyUpdate(child); 
			} 

//			 System.out.println("Name "+childElement.getName());
//			 System.out.println("Value "+childElement.getText());

			if(child.getLocalName().equals("source")) 
			{	
//				//create child element having tagName=number
//				Node childElement1 = child. Node("target");
//				childElement1.setTextContent("Human Translation 1");
//				Element childElement2 = root.createElement("target");
//				childElement2.setTextContent("Human Translation 2");
//				Element childElement3 = root.createElement("target");
//				childElement3.setTextContent("Human Translation 3");
//				 
//				child.appendChild(childElement1);
//				child.appendChild(childElement2);
//				child.appendChild(childElement3);
			}
		}
	}

	private static void loadHash(String resultsCSV) {
		
	}

	public static void main(String args[]){
		//createResponse(args[0],"res.xml");	
	}
}
