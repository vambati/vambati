package p2p;
import java.net.*; 
import java.io.*; 

public class CommunicatorThread extends Thread{
	// the socket where to listen/talk
	Socket socket;
	BufferedReader infromClient;
	PrintWriter outtoClient;
	
	// File to write this log to 
	BufferedWriter bw = null; 
	
	CommunicatorThread(Socket socket,String filename) {
		try {
	    System.out.println("Thread trying to create Object Input/Output Streams");
		this.socket = socket;
		infromClient  = new  BufferedReader(new InputStreamReader(socket.getInputStream()));
		outtoClient = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		bw = new BufferedWriter(new FileWriter(filename));
	
		}catch(Exception e){
			System.out.println("Exception reading/writing  Streams: " + e);
		}
	}
	
	public int receiveData(){
        int i=0;
		try
		{
			System.out.println("Thread waiting for a String from the Client");
	        String inpSentence="";
	        
	        if((inpSentence = infromClient.readLine()).startsWith(P2PServer.start)) {
				while(!(inpSentence = infromClient.readLine()).startsWith(P2PServer.end)) {
			           System.err.println("Received: " + inpSentence);
			           bw.write(inpSentence+"\n");
			           i++;
				}
	        }else{
	        	outtoClient.println("Sorry! Wrong Code Word.");
	        	return -1; 
	        }
			outtoClient.println("Received "+i+" lines. Thanks");
			return i; 
		}
		catch (Exception e) {
			System.out.println("Exception reading/writing  Streams: " + e);
			return -1;
		}
		
		finally 
		{
			try {
		        bw.flush();
		        outtoClient.flush();
				infromClient.close();
				outtoClient.close();
				bw.close();
		        socket.close();
			}
			catch (Exception e) {
				System.out.println("Closing all connections:" + e);
				return -1; 
			}
		}
	}
}
