package data;

import java.util.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AlignmentEntry extends Entry
{
	// Each Link can be scored as well 
	public HashMap<Integer,HashMap<Integer,Double>> LINKS;
 
	// Pattern p = Pattern.compile("\\((\\d+)\\,(\\d+)\\)");

	// INDEX offset (Starts with 0 or 1) 
	int offset = 0; 
	
	public String alignStr = "";
	public int size = 0;
	public int linkcount = 0;

	public AlignmentEntry(int i, String s, String t, String a,int offset)
	{
		super(i,s,t);
		alignStr = a;
		this.offset = offset;
		
		LINKS = new HashMap<Integer,HashMap<Integer,Double>>();
		setAlignmentGDF(a);
	}
	
	public boolean isAligned(Integer x,Integer y){
		if(LINKS.containsKey(x)){
			if(LINKS.get(x).containsKey(y)){
				return true;
			}
		}
		return false;
	}
	
	public void addLink(Integer x,Integer y)
	{
		if(LINKS.containsKey(x))
		{
			LINKS.get(x).put(y,0.0);
		}
		else
		{
			HashMap<Integer,Double> tmpV = new HashMap<Integer,Double>();
			tmpV.put(y,0.0);
			LINKS.put(x,tmpV);
		}
		linkcount++;
	}
	
	public void addAll(int x, HashMap<Integer, Double> yvalues) {
		
		for(int y:yvalues.keySet()){
			if(! LINKS.get(x).containsKey(y))
			{
				addLink(x,y);
			}			
		}		
 	}
	
	public void dropAll(int x) {
		if(LINKS.containsKey(x))
		{
			linkcount-=LINKS.get(x).size();
			LINKS.remove(x);
		}
	}
	
	public void dropLink(int x, int y) {
		if(LINKS.containsKey(x))
		{
			if(LINKS.get(x).containsKey(y)){
				//System.err.println(senid+": Dropping a link:"+x+"-"+y);
				LINKS.get(x).remove(y);	
			}
		}
		linkcount--;
	}

	// Given an alignment string 1-1 2-3 3-2 GDA, GDF  format 
	// Parse it and fill it into the Vector of Alignment ; 
	public void setAlignmentGDF(String alignstr)
	{
		Pattern p = Pattern.compile("(\\d+)\\-(\\d+)");
		Matcher m = p.matcher(alignstr);
		while (m.find()) 
		{
			Integer x = new Integer(Integer.parseInt(m.group(1)));
			Integer y = new Integer(Integer.parseInt(m.group(2)));
			addLink(x-offset,y-offset); // deduct offset to keep ArrayIndex in bound
		}
	} 

	// 0-1 1-0 2-2 (index starts from 0) 
	public String toSMTString()
	{
		String str="";

		for(int x: LINKS.keySet())
		{
			HashMap<Integer,Double> values = LINKS.get(x);
			for(int y:values.keySet())
			{
				x=x+offset; // add the offset back 
				y=y+offset; 
				str+=x+"-"+y+" ";
			}
		}
		str = str.replaceAll(" $", "");
		return str; 
	}
}
