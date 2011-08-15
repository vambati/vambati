package model.smt;

import java.io.BufferedReader;
import java.io.FileInputStream;
 
import java.io.InputStreamReader;
 
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

  
import utils.StringUtils;
 
/*
 Functions related to Alignment from GIZA used in SMT systems (MOSES) 
 */
public class GizaAlignment {
 	
	public static void loadAlignmentGDA(String sname,String tname,String aname) {
		System.err.println("Loading GDF alignment data from: "+sname+" and "+tname);
		int sennum = 0;
		
		try { 
		// Reading a GZIP file 
		BufferedReader sbr = new BufferedReader(new InputStreamReader(new FileInputStream(sname)));
		BufferedReader tbr = new BufferedReader(new InputStreamReader(new FileInputStream(tname)));
		BufferedReader abr = new BufferedReader(new InputStreamReader(new FileInputStream(aname)));
		
		String s = ""; String t =""; String a=""; 
		while((s=sbr.readLine())!=null){
			t = tbr.readLine(); 
			a = abr.readLine(); 
			// TODO 
			
			sennum++;
		}
		sbr.close(); 
		tbr.close();
		abr.close();
		}catch(Exception e){}
	}
	
	// Set alignment from GIZA files 
	public static String giza2gda(String one, String two, String three, int offset)
	{
		// Lets synthesize the alignment String from the giza style alignment 
		String str = "";
		// First contains score 
		Pattern p1 = Pattern.compile("score : ([0-9\\.e\\-]+)");
		Matcher m1 = p1.matcher(one);
		while (m1.find()) 
		{
			double score = Double.parseDouble(m1.group(1));
			//System.err.println("Score:"+score);
		}

		// Second contains source string 
		
		// Third contains target string and alignment indices  
		Pattern p2 = Pattern.compile("(.+?)\\s\\(\\{(.+?)\\}\\)");
		Matcher m2 = p2.matcher(three);
		
		int tpos = 0;
		while (m2.find()) 
		{
			String tword = m2.group(1);
			String taligns = m2.group(2);
			taligns = StringUtils.trim(taligns);
			
			if(!taligns.equals("")){
			String[] talignsArr = taligns.split("\\s+");
				for(String sposition: talignsArr){
					
					int spos=Integer.parseInt(sposition)-1; // Null is the first word
					if(spos<0){ // Check
						System.err.println("LINK negative:"+spos);
						System.exit(0);
					} 
					spos=spos+offset;
					tpos=tpos+offset;
					str+=spos+"-"+tpos+" ";
				}
			}
			tpos++;
		}
		return str;
	}
}