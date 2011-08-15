import java.util.ArrayList;
import java.util.PriorityQueue;


public class test {

	static PriorityQueue<box> pq = new PriorityQueue<box>(100);
	static ArrayList<box> list = new ArrayList<box>();
	 class box implements Comparable{
		public int score = 0;
		public box(int x){
			score = x;
		}
		public String toString(){
			return score+" ";
		}
		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			box b = (box)o;
			if(b.score>score){
				return -1;
			}else if(b.score<score){
				return 1;
			}else
				return 0;
		}
	}
	
	public static void main(String[] args){
		test t = new test();
		for(int i=1;i<=10;i++){
			box b = t.new box(i);
			list.add(b);
		}
		for(box b:list){
			pq.add(b);	
		}
		for(box b:list){
			b.score -= 1;
			pq.add(b);	
		}
		
		System.out.println(pq);
	}
}

