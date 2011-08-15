package qual;
/*
 * Mechanical Turk CSV file processing code
 * - Computes reliability scores for Oracles based on annotator agreement
 * - 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mturk.MturkInterface;

import channels.P2PServer;

import com.amazonaws.mturk.addon.*;
import com.amazonaws.mturk.dataschema.QuestionFormAnswers;
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType;
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType.AnswerType;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

import utils.MyNLP;
 
public class QualCheck {
	public int rejected = 0; 
	public int total = 0;

	int SELECT_FROM = 3; 
	
	// optimal number of queries 
	public static int OPT_QUERY_COUNT = 0;
	
	public static Hashtable<String,Oracle>  oracles = null;
	public static Hashtable<String,HIT> hits = null;
	public static TranslationChecker validator = null;
	public Hashtable<String,Integer> blockList = new Hashtable<String, Integer>();
	
	public Hashtable<String,String> INPUT = new Hashtable<String, String>(); 
	
	public QualCheck(String rFile,String bbnFile) throws Exception{
		// Initialize 
		oracles = new Hashtable<String, Oracle>(); 
		hits = new Hashtable<String, HIT>();
		 
		
		// Load the results 
		loadFile(rFile);
		
		/*
		// LOading block list of Turkers
		BufferedReader bLr = null;
		BufferedReader bSrc = null;
		  try {
			  bLr = new BufferedReader(new InputStreamReader(new FileInputStream(P2PServer.blockFile)));
			  bSrc = new BufferedReader(new InputStreamReader(new FileInputStream(bbnFile)));
			  
		  }catch (Exception e) {
				System.err.println(e.toString());
			} 
	
		  String uid = "";
		  try {
		   while((uid=bLr.readLine())!=null){
			   System.err.println("Block this turkere:"+uid);
			   blockList.put(uid, 1);
		   }
		  }catch(Exception e){
			  System.err.println("Can not open file:"+P2PServer.blockFile);
		  }
		  
		  String src = "";
		  try {
		   while((src=bSrc.readLine())!=null){
			   String[] srcArr = src.split("\\t");
			   INPUT.put(srcArr[0],srcArr[1]);
		   }
		  }catch(Exception e){
			  System.err.println("Can not open file:"+P2PServer.blockFile);
		  }
		  bLr.close();
		*/
	}
	
	public void setValidator(String inputFile, String googFile) {
		// Task validator 
		validator = new TranslationChecker();
		validator.loadGoogle(googFile);
		 
		// Compute sufficient stats for Gold standard Matching
		for(String hitid: hits.keySet()){
			HIT h = hits.get(hitid);
			for(String workerid: h.ASSIGNMENT.keySet()){
				// input, output 
				if(validator.isValid(h.input, h.ASSIGNMENT.get(workerid))){ // Check data entry problems
					oracles.get(workerid).errors++;
				}
				if(validator.matchGoogle(h.input, h.ASSIGNMENT.get(workerid)) ) { // Check gaming issues - Google, Yahoo?
					oracles.get(workerid).googlematch++;
				}
			}
		}
		
		// Compute sufficient stats for Majority Voting Matching
		for(String hitid: hits.keySet()){
			HIT h = hits.get(hitid);
			// Compute agreement 
			h.computeAgreement(validator);
			
			// Propagate and update agreement values amongst the oracles 
			// that participated in this particular HIT 
			for(String workerid: h.ASSIGNMENT.keySet()){
				oracles.get(workerid).agreement += h.AGREEMENT.get(workerid);
				
				//TODO: Set Agreement to 0 if Gaming involved 
				if(blockList.containsKey(workerid)) { 
					 oracles.get(workerid).agreement = 0;
				}
			}
		}
	}
	
	public static boolean selectBestOutput(String successFile,String outputFile,String sel_type){
 		String tag = successFile.replaceAll(".success$","");
		String resultsFile = successFile+".mturk";
 
		String bbnFile = tag+".bbn"; 
		String googFile = bbnFile+".google"; 
		
		File in = new File(bbnFile);
		File out = new File(googFile);
		
		if((!out.exists()) && in.exists()){
			System.err.println("Translating using google !");
			GoogleTranslation.translateFile(bbnFile);
		}
		
		try { 
		QualCheck mturk = new QualCheck(resultsFile,bbnFile);
		

		if(sel_type.equals("none") || sel_type.equals("all")){
			printAllHITs();
		}else{
			
			mturk.setValidator(bbnFile,googFile);
			
			// Compute Reliability of Oracles
			MajorityVotingEstimator reliabilityEstimator = null;
			reliabilityEstimator = new MajorityVotingEstimator(hits,validator);
			for(String workerid:oracles.keySet()){
				reliabilityEstimator.computeScore(oracles.get(workerid));
			}
			// Compute reliabilities then Select and Print output data as well 
			HITOutputSelector selector = new HITOutputSelector(hits, oracles,validator);
			selector.selectAnnotation(bbnFile,outputFile,sel_type); // Make selection and print in the order of the input provided
		}
				
		// mturk.printOracleStats();
		// mturk.printHITStats();
		}catch(Exception e) {
			System.err.println(e.toString());
			return false; 
		}
		return true;
	}

		
	public static void main(String args[]) throws Exception {

		if(args.length!=2){
			System.err.println("Usage: java Mturk <file.success> ");
			System.err.println("Will be used as: java Mturk <file.csv> <file> <file.google> <file.ref>");
			System.err.println("Sel_type: vote, wvote,rel, vote-rel, rand, ie, none");
			System.exit(0);
		}
		String successFile = args[0];
		String tag = successFile.replaceAll(".success$","");
		
		String mturkCSV = successFile+".mturk";
		// Get results 
		// MturkInterface.getResults(successFile,mturkCSV);
		
		String sel_type = args[1]; // majority, reliability, rand, iethreshold
		
		String bbnFile = tag+".bbn"; 
		String googFile = bbnFile+".google"; 
		
		selectBestOutput(successFile,bbnFile+".select_test",sel_type); // Make selection and print in the order of the input provided
	}
	
	private void printHITStats() {
		int assignments = 0;
		int mvote_eixsts = 0;
		for(String hitid:hits.keySet()){
			assignments+=hits.get(hitid).ASSIGNMENT.keySet().size();
			//assignments+=hits.get(hitid).assignments;
			if(hits.get(hitid).majorityExists()){
				mvote_eixsts++;
			}
		}
		
		System.err.println("------------");
		System.err.println("Hits:"+hits.keySet().size());
		System.err.println("Majority Vote exists for :"+mvote_eixsts);
		System.err.println("Assignments completed:"+assignments);
		System.err.println("Optimal Set of Queries:"+OPT_QUERY_COUNT);
	}
	
	private static void printAllHITs() {
  
		int count = 1; 
		for(String hitid:hits.keySet()) {
			int num_translations = hits.get(hitid).ASSIGNMENT.keySet().size();
			
			// Only print those with at least three translations ?
			if(num_translations < 3){
				continue; 
			}
			
			int rank = 1; 
			 System.out.println("SrcSent "+count+"\t"+hits.get(hitid).id);
			for(String key: hits.get(hitid).ASSIGNMENT.keySet() ) {
			 System.out.println(count+" "+rank+"\t"+hits.get(hitid).ASSIGNMENT.get(key));
			 rank++;
			}
		count++;
		}
	}

	
	private void printOracleStats() {
		System.err.println("------------");
		int gmatches = 0;
		for(String wid:oracles.keySet()){
			gmatches+=oracles.get(wid).googlematch;
			System.err.println(oracles.get(wid).toString());
		}
		System.err.println("------------");
		System.err.println("Oracles:"+oracles.keySet().size());
		System.err.println("GoogleMatches:"+gmatches);
	}
	
	// Directly from MTURK 
	public void loadFile2(String successFile) {
		RequesterService service = new RequesterService(new PropertiesClientConfig(P2PServer.propertiesFile));
		BufferedReader sBr = null; 
		  try {
			  sBr = new BufferedReader(new InputStreamReader(new FileInputStream(successFile)));
		  }catch (Exception e) {
				System.err.println(e.toString());
			} 
		  System.err.println("Qual Checking :"+successFile);	
	  	  
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
		   // System.err.println("  HIT Id: " + hitId);
		    for (Assignment assignment : assignments) {

		      //Only assignments that have been submitted will contain answer data
		    	AssignmentStatus aStatus = assignment.getAssignmentStatus();
		    	
	  		  // aStatus == AssignmentStatus.Rejected ||
		    	
		      if ( aStatus == AssignmentStatus.Submitted || aStatus == AssignmentStatus.Approved ) { 
		    	  
		        //By default, answers are specified in XML
		        String answerXML = assignment.getAnswer();

		        //Calling a convenience method that will parse the answer XML and extract out the question/answer pairs.
		        QuestionFormAnswers qfa = RequesterService.parseAnswers(answerXML);
		        List<QuestionFormAnswersType.AnswerType> answers = qfa.getAnswer();

				String assignmentId = assignment.getAssignmentId();
		        for (QuestionFormAnswersType.AnswerType answer : answers) {
		          String answerValue = RequesterService.getAnswerValue(assignmentId, answer);
		          System.err.println(answerValue);
		        }
		        
		        //Approving the assignment.
		        // service.approveAssignment(assignment.getAssignmentId(), "Well Done! Thanks");
		      }
		    }
			}
			} catch (IOException e) {
				e.printStackTrace();
		}
}

	// Loading a CSV results file from Amazon MTurk 
	public void loadFile(String file) throws Exception{
 
		Date firstSubmit = null;
		Date lastSubmit = null;
		double totalWorkTime = 0; 
		
		System.err.println("Loading from :"+file);
	      //Loads the .success file containing the HIT IDs and HIT Type IDs of HITs to be retrieved.
	      HITDataCSVReader csvFile = new HITDataCSVReader(file,'\t');

	      System.err.println("Total Rows in CSV file "+csvFile.getNumRows());
	      int i=0;
	      int rejected=0;
	      
	      for(i=1;i<csvFile.getNumRows();i++){
	    	  
	       Map<String,String> hitLine = csvFile.getRowAsMap(i);
	    	   
			if(hitLine.size()!=27){
				//System.err.println("ERROR: line "+i+": Has fields: "+hitLine.size());
				//continue;
			}
			
			// Fields in the CSV file from Mturk results 
			String hitid = hitLine.get("hitid");
			String assignmentid = hitLine.get("assignmentid");
			String workerid = hitLine.get("workerid");
			String dateString = hitLine.get("assignmentsubmittime");
			String status = hitLine.get("assignmentstatus");
			
			//double worktime = Double.parseDouble(hitLine.get("WorkTimeInSeconds"));
			//totalWorkTime += worktime;
			
			Date date = null;
			//Sun Feb 21 04:25:15 GMT 2010
			String pattern = "EEE MMM dd kk:mm:ss zzz yyyy";
		    SimpleDateFormat format = new SimpleDateFormat(pattern);
		    try {
		       date = format.parse(dateString);
		    } catch (ParseException pe) {
		      pe.printStackTrace();
		      System.err.println(pe.toString());
		    }
		    			
			// Submitted, Rejected, Accepted
			if(status.equals("Rejected")){
				rejected++;
				continue; 
			}
						
			String ansString  = hitLine.get("answers[question_id answer_value]");
			//System.err.println(ansString);
			
			Hashtable<String,String> ANSWERS = new Hashtable<String, String>(); 
			String[] ansArr = ansString.split("\\t");
			int n=0;
			while(n<ansArr.length-1){
				ANSWERS.put(ansArr[n++],ansArr[n++]);
			}
			String inpId = "";
			String tgt = ""; 
			
			// if(ANSWERS.containsKey("translation") && ANSWERS.containsKey("req_id")){
			if(ANSWERS.containsKey("translation")){
				inpId = ANSWERS.get("req_id");
				tgt = ANSWERS.get("translation");
				tgt.replaceAll("\\n"," ");
				tgt = tgt.replaceAll("\\p{Cntrl}", "");
		    	  
				// Skip the first line ? 
//				if(!src.startsWith("cmu") ) {
//					System.err.println("Skipping:"+src);
//					continue; 
//				}
				
				if(tgt.equals("")){
					System.err.println("EMPTY: line "+i+": Annotation can not be empty");
				}
				
				// HIT 
				String src = INPUT.get(inpId);
				if((!hits.containsKey(hitid))){ 
					HIT x = new HIT(inpId,src);
					hits.put(hitid,x);
				}
				// Compute from just "SELECT_FROM" assignments  
				if(hits.get(hitid).ASSIGNMENT.size() < SELECT_FROM){
					hits.get(hitid).addAssignment(workerid,assignmentid,tgt);	
				}
			}
			
			// Oracle 
			if(!oracles.containsKey(workerid)){
				Oracle x = new Oracle(workerid);
				oracles.put(workerid, x);
			}
			oracles.get(workerid).submitted++; 
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
	}
}