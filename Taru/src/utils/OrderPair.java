/*
* Desc: Rule Learning using Version Spaces 
*
* Author: Vamshi Ambati 
* Email: vamshi@cmu.edu 
* Carnegie Mellon University 
* Date: 27-Jan-2007
*/

package utils;

public class OrderPair implements Cloneable
{
	public int x = 0;
	public int y = 0;

	public OrderPair(int x,int y)
	{
		this.x = x;
		this.y = y;
	}
	public String toString()
	{
		return "("+x+","+y+")"; 
	}
}
