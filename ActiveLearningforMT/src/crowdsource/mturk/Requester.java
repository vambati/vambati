package crowdsource.mturk;

import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

/**
 * The MTurk Class for creating a simple HIT via the Mechanical Turk 
 * Java SDK. mturk.properties must be found in the current file path.
 */
public class Requester {

  protected RequesterService service;

  // Defining the atributes of the HIT to be created
  protected String title = "";
  protected String description = "An MT Square Hit";
  protected int numAssignments = 1;
  protected double reward = 0;

  public Requester(String mturkConfigFile) {
    service = new RequesterService(new PropertiesClientConfig(mturkConfigFile));
  }

  public boolean hasEnoughFund() {
    double balance = service.getAccountBalance();
    System.out.println("Got account balance: " + RequesterService.formatCurrency(balance));
    return balance > 0;
  }
}
