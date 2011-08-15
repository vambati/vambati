/**
 * 
 */
package taruHypothesis;

import java.util.Comparator;


/**
 * @author abhayaa
 *
 */
public class HypothesisStringComparator implements Comparator<Hypothesis> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * TODO convert to diff checking for floats
	 */
	// [TODO] Handle the case where one or the other may be null. Throw exception.
	public int compare(Hypothesis o1, Hypothesis o2) {
		return ((Hypothesis)o1).getWords().compareTo(((Hypothesis)o2).getWords());
	}

}
