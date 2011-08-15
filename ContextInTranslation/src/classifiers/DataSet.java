package classifiers;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import tasks.gls.GLS;


public class DataSet {

	// Unique and hence the identifier (Can be used as a name for the model too !!!) 
	String event; // Word that you are predicting Binary Outcome for 
	ArrayList<Entry> entries; // Positive entries and Negative Entries
	
	public int positive=0;
	public int negative=0;
	
	// Dataset path 
	String filepath = GLS.modelDir+"/"+event+".txt";
	
	public DataSet(String word)
	{
		event = word; 
		entries = new ArrayList<Entry>();
	}
	
	public void addPositiveEntry(Entry e){
		entries.add(e);
		positive++;
	}
	
	public void addNegativeEntry(Entry e){
		entries.add(e);
		negative++;
	}
	
	public void print()
	{
		for(int i=0;i<entries.size();i++)
			System.err.println(entries.get(i).toString());
	}
	
	public void printToDirectory(String dir) throws Exception
	{
		filepath = GLS.modelDir+"/"+event+".txt";
		BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
		for(int i=0;i<entries.size();i++)
			writer.write(entries.get(i).toString());
		writer.flush();
	}
}
