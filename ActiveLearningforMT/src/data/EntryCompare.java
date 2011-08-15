package data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import model.smt.TargetEntry;

public class EntryCompare {

public class TranslationEntryCompare implements Comparator<TranslationEntry>{
	// Descending Order 
	public int compare(TranslationEntry o1, TranslationEntry o2){
	double s1 = o1.score;
	double s2 = o2.score;
		if( s1 > s2)
			return -1;
		else if( s1 < s2)
			return 1;
		else{
			// In cases of TranslationEntry Prefer shorter sentence in TIES
			if(o1.sLength < o2.sLength){
				return -1;
			}else{
				return 1;
			}
		}
	}
}
public class AlignmentEntryCompare implements Comparator<AlignmentEntry>{

	// Descending Order 
	public int compare(AlignmentEntry o1, AlignmentEntry o2){
	double s1 = o1.score;
	double s2 = o2.score;
		if( s1 > s2)
			return -1;
		else 
			return 1;
	}
}
public class LinkEntryCompare implements Comparator<LinkEntry>{

	// Ascending Order 
	public int compare(LinkEntry o1, LinkEntry o2){
	double s1 = o1.score;
	double s2 = o2.score;
		if( s1 < s2)
			return -1;
		else  
			return 1;
		}
	}
public class PhraseEntryCompare implements Comparator<PhraseEntry>{

	// Ascending Order 
	public int compare(PhraseEntry o1, PhraseEntry o2){
	double s1 = o1.score;
	double s2 = o2.score;
		if( s1 < s2)
			return -1;
		else  
			return 1;
		}
	}
public class TargetEntryCompare implements Comparator<TargetEntry>{

	// Ascending Order 
	public int compare(TargetEntry o1, TargetEntry o2){
	double s1 = o1.score;
	double s2 = o2.score;
		if( s1 < s2)
			return -1;
		else  
			return 1;
		}
	}
public class DescendingCompare implements Comparator<Double>{
	// Reverse Ordering 
    public int compare(Double o1, Double o2) {
    	if(o1>o2){
    		return -1;
    	}else{
    		return 1;
    	}
    }
}	
	/* Method to sort values in a hashtable */
	public static Vector<Integer> sortHashtableByValues(Hashtable<Integer, TranslationEntry> passedMap, boolean ascending) {
		List<Integer> mapKeys = new ArrayList<Integer>(passedMap.keySet());
		List<TranslationEntry> mapValues = new ArrayList<TranslationEntry>(passedMap.values());

		// Collections.sort(mapValues,new TranslationEntryCompare());
		Collections.sort(mapKeys);

		if (!ascending) {
			Collections.reverse(mapValues);
		}

		Vector<Integer> resultids = new Vector<Integer>();
		
		for (TranslationEntry val: mapValues) {
			for (Integer key: mapKeys) { 
				if ( passedMap.get(key).equals(val) ) {
					passedMap.remove(key);
					mapKeys.remove(key);
					resultids.add(key); 
				break;
				}
			}
		}
		return resultids;
	}
}
