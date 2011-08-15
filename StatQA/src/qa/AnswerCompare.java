package qa;

import java.util.Comparator;

public class AnswerCompare implements Comparator<Answer>{

	// Descending Order 
	public int compare(Answer o1, Answer o2){
	double s1 = o1.score;
	double s2 = o2.score;
		if( s1 > s2)
			return -1;
		else if( s1 < s2)
			return 1;
		else
			return 0;
		} 
}
