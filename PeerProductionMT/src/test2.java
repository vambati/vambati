import mturk.*; 

public class test2 {

	public static void main(String args[]){
		MonitorThread mt = new MonitorThread(args[0]);
		// Valid answers ? 
		// mt.monitorTask(args[0]);
		
		// Block List turkers 
		// mt.blockWorkers(args[0],"/mnt/1tb/usr6/vamshi/PEER_PRODUCTION/ur-en/turkers.block");
		
	     MturkInterface.getResults(args[0],args[0]+".results");
	     
		// MturkInterface.cancelAll(args[0]);
	}
}
