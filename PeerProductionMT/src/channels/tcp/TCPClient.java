package channels.tcp;

import java.io.*;
import java.net.Socket;

class TCPClient
{
static String HOST = "chicago.lti.cs.cmu.edu";
// static String HOST = "kathmandu.lti.cs.cmu.edu";

static int PORT_NUMBER = 6789;
static String start = "BBN-CMU-START";
static String end = "BBN-CMU-END";

 public static void main(String argv[]) throws Exception
 {
	 if(argv.length!=1){
		 System.err.println("Usage: java P2PClient <file>");
	     System.exit(0);
	 }
	 
	 String inpFile = argv[0];
	 BufferedReader xmlData = null; 
	 try { 
		 xmlData = new BufferedReader( new FileReader(inpFile));
	 }catch(IOException ex){
		 System.err.println(ex.toString());
	 }

	 Socket clientSocket = new Socket(HOST, PORT_NUMBER);
	 PrintWriter outToServer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
	 
	 // Start protocol
	 outToServer.write(start+"\n");
	 
	 // Send the file content
	 System.err.println("Sending data from:"+inpFile);
	 String sentence = "";
	 while((sentence = xmlData.readLine())!=null){
		  outToServer.write(sentence+"\n");
	 }
 	//End protocol
 	outToServer.write(end+"\n");
 	System.err.println("Data sent!");
 	
 	outToServer.flush();
	 xmlData.close(); 
	 outToServer.close();
	 clientSocket.close();
 }
}