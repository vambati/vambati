/**
 * 
 */
package Rule;

import java.util.Hashtable;

/**
 * @author Vamshi Ambati
 *
 */
public class FeatureSet {
	// start_word
	// end_word
	// left_boundary_word 
	// right_boundary_word

	// head_word
	// const_type
	// left_const_type
	// right_const_type
	// left_const_headword
	// right_const_headword
	 
	// childseq="";
	// srclength;
	// tgtlength;
	
	// function_word
	// left_function_word
	// right_function_word

	/* OTHER FEATURES */
	// treedepth = -1; // What was the treedepth at which the rule was extracted (Source tree depth)
	// spanlength = -1;	// What was the yield of the node (How many terminals)

	// Parent_type
	// GrandParent_type (over ankunta !)
	
	public  Hashtable<String,String> featHash;
	
	public FeatureSet() {
		featHash = new Hashtable<String,String>();
		// initialize
		featHash.put("start_word","");
		featHash.put("end_word","");
		featHash.put("start_word_type","");
		featHash.put("end_word_type","");

		featHash.put("grandparent_node_type","");
		featHash.put("node_type","");
		featHash.put("left_node_type","");
		featHash.put("right_node_type","");
		
		featHash.put("left_boundary_word","");
		featHash.put("left_boundary_word_type","");
		featHash.put("right_boundary_word","");
		featHash.put("right_boundary_word_type","");
		
		featHash.put("headword","");
		featHash.put("headword_type","");
		featHash.put("left_headword","");
		featHash.put("left_headword_type","");
		featHash.put("right_headword","");
		featHash.put("left_headword_type","");
		
		featHash.put("functionword","");
		featHash.put("left_functionword","");
		featHash.put("right_functionword","");
  	}
	
	public String getFeatureValue(String str){
		return featHash.get(str);
	}
	
	public void addFeatureValue(String key, String val){
		featHash.put(key,val);
	}
	
	public String toString(){
		String str="";
		for(String key: featHash.keySet()){
			if(!featHash.get(key).equals(""))
				str+=key+":"+featHash.get(key)+" ";
		}
		return str;
	}
}