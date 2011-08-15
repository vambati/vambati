/*
* Desc: Rule Learning using Version Spaces 
*
* Author: Vamshi Ambati 
* Email: vamshi@cmu.edu 
* Carnegie Mellon University 
* Date: 27-Jan-2007
*/

package RuleLearner;
import java.util.*;

import Rule.Constituent;

public	class Comparer implements Comparator<Constituent> {
		public int compare(Constituent o1, Constituent o2) {
 			return (int) ((Constituent)o1).pos - ((Constituent)o2).pos; 
		}
}