package mturk;

import java.io.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class XML2MTurkCSVCreator extends DefaultHandler{
	
	//String csvHeader = "src_lang\ttgt_lang\tfile_id\tdatetime\treq_id\tsource";
	// String csvHeader = "src_lang,tgt_lang,file_id,datetime,req_id,source"; // For internal question
	String csvHeader = "params"; // For external question 
	String fileID = "";
	String datetime="";
	String sourceString = "";
	String sourceID = "";
	String src_lang = "Spanish";
	String tgt_lang = "English";
	
	BufferedWriter csvW = null;
	BufferedWriter bbnW = null;
	
	    public XML2MTurkCSVCreator(BufferedWriter csvW,BufferedWriter bbnW)
	    {
	    	super();
	    	this.csvW = csvW;
	    	this.bbnW = bbnW; 
	    }

	    ////////////////////////////////////////////////////////////////////
	    // Event handlers.
	    ////////////////////////////////////////////////////////////////////
	    public void startDocument ()
	    {
	    	System.err.println("---Starting to read document.....");
	    	try { 
				csvW.write(csvHeader+"\n");
			}catch(Exception e){System.err.println(e.toString());}
	    }

	    public void endDocument ()
	    {
			System.err.println("---Received");
			System.err.println("---File ID:"+fileID);
			System.err.println("---Date:"+datetime);
			try {  
			    csvW.flush();
			    bbnW.flush(); 
			    csvW.close();
			    bbnW.close();
			}catch(Exception e){System.err.println(e.toString());}
	    }

	    public void startElement (String uri, String name, String qName, Attributes atts)
	    {
			if ("".equals (uri)){
				if(qName.equals("request")){
					fileID = atts.getValue("file_id"); 
					datetime = atts.getValue("datetime");
					src_lang = atts.getValue("source_lang");
					tgt_lang = atts.getValue("target_lang");
					try {
						bbnW.write(src_lang+"\t"+tgt_lang+"\t"+fileID+"\n");
					} catch (IOException e) {
						e.printStackTrace();
					} // BBN format to create response string
				}
				if(qName.equals("translation_request")){
					// System.out.println("Start element: " + qName);
					sourceID = atts.getValue("req_id");
				}
			}
	    }

	    public void endElement (String uri, String name, String qName)
	    {
	    	try { 
	    	if ("".equals (uri)){
				if(qName.equals("source")){
					// Print the CSV tuple
					// csvW.write(src_lang+"\t"+tgt_lang+"\t"+fileID+"\t"+datetime+"\t"+sourceID+"\t"+sourceString+"\n");
					//csvW.write(src_lang+","+tgt_lang+","+fileID+","+datetime+","+sourceID+","+sourceString+"\n");
					
					// sourceString=StringEscapeUtils.escapeHtml(sourceString);
					
					bbnW.write(sourceID+"\t"+sourceString+"\n"); // BBN format to create response string
					
					String params = "src_lang="+src_lang+"&tgt_lang="+tgt_lang+"&file_id="+fileID+"&req_id="+sourceID;
					// Escape URL as it is passed via HTTP_GET 
					sourceString = java.net.URLEncoder.encode(sourceString); 
					csvW.write(params+"&source="+sourceString+"\n"); 
				}
			}
	    	}catch(Exception e){
	    		System.err.println(e.toString());
	    	}
	    }
	    
	    public void characters (char ch[], int start, int length)
	    {
			sourceString = new String(ch,start,length);
	    }
}
