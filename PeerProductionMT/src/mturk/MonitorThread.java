package mturk;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.*; 

import qual.QualCheck;

import channels.P2PServer;
import channels.mail.MailInterface;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.dataschema.QuestionFormAnswers;
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType;
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType.AnswerType;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

public class MonitorThread extends Thread {
 
	String filepath = ""; 
	String successFile,mturkCSV,bbnFile,bbnResultsFile,responseXMLFile = "";
	public MonitorThread(String filepath){
		
		this.filepath = filepath; 
		
		successFile = filepath.replaceAll("xml$","success");
		mturkCSV = successFile.replaceAll("success$", "success.mturk"); // Input in tab format
		bbnFile = filepath.replaceAll("xml$", "bbn"); // Input in tab format
		bbnResultsFile = filepath.replaceAll("xml$", "bbn.mturk"); // Results in tab format
		responseXMLFile = filepath.replaceAll("request-","response-");		
	}
	public void run(){
		// Get and save a CSV file 
		MturkInterface.getResults(successFile,mturkCSV);
		
			if(monitorTask(successFile)==1)
			 {
				System.err.println(successFile+" Completed !!!");
				// Convert CSV file to a BBN results file for XMLizing (NO PRUNING !!!!!)
				convertResults2BBN(mturkCSV,bbnResultsFile);
					
				// With PRUNING !!! Select using weighted majority vote
				boolean shipout = QualCheck.selectBestOutput(successFile,bbnResultsFile,"all"); 
				 
					if (shipout) {
						// Create response XML and Email to BBN
						createResponseXML(bbnFile,bbnResultsFile,responseXMLFile);
						
						// Email xml
						emailXML(responseXMLFile);
						
						// Remove LOG so it is not repeatedly queried
						MonitorMaster.remove(filepath);
					}else{
						System.err.println("Failed : Qualitfy Checking\n"); 		
					}
			 }
			 else{
				 System.err.println(filepath+" Not complete!!!");
			 }
	}
	
	// Reject work from the BLOCK list of users
	public void  blockWorkers(String successFile,String blockFile) {
		RequesterService service = new RequesterService(new PropertiesClientConfig(P2PServer.propertiesFile));
		BufferedReader sBr = null;
		BufferedReader bLr = null;
		
		Hashtable<String,Integer> blockList = new Hashtable<String, Integer>(); 
		  try {
			  sBr = new BufferedReader(new InputStreamReader(new FileInputStream(successFile)));
			  bLr = new BufferedReader(new InputStreamReader(new FileInputStream(blockFile)));
		  }catch (Exception e) {
				System.err.println(e.toString());
			} 
		  
		  // LOading block list
		  String uid = "";
		  try {
		   while((uid=bLr.readLine())!=null){
			   System.err.println("Block this turkere:"+uid);
			   blockList.put(uid, 1);
		   }
		  }catch(Exception e){
			  System.err.println("Can not open file:"+blockFile);
		  }
		  	
	  	  int ASSIGNMENTS_REJECTED=0;
	  	  	  
			try {
			String line = "";
		    System.err.println("--[Reviewing HITs]----------");
		    
		    String header = sBr.readLine(); // Skip header 
			while((line=sBr.readLine())!=null)
			{
			String[] toks = line.split("\\s+");
			String hitId = toks[0];
			String hitTypeId = toks[1];

		   Assignment[] assignments = service.getAllAssignmentsForHIT(hitId);
 
		    for (Assignment assignment : assignments) {
		      //Only assignments that have been submitted will contain answer data
		    	AssignmentStatus aStatus = assignment.getAssignmentStatus();
		    	
	  		  // aStatus == AssignmentStatus.Rejected ||
		    	
		      if ( aStatus == AssignmentStatus.Submitted || 
		    		  aStatus == AssignmentStatus.Approved ) { 
		        //By default, answers are specified in XML
		        String answerXML = assignment.getAnswer();

		        //Calling a convenience method that will parse the answer XML and extract out the question/answer pairs.
		        QuestionFormAnswers qfa = RequesterService.parseAnswers(answerXML);
		        List<QuestionFormAnswersType.AnswerType> answers = qfa.getAnswer();
		        String workerID = assignment.getWorkerId(); 
		        
		        	 if(blockList.containsKey(workerID)){
		        		 System.err.println("Rejecting:"+workerID);
		        		 ASSIGNMENTS_REJECTED++;
					     service.rejectAssignment(assignment.getAssignmentId(),"Your work has been rejected as it has been judged to be of low quality or very similar to existing online translation systems. Sorry!");
					     System.err.println("Extending:");
						 service.extendHIT(hitId, 1, new Long(1));  
		        	 }
		      }
		    } 
			}
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}

	
	public int monitorTask(String successFile) {
	RequesterService service = new RequesterService(new PropertiesClientConfig(P2PServer.propertiesFile));
	BufferedReader sBr = null; 
	  try {
		  sBr = new BufferedReader(new InputStreamReader(new FileInputStream(successFile)));
	  }catch (Exception e) {
			System.err.println(e.toString());
		} 
	  System.err.println("Monitoring:"+successFile);	
  	  int ASSIGNMENTS_VALID=0; 
  	  int ASSIGNMENTS_REJECTED=0;
  	  int ASSIGNMENTS_COMPLETED=0;
 
  	  int TOTAL_HITS = 0; 
  	  int MAX_ASSIGNMENTS=3;
  	  
		try {
		String line = "";
	    System.err.println("--[Reviewing HITs]----------");
	    
	    String header = sBr.readLine(); // Skip header 
		while((line=sBr.readLine())!=null)
		{
		String[] toks = line.split("\\s+");
		String hitId = toks[0];
		String hitTypeId = toks[1];

		// service.disposeHIT(hitId); 
		// service.extendHIT(hitId, 3, new Long(3600));
		
		// String hitId = success.getRowAsMap(i).get("hitid");
	
	   Assignment[] assignments = service.getAllAssignmentsForHIT(hitId);
	   // System.err.println("  HIT Id: " + hitId);
	    for (Assignment assignment : assignments) {

	      //Only assignments that have been submitted will contain answer data
	    	AssignmentStatus aStatus = assignment.getAssignmentStatus();
	    	
  		  // aStatus == AssignmentStatus.Rejected ||
	    	
	      if ( aStatus == AssignmentStatus.Submitted || 
	    		  aStatus == AssignmentStatus.Approved ) { 
	    	  ASSIGNMENTS_COMPLETED++;
	        //By default, answers are specified in XML
	        String answerXML = assignment.getAnswer();

	        //Calling a convenience method that will parse the answer XML and extract out the question/answer pairs.
	        QuestionFormAnswers qfa = RequesterService.parseAnswers(answerXML);
	        List<QuestionFormAnswersType.AnswerType> answers = qfa.getAnswer();

	        	 if(isValidAnswer(answers,assignment)){
	        		 ASSIGNMENTS_VALID++;
	        	 }else{
	        	ASSIGNMENTS_REJECTED++;
			       service.rejectAssignment(assignment.getAssignmentId(),"Please read instructions carefully. You are not eligible to complete this task, as you are not a native speaker. Sorry!");
				   service.extendHIT(hitId, 0, new Long(1));       
	        	 }
	        //Approving the assignment.
	        // service.approveAssignment(assignment.getAssignmentId(), "Well Done! Thanks");
	      }
	    }
	    TOTAL_HITS++; 
		}
		// STATS 
		System.err.println("Total Hits:"+TOTAL_HITS);
		System.err.println("Total Assignments:"+TOTAL_HITS*MAX_ASSIGNMENTS);
		System.err.println("Completed Assignments:"+ASSIGNMENTS_COMPLETED);
		System.err.println("Completed % :"+ASSIGNMENTS_COMPLETED*100/(TOTAL_HITS*MAX_ASSIGNMENTS)+"%");
		System.err.println("Rejected Assignments:"+ASSIGNMENTS_REJECTED);
		System.err.println("Valid Assignments:"+ASSIGNMENTS_VALID);
		System.err.println("Valid% :"+ASSIGNMENTS_VALID*100/(TOTAL_HITS*MAX_ASSIGNMENTS)+"%");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Check if completed and save it to a file 
		if(ASSIGNMENTS_VALID==TOTAL_HITS*MAX_ASSIGNMENTS){
			return 1; // Completed  
		}
		return 1; // Not complete 
	}
	
	private boolean isValidAnswer(List<AnswerType> answers, Assignment assignment) {
 
        if(answers==null || answers.size()==0){
        	return false; 
        }
        
//		String assignmentId = assignment.getAssignmentId();
//        for (QuestionFormAnswersType.AnswerType answer : answers) {
//          String answerValue = RequesterService.getAnswerValue(assignmentId, answer);
//          
//          if(answerValue.equals("India") || 
//        	   answerValue.equals("Pakistan") || 
//        		 answerValue.equals("China") || 
//        		   answerValue.equals("Sri Lanka") ){
//        	  System.err.println("Rejecting from :"+answerValue);
//        	  return false; 
//          }
//        }
		return true;
	}
	
	
	// Send the same mail back with some substitution
	public  void createResponseXML(String bbnFile,String bbnResultsFile,String responseXMLFile){
		String cmd = "/usr/local/bin/perl  /afs/cs/user/vamshi/workspace-eclipse/PeerProductionMT/scripts/generate_resp_xml.pl ";
 
        System.err.println("Creating dummy response using PERL script-");
        // Send back the same file as output as well !!!! 
        //cmd+=bbnFile+" "+bbnFile+" "+responseXMLFile;
        
        // bbnResultsFile should have been created 
        cmd+=bbnFile+" "+bbnResultsFile+" "+responseXMLFile;
        
        System.err.println(cmd);
        try {
	        Runtime rt = Runtime.getRuntime();
	        Process proc2 = rt.exec(cmd);
	        proc2.waitFor();
	        int exitVal = proc2.exitValue();
	        System.err.println("Process2 exitValue: " + exitVal);
        }catch(Exception e){System.err.println(e.toString());}
	}
	
	public void emailXML(String responseXMLFile){
        // Send mail 
        MailInterface mi = new MailInterface(); 
        // Send mail to BBN 
        mi.sendAttachmentMail(MailInterface.TO, responseXMLFile);
	}
	
	// Loading a CSV results file from Amazon MTurk 
	private  void convertResults2BBN(String mturkCSVFile, String bbnResultsFile) {
		String ANSWER_INDEX = "answers[question_id answer_value]";

		Date firstSubmit = null;
		Date lastSubmit = null;
		double totalWorkTime = 0; 
		
		System.err.println("Loading from :"+mturkCSVFile);
	      //Loads the .success file containing the HIT IDs and HIT Type IDs of HITs to be retrieved.
	      HITDataCSVReader csvFile=null;
	      BufferedWriter bW = null; 
		try {
			csvFile = new HITDataCSVReader(mturkCSVFile);  
			bW = new BufferedWriter(new FileWriter(bbnResultsFile));
	
		  bW.write("HEADER DUMMY GOES\n");
	      System.err.println("Total Rows in CSV file "+csvFile.getNumRows());
	      int i=0;
	      int rejected=0;
	      
	      for(i=1;i<csvFile.getNumRows();i++){
	       Map<String,String> hitLine = csvFile.getRowAsMap(i);
			// Fields in the CSV file from Mturk results 
			String hitid = hitLine.get("hitid");
			String assignmentid = hitLine.get("assignmentid");
			String workerid = hitLine.get("workerid");
			String dateString = hitLine.get("assignmentsubmittime");
			String status = hitLine.get("assignmentstatus");
			
			if(status.equalsIgnoreCase("Rejected")){
				continue;
			}
			
			String answers = hitLine.get(ANSWER_INDEX);
			StringTokenizer st = new StringTokenizer(answers,"\t");
			Hashtable<String,String> answerTable = new Hashtable<String, String>();
			while(st.hasMoreTokens()){
				String key = st.nextToken();
				String val = st.nextToken();
				answerTable.put(key, val);
			}
		    bW.write(answerTable.get("req_id")+"\t"+answerTable.get("translation")+"\n"); 
		}
		System.err.println("--------------------");
		System.err.println("Total:"+i);
		System.err.println("Rejected:"+rejected);
		System.err.println("Loaded:"+(i-rejected));
		System.err.println("FirstSubmitted:"+firstSubmit);
		System.err.println("LastSubmitted:"+lastSubmit);
		System.err.println("TotalTimeSpent mins:"+totalWorkTime/60);
		System.err.println("Avg secs per translation:"+totalWorkTime/i);
		System.err.println("Time to completion:");
		
		// Close stuff 
		bW.flush();bW.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}