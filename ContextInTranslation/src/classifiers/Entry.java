package classifiers;

import java.util.ArrayList;

public class Entry {

	// An Entry for training the MaxEnt models in GLS 
	// To keep it more generic one has to modify it to contain non-String features : TODO 
	
	public String outcome = "";
	public ArrayList<String> context = null;
	
	public Entry(String outcome, ArrayList<String> context){
		this.outcome = outcome; 
		this.context = context; 
	}
	
	public String toString(){
		String str = ""; // Print for opennlp.maxent format 
		// Context 
		for(int i=0;i<context.size();i++){
			str+="word"+i+"="+context.get(i)+" ";
		}
		// Outcome
		str+=outcome+"\n";
	return str; 
	}
}