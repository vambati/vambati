/**
 * 
 */
package taruHypothesis;

import java.util.Comparator;


/**
 * @author abhayaa
 *
 */
public class HypothesisHScoreComparator implements Comparator<Hypothesis> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * TODO convert to diff checking for floats
	 */
	// [TODO] Handle the case where one or the other may be null. Throw exception.
	// This comparator returns the values opposite to what you would expect so that
	// the min PriorityQueue becomes a max PriorityQueue
	public int compare(Hypothesis o1, Hypothesis o2) {
		if(((Hypothesis)o1).hScore > ((Hypothesis)o2).hScore){
			return -1;
		}
		else if(((Hypothesis)o1).hScore < ((Hypothesis)o2).hScore){
			return 1;
		}
		else {
			return 0;
		}
	}

}
