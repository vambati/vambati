import channels.P2PServer;
import mturk.*; 

public class test {

	public static void main(String args[]){
		String tag = args[0];

//		P2PServer p = new P2PServer(); 
//		p.isValid(tag);		
//		System.exit(0);
		
		RequestProcessor rp = new RequestProcessor(".",args[0]);
// 		rp.extractXMLtoCSV();
		
		rp.bbnFile = tag+".bbn";
		rp.inputCSVFile = tag+".csv";
		rp.successFile = tag+".success";
		rp.failFile = tag+".failure";
		
		// rp.crowdSource(); // No gold standard
		rp.crowdSource("gold-standard.sp"); // With gold standard
	}
}
