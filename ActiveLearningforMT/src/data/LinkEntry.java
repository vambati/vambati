package data; 
 
 
public class LinkEntry{ 
 
   public int  senid = -1;
   public int x = -1;
   public int y = -1;
   
   public String s="";
   public String t="";
   
   public double score = -1;
  
    public LinkEntry(int i,int x, int y) { 
          this.senid = i; 
          this.x = x; 
          this.y = y; 
    } 
    public void set(String s,String t){
    	this.s = s;
    	this.t = t;
    }
     
    public String toString(){ 
        String str = x+"-"+y; 
        return str; 
    } 
}  
