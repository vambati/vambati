package model.smt;

import java.util.Comparator;

public class LexicalEntryCompare {

	public class EntropyCompare implements Comparator<LexicalEntry>{

	// Descending Order 
	public int compare(LexicalEntry o1, LexicalEntry o2){
	double s1 = o1.getEntropy();
	double s2 = o2.getEntropy();
		if( s1 > s2)
			return -1;
		else if( s1 < s2)
			return 1;
		else
			return 0;
		}
	}

	public  class IGCompare implements Comparator<LexicalEntry>{

	// Descending Order 
	public int compare(LexicalEntry o1, LexicalEntry o2){
	double s1 = o1.getIG();
	double s2 = o2.getIG();
		if( s1 > s2)
			return -1;
		else if( s1 < s2)
			return 1;
		else
			return 0;
		}
	}

	public class IGRatioCompare implements Comparator<LexicalEntry>{

	// Descending Order 
	public int compare(LexicalEntry o1, LexicalEntry o2){
	double s1 = o1.getIGRatio();
	double s2 = o2.getIGRatio();
		if( s1 > s2)
			return -1;
		else if( s1 < s2)
			return 1;
		else
			return 0;
		}
	}
}