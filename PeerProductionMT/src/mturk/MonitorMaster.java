package mturk;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.*; 

import channels.P2PServer;
import channels.mail.MailInterface;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.dataschema.QuestionFormAnswers;
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class MonitorMaster {
 
	static Hashtable<String,Integer> LOG = null; 
	
	MonitorMaster(){
		try {
			BufferedReader logReader = new BufferedReader(new InputStreamReader(new FileInputStream(P2PServer.CURRENT_LOG)));
		  
			  // Load log 
			  LOG = new Hashtable<String, Integer>();
			  String filepath = "";
			  int i=0;
			  while((filepath=logReader.readLine())!=null){
				  LOG.put(filepath, 1);
				  i++;
			  }
			  System.err.println("I have files to work on: "+i);
			  logReader.close();
		 }catch (FileNotFoundException e) {
				System.err.println("No files to work on");
				System.exit(0);
		  } catch (IOException io) {
			io.printStackTrace();
		}
	}
	public void start() throws IOException{
		synchronized(LOG){
			Iterator<String> iter = LOG.keySet().iterator();
			while(iter.hasNext())
			{
				String filepath = (String) iter.next();
				MonitorThread mt = new MonitorThread(filepath); 
				try {
					mt.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Remove a filepath from LOG file after completion 
	public static synchronized void remove(String filepath){
		System.err.println("Completed and Removing:"+filepath);
		LOG.put(filepath,0); // Set it to ZERO 
	}
	
	public void stop() {
		
		// Then dump all the files to be worked on 
		try {
			int COUNT =0;
			BufferedWriter bw = new BufferedWriter(new FileWriter(P2PServer.CURRENT_LOG));
			for(String filepath:LOG.keySet()){
				if(LOG.get(filepath)==1){
					bw.write(filepath+"\n");
					COUNT++;
				}else{
					System.err.println("Purging file:"+filepath);
				}
			}
			bw.flush();bw.close();
			
			// If no files exist, then get rid of LOG 
			if(COUNT==0){ 
				System.err.println("No FILES to PROCESS ! Deleting CURRENT_LOG");
				File f = new File(P2PServer.CURRENT_LOG);
				f.delete(); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String args[]) throws IOException {
		
		MonitorMaster mm = new MonitorMaster(); 
 		try {
			mm.start();
		}
		finally{
			mm.stop();
		}
	}
}