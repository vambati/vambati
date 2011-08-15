package Utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Beam<T extends Comparable<T>> implements Iterable<T> {
	
	private final PriorityQueue<T> q;
	private final int maxSize;
	private final Comparator<T> comparator;
	
	public Beam(int maxSize, final boolean biggerIsBetter) {
		this.maxSize = maxSize;
		
		// head of heap should be the WORST item
		this.comparator = new Comparator<T>() {
			public int compare(T a, T b) {
				int result = a.compareTo(b);
				if(biggerIsBetter == false) {
					result = -result;
				}
				return result;
			}
		};
		this.q = new PriorityQueue<T>(maxSize, comparator);
	}
	
	public void add(T newItem) {
		if(q.size() < maxSize) {
			q.add(newItem);
		} else {
			T worstItem = getWorst();
			if(comparator.compare(getWorst(), newItem) < 0) {
				q.remove(worstItem);
				q.add(newItem);
			}
		}
	}

	public T getWorst() {
		return q.peek();
	}

	public Iterator<T> iterator() {
		return q.iterator();
	}
	
	public static class ExampleClass implements Comparable<ExampleClass> {
		public String key = "ohai";
		public int value1 = 1;
		public float value2 = 2;
		
		@Override
		public int compareTo(ExampleClass other) {
			return this.key.compareTo(other.key);
		}	
	}
	
	public static void main(String[] args) throws Exception {
		Beam<Float> beam = new Beam<Float>(3, true);
		beam.add(5f);
		beam.add(6f);
		beam.add(7f);
		beam.add(8f);
		beam.add(9f);
		beam.add(10f);
		beam.add(1f);
		beam.add(2f);
		beam.add(3f);
		for(float f : beam) {
			System.out.println(f);
		}
		
		beam = new Beam<Float>(3, false);
		beam.add(-5f);
		beam.add(-6f);
		beam.add(-7f);
		beam.add(-8f);
		beam.add(-9f);
		beam.add(-10f);
		beam.add(-1f);
		beam.add(-2f);
		beam.add(-3f);
		for(float f : beam) {
			System.out.println(f);
		}
		
		
	}
}
