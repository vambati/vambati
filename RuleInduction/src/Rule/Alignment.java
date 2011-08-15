/*
* Desc: Rule Learning using Version Spaces 
*
* Author: Vamshi Ambati 
* Email: vamshi@cmu.edu 
* Carnegie Mellon University 
* Date: 27-Jan-2007
*/

package Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Alignment implements Cloneable
{
	HashMap<Integer,Vector<Integer>> AMap;
	HashMap<Integer,Vector<Integer>> RAMap;
	public int maxX = -1;
	public int maxY = -1;
	
	String alignStr = "";
	int size = 0;

	public int size()
	{
		return size; 
	}
	
	public boolean isAligned(Integer x){
		if(AMap.containsKey(x)){
			return true;
		}
		return false;
	}
	
	public void addLink(Integer x,Integer y)
	{
		if(AMap.containsKey(x))
		{
			AMap.get(x).add(y);
		}
		else
		{
			Vector<Integer> tmpV = new Vector<Integer>();
			tmpV.add(y);
			AMap.put(x,tmpV);
		}
	}

	public Alignment(HashMap<Integer,Vector<Integer>> amap)
	{
		this.AMap = amap;
	}
	
	public Alignment(String str)
	{
		AMap = new HashMap<Integer,Vector<Integer>>();
		setAlignment(str);
		RAMap = new HashMap<Integer,Vector<Integer>>();
		setReverseAlignment(str);
		alignStr = str;
	}

	public Alignment reverseClone() {
		// Reversing the alignment String first   
		String rev_str="";
		Pattern p = Pattern.compile("\\((\\d+)\\,(\\d+)\\)");
		Matcher m = p.matcher(alignStr);
		while (m.find()) 
		{
			Integer x = new Integer(Integer.parseInt(m.group(1)));
			Integer y = new Integer(Integer.parseInt(m.group(2)));
			rev_str+="("+y+","+x+"),";
		}
		rev_str=rev_str.replaceAll(",$", ""); 
		rev_str="("+rev_str+")";

		// Create a new alignment object with the reversed string 
		Alignment rev_align = new Alignment(rev_str);
		return rev_align;
	}

	// Given an alignment string ((1,1),(2,2)) format 
	// Parse it and fill it into the Vector of Alignment ; 
	public void setAlignment(String alignstr)
	{
		Pattern p = Pattern.compile("\\((\\d+)\\,(\\d+)\\)");
		Matcher m = p.matcher(alignstr);
		while (m.find()) 
		{
			Integer x = new Integer(Integer.parseInt(m.group(1)));
			Integer y = new Integer(Integer.parseInt(m.group(2)));
			if(x>maxX){ maxX = x; }
			if(y>maxY) { maxY = y; }
			if(AMap.containsKey(x))
			{
				AMap.get(x).add(y);
			}
			else
			{
				Vector<Integer> v = new Vector<Integer>();
				v.add(y);
				AMap.put(x,v);	
			}
		}
	} 
	
	// Given an alignment string ((1,1),(2,2)) format 
	// Parse it and fill it into the Vector of Alignment ; 
	public void setReverseAlignment(String alignstr)
	{
		Pattern p = Pattern.compile("\\((\\d+)\\,(\\d+)\\)");
		Matcher m = p.matcher(alignstr);
		while (m.find()) 
		{
			Integer x = new Integer(Integer.parseInt(m.group(1)));
			Integer y = new Integer(Integer.parseInt(m.group(2)));
			if(RAMap.containsKey(y))
			{
				RAMap.get(y).add(x);
			}
			else
			{
				Vector<Integer> v = new Vector<Integer>();
				v.add(x);
				RAMap.put(y,v);	
			}
		}
	}
	
	public int getAlignmentType(int x)
	{
		Vector<Integer> v = AMap.get(new Integer(x));
		
		if(v!=null){  
			if(v.size() == 1 && RAMap.get(v.elementAt(0)).size() == 1)
				return 1;
	//		for(int i=0;i<v.size();i++)
	//		{
	//			Integer y = v.elementAt(i);
	//			if(RAMap.get(y).size()==1)
	//			{
	//				// One to One 
	//				return 1;
	//			}
	//			else
	//			{
	//				// Many to Many 
	//				return 0;
	//			}
	//		}
		}
	return 0;
	}

// This function assumes its only a one to one alignment. 
// Caller's responsiblity to check if it is actually one to one 
// Function "isOneOne" verifies this - 
	public Vector<Integer> getAlignment(int n) 
	{
//		System.out.println(AMap.toString());
		Integer key = new Integer(n);
		if(AMap.containsKey(key))
		{
			return AMap.get(key);
		}
		else
		{
			return null;
		}
	}

	public Vector<Integer> getReverseAlignment(int n) 
	{
//		System.out.println(AMap.toString());
		Integer key = new Integer(n);
		if(RAMap.containsKey(key))
		{
			return RAMap.get(key);
		}
		else
		{
			return null;
		}
	}

	// 0-1 1-0 2-2 (index starts from 0) 
	public String toSMTString()
	{
		String str="";
		List<Integer> l = new ArrayList<Integer>(AMap.keySet());
		Collections.sort(l);
		Iterator<Integer> iter = l.iterator(); 
	
		while(iter.hasNext())
		{
			Integer x = iter.next();
			Vector<Integer> values = AMap.get(x);
			for(int i=0;i<values.size();i++)
			{
				Integer y = values.elementAt(i);
				str+=(x.intValue()-1)+"-"+(y.intValue()-1)+" ";
			}
		}
		str = str.replaceAll(" $", "");
		return str; 
	}
	
	// 1-1 2-1 2-2 (index starts from 1)
	public String toConciseString()
	{
		String str="";
		List<Integer> l = new ArrayList<Integer>(AMap.keySet());
		Collections.sort(l);
		Iterator<Integer> iter = l.iterator(); 
		
		while(iter.hasNext())
		{
			Integer x = iter.next();
			Vector<Integer> values = AMap.get(x);
			for(int i=0;i<values.size();i++)
			{
				Integer y = values.elementAt(i);
				str+=(x.intValue())+"-"+(y.intValue())+" ";
			}
		}
		str = str.replaceAll(" $", "");
		return str; 
	}
	
	public String toString()
	{
		String str="";
		Iterator<Integer> iter = AMap.keySet().iterator();
		while(iter.hasNext())
		{
			Integer x = iter.next();
			Vector<Integer> values = AMap.get(x);
			for(int i=0;i<values.size();i++)
			{
				Integer y = values.elementAt(i);
				str+="(X"+x.toString()+"::Y"+y.toString()+")\n";
			}
		}
		return str; 
	}
	
	public void updateAlignmentStr()
	{
		String str="";
		Iterator<Integer> iter = AMap.keySet().iterator();
		while(iter.hasNext())
		{
			Integer x = iter.next();
			Vector<Integer> values = AMap.get(x);
			for(int i=0;i<values.size();i++)
			{
				Integer y = values.elementAt(i);
				str+="("+x.toString()+","+y.toString()+"),";
			}
			str.replaceAll(",$", "");
			str="("+str+")";
		}
		alignStr =  str; 
	}

	// What if the order of entries in the alignStr are different ? 
	// SO check for the entire hashmap ?? 
	public boolean equals(Alignment a){
		if(this.alignStr.equals(a.alignStr))
			return true;
		else 
			return false;
	}
}
