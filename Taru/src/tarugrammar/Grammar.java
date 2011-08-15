/**
 * 
 */
package tarugrammar;

/**
 * Load the grammar from the file
 * @author abhayaa
 *
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.io.StringReader;

import treeParser.*;

public class Grammar implements Serializable {
	
	private static String grammarFile = "";
	
	public static final String GLUE_EDGE = "glue"; 
	public static final String CONNECT_EDGE = "connect"; 
	
	// TODO: Tweak with these 
	// Can not be ZERO , infinity scores 
	public static double GLUE_RULE_SCORE = Math.log10(0.001);
	public static double CONNECT_RULE_SCORE = Math.log10(0.001);
	public static double OOV_RULE_SCORE = Math.log10(0.001);
	
	// TODO: drop the lexicalhash and just work with phraseHash 
	private static HashMap<String, HashMap<String, ArrayList<LexicalRule>>> lexicalHash;
	private static HashMap<String, HashMap<String, ArrayList<PhraseRule>>> phraseHash;
	
	private static HashMap<Integer,ArrayList<GrammarRule>> targetHash;
	private static HashMap<Integer,PatternNode> parsedSourceHash;
	private static HashMap<String,ArrayList<Integer>> patternHash;
	
	private static HashMap<String, double []> scoreHash;
	
	// TODO: Have to implement LEX_BEAM, RULE_BEAM while loading the Grammar itself to save memory

	/*static {
		lexicalHash = new HashMap<String, HashMap<String, ArrayList<LexicalRule>>>(100000);
		targetHash = new HashMap<Integer, ArrayList<GrammarRule>>(10000);
		parsedSourceHash = new HashMap<Integer, PatternNode>(10000);
		patternHash = new HashMap<String, ArrayList<Integer>>(10000);

		scoreHash = new HashMap<String, double[]>(40000);
		
		loadGrammar(grammarFile);
	} */
	
	public Grammar () {
		lexicalHash = new HashMap<String, HashMap<String, ArrayList<LexicalRule>>>(100000);
		targetHash = new HashMap<Integer, ArrayList<GrammarRule>>(10000);
		parsedSourceHash = new HashMap<Integer, PatternNode>(10000);
		patternHash = new HashMap<String, ArrayList<Integer>>(10000);

		scoreHash = new HashMap<String, double[]>(40000);
		
		// HACK: This will not support giving input sentence at STDIN anymore. Has to go through a file
		loadGrammar(grammarFile,null);
	}
	
	public Grammar (String gFile,HashMap<String,Integer> vocabulary) {
		lexicalHash = new HashMap<String, HashMap<String, ArrayList<LexicalRule>>>(100000);
		targetHash = new HashMap<Integer, ArrayList<GrammarRule>>(10000);
		parsedSourceHash = new HashMap<Integer, PatternNode>(10000);
		patternHash = new HashMap<String, ArrayList<Integer>>(10000);

		scoreHash = new HashMap<String, double[]>(40000);	
		loadGrammar(gFile,vocabulary);
	}
	
	public Grammar (String gFile,String phrFile,HashMap<String,Integer> vocabulary) {
		lexicalHash = new HashMap<String, HashMap<String, ArrayList<LexicalRule>>>(100000);
		phraseHash = new HashMap<String, HashMap<String, ArrayList<PhraseRule>>>(100000);

		targetHash = new HashMap<Integer, ArrayList<GrammarRule>>(10000);
		parsedSourceHash = new HashMap<Integer, PatternNode>(10000);
		patternHash = new HashMap<String, ArrayList<Integer>>(10000);

		scoreHash = new HashMap<String, double[]>(40000);
		
		loadGrammar(gFile,vocabulary);
		loadPhrases(phrFile,vocabulary);
	}

	private static void loadPhrases(String fileName,HashMap<String,Integer> inputVocabulary)
	{	
		System.err.println("Loading phrases from file: "+fileName);
		//System.err.println("Restricting to vocabulary: "+inputVocabulary.toString());
		 
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = "";
			String ruleScore  = "";
			
			int phraseCount = 0;
			int skipCount = 0;
			 
			while((line = br.readLine()) != null)
			{
				ruleScore = br.readLine();
				
				if(phraseCount % 50000 == 0)
					System.err.println(phraseCount);

				phraseCount++;
				
				// Check if this rule has known input Vocabulary, and prune it
			
				String [] strarr = line.split("\t");
				if(strarr.length!=4){
					System.err.println(phraseCount+":Error.."+line+"\nscore"+ruleScore);
					continue;
				}
				String typeS = strarr[0];
				String typeT = strarr[1];
				String src = strarr[2];
				String tgt = strarr[3];
				 
				
				// Load everything if inputVocab is NULL 
				if(inputVocabulary!=null){
				
					ArrayList<String> vocab = new ArrayList<String>(50);
					String [] srcarr = src.split("\\s+");
					for(int i =0; i < srcarr.length; i++){
						vocab.add(srcarr[i]);
					}
					boolean skipflag = false;
					if(vocab.size()!=0) {
						//System.out.println("Trying Vocab:"+vocab.toString());
						for(String v: vocab) {
							if(!inputVocabulary.containsKey(v)) {
								//System.err.println("Skipping phrase"+src);
								skipflag = true;
								break;
							}
						}
						if(skipflag==true) {
							skipCount++;
							 // Skip loading of the rule
							continue; 
						}
					}
				}
				
				// Differentiating rule ids using 'p', so that we can use the same "scoreHash"
				String ruleId = typeS+"p"+phraseCount;
				// Read the scores
				String [] scoreStr = ruleScore.split("\t");
				double [] scores = new double[scoreStr.length];
				for(int i =0; i < scoreStr.length; i++){
					scores[i] = Math.log10(Double.parseDouble(scoreStr[i]));
				}
				scoreHash.put(ruleId, scores);
 					
				PhraseRule prule = new PhraseRule(ruleId,typeS,src,typeT,tgt);
				if(phraseHash.containsKey(src)){
						ArrayList<PhraseRule> matches = phraseHash.get(src).get(typeS);
						if(matches == null){
							matches = new ArrayList<PhraseRule>();
						}	
						matches.add(prule);
						phraseHash.get(src).put(typeS, matches);
					}else{
						ArrayList<PhraseRule> tmpt = new ArrayList<PhraseRule>();
						tmpt.add(prule);
						
						HashMap<String, ArrayList<PhraseRule>> tmpmap = new HashMap<String, ArrayList<PhraseRule>>();
						tmpmap.put(typeS, tmpt);
						phraseHash.put(src, tmpmap);
					}
			}
			br.close();
			System.err.println("Total phrases:"+phraseCount);
			System.err.println("Skipped phrases:"+skipCount);
		}catch(Exception e){e.printStackTrace();}
	}
	
	private static void loadGrammar(String fileName,HashMap<String,Integer> inputVocabulary){
		
		int lrcount=0;
		int rcount=0;
		System.err.println("Loading grammar from file: "+fileName);
		//System.err.println("Restricting to vocabular: "+inputVocabulary.toString());
		
		HashMap<String,Integer> sourceHash = new HashMap<String, Integer>(10000);;

		String source = "";
		String target = "";
		String ruleScore = "";

		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			int ruleCount = 0;
			int skipCount = 0;
			int srcCount = 0;
			while((source = br.readLine()) != null){
				target = br.readLine();
				ruleScore = br.readLine();
				
				if(ruleCount % 50000 == 0)
					System.err.println(ruleCount);

				ruleCount++;

				StringReader sr = new StringReader(source);
				ParseTreeLexer ptl = new ParseTreeLexer(sr);
				RuleParser rp = new RuleParser(ptl);
				PatternNode ptn = rp.parse_rule();
				
				// Check if this rule has known input Vocabulary, and prune it
				ArrayList<String> vocab = new ArrayList<String>(50);
				PatternNode.getVocab(ptn,vocab);
				
				// Load everything if inputVocab is NULL 
				if(inputVocabulary!=null){
					boolean skipflag = false;
					if(vocab.size()!=0) {
						//System.out.println("Trying Vocab:"+vocab.toString());
						for(String v: vocab) {
							if(!inputVocabulary.containsKey(v)) {
							 // Skip loading of the rule
								skipflag = true;
								break;
							}
						}
						if(skipflag==true) {
							skipCount++;
							 // Skip loading of the rule
							continue; 
						}else {
							//System.err.println("Good:"+source);
						}
					}
				}

				Integer srcId = -1;
				if(sourceHash.containsKey(source)){
					srcId = sourceHash.get(source).intValue();
				}
				else{
					srcCount++;
					srcId = new Integer(srcCount);
					sourceHash.put(source,srcId);
				}
				
				String ruleId = ptn.nodetype+ruleCount;

				// Read the scores
				String [] scoreStr = ruleScore.split("\\s+");
				double [] scores = new double[scoreStr.length];
				for(int i =0; i < scoreStr.length; i++){
					scores[i] = Math.log10(Double.parseDouble(scoreStr[i]));
				}

				scoreHash.put(ruleId, scores);
				
				if(ptn.isRoot() && ptn.isTerminal()){
					lrcount++;
					
					LexicalRule lrule = new LexicalRule(ruleId, target);
					String [] tokens = source.split(" +");
					
					if(lexicalHash.containsKey(tokens[2])){
						ArrayList<LexicalRule> matches = lexicalHash.get(tokens[2]).get(tokens[1]);
						if(matches == null){
							matches = new ArrayList<LexicalRule>();
						}	
						matches.add(lrule);
						lexicalHash.get(tokens[2]).put(tokens[1], matches);
					}
					else{
						ArrayList<LexicalRule> tmpt = new ArrayList<LexicalRule>();
						tmpt.add(lrule);
						
						HashMap<String, ArrayList<LexicalRule>> tmpmap = new HashMap<String, ArrayList<LexicalRule>>();
						tmpmap.put(tokens[1], tmpt);
						lexicalHash.put(tokens[2], tmpmap);
					}
				}
				else{
					rcount++;
					GrammarRule rule = new GrammarRule(ruleId, target);

					if(targetHash.containsKey(srcId)){
						targetHash.get(srcId).add(rule);
					}
					else{
						ArrayList<GrammarRule> tmps = new ArrayList<GrammarRule>();
						tmps.add(rule);
						targetHash.put(srcId, tmps);
						
						parsedSourceHash.put(srcId, ptn);
						
						String patternHashCode = getHashCode(ptn);
						if(patternHash.containsKey(patternHashCode)){
							patternHash.get(patternHashCode).add(srcId);
//							System.out.println(patternHashCode + " " + patternHash.get(patternHashCode).size());
						}
						else{
							ArrayList<Integer> tmpi = new ArrayList<Integer>(1000);
							tmpi.add(srcId);
							patternHash.put(patternHashCode,tmpi);
						}
					}

				}
			}
			br.close();
			System.err.println("Total rules:"+ruleCount);
			System.err.println("Lexical rules:"+lrcount);
			System.err.println("Grammar rules:"+rcount);
			System.err.println("Skipped rules:"+skipCount);
		}catch(Exception e){System.err.println("Error at line: "+ source +"\n"+target+"\n"+ruleScore);
						e.printStackTrace();}
	}
	
	public static double[] getRuleScores(String ruleId){
		double [] score = {0.0,0.0};
		if(ruleId.equalsIgnoreCase(Grammar.GLUE_EDGE))
			score[0] = Grammar.GLUE_RULE_SCORE;
		else if(ruleId.equalsIgnoreCase(Grammar.CONNECT_EDGE))
			score[0] = 0.0;
		else
			score = scoreHash.get(ruleId);
		return score;
	}
	
	public static String getHashCode(PatternNode ptn){
		String hashCode = ptn.nodetype + ptn.children.size();
		for(PatternNode child : ptn.children){
			hashCode += " "+child.nodetype;
		}
		return hashCode;
	}
	
	public static String getHashCode(ParseTreeNode ptn){
		String hashCode = ptn.nodetype + ptn.children.size();
		for(ParseTreeNode child : ptn.children){
			hashCode += " "+child.nodetype;
		}
		return hashCode;
	}
	
	public HashMap<Integer,PatternNode> getPossibleMatches(ParseTreeNode ptn){
		HashMap<Integer,PatternNode> patMap = new HashMap<Integer, PatternNode>(100);
		ArrayList<Integer> patIds = patternHash.get(Grammar.getHashCode(ptn));
		if(patIds != null){
			for(Integer i : patIds){
				patMap.put(i,parsedSourceHash.get(i));
			}
			return patMap;
		}
		return null;
	}
	
	public ArrayList<GrammarRule> getTargetSides(Integer patId){
		return targetHash.get(patId);
	}
	
	public ArrayList<PhraseRule> getPhraseMatches(ParseTreeNode ptn) {
		
		ArrayList<PhraseRule> matches = new ArrayList<PhraseRule>();
		HashMap<String, ArrayList<PhraseRule>> map = phraseHash.get(ptn.yield);
		 
		if(map != null){
			boolean getAll = ptn.targetNodeTypes.contains("X");
			// Let's see if the required POS is available
			if(map.containsKey(ptn.nodetype)) {
				for(PhraseRule l : map.get(ptn.nodetype)){
					// Good, but only send back rules that connect to some node in target forest
					// if(ptn.targetNodeTypes.contains(l.getTargetPOS()))
						matches.add(l);
				}
			}
		}
		return matches;
	}

	
	public ArrayList<PhraseRule> getLexicalMatches2(ParseTreeNode ptn){
//		System.out.println(ptn.targetNodeTypes);
		
		ArrayList<PhraseRule> matches = new ArrayList<PhraseRule>();
		
		HashMap<String, ArrayList<PhraseRule>> map = phraseHash.get(ptn.getS());
		if(map == null){
			// The word is OOV. Will be handled according to current OOV policy in the decoder
			System.err.println("OOV lexical phrase-"+ptn.getS());
		}
		else{
			boolean getAll = ptn.targetNodeTypes.contains("X");
			// Let's see if the required POS is available
			if(map.containsKey(ptn.nodetype)){
				// Good, but only send back rules that connect to some node in target forest
				for(PhraseRule l : map.get(ptn.nodetype)){
					if(ptn.targetNodeTypes.contains(l.getTargetPOS()))
						matches.add(l);
				}
			}
			// If we did not find any match above, allow all POS
			// and also keep all the target candidates
			HashMap<String, Double> tgtCand = new HashMap<String, Double>();
			if(matches.size() == 0 || getAll ){
				// The exact POS is not present, so use all other POS
				// but still only send back rules that connect to some node in target forest
				for(String pos : map.keySet()){
					if(getAll){
						matches.addAll(map.get(pos));
						continue;
					}
					
					for(PhraseRule l : map.get(pos)){
						if(ptn.targetNodeTypes.contains(l.getTargetPOS()))
							matches.add(l);
						else
							tgtCand.put(l.getTargetWord(), Grammar.getRuleScores(l.getId())[0]);
					}
				}
			}
		}
		return matches;
	}
	
	public ArrayList<LexicalRule> getLexicalMatches(ParseTreeNode ptn){
//		System.out.println(ptn.targetNodeTypes);
		
		ArrayList<LexicalRule> matches = new ArrayList<LexicalRule>();
		
		HashMap<String, ArrayList<LexicalRule>> map = lexicalHash.get(ptn.getS());
		if(map == null){
			// The word is OOV. Will be handled according to current OOV policy in the decoder
		}
		else{
			boolean getAll = ptn.targetNodeTypes.contains("X");
			// Let's see if the required POS is available
			if(map.containsKey(ptn.nodetype)){
				// Good, but only send back rules that connect to some node in target forest
				for(LexicalRule l : map.get(ptn.nodetype)){
					if(ptn.targetNodeTypes.contains(l.getTargetPOS()))
						matches.add(l);
				}
			}
			// If we did not find any match above, allow all POS
			// and also keep all the target candidates
			HashMap<String, Double> tgtCand = new HashMap<String, Double>();
			if(matches.size() == 0 || getAll ){
				// The exact POS is not present, so use all other POS
				// but still only send back rules that connect to some node in target forest
				for(String pos : map.keySet()){
					if(getAll){
						matches.addAll(map.get(pos));
						continue;
					}
					
					for(LexicalRule l : map.get(pos)){
						if(ptn.targetNodeTypes.contains(l.getTargetPOS()))
							matches.add(l);
						else
							tgtCand.put(l.getTargetWord(), Grammar.getRuleScores(l.getId())[0]);
					}
				}
			}
		}
		return matches;
	}
	
	public LexicalRule getBestSeenTranslation(ParseTreeNode ptn){
		HashMap<String, ArrayList<LexicalRule>> map = lexicalHash.get(ptn.getS());
		double maxScore = -100;
		LexicalRule maxL = null;
		if(map ==null){
			return null;
		}
		else{
			for(String pos : map.keySet()){				
				for(LexicalRule l : map.get(pos)){
					if(Grammar.getRuleScores(l.getId())[0] > maxScore){
						maxScore = Grammar.getRuleScores(l.getId())[0];
						maxL = l; 
					}
				}
			}
			return maxL;
		}
	}
}
