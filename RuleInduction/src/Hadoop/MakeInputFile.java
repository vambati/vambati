package Hadoop;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

public class MakeInputFile {

	public static void main(String[] args) throws Exception {
		if (args.length != 4 && args.length != 5) {
			System.err.println("Usage: program fCorpusFile eCorpusFile eParseFile alignmentFile [fParseFile]");
			System.exit(1);
		}
		
		boolean useTarget = args.length == 5;

		BufferedReader fCorpusIn = new BufferedReader(new FileReader(args[0]));
		BufferedReader eCorpusIn = new BufferedReader(new FileReader(args[1]));
		BufferedReader eParseIn = new BufferedReader(new FileReader(args[2]));
		BufferedReader alignmentIn = new BufferedReader(new FileReader(args[3]));
		BufferedReader fParseIn = null;
		if(useTarget)
			 fParseIn = new BufferedReader(new FileReader(args[4]));

		int nLines = 0;
		String fcLine;
		while ((fcLine = fCorpusIn.readLine()) != null) {
			String ecLine = eCorpusIn.readLine();
			String epLine = eParseIn.readLine();
			String aLine = alignmentIn.readLine();
			String fpLine = null;
			if(useTarget)
				fpLine = fParseIn.readLine();

			
			if(nLines % 100000 == 0) {
				System.err.println("Read " + nLines + " lines so far...");
			}
			nLines++;
			
			StringTokenizer alignmentTokenizer = new StringTokenizer(aLine);
			StringBuilder newAlignments = new StringBuilder(aLine.length()*2);
			newAlignments.append("(");
			int n = alignmentTokenizer.countTokens();
			for(int i=0; i<n; i++) {
				String link = alignmentTokenizer.nextToken();
				
				StringTokenizer linkTokenizer = new StringTokenizer(link, "-");
				int x = Integer.parseInt(linkTokenizer.nextToken());
				int y = Integer.parseInt(linkTokenizer.nextToken());
				
				newAlignments.append("(" + (y+1) + "," + (x+1) + ")");;
				if(i < n-1) {
					newAlignments.append(",");
				}
			}
			newAlignments.append(")");

			final String line;
			if(useTarget)
				line = ecLine + " ||| " + fcLine + " ||| " + epLine + " ||| " + newAlignments.toString() + " ||| " + fpLine;
			else
				line = ecLine + " ||| " + fcLine + " ||| " + epLine + " ||| " + newAlignments.toString();
			System.out.println(line);
		}
		
		System.err.println("Read " + nLines + " lines.");
		
		if(eCorpusIn.readLine() != null)
			throw new RuntimeException("Too many lines in eCorpus");
		if(fParseIn != null && fParseIn.readLine() != null)
			throw new RuntimeException("Too many lines in fParse");
		if(eParseIn.readLine() != null)
			throw new RuntimeException("Too many lines in eParse");
		if(alignmentIn.readLine() != null)
			throw new RuntimeException("Too many lines in alignmentFile");
		
		fCorpusIn.close();
		eCorpusIn.close();
		if(fParseIn != null)
			fParseIn.close();
		eParseIn.close();
		alignmentIn.close();
	}
}
