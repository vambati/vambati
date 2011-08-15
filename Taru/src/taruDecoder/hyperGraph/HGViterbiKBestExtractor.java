/**
 * Taru
 */
package taruDecoder.hyperGraph;

import java.util.HashMap;
import java.util.PriorityQueue;

import taruDecoder.Scorer;
import taruHypothesis.Hypothesis;
import taruHypothesis.HypothesisScoreComparator;

/**
 * @author vamshi
 *
 */
public class HGViterbiKBestExtractor {

	private HyperGraph hg;
	int k;
	int explored;
	
	// candidate queues for nodes
	HashMap<Integer,PriorityQueue<Hypothesis>> candidates;

	public HGViterbiKBestExtractor(HyperGraph hg){
		this.hg = hg;
		// Initialize the candidate list
		System.out.println("Vertices:"+hg.getVertexSize());
		candidates = new HashMap<Integer,PriorityQueue<Hypothesis>>(hg.getVertexSize());
		System.out.println("Edges:"+hg.getEdgeSize());
	}

	public Hypothesis getKthBest(int index, int k){
		this.k = k;		 
		return extractKthBest(index, k);
	}
	
	private Hypothesis extractKthBest(int index, int j){
		// get the node
		HGVertex v = hg.getVertex(index);
		if(v.visiting){
			System.out.println("Loop loop !");
			System.exit(0);
		}
		
		v.visiting = true;
		
		if(v.getInEdges().size() > 0)
			System.out.println("Visiting node " + v + " for extracting " + j);
		
		// Check if we already have kbest available or if this is a terminal vertex
		if(!(v.isExhausted () || v.getInEdges().size() == 0 || v.availableKBest() >= j)) {
			// Check if the computation has happened at this node
			if(!candidates.containsKey(new Integer(index))){
				System.out.println("Populating node for the first time" + v);
				// Initialize the candidate queue
				PriorityQueue<Hypothesis> cand = new PriorityQueue<Hypothesis>(this.k,new HypothesisScoreComparator());
				candidates.put(new Integer(index), cand);
				
				// populate the candidate list with 1best from all the in edges
				int[] kindex = new int[2];
				kindex[0] = kindex[1] = 1;
				
				for(Integer i : v.getInEdges()){
					System.out.println("Get 1-best from edge " + hg.getEdge(i));
					addCandidates(i.intValue(), kindex, cand, index);
				}
			}
			
			PriorityQueue<Hypothesis> cand = candidates.get(index);			
			// Extract k-best if needed
			while(!(cand.isEmpty()) && v.availableKBest() < this.k && v.availableKBest() < j){
				Hypothesis head = cand.poll();
				if(v.addUniqueKBest(head.getWords())) {
					//buffer.add(head);
				}
				// add the successors of removed hyp to candidate if we have computed less than k
 				addSuccessors(head.getEdgeId(), head.getKIndex(), cand, index);
							
			// see if the candidates has been exhausted
			// either we couldn't extract the required number of hyps
			// or we have extracted global k number of hyps
			if(v.availableKBest() < j || v.availableKBest() >= this.k)
				v.setExhausted();
			}
		}
		v.visiting = false;
		return v.getKthHypothesis(j);
	}
	
	private void addSuccessors(int edgeId, int[] kindex, PriorityQueue<Hypothesis> cand, int index){
		for(int i = 0; i < hg.getEdge(edgeId).getItems().length; i++){
			int [] temp = kindex.clone();
			temp[i]++;
			addCandidates(edgeId, temp, cand, index);
		}
	}
	
	private void addCandidates(int edgeId, int[] kindex, PriorityQueue<Hypothesis> cand, int index) {
		HGEdge e = hg.getEdge(edgeId);
		int [] itemIds = e.getItems();
		
		//System.out.println("Adding along k-index : " + kindex[0] + " " + kindex[1]);
		Hypothesis h1 = extractKthBest(itemIds[0], kindex[0]);
		if(h1 == null)
			return;

		String hypWords = h1.getWords();

		Hypothesis h2 = null;
		// Try to get h2 only if it is a binary edge
		if(itemIds.length == 2){
			h2 = extractKthBest(itemIds[1], kindex[1]);
			if(h2 == null)
				return;
			hypWords += " " + h2.getWords();
		}
		
		Hypothesis h = combineHypothesis(h1, h2, edgeId, kindex, index);
			System.out.println("Adding candidate " + h.getWords() + " along " + e);
			cand.add(h);
	}
	
	private Hypothesis combineHypothesis(Hypothesis h1, Hypothesis h2, int edgeId, int[] kindex, int index){
		// Check if h2 is null which means a unary edge
		if(h2 == null){
			Hypothesis h = new Hypothesis(h1.getWords(),h1.getSrcWords(),edgeId,kindex);
			h.addFeatures(h1.getFeatures());
			h.setScore(h1.getScore() + Scorer.getScorer().computeCombinationCostUnary(h, h1, edgeId));
			// nodeHScores.get(new Integer(index)).add(h);
			return h;
		}
		
		Hypothesis h = new Hypothesis(h1.getWords() + " " + h2.getWords(),h1.getSrcWords() + " " + h2.getSrcWords(), edgeId, kindex);

		// Add the features from the antecedents
		h.addFeatures(h1.getFeatures());
		h.addFeatures(h2.getFeatures());

		// contribution from antecedents
		double score = h1.getScore() + h2.getScore();
		System.out.println("Hyp before combination: " + h.getWords());
		score += Scorer.getScorer().computeCombinationCostBinary(h, h1, h2, edgeId, true); // No Bounds
		h.setScore(score);
		System.out.println("Hyp after combination: " + h.getWords());
		return h;
	}	
}