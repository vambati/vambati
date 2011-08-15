package qa;

public class Question  {

	public String q = ""; 
	public int id = -1;
	String qpath = "";

	public String qOriginal = ""; 
	
	// Do not need for now 
	//public SparseVector features= null;

	public Question(String src,String orig,String qpath,int id) {
		q = src;
		qOriginal = orig; 
		this.id = id;
		this.qpath = qpath;
	}

	public String toString(){
		return q+"\n"+qOriginal; 
	}

}
