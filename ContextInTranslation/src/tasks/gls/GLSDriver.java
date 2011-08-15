package tasks.gls; 

import options.*;

/**
 * Global lexical selection
 * @author Vamshi Ambati
 * 1 Mar 2009
 * Carnegie Mellon University
 *
 */

public class GLSDriver 
{
	public static void main(String args[]) throws Exception
	{
		if(args.length==1)
		{
			System.out.println("Config path is: "+args[0]);
			Options opts = new Options(args[0]); 

		 	long sTime = System.currentTimeMillis();
		 	GLS gls = new GLS(opts);
			 gls.start();
			long eTime = System.currentTimeMillis();
			double secs = (eTime - sTime) / 1000;
					
			System.out.print("Time Taken: "+secs);
		}
		else
		{
			System.out.println("Usage: java GLSDriver <.config> file");
			System.exit(0);
		}
	}
}