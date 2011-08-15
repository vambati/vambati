/*
* Desc: Rule learning 
*
* Author: Vamshi Ambati 
* Email: vamshi@cmu.edu 
* Carnegie Mellon University 
* Date: 27-Jan-2007
*/

package RuleLearner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import Rule.RuleLearnerException;
import Scoring.RuleException;
import Utils.MyUtils;

public class Corpus
{
// CorpusFile should follow the Elicitation CORPUS format.
// No extra checks are made here. 
public int skiplength = -1; 

public Corpus(RuleLearner rl)  
{
	skiplength = rl.skiplength;
}

public void load(RuleLearner rl) throws Exception
{
	if(rl.input_mode.equals("T2S"))
	{
		System.err.println("Working in T2S Input mode...");
		loadCorpus_t2s(rl);
	}
	else if(rl.input_mode.equals("T2T")) {
		loadCorpus_t2t(rl);
	}
	else if(rl.input_mode.equals("T2TS")) {
		loadCorpus_t2t(rl);
	}
	else if(rl.input_mode.equals("TS2TS")) {
		loadCorpus_t2t(rl);
	}
	else
	{
		System.out.println("Input mode is not defined "+rl.input_mode);
		System.exit(0);
	}
}

public void loadCorpus_t2s (RuleLearner rulelearner) throws Exception
{
   	BufferedReader corpusReader = null ;
   	BufferedReader sparseReader = null ;
   	int i=1;
   	
	try {		
	corpusReader= new BufferedReader(new InputStreamReader(new FileInputStream(rulelearner.corpusFile)));
	sparseReader= new BufferedReader(new InputStreamReader(new FileInputStream(rulelearner.sparseFile)));
	}catch(IOException ioe){}

	String str = "";
	String sl ="",tl="",type="",align = "",sparsetree="";

	String[] tokens;
	
	while( (str = corpusReader.readLine())!=null)
	{
		if(str.startsWith("Alignment:"))	{
		      tokens   = str.split(":"); 
		      if(tokens.length > 1) 
			align = tokens[1];
		}
		else if(str.startsWith("SL:"))	{
		      str = str.replaceAll("^SL:", ""); 
		      sl = str;
		}
		else if(str.startsWith("TL:"))	{
			str = str.replaceAll("^TL:", ""); 
		      tl = str;
		}
		else if(str.startsWith("Type:"))	{
		      tokens   = str.split(":"); 
		      if(tokens.length > 1) 
		      type = tokens[1];
		}
	
	// New line needs to be present at the end in order to ADD
		else if(str.equals(""))
		{
			if((sparsetree = sparseReader.readLine())!=null)
			{
				if((sparsetree.equals("NULL")) || (sparsetree.length() == 0))
				{
					System.out.println("SParse Error at -"+ i);
					continue;
				}
				else {
					CorpusEntry ce = createEntry(sl,tl,type,sparsetree,align); 
					rulelearner.transduce_t2s(ce,i);

					sl= tl=type=sparsetree=align="";
					i++;
				}
			}
		}
		else 
		{
			continue;
		}
	}
}

public void loadCorpus_t2t (RuleLearner rulelearner) throws IOException, InterruptedException
{
   	BufferedReader corpusReader = null ;
   	BufferedReader sparseReader = null ;
   	BufferedReader tparseReader = null ;
   	int i=1;
   	
	corpusReader= new BufferedReader(new InputStreamReader(new FileInputStream(rulelearner.corpusFile)));
	sparseReader= new BufferedReader(new InputStreamReader(new FileInputStream(rulelearner.sparseFile)));
	tparseReader= new BufferedReader(new InputStreamReader(new FileInputStream(rulelearner.tparseFile)));

	String str = "";
	String sl ="",tl="",type="",align = "";
	String sparsetree="",tparsetree="";

	String[] tokens;
	
	while( (str = corpusReader.readLine())!=null)
	{
		if(str.startsWith("Alignment:"))	{
		      tokens   = str.split(":"); 
		      if(tokens.length > 1) 
			align = tokens[1];
		}
		else if(str.startsWith("SL:"))	{
		      str = str.replaceAll("^SL:", ""); 
		      sl = str;
		}
		else if(str.startsWith("TL:"))	{
			str = str.replaceAll("^TL:", ""); 
		      tl = str;
		}
		else if(str.startsWith("Type:"))	{
		      tokens   = str.split(":"); 
		      if(tokens.length > 1) 
		      type = tokens[1];
		}
	
	// New line needs to be present at the end in order to ADD
		else if(str.equals(""))
		{
			if(
					(sparsetree = sparseReader.readLine())!=null
					&&
					(tparsetree = tparseReader.readLine())!=null
				)
			{
				//System.out.println("SParseTree: "+sparsetree);
				//System.out.println("TParseTree: "+tparsetree);
				if((sparsetree.equals("NULL")) || (sparsetree.length() == 0))
				{
					System.out.println("SParse Error at -"+ i);
					continue;
				}
				else if(align.equals("()"))
				{
					System.out.println("Alignment Error at -"+ i);
					continue;
				}
				else {
					if(i % 10000 == 0){
						System.out.println("Reading corpus entry " + i);
//						System.gc();
					}
					CorpusEntry ce = createEntry(sl,tl,type,sparsetree,tparsetree,align);
					
					try {
						if(rulelearner.input_mode.equals("T2T")) {
							rulelearner.transduce_t2t(ce,i);
						}else if(rulelearner.input_mode.equals("T2TS")) {
							rulelearner.transduce_t2ts(ce,i);
						}else if(rulelearner.input_mode.equals("TS2TS")) {
							rulelearner.transduce_ts2ts(ce,i);
						}
					} catch(RuleException e) {
						throw new RuntimeException("Error processing corpus entry: " + ce.toString(), e);
					} catch (RuleLearnerException e) {
						throw new RuntimeException("Error processing corpus entry: " + ce.toString(), e);
					}
								
					sl = tl = type = sparsetree = align = "";
					i++;
				}
			}
		}
		else 
		{
			continue;
		}
	}
}

public CorpusEntry createEntry (  String sl, 	
		String tl, 
		String type, 
		String cs, 
		String align
		)
{
//System.out.println("SL:"+sl);
sl = MyUtils.trim(sl);
tl = MyUtils.trim(tl);
CorpusEntry ce = new CorpusEntry(sl,tl,type,cs,align);
return ce;
}

public CorpusEntry createEntry (  String sl, 	
			String tl, 
			String type, 
			String cs,
			String tcs, 
			String align
			)
{
	//System.out.println("SL:"+sl);
	sl = MyUtils.trim(sl);
	tl = MyUtils.trim(tl);
	CorpusEntry ce = new CorpusEntry(sl,tl,type,cs,tcs,align);
	return ce; 
}
}
