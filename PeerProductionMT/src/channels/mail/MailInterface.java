package channels.mail;
import java.util.*;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;
import javax.mail.internet.* ;
import javax.mail.search.FlagTerm;
import javax.mail.* ;

import mturk.RequestProcessor;

import java.io.*;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import channels.P2PServer;

public class MailInterface {
		
	String IMAP_HOST = "imap.gmail.com";
	int IMAP_PORT  = 993;
	
	String SMTP_HOST = "smtp.gmail.com";
	int SMTP_PORT  = 465; 
	
	String user = "peerxml";
	String passwd = "bbncmu123";
	
	public String FROM = "peerxml@gmail.com";
	public static String TEST_TO = "vamshi.ambati@gmail.com";
	public static String TO = "peerxml@bbn.com";
	
	Properties props = null; 
	
	// Email variables 
	Folder inbox = null; 
	Store store = null; 
	
	public MailInterface(){
	
	// Get system properties
	props = System.getProperties();
	
	// SMTP mail server
	 props.put("mail.smtp.host", SMTP_HOST);
	 props.put("mail.smtp.port", SMTP_PORT+"");
	 props.put("mail.smtp.starttls.enable","true");
	 
	// IMAP related 
	 props.setProperty("mail.imap.host", IMAP_HOST);
	 props.setProperty("mail.imap.port", IMAP_PORT+"");
	 props.setProperty("mail.imap.connectiontimeout", "5000");
	 props.setProperty("mail.imap.timeout", "5000");
	 props.setProperty("mail.store.protocol","imaps");
	 props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	 props.setProperty("mail.imap.socketFactory.fallback", "false");
	}

	// Return emails from an inbox 
	public void openMail(){
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		try {
		  Session session = Session.getDefaultInstance(props, null);
		  store = session.getStore("imaps");
		  store.connect(SMTP_HOST, user+"@gmail.com", passwd);
		  System.err.println("Connected!");
		} catch (NoSuchProviderException e) {
			  e.printStackTrace();
			  System.exit(1);
			} catch (MessagingException e) {
			  e.printStackTrace();
			  System.exit(2);
			}
	}
	
	public Message[] getMessages(String foldername){
		Message[] messages = null; 
		try {
			inbox = store.getFolder(foldername);
			inbox.open(Folder.READ_WRITE); // After you fetch turn it as READ 
			//inbox.open(Folder.READ_ONLY); // Only fetch
			
			messages = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
			
			System.err.println("Total mails:"+inbox.getMessageCount());
			System.err.println("Unread mails:"+messages.length);
		} catch (MessagingException e) {
			e.printStackTrace();
		} 		  		  
		return messages;
	}
	
	public void closeMail(){	  
		try {
			inbox.close(true);
			store.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	// Send an attachment file as email
	public void sendAttachmentMail(String to,String filePath){
		// Define message
		try { 
		Session session = Session.getDefaultInstance(props, null);
		
		MimeMessage message = new MimeMessage(session); 
		message.setFrom(new InternetAddress(FROM));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject("[Peer-Production] response XML");
		
		// Message Body
		MimeBodyPart messagePart = new MimeBodyPart();
		messagePart.setText("Response from CMU");
		
		// Attachment 
		MimeBodyPart attachmentPart = new MimeBodyPart();
		FileDataSource fileDataSource = new FileDataSource(filePath);
		attachmentPart.setDataHandler(new DataHandler(fileDataSource));
		attachmentPart.setFileName(fileDataSource.getName());
		
		Multipart mp = new MimeMultipart(); 
		mp.addBodyPart(messagePart);
		mp.addBodyPart(attachmentPart);
		
		// Set body 
		message.setContent(mp);
		
		// Send message
		 final Transport transport = session.getTransport("smtps");
	     transport.connect(SMTP_HOST, SMTP_PORT, user, passwd);
	     
	     Address recepient = new InternetAddress(to);
	     Address[] recepients = new Address[1]; 
	     recepients[0] = recepient; 
	     transport.sendMessage(message,recepients);
	     
	     System.err.println("Attachment Message sent to:"+to);
		}catch(Exception e ){
			System.err.println(e.toString());
		}
	}
	
	// Send an attachment file as email
	public void sendConfirmationMail(String to){
		// Define message
		try { 
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage message = new MimeMessage(session); 
		message.setFrom(new InternetAddress(FROM));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject("[Peer-Production] from CMU");
		message.setText("Test");

		// Send message
		 final Transport transport = session.getTransport("smtps");
	     transport.connect(SMTP_HOST, SMTP_PORT, user, passwd);
	     Address recepient = new InternetAddress(to);
	     Address[] recepients = new Address[1]; 
	     recepients[0] = recepient; 
	     transport.sendMessage(message,recepients);
	     System.err.println("Message sent to:"+to);
		}catch(Exception e ){
			System.err.println(e.toString());
		}
	}

	public static void main(String args[]){
		if(args.length!=2){
			System.err.println("Usage: java MainInterface <emailaddress> <file-attachment>");
			System.exit(0);
		}
		MailInterface mi = new MailInterface();
		String to= args[0];
		String file= args[1];
		if(!to.equals("") && !file.equals("")){
			mi.sendAttachmentMail(to,P2PServer.dirPath+"/"+args[1]);
		}
	}
}
