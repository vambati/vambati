package ml.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class SparseVector {

	private HashMap<String, Double> elements;
	
	public SparseVector(){
		elements = new HashMap<String, Double>();
	}
	public SparseVector(SparseVector vec) throws CloneNotSupportedException{
		elements = new HashMap<String, Double>();
		//elements = (HashMap<String,Double>)vec.clone();
	}

	public boolean exists(String i){
		if(elements.containsKey(i)){
			return true;
		}
		return false; 
	}
	
	public double get(String i) {
		return elements.get(i).doubleValue();
	}
	
	public void set(String str, double value){
		elements.put(str, new Double(value));
	}
	
	public int size(){
		return elements.size();
	}
	
	public Set<String> getFeatures(){
		return elements.keySet();
	}

	public void scale(double scaleFactor){
		for(String i : elements.keySet()){
			set(i, this.get(i) * scaleFactor);
		}
	}

//	Some features here are not used in other FeatureVectors, so make sure its the same 
 /*  public double dotProduct(SparseVector s){
		double prod = 0.0;
		for(String i : getFeatures()){
			if(s.exists(i))
				prod += s.get(i)*get(i);
		}
		return prod;
	} */
	
	public void add(SparseVector s){
		for(String fn : elements.keySet()){
			if(s.exists(fn)){
				set(fn, this.get(fn)+ s.get(fn) );
			}
		}
	}
	public void subtract(SparseVector s){
		for(String fn : elements.keySet()){
			if(s.exists(fn)){
				set(fn, this.get(fn) - s.get(fn));
			}
		}
	} 
		
	public String toString(){
		return elements.toString();
	}
	
	public String toNBestFormat(){
		Collection<Double> values = elements.values();
		String str = "";
		for(Double d : values)
			str += d + " ";
		return str.trim();
	}
}
