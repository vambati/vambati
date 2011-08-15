package mturk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import channels.P2PServer;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.addon.HITDataCSVWriter;
import com.amazonaws.mturk.addon.HITDataInput;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.addon.HITTypeResults;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

public class MturkInterface {

	public static void main(String args[]){
		RequesterService service = new RequesterService(new PropertiesClientConfig(P2PServer.propertiesFile));
		if (service.getAccountBalance()>0) {
		      System.err.println("Yes, we have enough funds!");
		 }
		// String file = args[0];
		// postHITs(file,file+".success",file+".failure");
		// reviewHITS(file,file+".csv");
	}
	
	  public static void getResults(String successFile, String outputFile) {
		  RequesterService service = new RequesterService(new PropertiesClientConfig(P2PServer.propertiesFile));
		    try {
		      //Loads the .success file containing the HIT IDs and HIT Type IDs of HITs to be retrieved.
		      HITDataInput success = new HITDataCSVReader(successFile);

		      //Retrieves the submitted results of the specified HITs from Mechanical Turk
		      HITTypeResults results = service.getHITTypeResults(success);
			      // APPEND vs. OVERWRITE (Want to delete here) 
			      File f = new File(outputFile);
			      f.delete(); 
		          results.setHITDataOutput(new HITDataCSVWriter(outputFile));

		      
		      //Writes the submitted results to the defined output file.      
		      results.writeResults();
		      System.err.println("Results have been written to: " + outputFile);
		    } catch (Exception e) {
		      System.err.println("ERROR: Could not print results: " + e.getLocalizedMessage());
		    }
		  }
	  
	  public static void cancelAll(String successFile) {
			RequesterService service = new RequesterService(new PropertiesClientConfig(P2PServer.propertiesFile));
			BufferedReader sBr = null; 
			  try {
				  sBr = new BufferedReader(new InputStreamReader(new FileInputStream(successFile)));
				  System.err.println("Cancelling:"+successFile);	
		 
				String line = "";
			    System.err.println("--[Reviewing HITs]----------");
			    
			    String header = sBr.readLine(); // Skip header 
				while((line=sBr.readLine())!=null)
				{
					String[] toks = line.split("\\s+");
					String hitId = toks[0];
					String hitTypeId = toks[1];
	
					try{
						service.forceExpireHIT(hitId);
						service.disposeHIT(hitId);
					}catch(Exception te){
						System.err.println("Cannot cancel:"+hitId);
					}
				}
				sBr.close();
			  }catch (Exception e) {
					System.err.println(e.toString());
				}
		  }
}
