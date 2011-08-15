// TransferRuleSet.java
// (c) by Greg Hanneman.  Written for 10-701.
// Last modified October 23, 2008.


package grammar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrammarReader
{	
	// ONELINE FORMAT
	public static final Pattern ONELINE_RULE_PATTERN =
		Pattern.compile("^([^:]+)\t([^:]+)\t\\[([^\\]]*)\\]\t\\[([^\\]]*)\\]\t\\{([^\\}]*)\\}");
	public static final Pattern ONELINE_ALIGN_PATTERN = Pattern.compile("(\\d+)-(\\d+)");
	
	// AVENUE FORMAT 
	// ID: ///////////////////////////////////////////////////////////
	public static final Pattern RULE_END_PATTERN =
		Pattern.compile("^\\)");
	
	public static final Pattern RULE_ID_PATTERN =
		Pattern.compile("^\\{([^:]+),(\\d+)\\}");
	
	// Constants: ///////////////////////////////////////////////////////////
	public static final Pattern PHR_RULE_PATTERN =
		Pattern.compile("^([^:]+)::([^:]+) \\|: \\[([^\\]]*)\\] -> \\[([^\\]]*)\\]");
	
	// Group 1: source node label.  Group 2: target node label.
	// Group 3: source right-hand side.  Group 4: target right-hand side.
	public static final Pattern CFG_RULE_PATTERN =
		Pattern.compile("^([^:]+)::([^:]+)\\s+\\[([^\\]]*)\\] -> \\[([^\\]]*)\\]");
	
	// Group 1: source-given-target rule score
	public static final Pattern SGT_SCORE_PATTERN =
		Pattern.compile("\\(\\*sgtrule\\* ([^)]+)\\)");

	// Group 1: target-given-source rule score
	public static final Pattern TGS_SCORE_PATTERN =
		Pattern.compile("\\(\\*tgsrule\\* ([^)]+)\\)");
	
	// Group 1: frequency count
	public static final Pattern FREQ_PATTERN =
		Pattern.compile("\\(\\*freq\\* ([^)]+)\\)");
	
	// Group 1: source constituent index.  Group 2: target constituent index.
	public static final Pattern CONSTIT_ALIGN_PATTERN =
		Pattern.compile("\\([Xx](\\d+)::[Yy](\\d+)");

	// Constructors: ////////////////////////////////////////////////////////
	// root    root    [s ]    [s ]    {1-1}
	public static void loadGrammarOneline(String rawFile, RuleVisitor rv) throws Exception {
		BufferedReader ruleReader = new BufferedReader(new FileReader(rawFile));
		String line = ruleReader.readLine();
		
		int id = 1;
		Rule currRule = null;
		
		while( (line = ruleReader.readLine()) != null)
		{
			String[] arr = line.split("\\|\\|\\|");
			String ruleStr = "";
			String featureStr = "";

			ruleStr = arr[0];
			
			// See what kind of a line we have; act accordingly:
			Matcher oneline = GrammarReader.ONELINE_RULE_PATTERN.matcher(ruleStr);
			if (oneline.find()){				
				// Starting a new rule;
				// System.err.println(line);
				
				currRule = new Rule(oneline.group(1), oneline.group(2),oneline.group(3), oneline.group(4));
				// Consists of context based features
				if(arr.length==2) {
					featureStr = arr[1];
					currRule.addFeatureSet(featureStr);	
				}
				String alignstr = oneline.group(5);

				Matcher alignpattern = GrammarReader.ONELINE_ALIGN_PATTERN.matcher(alignstr);
				while(alignpattern.find()){
					int x = Integer.parseInt(alignpattern.group(1));
					int y = Integer.parseInt(alignpattern.group(2));
					currRule.AddConstituentAlignment(x,y);
				}
				currRule.setID(id++);
				rv.action(currRule);
			}
		}
	}
	
	public static void loadGrammar(String scoredRuleFile, RuleVisitor rv)
	{
		// Read rules from input file to build list:
		try
		{
			BufferedReader ruleReader =
				new BufferedReader(new FileReader(scoredRuleFile));
			String line = ruleReader.readLine();
			
			int curID = -2;
			Rule currRule = null;
			while(line != null)
			{
				// See what kind of a line we have; act accordingly:
				Matcher id = GrammarReader.RULE_ID_PATTERN.matcher(line);
				Matcher end = GrammarReader.RULE_END_PATTERN.matcher(line);
				Matcher cfg = GrammarReader.CFG_RULE_PATTERN.matcher(line);
				Matcher sgt = GrammarReader.SGT_SCORE_PATTERN.matcher(line);
				Matcher tgs = GrammarReader.TGS_SCORE_PATTERN.matcher(line);
				Matcher frq = GrammarReader.FREQ_PATTERN.matcher(line);
				Matcher con = GrammarReader.CONSTIT_ALIGN_PATTERN.matcher(line);
				
				if(id.find())
				{
					// Add the id of the rule 
					curID = Integer.parseInt(id.group(2));
				}
				else if(end.find())
				{
					// Add the old one to the list:
					if(currRule != null) {
						rv.action(currRule);
					}
					
				}else if (cfg.find()){					
					// Starting a new rule;
					currRule = new Rule(cfg.group(1), cfg.group(2),cfg.group(3), cfg.group(4));
					currRule.setID(curID);
				}
				else if(sgt.find())
				{
					// Add the source-given-target score as a feature:
					currRule.SetSGTScore(Double.parseDouble(sgt.group(1)));
				}
				else if(tgs.find())
				{
					// Add the target-given-source score as a feature:
					currRule.SetTGSScore(Double.parseDouble(tgs.group(1)));
				}
				else if(frq.find())
				{
					// Add the frequency count as a feature:
					currRule.SetFrequency(Integer.parseInt(frq.group(1)));
				}
				else if(con.find())
				{
					// Add a new constituent alignment to the rule:
					currRule.AddConstituentAlignment(Integer.parseInt(con.group(1)),
													 Integer.parseInt(con.group(2)));
				}
				line = ruleReader.readLine();
			}
		}
		catch(IOException e)
		{
			System.err.println("Error reading from scored rule file!");
			throw(new RuntimeException(e));
		}
	}		
}
