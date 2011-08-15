/**
 * 
 */
package tarugrammar;

/**
 * @author abhayaa
 *
 */
public class LexicalRule extends Rule{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String targetPOS;
	private String targetWord;
	
	public LexicalRule(String id, String target){
		super(id);
		extractPOS(target);
	}
	
	private void extractPOS(String target){
		String [] tokens = target.split(" +");
		this.targetPOS = tokens[0];
		this.targetWord = tokens[1];
	}
	
	public String getTargetPOS(){
		return targetPOS;
	}

	public String getTargetWord(){
		return targetWord;
	}

	public String toString(){
		return targetPOS + " " + targetWord ;
	}
}
