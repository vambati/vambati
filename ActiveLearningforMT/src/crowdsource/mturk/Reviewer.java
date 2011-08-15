package crowdsource.mturk;

import java.io.File;

import com.amazonaws.mturk.addon.HITDataCSVReader;
import com.amazonaws.mturk.addon.HITDataCSVWriter;
import com.amazonaws.mturk.addon.HITDataInput;
import com.amazonaws.mturk.addon.HITTypeResults;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

/**
 * The Reviewer sample application will retrieve the completed assignments for a given HIT,
 * output the results and approve the assignment.
 *
 * mturk.properties must be found in the current file path.
 * You will need to have the HIT ID of an existing HIT that has been accepted, completed and
 * submitted by a worker.
 * You will need to have the .success file generated from bulk loading several HITs (i.e. Site Category sample application).
 *
 * The following concepts are covered:
 * - Retrieve results for a HIT
 * - Output results for several HITs to a file
 * - Approve assignments
 */
public class Reviewer {

  public RequesterService service;
  public Reviewer(String propFile) {
	  service = new RequesterService(new PropertiesClientConfig(propFile));
  }

  /**
   * Prints the submitted results of HITs when provided with a .success file.
   * @param successFile The .success file containing the HIT ID and HIT Type ID
   * @param outputFile The output file to write the submitted results to
   */
  public void getResults(String successFile, String outputFile) {

    try {
      //Loads the .success file containing the HIT IDs and HIT Type IDs of HITs to be retrieved.
      HITDataInput success = new HITDataCSVReader(successFile);

      //Retrieves the submitted results of the specified HITs from Mechanical Turk
      HITTypeResults results = service.getHITTypeResults(success);
      results.setHITDataOutput(new HITDataCSVWriter(outputFile));
      
      //Writes the submitted results to the defined output file.      
      results.writeResults();
      System.err.println("Results have been written to: " + outputFile);
    } catch (Exception e) {
      System.err.println("ERROR: Could not print results: " + e.getLocalizedMessage());
    }
  }
}
