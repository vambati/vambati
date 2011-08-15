/*
* Desc: Rule learning 
*
* Author: Vamshi Ambati 
* Email: vamshi@cmu.edu 
* Carnegie Mellon University 
* Date: 27-Jan-2007
*/

package RuleLearner;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class CorpusEntry
{
String S; 
String T; 
String Type; 
String SParse;
String TParse;
String AlignmentStr; 

int sLength;
int tLength;

public Vector<String> sSeq;
public Vector<String> tSeq;

// TODO 
public CorpusEntry(     
			String sl, 
			String tl, 
			String type, 
			String cs, 
			String align
 		  )
{ 
	S = sl;
	T = tl;
	Type = type;
	SParse = cs;
	AlignmentStr = align;
	
	sSeq = new Vector<String>();
	tSeq = new Vector<String>();
	
	StringTokenizer st = new StringTokenizer(S);
	sLength = st.countTokens();
	while (st.hasMoreTokens()) {
		sSeq.add(st.nextToken());
    }
	
	StringTokenizer tt = new StringTokenizer(T);
	tLength = tt.countTokens();
	while (tt.hasMoreTokens()) {
       tSeq.add(tt.nextToken());
    }
}

public CorpusEntry(     
		String sl, 
		String tl, 
		String type, 
		String cs,
		String tcs,
		String align
		  )
{ 
S = sl;
T = tl;
Type = type;
SParse = cs;
TParse = tcs;
AlignmentStr = align;

sSeq = new Vector<String>();
tSeq = new Vector<String>();

StringTokenizer st = new StringTokenizer(S);
sLength = st.countTokens();
while (st.hasMoreTokens()) {
	sSeq.add(st.nextToken());
}

StringTokenizer tt = new StringTokenizer(T);
tLength = tt.countTokens();
while (tt.hasMoreTokens()) {
   tSeq.add(tt.nextToken());
}
}

public String getS()
{	return S; }
public int getSLength()
{	return sLength; }

public String getSParse()
{	return SParse; }

public String getTParse()
{	return TParse; }


public String getT()
{	return T; }
public int getTLength()
{	return tLength; }


public String getType()
{	return Type; }

public String getAlignmentStr()
{	return AlignmentStr; }

// Given an alignment string ((1,1),(2,2)) format 
// Parse it and fill it into the Vector of Alignment ; 
public String getReverseAlignmentStr()
{
	String RAlignmentStr="";
	Pattern p = Pattern.compile("\\((\\d+)\\,(\\d+)\\)");
	Matcher m = p.matcher(AlignmentStr);
	while (m.find()) 
	{
		Integer x = new Integer(Integer.parseInt(m.group(1)));
		Integer y = new Integer(Integer.parseInt(m.group(2)));
		RAlignmentStr+="("+y+","+x+"),";
	}
	RAlignmentStr=RAlignmentStr.replaceAll(",$", ""); 
	RAlignmentStr="("+RAlignmentStr+")";
	return RAlignmentStr;
}
public String toString()
{
	String str = "";
	str += "SL ("+sLength+"):"+ S+"\n";
	str += "TL("+tLength+"):"+ T+"\n";
	str += "Alignment:"+ AlignmentStr+"\n";
	str += "Type:"+ Type+"\n";
	str += "SParseTree:"+ SParse+"\n";
	if(TParse!="")
		str += "TParseTree:"+ TParse+"\n";
return str; 
}
}
