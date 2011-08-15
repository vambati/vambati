package taruDecoder.hyperGraph;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import taruDecoder.Decoder;
import taruDecoder.Scorer;
import taruHypothesis.Hypothesis;
import tarugrammar.Grammar;
import tarugrammar.GrammarRule;
import tarugrammar.LexicalRule;
import tarugrammar.PhraseRule;
import treeParser.ParseTree;
import treeParser.ParseTreeNode;
import treeParser.PatternNode;

/* * Hypergraph based decoder
 * 
 * Takes care of insertions to create the targetForest -
 *   Could insert into Hypergraph for CubeGrowing and CubePruning decoding 
 */
public class HGDecoder implements Decoder {

	//TODO: Implement thresholds for both rule and lexical beams 
	private int RULE_BEAM = 10;
	private int LEXICAL_BEAM = 10;
	private int PHRASE_BEAM = 10;


	// Packed forest of target side parses
	public HyperGraph hg;

	// Hashmaps to enable packing of the forest. A matching node is keyed on type and source span it covers. A inserted node or a lexical node is keyed on the lexical contents
	public HashMap<String, Integer> matchingNodes;

	// These are the partial sequence of lexical items found in RULES. They are packed for efficient computation using the LM ;Keyed on the phrase itself
	public HashMap<String, Integer> insertedNodes;


	public HGDecoder(){
		// Initialize the data structures
		hg = new HyperGraph(10);
		// Add a top level node
		hg.addVertex(new HGVertex(0, 0, "TOP"));
		
		matchingNodes = new HashMap<String, Integer>();
		insertedNodes = new HashMap<String, Integer>();
	}
	
	public List<Hypothesis> extract_phase(int N)
	{
		// Hypergraph was already built by now 
		// So just binarize it and perform extraction 
		HyperGraph binHG = HGUtils.binarizeHG(hg);
  		
		//HGUtils.extractKBestHypothesis(binHG, 50, model, lm);
		Scorer.getScorer().setHyperGraph(binHG);
		
		// Extracts and sets the target hypothesis for the TOP node
		HGUtils.extractKBestHypothesis(binHG, 100);
	
		return binHG.getVertex(0).getTopKHypothesis(N);
	}
	
	public void handleOOV(ParseTreeNode ptn) {
		// Now add a hypothesis just copying over the source word.
		int[] kindex = new int[2];
		kindex[0] = kindex[1] = -1;
		Hypothesis hyp = new Hypothesis(ptn.getS(),ptn.getS(), -1, kindex);
		Scorer.getScorer().initializeHypothesisFeatures(hyp);
		// [TODO] This is a hack to get things working quickly
//		hyp.setScore(Grammar.OOV_RULE_SCORE);
//		hyp.getFeatures().increment(1, Grammar.OOV_RULE_SCORE);

		// First find all the target side nodes generated by this source node.
		for (String nodeType : ptn.targetNodeTypes) {
			//System.err.println("Lexical OOV handling - Adding to " + nodeType);
			hg.getVertex(matchingNodes.get(ptn.spanString + ":" + nodeType)).addHypothesis(hyp);
		}
	}

	public void addLexicalBackoff(LexicalRule backoff, ParseTreeNode ptn){
		// Now add a hypothesis just copying over the source word.
		int[] kindex = new int[2];
		kindex[0] = kindex[1] = -1;
		Hypothesis hyp = new Hypothesis(backoff.getTargetWord(),ptn.getS(), -1, kindex);
		Scorer.getScorer().initializeHypothesisFeatures(hyp);
		// [TODO] This is a hack to get things working quickly
//		hyp.setScore(Grammar.OOV_RULE_SCORE);
//		hyp.getFeatures().increment(1, Grammar.OOV_RULE_SCORE);

		// First find all the target side nodes generated by this source node.
		for (String nodeType : ptn.targetNodeTypes) {
			System.err.println("Lexical backoff handling - Adding to " + nodeType);
			hg.getVertex(matchingNodes.get(ptn.spanString + ":" + nodeType)).addHypothesis(hyp);
		}
	}

	public boolean processMatch(ParseTreeNode ptn, Integer patId, ArrayList<ParseTreeNode> frontiers,
					ArrayList<GrammarRule> targetStrings) {

		boolean result = false;
		for (GrammarRule targetInfo : targetStrings) {
			result = addToTargetForest(targetInfo, frontiers, ptn.isRoot) || result;
		}
		return result;
	}
 
	public void addGlueRulesToTargetForest(ParseTreeNode ptn) {

		// Create a Glue node 
		String goalkey = ptn.spanString+":X";
		int goalId = -1;
		if (matchingNodes.containsKey(goalkey)) {
			goalId = matchingNodes.get(goalkey);
		} else {
			HGVertex goal = new HGVertex(ptn.sStart, ptn.sEnd, "X");
			goalId = hg.addVertex(goal);
			matchingNodes.put(goalkey, new Integer(goalId));
			ptn.targetNodeTypes.add("X");
			
			// If this is the top node, create a edge to the target top node
			if (ptn.isRoot) {
				//System.err.println("Attaching Glue rule to TOP");
				int[] itemIds = { goalId };
				itemIds[0] = goalId;
				hg.addEdge(itemIds, 0, Grammar.CONNECT_EDGE);
			}
		}
		
		// Monotonically string the children as ITEMs
		int[] itemIds = new int[ptn.children.size()];
		for (int i = 0; i < ptn.children.size(); i++) {
			ParseTreeNode child = ptn.children.elementAt(i);
			String nodeKey = child.spanString + ":"+child.nodetype;
			if (matchingNodes.containsKey(nodeKey)) {
				itemIds[i] = matchingNodes.get(nodeKey);
			} else {
				HGVertex item = new HGVertex(child.sStart, child.sEnd, child.nodetype);
				// TODO: Since it is not by projection, if you use src type for tgt type, there will be inconsistencies
				// in the goal creation
				child.targetNodeTypes.add(child.nodetype);
				
				itemIds[i] = hg.addVertex(item);
				matchingNodes.put(nodeKey, new Integer(itemIds[i]));
			}
		}

		// Now create the Edge with the GOAL and the ITEMS 
		for (String targetType : ptn.targetNodeTypes) {
			//System.err.println("Adding glue edge for " + ptn.nodetype + " -> " + targetType);
			Integer goalId2 = matchingNodes.get(ptn.spanString + ":"+ targetType);
			hg.addEdge(itemIds, goalId2, Grammar.GLUE_EDGE);
		}		
	}

	// Adding a syntactic phrase, (SPMT) style, instead of actual phrases
	public void addPhraseEntriesToForest(ArrayList<PhraseRule> targetList, ParseTreeNode frontier, boolean isRoot){

		boolean addAll = matchingNodes.containsKey(frontier.spanString + ":X");
		HGVertex allNode = null;
		if(addAll){
			allNode = hg.getVertex(matchingNodes.get(frontier.spanString + ":X").intValue());
		}
		for(PhraseRule prule : targetList){
			// Create the hyp
			int [] kindex = new int[2];
			kindex[0] = kindex[1] = -1;
			Hypothesis hyp = new Hypothesis(prule.getTargetWord(), prule.getSourceWord(), -1, kindex);
			// [TODO] This is a hack to get things working quickly
			Scorer.getScorer().initializeHypothesisFeatures(hyp,prule);
	
			int goalId = -1;
			String goalKey = frontier.spanString + ":" + prule.getTargetPOS();
			if (matchingNodes.containsKey(goalKey)){
				 goalId = matchingNodes.get(goalKey).intValue();
				hg.getVertex(goalId).addHypothesis(hyp);
			}
			else{
				String [] args = goalKey.split(":");
				HGVertex goal = new HGVertex(Integer.parseInt(args[0]),Integer.parseInt(args[1]), args[2]);
				goal.addHypothesis(hyp);
				goalId = hg.addVertex(goal);
				matchingNodes.put(goalKey, new Integer(goalId));
				frontier.targetNodeTypes.add(args[2]);
				
				// If this is the top node, create a edge to the target top node
				if (isRoot) {
					System.err.println("Phrasal node addition at root");
					int[] itemIds = { goalId };
					itemIds[0] = goalId;
					hg.addEdge(itemIds, 0, Grammar.CONNECT_EDGE);
				}
				// Also check for X. If present add the hyp to that also
				if(addAll){
					allNode.addHypothesis(hyp);
				}
			}
		}
	}

	public boolean addToTargetForest(GrammarRule targetInfo, ArrayList<ParseTreeNode> frontiers, boolean isSrcRoot) {
		String targetStr = targetInfo.getTarget();
		// 0:NP to come 1:NP 2:VP
		String[] tokens = targetStr.split(" +");
		// StringTokenizer tok = new StringTokenizer(targetStr,"\\s+");
		// NP get the goal information
		//String[] tmp = tokens[0].split(":");
		
		//TODO: Important always use projection - so used SRC type for TGT type (Fix the rules to do that!!)	
		String goalKey = frontiers.get(0).spanString + ":" + tokens[0];
		int goalId = -1;
		if (matchingNodes.containsKey(goalKey)) {
			// vertex already exists, so get it
			goalId = matchingNodes.get(goalKey).intValue();
		} else {
			if (!isSrcRoot) {
				// If a X node is present on this source span, then connect to that
				if (matchingNodes.containsKey(frontiers.get(0).spanString + ":X")) {
					goalId = matchingNodes.get(frontiers.get(0).spanString + ":X");
				} else {
					// // No point in creating this node since it will not be connected to anything  in the forest
					System.err.println("Skipping creating a new node ! Something is off !"+goalKey+"->"+ targetStr);
					return false;
				}
			}
			else{
				// create the hypergraph vertex
				String[] args = goalKey.split(":");
				HGVertex goal = new HGVertex(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
				goalId = hg.addVertex(goal);
				matchingNodes.put(goalKey, new Integer(goalId));
				frontiers.get(0).targetNodeTypes.add(args[2]);
	
				// If this is the top node, create a edge to the target top node
				if (isSrcRoot) {
					int[] itemIds = { goalId };
					itemIds[0] = goalId;
					hg.addEdge(itemIds, 0, Grammar.CONNECT_EDGE);
				}
			}
		}

		//System.err.println("Adding!"+goalKey+"->"+ targetStr);
		// Get the items information
		ArrayList<Integer> itemIds = new ArrayList<Integer>(frontiers.size());

		for (int i = 1; i < tokens.length; i++) {
			Integer itemId = null;
			if (tokens[i].contains(":")) {
				// This is a frontier node
				String [] tmp = tokens[i].split(":");
				int itemPos = Integer.parseInt(tmp[0]);
				String itemKey = frontiers.get(itemPos).sStart + ":" + frontiers.get(itemPos).sEnd + ":" + tmp[1];
				if (matchingNodes.containsKey(itemKey)) {
					// vertex already exists, so get it
					itemId = matchingNodes.get(itemKey);
				} else {
					// create the hypergraph vertex
					String[] args = itemKey.split(":");
					HGVertex item = new HGVertex(Integer.parseInt(args[0]),Integer.parseInt(args[1]), args[2]);
					itemId = new Integer(hg.addVertex(item));
					matchingNodes.put(itemKey, itemId);
					frontiers.get(itemPos).targetNodeTypes.add(args[2]);
				}
			} else {
				String phrase = "";
				// Need to insert a lexical/phrasal node
				// First get the phrase by walking on the string
				int phraseLength = 0;
				while (i < tokens.length && !(tokens[i].contains(":"))) {
					phrase += " " + tokens[i];
					i++;
					phraseLength++;
				}
				phrase = phrase.trim();
				
				// Check if we already have the node
				if (insertedNodes.containsKey(phrase)) {
					itemId = insertedNodes.get(phrase);
				} else {
					// since this is a completely lexcial vertex and it won't
					// have any inEdges, let us initialize the hyp queue also
					int[] kindex = new int[2];
					kindex[0] = kindex[1] = -1;
					Hypothesis hyp = new Hypothesis(phrase.trim(), "", -1, kindex);

					if (frontiers.size() > 1) {
						// This is a dummy node, only wc,LM feature is active here
						Scorer.getScorer().initializeHypothesisFeatures(hyp,phraseLength);
					} else {
						Scorer.getScorer().initializeHypothesisFeatures(hyp, targetInfo);
						// Let us just add the hyp to the goal vertex
//						hg.getVertex(goalId).addHypothesis(hyp);
//						// And return. Nothing more to be done
//						return true;
					}

					// create the hypergraph vertex
					HGVertex item = new HGVertex(-1, -1, phrase);
					itemId = new Integer(hg.addVertex(item));
					insertedNodes.put(phrase, itemId);

					// Add the hyp to this new vertex
					item.addHypothesis(hyp);
				}
				if (i < tokens.length)
					i--;
			}
			itemIds.add(itemId);
		}

		// Convert the ArrayList to int [] : so sad !
		int[] itemIntIds = new int[itemIds.size()];
		for (int i = 0; i < itemIds.size(); i++) {
			itemIntIds[i] = itemIds.get(i).intValue();
		}

		// We have the goal and items, so let's create the edge
		// [TODO] Also pass in some kind of rule id which will tell us which rule created it.
		if (hg.addEdge(itemIntIds, goalId, targetInfo.getId())) {
			//System.err.println("\tAdded: "+targetStr);
			return true;
		}
		else{
			return false;
		}
	}	
}
