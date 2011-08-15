/*
* Desc: Rule Learning using Version Spaces 
*
* Author: Vamshi Ambati 
* Email: vamshi@cmu.edu 
* Carnegie Mellon University 
* Date: 27-Jan-2007
*/

package Rule;

public class Constituent implements Cloneable
{
	// Approx position to sort and move constituents in the rules 
	public int pos = 0;
	
	// Identifier to keep track of position in the Final Rule 
	public int match = 0;
	
	// Pointers to position in the respective Source and Target sentences from where this constituent 
	// was extracted 
	public int start = -1;
	public int end = -1;
	
	// Actual content, 'LEXICAL', 'POS' or 'CONSTITUENT'
	public String word = "";
	
	// 0 - Lexical item
	// 1 - POS item 
	// 2 - Const item 
	public final int type;
	
	public static final int LEXICAL = 0;
	public static final int POS = 1;
	public static final int CONST = 2;

	public Constituent(int pos,int type,String str,int x,int y, int match) 
	{
		this.pos = pos;
		this.type = type;
		
		this.word = str;
		this.start = x;
		this.end = y;
		this.match = match;
		
		if(type != LEXICAL && type != POS) {
			throw new RuntimeException("Constituent type not recognized: " + type);
		}
	}
	public String toString()
	{
		if(isLexical())
		{
			// Quotations need to be escaped 
			if(word.equals("\"")){
				return "\"\\"+word+"\"";
			}
			return "\""+word+"\"";
		}
		else
		{
			//return word+":"+match;
			return word;
		}
	}
	
	public boolean isLexical() {
		return (type == 0);
	}
}