import channels.P2PServer;
import mturk.*; 

public class imagerender {

	public static void main(String args[]){
		String dir = args[0];
		String file = args[1];

		P2PServer.createImages(dir,file); 
	}
}
