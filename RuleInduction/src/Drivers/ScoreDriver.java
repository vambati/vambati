package Drivers; 

import RuleLearner.*;
import Options.*;
import Utils.*;

/**
 * Rule Transduction Code (Tree to Tree, Tree to String ) (Output format 'String' or 'Tree' )
 * @author Vamshi Ambati
 * 12 Mar 2008
 * Carnegie Mellon University
 *
 */

public class ScoreDriver 
{
	public static void main(String args[]) throws Exception
	{
		if(args.length==1)
		{
			System.out.println("Rules file path is: "+args[0]); 

		 	long sTime = System.currentTimeMillis();
		 	 Scorer scorer = new Scorer(args[0]);
			 scorer.start();
			long eTime = System.currentTimeMillis();
			double secs = (eTime - sTime) / 1000;
					
			System.out.print("Time Taken: "+secs);
		}
		else
		{
			System.out.println("Usage: java ScoreDriver <rulefile>");
			System.exit(0);
		}
	}
}