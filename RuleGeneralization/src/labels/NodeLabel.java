package labels;


public class NodeLabel {

	public String type="";
	/*
	 * 1. Times it is the PARENT 
	 * - Is the target side PARENT also the same or different 
	 * - Do we want to keep track of the distribution of the target labels it rewrote itself as ?
	 * 2. Times it is in the CHILD LIST 
	 * 3. Relative Position in the Source Side Child List
	 * 4. Relative Position in the Source Target Child List
	 * 5. Was the rule monotonic ? Or non-monotonic 
	 * 6. Were there literal introductions in the rule? 
	 */ 
	public int count=0; // Total number of times seen in the rule DB 
	public int isparent=0; // times label occurs as parent 
	public int ischild = 0; // times label occurs in child list
	
	public int isparent_monotone = 0; // When parent is that rule a monotone rule?
	public int isparent_reorder = 0; // When parent is that rule a reorder rule?
	public double isparent_order_entropy = 0.0;
	
	public int ischild_monotone = 0; // When child is that rule a monotone rule?
	public int ischild_reorder = 0; // When child is that rule a reorder rule?
	public double ischild_order_entropy = 0.0;
	
	// Relative position features (Are these important ?)
	public int firstHalf = 0; // Label occurs in the first half of the RULE chlid
	public int secondHalf = 0; // Label occurs in the first half of the RULE chlid 
	
	public NodeLabel(String type){
		this.type = type;
	}
	
	public String toDetailedString(){
		String str="";
		str+="Node Label:"+type+"\n";
		str+="Occurs as Parent:"+isparent+"\n";
		str+="When Parent the rewrite rule is monotonic:"+isparent_monotone+"\n";
		str+="When Parent the rewrite rule is non-monotonic:"+isparent_reorder+"\n";
		str+="When Parent the Order entropy:"+isparent_order_entropy+"\n";
		str+="Occurs as Child:"+ischild+"\n";
		str+="When Child the rewrite rule is monotonic:"+ischild_monotone+"\n";
		str+="When Child the rewrite rule is non-monotonic:"+ischild_reorder+"\n";
		str+="When Child the Order entropy:"+ischild_order_entropy+"\n";
		return str;
	}
	
	public String toString(){
		String str="";
		str+=type+"\t"+isparent+"\t"+isparent_monotone+"\t"+isparent_reorder+"\t"+isparent_order_entropy+"\t"
			+ischild+"\t"+ischild_monotone+"\t"+ischild_reorder+"\t"+ischild_order_entropy;
		return str;
	}

}
