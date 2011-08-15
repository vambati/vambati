package ml.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class SparseVector {

	private HashMap<Integer, Double> elements;
	
	public SparseVector(){
		elements = new HashMap<Integer, Double>();
	}
	
	public double get(int i) {
		return get(new Integer(i));
	}

	public double get(Integer i) {
		if(elements.containsKey(i))
			return elements.get(i).doubleValue();
		else
			return 0.0;
	}
	
	public void set(int i){
		set(new Integer(i));
	}
	
	public void set(Integer i){
		elements.put(i, new Double(1));
	}
	
	public void set(int index, double value){
//		if(value - 0.0 < .0000001)
//			return; 
		set(new Integer(index), new Double(value));
	}
	
	public void set(int index, Double value){
		set(new Integer(index), value);
	}
	
	public void set(Integer i, Double value){
		elements.put(i, value);
	}

	public void set(Integer i, double value){
		elements.put(i, new Double(value));
	}

	public void increment(int index, double value){
		increment(new Integer(index), value);
	}
	
	public void increment(Integer i, double value){
//		double newval = elements.get(i) + value;
//		if(newval - 0.0 < .00000001){
//			elements.remove(i);
//			return;
//		}
		elements.put(i, new Double(get(i) + value));
	}
	
	public int size(){
		return elements.size();
	}
	
	public Set<Integer> getFeatureIds(){
		return elements.keySet();
	}

	public void scale(double scaleFactor){
		for(Integer i : elements.keySet()){
			elements.put(i, elements.get(i).doubleValue()/scaleFactor);
		}
	}

	public double dotProduct(SparseVector s){
		double prod = 0;
		if(s.size() < elements.size()){
			for(Integer i : s.getFeatureIds()){
				prod += s.get(i)*get(i);
			}
		}
		else{
			for(Integer i : getFeatureIds()){
				prod += s.get(i)*get(i);
			}
		}
		return prod;
	}

	public void add(SparseVector s){
		for(Integer i : s.getFeatureIds()){
			if(s.get(i) != 0.0)
				increment(i,s.get(i));
		}
	}
	
	public void subtract(SparseVector s){
		for(Integer i : s.getFeatureIds()){
			if(s.get(i) != 0.0)
				increment(i, -1*s.get(i));
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
