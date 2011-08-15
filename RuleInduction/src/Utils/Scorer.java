package Utils;

import java.io.*;

public class Scorer {

	public Scorer(String file)
	{
	}
	public void start()
	{
		
	}
	
	public void load(String ruleFile) throws Exception
	{
	   	BufferedReader corpusReader = null ;
	   	int i=1;  	
		try {	
			corpusReader= new BufferedReader(new InputStreamReader(new FileInputStream(ruleFile)));
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
						i++;
			}
			else 
			{
				continue;
			}
		}
	}
}
