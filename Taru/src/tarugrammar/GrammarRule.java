/**
 * 
 */
package tarugrammar;

/**
 * @author abhayaa
 *
 */
public class GrammarRule extends Rule{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String tgtString;
	int phraseLength = -1;
	
	public GrammarRule(String id, String target){
		super(id);
		this.tgtString = target;
		// Hopefully only phrasal rules will be queried for ti
		phraseLength = tgtString.split(" +").length - 1;
	}
	
	public String getTarget(){
		return tgtString;
	}

	public int getPhraseLength(){
//		System.out.println(tgtString + " " + phraseLength);
		return phraseLength;
	}
	
	public String toString(){
		return tgtString;
	}
}
