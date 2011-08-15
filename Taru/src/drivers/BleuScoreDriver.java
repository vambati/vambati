package drivers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import utils.evaluation.*;

/** 
 * First param: hypothesis file
 * Second param: Reference file
 *  */

public class BleuScoreDriver
{
	public static void main(String[] args) throws Exception
	{
		String hypFile = args[0];
		String refFile = args[1];
		
		ArrayList<String> hyps = readFile(hypFile);
		ArrayList<String> refs = readFile(refFile);
		
		BleuScore bleu = new BleuScore(hyps,refs);
		double value = bleu.printStats();
		System.out.println(value);
		
		// Print Statistics from within implementation
		bleu.printStats();
	}
		
	public static ArrayList<String> readFile(String fileReference) throws Exception
	{
		ArrayList<String> out = new ArrayList<String>();
		BufferedReader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileReference)));
		String lineFile;
		int cnt = 0;
		
		while ((lineFile = inputFile.readLine()) != null)
		{
			out.add(lineFile);
			cnt++;
		}
		inputFile.close();
		System.out.println("Loaded:"+cnt+" sentences");
		return out;
	}
}
