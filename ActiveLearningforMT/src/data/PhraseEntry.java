package data;
import java.util.*;

public class PhraseEntry {
	public double score = 0; 
	public String src = "";
	public String tgt = ""; 
	
	public PhraseEntry(String src, String tgt) {
		this.src = src; 
		this.tgt = tgt; 
	}
	
	public String toString(){
		String str = "";
		str+= "\nSrc:"+src;
		str+="\nTgt:"+tgt;
		str+="\nScore:"+score;
		return str;
	}
}
