/**
 * 
 */
package tarugrammar;

import java.io.Serializable;

/**
 * @author abhayaa
 *
 */
public class Rule implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected String ruleId;
	protected int identifier;
	
	public Rule(String id){
		this.ruleId = id;
	}

	public String getId(){
		return ruleId;
	}
	
	public void setIdentifier(int x){
		identifier = x;
	}
}
