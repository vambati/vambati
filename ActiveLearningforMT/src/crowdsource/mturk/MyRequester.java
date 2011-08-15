package crowdsource.mturk;

import java.io.*;
import java.util.Date;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.addon.HITDataCSVWriter;
import com.amazonaws.mturk.addon.HITDataInput;
import com.amazonaws.mturk.addon.HITDataOutput;
import com.amazonaws.mturk.addon.HITProperties;
import com.amazonaws.mturk.addon.HITQuestion;
import com.amazonaws.mturk.requester.HIT;

public class MyRequester extends Requester{
	  	
  public MyRequester(String propertiesFile) {
	  super(propertiesFile);
  }
  
  public void fromFile(String hitPropertiesFile, String xmlFile,String dataFile,String successFile,String failFile)  {
	  try { 
		  HITProperties prop = new HITProperties(hitPropertiesFile);
		  System.err.println(prop.getAnnotation());
		  System.err.println(prop.getTitle());
		  
	      HITQuestion question = new HITQuestion(xmlFile);
	      HITDataInput input = new HITDataCSVReader(dataFile);
	      HITDataOutput success = new HITDataCSVWriter(successFile);
	      HITDataOutput failure = new HITDataCSVWriter(failFile);
	      
	      System.out.println("--[Loading HITs]----------");
	      Date startTime = new Date();
	      System.out.println("Start time: " + startTime);
	      HIT[] hits = null;
	      	hits = service.createHITs(input, prop, question, success, failure);
	      Date endTime = new Date();
	      System.out.println("--[End Loading HITs]----------");
	      System.out.println("--End time: " + endTime);
	      System.out.println("--[Done Loading HITs]----------");
	      System.out.println("--Total load time: " + (endTime.getTime() - startTime.getTime())/1000 + " seconds.");

	      if (hits == null) {
	    	  System.err.println("ERROR: Failed to create HITS");
	        throw new Exception("Could not create HITs");
	      }else{
	    	  System.err.println("Created Hits");
	    	  for(HIT hit: hits){
	    		  System.err.println("You may see your HIT with HITTypeId '" + hit.getHITId() + "' here: ");
	    		  System.err.println(service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId());
			  } 
	      }
	    } catch (Exception e) {
	      System.err.println(e.getLocalizedMessage());
	    }
  }
  
	  public void translateFile(String inputFile,String srcLang,String tgtLang,String hitFile){
	  try {
	  File file = new File(inputFile);
	  BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF8"));
	  BufferedWriter hitWriter = new BufferedWriter(new FileWriter(hitFile));
	  String src = "";
	  int i=1;
	  hitWriter.write("\"hitid\"\t\"hittypeid\"\n");
	  while((src=inputReader.readLine())!=null){
		  System.err.println("Posting:"+src);
		  HIT hit = postSingleHIT(src,srcLang,tgtLang);
		  if(hit!=null){
			  hitWriter.write("\""+hit.getHITId()+"\"\t\""+hit.getHITTypeId()+"\"\n");
		  }else{
			  System.err.println("Failed to post sentence: "+i);
		  }
		  i++;
	  }
	  hitWriter.close();
	  inputReader.close(); 
	  System.err.println("Posted:"+i+" hits");
	  }catch(Exception e){}
  }
  
  /* Returns hitid if successfully published, else NULL */ 
  public HIT postSingleHIT(String str,String srcLang,String tgtLang)  {
	  
	  title = "Please translate the following from "+srcLang+" "+tgtLang+":";
	  description = "An MT Square task";
	  reward = 0.01;
	  numAssignments = 2;

	  try {
	  HITQuestion question = new HITQuestion("test");
      String qstr = question.getQuestion();
      qstr = qstr.replaceAll("INPUTSENTENCE", str);
     // QAPValidator.validate(question.getQuestion());
      
      HIT hit = service.createHIT(
              title,
              description,
              reward,
              qstr,
              numAssignments);
       
      System.err.println("Src:"+str);
      System.err.println("You may see your HIT with HITTypeId '" + hit.getHITId() + "' here: ");
      System.err.println(service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId());
      return hit;
	  }catch (Exception e) {
      System.err.println(e.getLocalizedMessage());
    }
    return null; 
  }
}
