/**
 * 
 */
package taruDecoder.hyperGraph;


import java.util.BitSet;
import java.util.HashMap;

import ml.utils.SparseVector;

import taruDecoder.Scorer;
import taruHypothesis.Hypothesis;
import tarugrammar.Grammar;
import utils.lm.NgramLanguageModel;

/**
 * @author abhayaa
 *
 */
public class HGUtils {

	public static String extractKBestHypothesis(HyperGraph hg, int k) {
		
		if(!hg.isBinarized())
			return extractKBestHypothesis(HGUtils.binarizeHG(hg), k);
		
		//HGLazyKBestExtractor kbestExtractor = new HGLazyKBestExtractor(hg);
		HGViterbiKBestExtractor kbestExtractor = new HGViterbiKBestExtractor(hg);
		
		// Compute the kth best on root. This will compute the first k and store in the hg
		kbestExtractor.getKthBest(0, k);
		
		if(hg.getVertex(0).availableKBest() < k){
			System.err.println("Only " + hg.getVertex(0).availableKBest() + " hyps available in the given forest !");
			return null;			
		}
		else{
			return hg.getVertex(0).getKthHypothesis(k).getWords();
		}
	}

	
	public static HyperGraph binarizeHG(HyperGraph hg){
		HyperGraph binHG = new HyperGraph(hg.vertices.size(),true);
		
		// Add all the original vertices first since we will need them all anyway
		for(HGVertex v : hg.vertices){
			binHG.addVertex(v);
		}
		
		// Since we are going to change the edges, all the inEdge pointers stored in the
		// vertices will be invalidated. So let us flush them
		// This also clears any hyps stored on the vertices since every hyp 
		// stores back pointers.
		binHG.flushEdgePointers();
				
		// Keep track of inserted nodes so that they can be shared
		HashMap<String, Integer> insertedNodes = new HashMap<String, Integer>();
		
		// Now pick up edges one by one, add nodes as required
		for(HGEdge e : hg.edges){

			int [] items = e.getItems();
			int k = items.length;
			
			// Check if the edge is unary or binary
			if(k < 3){
				binHG.addEdge(items, e.getGoal(), e.getRuleId());
				continue;
			}

//			System.out.println("Binarizing :" + e);
						
			int lastId = items[0];
			
			for(int i = 1; i < k; i++){
				HGVertex last = binHG.getVertex(lastId);
				HGVertex v = hg.getVertex(items[i]);

//				System.out.println(last);
//				System.out.println(v);
				
				int tempId;
				String ruleId;
				if(i < k-1){
					// Get the span and type of the node we will need to create
					String type = last.getType() + "+" + v.getType();
//					System.out.println(type);
					BitSet span = new BitSet();
					span.or(last.getSpan());
					span.or(v.getSpan());				
					
					// Create the key
//					SpanTypeHashKey key = new SpanTypeHashKey(span, type);
					String key = span.toString()+type;
					// Check if the required vertex has already been created
					if(insertedNodes.containsKey(key)){
						tempId = insertedNodes.get(key).intValue();
					}
					else{
						// Create a new intermediate node
						HGVertex temp = new HGVertex(type);
						temp.addSpan(span);
						tempId = binHG.addVertex(temp);
						insertedNodes.put(key, new Integer(tempId));
					}
					ruleId = Grammar.CONNECT_EDGE;
				}
				else{
					// last two edges connect back to the original goal node
					tempId = e.getGoal();
					ruleId = e.getRuleId();
				}
				
				int [] itemIds = new int[2];
				itemIds[0] = lastId;
				itemIds[1] = items[i];
				
				// add the edge
				// pass a glue rule id that will say that the probability is 1
				binHG.addEdge(itemIds, tempId, ruleId);
				
				// Update the last
				lastId = tempId;
			}
		}
		return binHG;
	}
	
	public static String extractKBestHypothesisParse(HyperGraph hg, int k){
		HGVertex root = hg.getVertex(0);
		// Check if we have kth hyp, else try to compute it
		if(root.availableKBest() < k){
			if(root.isExhausted()){
				return "Only " + root.availableKBest() + " hyps available in the given forest.";
			}
			else{
				HGLazyKBestExtractor kbestExtractor = new HGLazyKBestExtractor(hg);
				kbestExtractor.getKthBest(0, k);
				if (root.availableKBest() < k)
					return "Only " + root.availableKBest() + " hyps available in the given forest.";
			}
		}
		
		// Being here means we have the kth hyp available in the forest.
		return extractParse(hg, 0, k);
	}
	
	public static String extractParse(HyperGraph hg, int vertexId, int k){
		String parse = "";
		HGVertex v = hg.getVertex(vertexId);
		
		// Collect the information from this vertex
		parse += "( " + vertexId + " " + v.getSpan() + " " + v.getType() + " ";

		Hypothesis h = v.getKthHypothesis(k);
		// Check if there are any children
		if(h.getEdgeId() != -1){
			parse += hg.getEdge(h.getEdgeId()).getRuleId() + " ";
			parse += Grammar.getRuleScores(hg.getEdge(h.getEdgeId()).getRuleId())[0] + " ";
			parse += Grammar.getRuleScores(hg.getEdge(h.getEdgeId()).getRuleId())[1] + " ";
			int [] child = hg.getEdge(h.getEdgeId()).getItems();
			int [] kindex = h.getKIndex();
			// Recursively construct trees for children
			for(int i = 0; i < child.length; i++){
				parse += extractParse(hg, child[i], kindex[i]);
			}
		}
		else{
			parse += "\"" + h.getWords().trim() + "\" ";
		}
		// Finish up
		parse += " ) ";
		
		return parse;
	}
	
	public static String extractOriginalParse(HyperGraph hg, int vertexId, int k){
		String parse = "";
		HGVertex v = hg.getVertex(vertexId);
		
		// Collect the information from this vertex if this is not a introduced node
		if(!v.getType().contains("+")){
			parse += "( " + v.getType() + "<" + v.getSourceStart() +"-"+ v.getSourceEnd()+"> ";
		}
		Hypothesis h = v.getKthHypothesis(k);
		// Check if there are any children
		if(h.getEdgeId() != -1){
			int [] child = hg.getEdge(h.getEdgeId()).getItems();
			int [] kindex = h.getKIndex();
			// Recursively construct trees for children
			for(int i = 0; i < child.length; i++){
				parse += extractOriginalParse(hg, child[i], kindex[i]);
			}
		}
		else{
			parse += "\"" + h.getWords().trim() + "\" ";
		}
		// Finish up, only if we started
		if(!v.getType().contains("+"))
			parse += " ) ";
		
		return parse;
	}
}
