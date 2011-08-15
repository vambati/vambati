package mturk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import channels.P2PServer;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.addon.HITDataCSVWriter;
import com.amazonaws.mturk.addon.HITDataInput;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.addon.HITTypeResults;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

public class MturkPoster {
	String inputCSVFile = ""; 
	String successFile = ""; 
	String failFile = ""; 
	long delayinMilliSec = 0; 
	int batch_size = 1; 
	
	boolean goldFlag = false; 
	public Vector<String> GOLD = null;
	
	public MturkPoster(String inputCSVFile2, String successFile2, String failFile2, long delay, int batch_size) {
		  System.out.println("Mechanical Turk Interactions");
		  this.inputCSVFile = inputCSVFile2; 
		  this.successFile = successFile2; 
		  this.failFile = failFile2; 
		  this.delayinMilliSec = delay;
		  this.batch_size = batch_size; 
	}
	
	public MturkPoster(String inputCSVFile2, String goldSeedFile, String successFile2, String failFile2, long delay, int batch_size) {
		  System.out.println("Mechanical Turk Interactions");
		  this.inputCSVFile = inputCSVFile2; 
		  this.successFile = successFile2; 
		  this.failFile = failFile2; 
		  this.delayinMilliSec = delay;
		  this.batch_size = batch_size; 
		  
		  // Load Gold standard data
		  GOLD = new Vector<String>();
		  goldFlag  = true; 
		  System.out.println("Loading gold standard data-");
		  try {
		  BufferedReader gr = new BufferedReader(new InputStreamReader(new FileInputStream(goldSeedFile)));
		  String line = "";
		  while((line = gr.readLine())!=null){
			  GOLD.add(line);
		  }
		  }catch(Exception e){}
		  System.err.println("Loaded Gold standard entries:"+GOLD.size());
	}
	
	// IMPORTANT: You can only post once from each LOGIN (can not have simultanesou threads do it)
	public synchronized void start() {
		
	int frameHeight = 500; 
	
	RequesterService service = new RequesterService(new PropertiesClientConfig(P2PServer.propertiesFile));
	
	 if((service!=null) &&(service.getAccountBalance()!=0)) {
	 System.err.println("Starting HIT posts :");
		  try {
			  PrintWriter successW = new PrintWriter(new File(successFile));
			  successW.write("hitID\thitTypeID\n"); //Header for Success file
			  
			  PrintWriter failureW = new PrintWriter(new File(failFile));
			  HITProperties prop = new HITProperties(P2PServer.hitPropertiesFile);

		       
		      Date startTime = new Date();
		      System.out.println("Start time: " + startTime);

			  BufferedReader csvBr = new BufferedReader(new InputStreamReader(new FileInputStream(inputCSVFile)));
			  // Gobble PARAMS line
			  String line = csvBr.readLine(); 
			  int i=0;
			  while((line = csvBr.readLine())!=null){
				 i++; 
		      // for(int i=1;i<input.getNumRows();i++){
		      // hitLine.get("params")
		    	//  Map<String,String> hitLine = input.getRowAsMap(i);

		    	  // Create an external question (URL, PARAMS, FRAME_HEIGHT)
		    	  String question = "";
		    	  if(goldFlag==true){
		    		  String goldstr = GOLD.elementAt(i); // For each sentence you should have a different GOLD standard
		    		  String[] arr = goldstr.split("\\t+");
					  String eSrc = arr[0];
					  String tSrc = arr[1];
		    		  
		    		  System.err.println(goldstr);
//		    		  question =  line + "&gold_check="+eSrc;
	
		    		  question =  line + "&eval_src="+eSrc+ "&eval_tgt="+tSrc; 
		    		  System.err.println(question.toString());
		    		  question = ExternalQuestionFormatter.createQuestion(P2PServer.externalURL+question,frameHeight);
		    	  }else{
		    		  question = ExternalQuestionFormatter.createQuestion(P2PServer.externalURL+line,frameHeight);
		    	  }
		    	  
		    	  String title = "Please translate "+P2PServer.SOURCE_LANGUAGE+ " to "+ P2PServer.TARGET_LANGUAGE + " (Contribute to a research project)";
		    	  
		          HIT hit = service.createHIT( null,
		                  title,
		                  prop.getDescription(), null,
		                  question, prop.getRewardAmount(),
		                  prop.getAssignmentDuration(),prop.getAutoApprovalDelay(),
		                  prop.getLifetime(),prop.getMaxAssignments(), null,
		                  prop.getQualificationRequirements(),
		                  null 
		        	      );
		           
		          if (hit == null) {
			    	  System.err.println("ERROR: Failed to create HIT");
			    	  failureW.write(hit.getHITTypeId()+"\t"+hit.getHITId()+"\n");
			    	  failureW.flush();
			        throw new Exception("Could not create HITs");
			      }else{
			    	  successW.write(hit.getHITId()+"\t"+hit.getHITTypeId()+"\n");
			    	 // System.err.println("Created Hit:");
			    	 // System.err.println("You may see your HIT with HITTypeId '" + hit.getHITId() + "' here: ");
		    		 // System.err.println(service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId());
			    	  successW.flush();
			      }
	          
		          if(i%batch_size == 0){
		        	  System.err.println(i+": Waiting before secs :"+(delayinMilliSec/1000));
		        	//  this.wait(delayinMilliSec);
		          }
		      } 
		      Date endTime = new Date();
		      System.out.println("--[End Loading HITs]----------");
		      System.out.println("--End time: " + endTime);
		      System.out.println("--[Done Loading HITs]----------");
		      System.out.println("--Total load time: " + (endTime.getTime() - startTime.getTime())/1000 + " seconds.");
		  
		      csvBr.close();
		      successW.flush(); successW.close();
		      failureW.flush(); failureW.close(); 
		  } catch (Exception e) {
		      System.err.println(e.getLocalizedMessage());
		    }  
	    } else {
	      System.err.println("You do not have enough funds to create the HIT.");
	    }
	  }
}
