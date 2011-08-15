package mosesannotator;

public class Span {
	public int x = -1;
	public int y = -1;
	public String type = "";
	
	public Span(int x,int y, String type){
		this.x = x; 
		this.y = y;
		this.type = type;
	}
}
