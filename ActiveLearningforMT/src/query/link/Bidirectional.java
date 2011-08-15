package query.link;

import data.*;
import query.*; 

public  class Bidirectional implements QuerySelector {

	// Alignment links
	public AlignmentData s2t = null;
	public AlignmentData t2s = null;
	 
	public Bidirectional(String sgtFile,String tgsFile,int offset){
		// Loading the GIZA format alignments bidirectional
		s2t = new AlignmentData(sgtFile);
		t2s = new AlignmentData(tgsFile); 
	}
	
	// Confidence in alignment 
	public double computeScore(Entry e) {
		AlignmentEntry ae = (AlignmentEntry) e; 
		 
 		// Query by Comitte style - committe here is the bidirectional alignments from GIZA 
		for(int x: ae.LINKS.keySet()){
			for(int y: ae.LINKS.get(x).keySet()){
				double union = 1; 
				double intersect = 0;
				if(s2t.data.get(ae.senid).isAligned(x,y)){
					intersect++;
				}
				if(t2s.data.get(ae.senid).isAligned(y,x)){
					intersect++;
				}
				
				// Favours points in Union but not in intersection
				double score = Math.abs(union-intersect);
				score+=1; // Skipping zero 
				ae.LINKS.get(x).put(y,score);
			}
		}
		return 0;
	}
	
	public double computeLinkScore(int senid, int x, int y){
		double union = 1; 
		double intersect = 0;
		if(s2t.data.get(senid).isAligned(x,y)){
			intersect++;
		}
		if(t2s.data.get(senid).isAligned(y,x)){
			intersect++;
		}
		
		// Favours points in Union but not in intersection
		double score = Math.abs(union-intersect);
		score+=1; // Skipping zero 
		return score; 
	}
}