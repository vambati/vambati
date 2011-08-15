/**
 * 
 */
package taruDecoder;

import java.util.*;
import java.util.logging.Logger;
import options.Options;

import taruDecoder.features.LMFeatureFunction;
import taruDecoder.features.LengthPenaltyFeatureFunction;
import taruDecoder.features.LexicalWeightFeatureFunction;
import taruDecoder.hyperGraph.HGDecoder;
import taruHypothesis.Hypothesis;
 
import tarugrammar.Grammar;
import tarugrammar.GrammarRule;
import tarugrammar.LexicalRule;
import tarugrammar.PhraseRule;
import treeParser.ParseTree;
import treeParser.ParseTreeNode;
import treeParser.PatternNode;

/**
 * This class implements a top down tree to tree Matcher
 * Decoder is included in the matcher with the following options
 * 1. Hypergraph based decoder that first builds a target hypergraph and extracts KBest with Cube Growing Algorithm on it
 * 2. Simple Beam Search based stack decoder that loads hypothesis into beams and performs a left-right decoding 
 * 
 * @author abhayaa
 * 
 */
public class Taru {

	private static Logger matchLogger = Logger.getLogger("Taru.match");

	// Global resource variables 
	String graFile; 
	String phrFile ; 
	String sgtlexfile; 
	String tgslexfile ; 
	
	String lmFile_bin ;
	String lmFile_arpa;
	String modelFile ;
	 
	private Grammar g;
	
	// Decoder used within Taru to create target forest/beams and decode it 
	private Decoder decoder;
	
	// Currently used constructor - All others have issues !!!
	public Taru(String cfgfile,HashMap<String,Integer> vocab) {
		System.err.println("Initializing Taru using config file : "+cfgfile);
		Options opts = new Options(cfgfile); 

		 graFile = opts.get("GRAMMAR"); 
		 phrFile = opts.get("PHRASES"); 
		 sgtlexfile = opts.get("SGTLEX"); 
		 tgslexfile = opts.get("TGSLEX"); 
		
		 lmFile_bin = opts.get("LM_BIN");
		 lmFile_arpa = opts.get("LM_ARPA");
		 modelFile = opts.get("MODEL");
		double lratio = Double.parseDouble(opts.get("LENGTHRATIO"));
		
		System.err.println("GraFile...."+graFile);
		System.err.println("Phrases...."+phrFile);
		System.err.println("Model File...."+modelFile);
		
		// TODO: IMPORTANT: This needs to be set before any runs (Find a better alternative) 
		g = new Grammar(graFile,phrFile,vocab);
		
		Scorer.modelFile = modelFile;
		LexicalWeightFeatureFunction.sgtlexfile = sgtlexfile;
		LexicalWeightFeatureFunction.tgslexfile = tgslexfile;
		LexicalWeightFeatureFunction.loadLex();

		Scorer.modelFile = modelFile;
		// Set the length ratio parameter from config file 
		LengthPenaltyFeatureFunction.lengthratio = lratio; 
		LMFeatureFunction.arpaLmFile = lmFile_arpa;
		LMFeatureFunction.lmFile = lmFile_bin;
		LMFeatureFunction.loadLM();
		// IMPORTANT: END
	}
	
	// Main method for the decoding of Tree to String 
	public List<Hypothesis> decodeParseTree (String src,int N) {
		
		ParseTreeNode ptn = ParseTree.collapseUnaryToLowest(ParseTree.buildTree(src));

		// A hypergraph based decoder: reset everytime  
		// Implementation 1: Hypergraph
		decoder = new HGDecoder();
		// Implementation 2: Beamsearch
		//decoder = new StackDecoder();
		
		// Phase 1: Match tree and create target side forest/beams
		//System.out.println("Decoding yield:"+ptn.yield);
		transduce_phase(ptn);
		
		// Phase 2: Perform decoding on target side structure to create hypothesis and return them 
		return decoder.extract_phase(N);
	}
	
	// Phase 1: Tree matching (Parsing)
	public boolean transduce_phase(ParseTreeNode ptn) {
		System.out.println(ParseTree.getString(ptn));
		return treeWalk(ptn);
	}

	private boolean treeWalk(ParseTreeNode ptn) {
		// System.out.println("decoding " + ptn.nodetype);
		boolean match = true;
		
		// Perform lexical matches 
		if (ptn.targetNodeTypes.size() > 0 && ptn.isTerminal) {
			// ArrayList<LexicalRule> matches = g.getLexicalMatches(ptn);
			// Use only phrasal rules instead of any lexical rules (Vamshi) / Backoff must be added in a cleaner way !!! TODO
			ArrayList<PhraseRule> matches = g.getLexicalMatches2(ptn);
			if (matches.size() == 0) {
				// TODO Do something about the unknown words
				// matchLogger.warning("OOV word: " + ptn.getS());
				LexicalRule backoff = g.getBestSeenTranslation(ptn);
				if(backoff == null)
					decoder.handleOOV(ptn);
				else{
					decoder.addLexicalBackoff(backoff, ptn);
				}
			}else {
				// matchLogger.info("Matched " + ptn.getS() + ", " + matches.size() + " possible translations.");
				// Add the matches to the hypergraph
				// addLexicalEntriesToForest(matches, ptn, false);
				//System.err.println("Adding lexical via phrasal-"+ptn.getS());
				decoder.addPhraseEntriesToForest(matches, ptn, false); // temporary testing 
				return true;
			}
		} else {
			// Also look for a phrase match, so that you don't have to compose it from the rule
			ArrayList<PhraseRule> matches = g.getPhraseMatches(ptn);
			if (matches.size() != 0) {
				// System.err.println("Matched phrase:" +ptn.nodetype+":" + ptn.yield + ", " + matches.size() + " possible translations.");
				// Add the matches to the hypergraph
				decoder.addPhraseEntriesToForest(matches, ptn, ptn.isRoot);
				//return true;
			}
			
			// Parse for a Rule Match at this node 
			if (ptn.targetNodeTypes.size() > 0 || ptn.isRoot) {
				match = match && matchNode(ptn);
			}
		 	for (ParseTreeNode child : ptn.children) {
				// System.out.println("Child " + child.nodetype);
				match = treeWalk(child) && match;
			}
		}
		return match;
	}

	// Matching Rule Patterns
	private boolean matchNode(ParseTreeNode ptn) {
		boolean result = false;

		HashMap<Integer, PatternNode> patList = g.getPossibleMatches(ptn);
		if (patList != null) {
			//System.err.println(patList.size() + " possible pattern matches at "+ptn.nodetype);
			for (Integer patId : patList.keySet()) {
				
				if ( matchTreePattern(ptn, patList.get(patId)) ) {
					//System.err.println("Matched pattern: "+PatternNode.getString(patList.get(patId)));
					// Collect the source spans of frontier nodes
					ArrayList<ParseTreeNode> frontiers = new ArrayList<ParseTreeNode>();
					collectSourceFrontiers(ptn, patList.get(patId), frontiers);
					
					// Get all the target sides for this source pattern
					ArrayList<GrammarRule> targetStrings = g.getTargetSides(patId);
					// Process the match to create a Hypergraph
					 result = decoder.processMatch(ptn, patId, frontiers,targetStrings) || result;
				}
			}
		}

		if (!result) {
			// No rule matched. Need to use glue rule
			//matchLogger.info("No rules match , so glue it at "+ptn.nodetype);
			decoder.addGlueRulesToTargetForest(ptn);
		}
 		return result;
	}

	private boolean matchTreePattern(ParseTreeNode subject, PatternNode pattern) {
		// System.out.println("matching " + pattern.nodetype);
		if (pattern.isTerminal()) {
			// A terminal node is the lexicalized node; Need to match the node type and the word
			return subject.isTerminal
					&& pattern.nodetype.equalsIgnoreCase(subject.nodetype)
					&& pattern.getS().equalsIgnoreCase(subject.getS());
		} else if (pattern.children.isEmpty()) {
			// This is a frontier node, so match the node type only; This may even be a POS node. Generalized POS nodes are not marked as terminal. This is different from Rule learning
			return pattern.nodetype.equalsIgnoreCase(subject.nodetype);
		} else {
			// This is a intermediate node. Match the nodetype, number of children
			if (pattern.nodetype.equalsIgnoreCase(subject.nodetype)
					&& subject.children != null
					&& (pattern.children.size() == subject.children.size())) {
				// match all the children
				boolean match = true;
				for (int i = 0; i < pattern.children.size(); i++) {
					match = matchTreePattern(subject.children.elementAt(i),
									pattern.children.elementAt(i)) && match;
					if (!match)
						break;
				}
				return match;
			} else {
				return false;
			}
		}
	}
	
	private void collectSourceFrontiers(ParseTreeNode subject, PatternNode pattern, ArrayList<ParseTreeNode> frontiers) {
		if (frontiers.isEmpty()) {
			frontiers.add(subject);
		}

		if (pattern.isTerminal()) {
			// Nothing
		} else if (pattern.children.isEmpty()) {
			// This is a frontier node, so match the node type only;
			// Also collect the ids of the frontiers for use in processTargetString
			frontiers.add(subject);
		} else {
			// This is a intermediate node. Match the nodetype, number of children
			for (int i = 0; i < pattern.children.size(); i++) {
				collectSourceFrontiers(subject.children.elementAt(i),
						pattern.children.elementAt(i), frontiers);
			}
		}
	}	
}
