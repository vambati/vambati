package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.FileNotFoundException;

public class GrammarTestsetFilterer
{

	public static void main(String[] args)
		throws FileNotFoundException, IOException
	{
		// Check usage:
		if(args.length != 3)
		{
			System.err.println("Usage: java GrammarTestsetFilterer " +
							   "<lexicon> <testset> <c|i>");
			System.err.println("  <Grammar> is scored and in transfer format");
			System.err.println("  <testset> is a plaintext file of test sentences");
			System.err.println("  c = case sensitive; i = case insensitive");
			return;
		}

		// Get casing style:
		boolean cased = true;
		if(args[2].equalsIgnoreCase("i"))
			cased = false;

		// Process:
		ArrayList<String> testSents = LoadTestset(args[1], cased);
		FilterGrammarFile(args[0], testSents, cased);
		return;
	}


	public static ArrayList<String> LoadTestset(String testFile,
												boolean cased)
		throws FileNotFoundException, IOException
	{
		// Open the file of test set sentences:
		BufferedReader br = new BufferedReader(new FileReader(testFile));
		String line = br.readLine();
		ArrayList<String> sents = new ArrayList<String>();

		// Read in and store each sentence:
		while(line != null)
	    {
			if(cased)
				//line = toLoverCase(Locale.FRENCH);
				line = line.toLowerCase();
			sents.add(line);
			line = br.readLine();
		}
		br.close();
		System.err.println("Loaded test set: " + sents.size() + " sentences.");

		return sents;
	}


	public static void FilterGrammarFile(String lexFile,
										 ArrayList<String> testSents,
										 boolean cased)
		throws FileNotFoundException, IOException
	{
		// Initialize counts:
		int keptCount = 0;
		int totalCount = 0;

		// Pattern for getting source side out of lexical entry:
		Pattern lexHead = Pattern.compile("[^:]+::[^:]+\\s+\\[\"(.+)\"\\s*\\]\\s+->\\s+\\[.+\\]");

		// Open grammar file:
		BufferedReader br = new BufferedReader(new FileReader(lexFile));
		String line = br.readLine();
		while(!line.matches("\\s*\\{[^,]+,\\d+\\}\\s*"))
			line = br.readLine();

		while(true)
	    {
			// Get the complete text of the next lexical entry:
			String lexEntry = line;
			line = br.readLine();
			if(line == null)
				break;
			while(!line.matches("\\s*\\{[^,]+,\\d+\\}\\s*"))
			{
				lexEntry += ("\n" + line);
				line = br.readLine();
				if(line == null)
					break;
			}
			if(line == null)
				break;

			// See if the entry's source side is in the test set:
			Matcher m = lexHead.matcher(lexEntry);
			if(m.find())
			{
				// Get source side:
				String srcPhrase = m.group(1);
				srcPhrase = srcPhrase.replaceAll("\"\\s+\"", " ");
				if(cased)
					//line = toLoverCase(Locale.FRENCH);
					srcPhrase = srcPhrase.toLowerCase();
				totalCount++;

				// Check against test set sentences:
				for(String sent : testSents)
				{
					if(sent.contains(srcPhrase))
					{
						System.out.println(lexEntry);
						keptCount++;
						break;
					}
				}

				// Display periodic progress update:
				if(totalCount % 10000 == 0)
					System.err.println(keptCount + " / " + totalCount);
			}
		}
		br.close();
		double pctKept = 100.0 * (double)keptCount / (double)totalCount;
		System.err.println("Kept " + keptCount + " of " + totalCount +
						   " lexical entries (" + pctKept + " percent)");
		return;
	}

}