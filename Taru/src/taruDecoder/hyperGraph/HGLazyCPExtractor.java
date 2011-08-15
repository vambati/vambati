/**
 * Taru
 */
package taruDecoder.hyperGraph;

import java.util.HashMap;
import java.util.PriorityQueue;

import taruDecoder.Scorer;
import taruHypothesis.Hypothesis;
import taruHypothesis.HypothesisHScoreComparator;
import taruHypothesis.HypothesisScoreComparator;

/**
 * @author vamshi
 *
 */
public class HGLazyCPExtractor {

	private HyperGraph hg;
	
	int k;
	boolean bound;
	int explored;
	
	// candidate queues for nodes
	HashMap<Integer,PriorityQueue<Hypothesis>> candidates;

	// Buffer for taking care of non monotonicity when using non local features
	HashMap<Integer,PriorityQueue<Hypothesis>> buffers;

	// Current lower bound on hg nodes
	HashMap<Integer,PriorityQueue<Hypothesis>> nodeHScores;

	// Grids for edges
	HashMap<Integer,boolean[][]> grids;
	
	// Map for lower bounds along hg edges
//	double[] edgeLowerBound;

	public HGLazyCPExtractor(HyperGraph hg){
		this.hg = hg;
		// Initialize the candidate list
		candidates = new HashMap<Integer,PriorityQueue<Hypothesis>>(hg.getVertexSize());
		buffers = new HashMap<Integer,PriorityQueue<Hypothesis>>(hg.getVertexSize());
		nodeHScores = new HashMap<Integer,PriorityQueue<Hypothesis>>(hg.getVertexSize());
		grids = new HashMap<Integer, boolean[][]>(hg.getEdgeSize());
//		this.model = null;
	}

	public Hypothesis getKthBest(int index, int k){
		this.k = k;
		this.bound = true;
		// First compute top-i derivations with only the local features
		// to estimate the bounds
//		extractKthBest(index,1);
		// Flush the hyps and recompute with all the features
//		hg.flushHyps();
//		candidates = new HashMap<Integer,PriorityQueue<Hypothesis>>(hg.getVeretxSize());
//		buffers = new HashMap<Integer,PriorityQueue<Hypothesis>>(hg.getVeretxSize());
//		nodeHScores = new HashMap<Integer,PriorityQueue<Hypothesis>>(hg.getVeretxSize());
//		bound = false;
//		this.local = false;
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
		
//		if(v.getInEdges().size() > 0)
//			System.out.println("Visiting node " + v + " for extracting " + j);
		
		// Check if we already have kbest available or if this is a terminal vertex
		if(!(v.isExhausted () || v.getInEdges().size() == 0 || v.availableKBest() >= j)){
			// Check if the computation has happened at this node
			if(!candidates.containsKey(new Integer(index))){
//				System.out.println("Populating node for the first time" + v);
				// Initialize the candidate queue
				PriorityQueue<Hypothesis> cand = new PriorityQueue<Hypothesis>(this.k,new HypothesisScoreComparator());
				PriorityQueue<Hypothesis> buffer = new PriorityQueue<Hypothesis>(this.k,new HypothesisScoreComparator());
				PriorityQueue<Hypothesis> candH = new PriorityQueue<Hypothesis>(this.k, new HypothesisHScoreComparator());
				
				candidates.put(new Integer(index), cand);
				buffers.put(new Integer(index), buffer);
				nodeHScores.put(new Integer(index), candH);
				
				// populate the candidate list with 1best from all the in edges
				int[] kindex = new int[2];
				kindex[0] = kindex[1] = 1;
				for(Integer i : v.getInEdges()){
//					System.out.println("Get 1-best from edge " + hg.getEdge(i));
					addCandidates(i.intValue(), kindex, cand, index);
				}
			}
			
			PriorityQueue<Hypothesis> cand = candidates.get(index);
			PriorityQueue<Hypothesis> buffer = buffers.get(index);
			PriorityQueue<Hypothesis> nodeH = nodeHScores.get(index);
			
			// Extract k-best if needed
			int explored = 0;
			while(!(cand.isEmpty()) && v.availableKBest() + buffer.size() < this.k && v.availableKBest() < j){
//				System.out.println("cand is: " + cand.isEmpty() + " for node " + j);
				explored++;
				// To break when too many repeated hyps are encountered
//				if(explored > this.k + 10000)
//					break;
				// pop out the best hyp
				Hypothesis head = cand.poll();
				nodeH.remove(head);
				
//				System.out.println("cand is: " + cand.isEmpty() + " for node " + j);

				// Add the popped candidate to buffer if it is unique
				// [TODO] clean up, basically all the management of hyps
				// should be done here itself rather than at vertex
				if(v.addUniqueKBest(head.getWords()))
					buffer.add(head);

				// add the successors of removed hyp to candidate if we have computed less than
				// global k
//				if(v.availableKBest() < this.k)
				addSuccessors(head.getEdgeId(), head.getKIndex(), cand, index);
				
				// add the hyp to the final candidate list
//				System.out.println("Popping out : " + head);
				if(nodeH.size() == 0){
//					System.out.println("nodeH is empty, so empty the buffer.");
					while(buffer.size() > 0){
						v.addKBestHypothesis(buffer.poll());
					}
				}
				else{
					double bound = nodeH.peek().hScore;
					if(buffer.size() > 0)
//						System.out.println("Bound: " + bound + " Buffer: " + buffer.peek().getScore() + " Size: " + buffer.size());
					while(buffer.size() > 0 && buffer.peek().getScore() >= bound){
						v.addKBestHypothesis(buffer.poll());
					}
//					System.out.println();
				}
			}
			
			// pop condidates remaining in buffer to kbest
//			System.out.println("Poping the rest from buffer !");
			while(buffer.size() > 0){
				v.addKBestHypothesis(buffer.poll());
			}
			
			// see if the candidates has been exhausted
			// either we couldn't extract the required number of hyps
			// or we have extracted global k number of hyps
			if(v.availableKBest() < j || v.availableKBest() >= this.k)
				v.setExhausted();
		}
		v.visiting = false;
		return v.getKthHypothesis(j);
	}
	
	private void addSuccessors(int edgeId, int[] kindex, PriorityQueue<Hypothesis> cand, int index){
//		int edgeId = h.getEdgeId();
//		int [] kindex = h.getKIndex();
//		System.out.println("Adding successors. Current k-index is : " + kindex[0] + " " + kindex[1]);
		for(int i = 0; i < hg.getEdge(edgeId).getItems().length; i++){
			int [] temp = kindex.clone();
			temp[i]++;
			if(!exploredCell(edgeId,temp))
				addCandidates(edgeId, temp, cand, index);
		}
	}


	private boolean exploredCell(int edgeId, int[] cell){
		if(!grids.containsKey(edgeId)){
			boolean [][] barray = new boolean[k+1][k+1];
			barray[cell[0]-1][cell[1]-1] = true;
			grids.put(edgeId, barray);
			return false;
		}
		else{
			boolean [][] barray = grids.get(edgeId);
			if(barray[cell[0]-1][cell[1]-1]){
//				System.out.println("Rejecting " + edgeId + "[" + cell[0] + " " + cell[1] + "]");
				return true;
			}
			else{
				barray[cell[0]-1][cell[1]-1] = true;
				return false;
			}
		}
	}
	
	private void addCandidates(int edgeId, int[] kindex, PriorityQueue<Hypothesis> cand, int index){
		HGEdge e = hg.getEdge(edgeId);
		int [] itemIds = e.getItems();
//		System.out.println("Adding along k-index : " + kindex[0] + " " + kindex[1]);
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
		
		// Check if this candidate is duplicate, if yes, add it's successors
//		if(hg.getVertex(index).containsHyp(hypWords)){
//			explored++;
//			addSuccessors(edgeId, kindex, cand, index);
//			return;
//		}
		
		Hypothesis h = combineHypothesis(h1, h2, edgeId, kindex, index);
//		System.out.println("Adding candidate " + h + " along " + e);
//		
//			System.out.println(cand);
//			System.out.println("Really Adding candidate " + h + " along " + e);
			cand.add(h);
//		}
	}
	
	private Hypothesis combineHypothesis(Hypothesis h1, Hypothesis h2, int edgeId, int[] kindex, int index){
		// Check if h2 is null which means a unary edge
		if(h2 == null){
			Hypothesis h = new Hypothesis(h1.getWords(),h1.getSrcWords(),edgeId,kindex);
			h.addFeatures(h1.getFeatures());
			h.setScore(h1.getScore() + Scorer.getScorer().computeCombinationCostUnary(h, h1, edgeId));
			nodeHScores.get(new Integer(index)).add(h);
			return h;
		}
		
		Hypothesis h = new Hypothesis(h1.getWords() + " " + h2.getWords(),h1.getSrcWords() + " " + h2.getSrcWords(), edgeId, kindex);

		// Add the features from the antecedents
		h.addFeatures(h1.getFeatures());
		h.addFeatures(h2.getFeatures());

		// contribution from antecedents
		double score = h1.getScore() + h2.getScore();
		System.out.println("Hyp before combination: " + h);
		score += Scorer.getScorer().computeCombinationCostBinary(h, h1, h2, edgeId, bound);
		h.setScore(score);
		System.out.println("Hyp after combination: " + h);
		
		nodeHScores.get(new Integer(index)).add(h);
//		System.out.println(h.getScore() - h.lmscore  + " " + h.lmscore);
		return h;
	}	
}