package channels.mail;

import javax.mail.* ;
import mturk.RequestProcessor;

import channels.P2PServer;

import java.io.*;

public class MailGetter extends P2PServer {
		
	MailInterface myMail = null;
	
	public MailGetter(){
		myMail = new MailInterface();
	}
	
	public void processMails(){
		// OPEN 
		myMail.openMail(); 
		
		// GET and PROCESS
		Message[] messages = myMail.getMessages("Inbox");
		BufferedWriter logWriter = null;
		
		if(messages.length>0){ // New mails
			System.err.println("New emails exist - Processing them!!!");
			try {  
				logWriter = new BufferedWriter(new FileWriter(P2PServer.CURRENT_LOG,true)); // APPEND 
			} catch(Exception e){
				System.err.println(e.toString());
			}
		}else{
			System.err.println("No new emails!");
			return; 
		}
		for (int i = 0; i < messages.length; i++) {
		Message message = messages[i];
		Address[] a;
		try {
			if ((a = message.getFrom()) != null) {
				String subject = message.getSubject();
				//System.err.println("SUBJECT:"+ subject);
				for (int j=0; j < a.length; j++) { 
					//System.err.println("FROM:"+ a[j].toString());
					String contentType = message.getContentType();
					//System.err.println("Content Type : " + contentType);
					
					// Extract attachments 
					Multipart mp = (Multipart) message.getContent();
					for (int k = 0; k < mp.getCount(); k++)
					{
						Part p = mp.getBodyPart(k);
						// If Attachment Exists 
						if(p.getFileName()!=null){
							System.err.println("Attached File: " + p.getFileName());
							// Write this as output 
							String filePath = P2PServer.dirPath+"/"+p.getFileName();
							BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath),"UTF8"));
							InputStreamReader isr = new InputStreamReader(p.getInputStream(),"UTF8");
							BufferedReader is = new BufferedReader(isr);
							int c; 
							while((c=is.read())!=-1){
								bw.write(c);
							}
							
							// Process it 
							System.err.println("Starting a thread to handle this File from Client");
							// Log the file 
							logWriter.write(filePath+"\n");
							
							// Close file handlers before starting to process data 
							bw.flush();bw.close();
							is.close();
							
					        RequestProcessor cT = new RequestProcessor(dirPath,p.getFileName()); // make a thread of it
					        
							try {
							       // Step 1: Validate XML and convert to CSV format for MTurk  
							       System.out.println("2.1 Validating XML ");
							       if(P2PServer.isValid(filePath))
							       {
							    	   System.out.println("2.2 Parsing XML to create Mturk CSV file - translation specific");
							    	   cT.extractXMLtoCSV();
							    	   
							    	   cT.crowdSource(P2PServer.GOLD_FILE);
							    	   // cT.crowdSource();
							       }
							       else{
							    	   System.err.println("ERROR: Invalid XML ");
							       }
							       								     
								} catch (Exception e) {
									System.err.println(e.toString());
								}
							}
						logWriter.flush();logWriter.close();
						}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
		// CLOSE
		myMail.closeMail();
	}
	
	public static void main(String args[]){
		MailGetter mi = new MailGetter();
		mi.processMails();
	}
}
