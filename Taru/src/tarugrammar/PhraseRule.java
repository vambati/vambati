/**
 * 
 */
package tarugrammar;

/**
 * @author vamshi
 *
 */
public class PhraseRule extends Rule{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String targetPOS;
	private String targetWord;
	
	private String srcPOS;
	private String srcWord;
	
	public PhraseRule(String id, String stype, String src, String ttype, String target){
		super(id);
		
		srcPOS = stype; 
		srcWord = src; 
		targetPOS = ttype; 
		targetWord = target; 
	}
	
	public String getTargetPOS(){
		return targetPOS;
	}

	public String getTargetWord(){
		return targetWord;
	}

	public String getSourcePOS(){
		return srcPOS;
	}

	public String getSourceWord(){
		return srcWord;
	}

	public String toString(){
		return targetPOS + " " + targetWord ;
	}
}
