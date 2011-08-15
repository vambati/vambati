package qual;

import java.util.Hashtable;

public class Oracle {
	private String id = "";
	double reliability = 0;
	
	// stats
	public int submitted = 0;
	public int accepted = 0;
	public int rejected = 0;
		
	// Check fields 
	public int errors = 0;
	
	// How well does he agree with others who participated in the same task
	public double agreement = 0; 
 	
	// Match google values to make sure he isnt messing up 
	public double googlematch = 0;
	
	// How good is he with respect to intersperesed gold standard sentences ? 
	public double goldmatch = 0;
	
	public Oracle(String workerid) {
			id = workerid; 
	}
	public String toString(){
		double g = (double)googlematch*100/(double)submitted;
		return id+"\t"+submitted+"\t"+goldmatch+"\t"+googlematch+"\t("+g+"%)\t"+getReliability()+"\t"+getUpperInterval();
	}
	
	public double getReliability(){
		return agreement/(double)submitted; 
	}
	
	// Computing IEThresholding 
	// Total annotations 
	private double n = 0;
	private double reward = 0;
	private double square_reward = 0; 
	
	public void addTask(double points) {
     n++;
     reward += points;
     square_reward += points * points;
	}

	public double getMean() {
		return reward / n; 
	}
	public double getSTD(){
		double mean = getMean();
	    return Math.sqrt( square_reward/n - mean*mean );
	}
	public double getUpperInterval (){
		if(n==0){
			return -1000;
		}
		double ui = getMean() + getStudentT(n-1,0.05) * (getSTD() / Math.sqrt(n));
		return ui; 
	}
	public double getStudentT(double degreesOfFreedom,double alpha){
		Hashtable<Double,Double> studentT = new Hashtable<Double, Double>();
		studentT.put(0.0,9.313);
		studentT.put(1.0,6.313);
		studentT.put(2.0,2.91);
		studentT.put(3.0,2.35);
		studentT.put(4.0,2.13);
		studentT.put(5.0,2.01);
		studentT.put(6.0,1.94);
		studentT.put(7.0,1.89);
		studentT.put(8.0,1.85);
		studentT.put(9.0,1.83);
		studentT.put(10.0,1.81);
		studentT.put(11.0,1.79);
		studentT.put(12.0,1.77);
		studentT.put(13.0,1.76);
		studentT.put(14.0,1.75);
		studentT.put(15.0,1.74);
		studentT.put(16.0,1.74);
		studentT.put(17.0,1.73);
		
		if(studentT.containsKey(degreesOfFreedom)){
			return studentT.get(degreesOfFreedom);
		}else{
			// System.err.print("n="+degreesOfFreedom);
			return 1.69;
		} 
	}
}
