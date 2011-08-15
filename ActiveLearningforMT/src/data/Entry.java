package data;
public class Entry {
	
	// This is to keep track of position in output . Is this required? For moses decoding may be
	// Where the only way to identify a sentence is to hash against the string, POSITION in output is another
	public int position_unlabeled = -1; 
	
	public int senid = -1;
	public String source=""; 
	public String target="";
	
	public int sLength = -1;
	public int tLength = -1;
	
	
	public double score = 0;
	public double cost=-1;
	
	// Which round was this entry selected in ? 
	int round = 0; 
	
	public String desc="";
	 
	public Entry(int i,String src, String tgt) {
		senid = i;
		position_unlabeled = senid; 
		
		source = src; 	 
		target = tgt;
		
		sLength = source.split("\\s+").length ;
		tLength = target.split("\\s+").length ;
		
		cost = (double)sLength; 
	}
	
	public void setRound(int tag){
		this.round = tag; 
	}
	
	public String toString(){
		String str = "";
		//str += "Sen:"+senid;
		str+= "\nSrc:"+source;
		str+="\nTgt:"+target;
		str+="\nDesc:"+desc;
		str+="\nScore:"+score;
		return str;
	}
}
