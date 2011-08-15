package qual;

import java.util.Hashtable;
 
// TODO: (Refactor as TranslationHIT extends HIT ) 

public class HIT {
	String id = "";
	public String input = "";
	
	// Worker ID and Annotation 
	public Hashtable<String,String> ASSIGNMENT = null;
	
	// Worker ID and Count of agreement 
	public Hashtable<String,Double> AGREEMENT = null;

	double score = 0;
	String annotation = "";
	public int assignments=0;
	
	private boolean majority_exists = false; 
	
	// input, hitid, wid, assgnid, tgt
	public HIT(String id, String src){
		this.id = id;  
		this.input = src; 	
		ASSIGNMENT = new Hashtable<String, String>(); 
	}
	
	public void addAssignment(String wid, String aid,String tgt){
		if(!ASSIGNMENT.containsKey(wid)){
			ASSIGNMENT.put(wid,tgt);
		}else{
			System.err.println("Same worker can't do two assignments");
			System.exit(0);
		}
		assignments++;
	}
	
	// Compare one with other and award a 'vote' 
	// Hypothesis can match against itself and 'vote' for itself
	public void computeAgreement(TranslationChecker validator){
		AGREEMENT = new Hashtable<String, Double>(); 
		
		for(String id1: ASSIGNMENT.keySet()){
			if(!AGREEMENT.containsKey(id1)){
				AGREEMENT.put(id1, 0.0);
			}
			String one = ASSIGNMENT.get(id1);
			for(String id2: ASSIGNMENT.keySet()){
				String two = ASSIGNMENT.get(id2);
				if(validator.match(one,two)){
					AGREEMENT.put(id1, AGREEMENT.get(id1)+1.0);
				}
			}
		}
	}
	
	public boolean majorityExists() { 
		// Does a majority vote consensus exist ?
		int x=0;
		for(String id1: AGREEMENT.keySet()){
 			// if(AGREEMENT.get(id1)>=ASSIGNMENT.size()){ // Strict majority (Landslide)
			if(AGREEMENT.get(id1)>1){ // Some victory 
				majority_exists = true; 
			}
			x++;
		}
		return majority_exists;
	}
	
	// Compare one with other and award a 'vote' 
	// Hypothesis can match against itself and 'vote' for itself
	public void computeAgreement(TranslationChecker validator,Hashtable<String,Oracle> oracles){
		AGREEMENT = new Hashtable<String, Double>(); 
		
		for(String id1: ASSIGNMENT.keySet()){
			if(!AGREEMENT.containsKey(id1)){
				AGREEMENT.put(id1, 0.0);
			} 
			String one = ASSIGNMENT.get(id1);
			for(String id2: ASSIGNMENT.keySet()){
				String two = ASSIGNMENT.get(id2);
				
				// Compute only for those oracles that are short-listed by IEThreshold
				if(oracles.containsKey(id1) && oracles.containsKey(id2)){
					if(validator.match(one,two)){
						AGREEMENT.put(id1, AGREEMENT.get(id1)+1.0);
					}
				}
			}
		}
	}
	
	// Compare one with other and award a 'vote' 
	// Hypothesis can match against itself and 'vote' for itself
	public void computeWeightedAgreement(TranslationChecker validator,Hashtable<String, qual.Oracle> oracles){
		AGREEMENT = new Hashtable<String, Double>(); 
		
		for(String id1: ASSIGNMENT.keySet()){
			if(!AGREEMENT.containsKey(id1)){
				AGREEMENT.put(id1, 0.0);
			} 
			String one = ASSIGNMENT.get(id1);
			for(String id2: ASSIGNMENT.keySet()){
				String two = ASSIGNMENT.get(id2);
				
				double reliability = oracles.get(id1).getReliability();
				if(validator.match(one,two)){
					AGREEMENT.put(id1, AGREEMENT.get(id1)+ reliability);
				}
			}
		}
	}
}
