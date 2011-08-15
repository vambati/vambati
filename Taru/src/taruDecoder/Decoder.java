package taruDecoder;

import java.util.*;
 
import taruHypothesis.Hypothesis;
import tarugrammar.GrammarRule;
import tarugrammar.LexicalRule;
import tarugrammar.PhraseRule;
import treeParser.ParseTreeNode;
 
/*
 * Takes care of insertions to create the targetForest -
 * 1. Could insert into Hypergraph for CubeGrowing and CubePruning decoding
 * 2. Could insert into Beams for Simple Beam Search (Tree Postorder traversal) 
 */
public interface Decoder {
  	 
	// Main method that every decoder needs to overwrite 
	public List<Hypothesis> extract_phase(int N);

	// Once a tree segment is matched with a pattern , what do we do with it ? 
	public boolean processMatch(ParseTreeNode ptn, Integer patId,
			ArrayList<ParseTreeNode> frontiers,
			ArrayList<GrammarRule> targetStrings);
	
	public void addPhraseEntriesToForest(ArrayList<PhraseRule> targetList, ParseTreeNode frontier, boolean isRoot);
	// Add grammar rules to forest 
	public boolean addToTargetForest(GrammarRule targetInfo, ArrayList<ParseTreeNode> frontiers, boolean isSrcRoot);
	// Add glue rules
	public void addGlueRulesToTargetForest(ParseTreeNode ptn);
	// Handle OOV - copy, delete, transliterate etc - Perhaps put in a different class later TODO
	public void handleOOV(ParseTreeNode ptn);
	public void addLexicalBackoff(LexicalRule backoff, ParseTreeNode ptn);
}