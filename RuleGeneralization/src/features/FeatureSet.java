/**
 * 
 */
package features;

import java.util.Hashtable;

/**
 * @author Vamshi Ambati
 *
 */
public class FeatureSet {
 
	public  Hashtable<String, String> featHash;
	
	public FeatureSet() {
		featHash = new Hashtable<String,String>(); 
  	}
	
	// Initialize from a string
	public void initialize(String str) {
		featHash = new Hashtable<String,String>();
		
		String[] arr = str.split("\\s");
		for(String feat_value: arr){
			String[] tmp = feat_value.split(":");
			if(tmp.length==2){
				featHash.put(tmp[0],tmp[1]);
			}
		}
  	}
	
	
	public String getFeatureValue(String str){
		return featHash.get(str);
	}
	
	public void addFeatureValue(String key, Integer val){
		featHash.put(key,val.toString());
	}
	
	public String toString(){
		String str="";
		for(String key: featHash.keySet()){
				str+=key+":"+featHash.get(key)+" ";
		}
		return str;
	}
}
