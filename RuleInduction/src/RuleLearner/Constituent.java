/*
* Desc: Rule Learning using Version Spaces 
*
* Author: Vamshi Ambati 
* Email: vamshi@cmu.edu 
* Carnegie Mellon University 
* Date: 27-Jan-2007
*/

package RuleLearner;

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
	public int type = 0;

	public Constituent(int pos,int type,String str,int x,int y, int match) 
	{
		assert x > 0;
		assert y > 0;
		
		this.pos = pos;
		this.type = type;
		
		this.word = str;
		this.start = x;
		this.end = y;
		this.match = match;
	}
	public String toString()
	{
		if(type==0)
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
}