package Drivers; 

import RuleLearner.*;
import Options.*;

/**
 * Rule Transduction Code (Tree to Tree, Tree to String ) (Output format 'String' or 'Tree' )
 * @author Vamshi Ambati
 * 12 Mar 2008
 * Carnegie Mellon University
 *
 */

public class RuleLearningDriver 
{
	public static void main(String args[]) throws Exception
	{
		if(args.length==1)
		{
			System.err.println("Config path is: "+args[0]);
			Options opts = new Options(args[0]); 

		 	long sTime = System.currentTimeMillis();
		 	RuleLearner rl = new RuleLearner(opts);
			 rl.start();
			long eTime = System.currentTimeMillis();
			double secs = (eTime - sTime) / 1000;
					
			System.err.println("Time Taken: "+secs);
		}
		else
		{
			System.err.println("Usage: java RuleLearningDriver <.config> file");
			System.exit(0);
		}
	}
}