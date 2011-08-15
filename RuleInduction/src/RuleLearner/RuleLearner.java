package RuleLearner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;

import Options.Options;
import Rule.Alignment;
import Rule.Rule;
import Rule.RuleLearnerException;
import RuleLearner.aligner.AlignerT2SBitSet;
import RuleLearner.aligner.AlignerT2TBitset;
import RuleLearner.aligner.AlignerT2TSBitset;
import RuleLearner.extractor.RuleExtractorS2S;
import RuleLearner.extractor.RuleExtractorS2S_Union;
import RuleLearner.extractor.RuleExtractorT2S;
import RuleLearner.extractor.RuleExtractorT2TAbhaya;
import Scoring.RuleException;
import TreeParser.ParseTreeNode;
import Utils.MyUtils;
import Utils.ParseTree;

/**
 * Rule Transduction Code (Tree to Tree, Tree to String ) (Output format
 * 'String' or 'Tree' )
 * 
 * @author Vamshi Ambati 12 Mar 2008 Carnegie Mellon University
 */

public class RuleLearner {
	private static HashMap<String, Integer> ruleRepository = new HashMap<String, Integer>(55000);
	private static ArrayList<String> corpusParseTrees = new ArrayList<String>(20000);
	private static int[] ruleCountsBySize;
	private static HashMap<Integer, Integer> collision = new HashMap<Integer, Integer>(20000);
	private static int collisionCount = 0;
	// private static final String barrow = "/usr3/data/de-en/300k-matching/";

	public String corpusFile;
	public String sparseFile;
	public String graFile;
	public String ptableFile;
	public String lexiconFile;
	public String tparseFile;
	public int stopat = -1;
	public int startfrom = -1;

	public int skiplength = -1;

	public String tibTrainFile;
	public String tibTranducerFile;

	// Modes
	public String input_mode, output_mode, tool_mode;

	// parse tree modifications
	// 0 - NONE, 1- HEAD, 2- HEADTYPE
	public static int lexicalization_mode = 0;
	// 0 - NONE, 1- PARENT, 2-PARENTTYPE
	public static int markovization_mode = 0;

	// COMPUTE EXTRA context features for each rule
	public static int context_features = 0;

	IPrinter mywriter;

	// To keep track of number of rules learnt in this setup
	public int rulecount = 0;
	public int rulecount_union = 0; // Only used in T2T2S for unique rule count
									// when symmetrizing 25/March/2009

	public int rulecount_dir1 = 0; // How many were actually node-aligned
	public int rulecount_dir2 = 0; // How many were backed off to projection
									// mode

	// To keep track of phrase count
	public int phrasecount = 0;
	// To keep track of lexicon count
	public int lexcount = 0;

	public RuleLearner(Configuration conf, IPrinter rulePrinter) {
		int ruleSize = Integer.parseInt(conf.get("MAX_RULE_SIZE"));
		RuleExtractorT2TAbhaya.setMaxRuleSize(ruleSize);
		ruleCountsBySize = new int[ruleSize + 1];
		input_mode = conf.get("INPUT_MODE");
		output_mode = conf.get("OUTPUT_MODE");
		tool_mode = conf.get("TOOL_MODE");
		mywriter = rulePrinter;
	}

	public RuleLearner(Options opts) throws Exception {
		// MAX RULE SIZE
		int ruleSize = Integer.valueOf(opts.get("MAX_RULE_SIZE")).intValue();
		RuleExtractorT2TAbhaya.setMaxRuleSize(ruleSize);
		ruleCountsBySize = new int[ruleSize + 1];

		// CORPUS and Parses
		corpusFile = opts.get("CORPUS_FILE");
		sparseFile = opts.get("SPARSE_FILE");

		if (opts.defined("STOPAT")) {
			stopat = opts.getInt("STOPAT");
		}
		if (opts.defined("STARTFROM")) {
			startfrom = opts.getInt("STARTFROM");
		}

		// TODO: implement a max length on parse or sentences on source/target
		if (opts.defined("SKIP_LENGTH")) {
			skiplength = opts.getInt("SKIP_LENGTH");
		}
		// Output files
		graFile = opts.get("GRA_FILE");
		ptableFile = opts.get("PTABLE_FILE");
		lexiconFile = opts.get("LEXICON_FILE");

		// MODE
		input_mode = opts.get("INPUT_MODE");
		output_mode = opts.get("OUTPUT_MODE");

		tool_mode = opts.get("TOOL_MODE");

		if (input_mode.equals("T2T") || input_mode.equals("T2TS") || input_mode.equals("TS2TS")) {
			tparseFile = opts.get("TPARSE_FILE");
		}

		// Parse Tree modifications
		if (opts.defined("LEXICALIZATION")) {
			if (opts.get("LEXICALIZATION").equals("HEAD")) {
				lexicalization_mode = 1;
			} else if (opts.get("LEXICALIZATION").equals("FOREIGNHEAD")) {
				lexicalization_mode = -1;
			} else if (opts.get("LEXICALIZATION").equals("HEADTYPE")) {
				lexicalization_mode = 2;
			} else {
				lexicalization_mode = 0;
			}
		}
		if (opts.defined("MARKOVIZATIOn")) {
			if (opts.get("MARKOVIZATION").equals("PARENT")) {
				markovization_mode = 1;
			} else if (opts.get("MARKOVIZATION").equals("PARENTTYPE")) {
				markovization_mode = 2;
			} else {
				markovization_mode = 0;
			}
		}
		// Compute extra source and target side features
		if (opts.defined("CONTEXT_FEATURES")) {
			if (opts.get("CONTEXT_FEATURES").equals("TRUE"))
				context_features = 1;
		}

		// Tiburon params
		if (tool_mode.equals("TIBURON")) {
			tibTranducerFile = opts.get("TIBURON_TRANSDUCER");
			tibTrainFile = opts.get("TIBURON_TRAIN");
		}

		System.out.println("Input Mode: " + input_mode);
		System.out.println("Output Mode: " + output_mode);

		mywriter = new Printer(graFile, ptableFile, lexiconFile, opts);
	}

	public void start() throws Exception {
		System.out.println("Rule transduction starts...");
		Corpus corpus = new Corpus(this);
		corpus.load(this);
		mywriter.close();
		System.out.println("Completed.");

		if (tool_mode.equalsIgnoreCase("TIBURON")) {
			mywriter.writeTiburonTransducerFile(tibTranducerFile, ruleRepository);
			mywriter.writeTiburonTrainFile(tibTrainFile, corpusParseTrees);
			printRuleStats();
			System.out.println("Unique Rules: " + ruleRepository.size());
		} else {
			System.err.println("Generated:" + rulecount + " rules");
			if (input_mode.equals("TS2TS")) {
				System.err.println("Count dir 1: " + rulecount_dir1 + " rules");
				System.err.println("Count dir 2: " + rulecount_dir2 + " rules");
				System.err.println("Unique: " + rulecount_union + " rules");
				System.err.println("Aligned Nodes: " + AlignerT2TSBitset.rulecount_aligned);
				System.err.println("Projected Nodes: " + AlignerT2TSBitset.rulecount_projected);
				// TODO:
				// System.err.println("SynPhrase Count: "+(rulecount-rulecount_union));
			} else if (input_mode.equals("T2TS")) {
				System.err.println("Aligned Nodes: " + AlignerT2TSBitset.rulecount_aligned);
				System.err.println("Projected Nodes: " + AlignerT2TSBitset.rulecount_projected);
				System.err.println("SynPhrase Count: "
						+ (rulecount - (AlignerT2TSBitset.rulecount_aligned + AlignerT2TSBitset.rulecount_projected)));
			} else if (input_mode.equals("T2T")) {
				System.err.println("Aligned Nodes: " + AlignerT2TBitset.rulecount_aligned);
			} else if (input_mode.equals("T2S")) {
				System.err.println("Projected Nodes: " + AlignerT2SBitSet.rulecount_projected);
			}
		}
	}

	private void printRuleStats() {
		for (int i : ruleCountsBySize) {
			System.out.println(i);
		}
	}

	public static void addRule(String rule, int ruleSize) {

		if (!ruleRepository.containsKey(rule)) {
			ruleRepository.put(rule, new Integer(1));
			// if(collision.containsKey(new
			// Integer(MyUtils.getHashCode(rule)))){
			// // System.out.println("Collision !");
			// collisionCount++;
			// }
			// collision.put(new Integer(MyUtils.getHashCode(rule)), new
			// Integer(1));
			ruleCountsBySize[ruleSize]++;
		}
	}

	public static void addCorpusParseTree(String srcParse, String tgtParse) {
		corpusParseTrees.add(srcParse);
		corpusParseTrees.add(tgtParse);
	}

	public void transduce_t2ts(CorpusEntry ce, int sennum) throws RuleLearnerException, RuleException, IOException, InterruptedException {
		if (sennum < startfrom) {
			return;
		} else if (sennum == stopat) {
			System.err.println("Stopping here after " + stopat);
			System.err.println("Rule count " + rulecount);
			System.err.println("Unique count " + rulecount_union);
			mywriter.close();
			System.exit(0);
		}

		if ((sennum % 10000) == 0) {
			System.out.println(sennum + " sens - " + rulecount + " rules");
		}
		Alignment amap = new Alignment(ce.getAlignmentStr());

		ParseTreeNode stree = null;
		if (lexicalization_mode == 0) {
			stree = ParseTree.buildTree(ce.getSParse(), lexicalization_mode);
		} else {
			// Lexicalized Parse tree - head annotation
			stree = ParseTree.buildLexTree(ce.getSParse(), lexicalization_mode);
		}

		// Compute more contextual info for the trees
		if (context_features == 1) {
			ParseTree.annotateLeftContext(stree);
			ParseTree.annotateRightContext(stree);
		}
		// Parse tree - no annotation
		ParseTreeNode ttree = ParseTree.buildTree(ce.getTParse(), 0);

		if (stree.sEnd != ce.sSeq.size() || ttree.sEnd != ce.tSeq.size()) {

			String sp = ParseTree.getString(stree);
			String tp = ParseTree.getString(ttree);
			Vector<String> spSeq = new Vector<String>();
			Vector<String> tpSeq = new Vector<String>();

			StringTokenizer st = new StringTokenizer(sp);
			while (st.hasMoreTokens()) {
				spSeq.add(st.nextToken());
			}
			st = new StringTokenizer(tp);
			while (st.hasMoreTokens()) {
				tpSeq.add(st.nextToken());
			}

			System.out.println("S:" + ce.sSeq + ":" + ce.sSeq.size());
			System.out.println("SP:" + spSeq + ":" + spSeq.size() + "-" + stree.sEnd);
			System.out.println("T:" + ce.tSeq + ":" + ce.tSeq.size());
			System.out.println("TP:" + tpSeq + ":" + tpSeq.size() + "-" + ttree.sEnd);

			// String stp = ParseTree.treeString(stree);
			// System.out.println("STP:"+stp);
			System.err.println(sennum + ": Mismatch in parse and sentence. Skipping !");
			return;
		}
		if (amap.maxX > stree.sEnd || amap.maxY > ttree.sEnd) {
			System.err.println(sennum
					+ ": Mismatch in alignment info and sentence lengths. Skipping !");
			return;
		}

		// Align Tree to Trees
		AlignerT2TSBitset.calculateSpans(stree, ttree, ce.getS(), ce.getT(), amap);
		// Still Extract only Tree to String rules
		if (output_mode.equals("S2S")) {
			rulecount =
					RuleExtractorS2S.extractRules(stree, mywriter, ce.sSeq, ce.tSeq, sennum,
							rulecount, amap);
		} else if (output_mode.equals("T2S")) {
			rulecount =
					RuleExtractorT2S.extractRules(stree, mywriter, ce.sSeq, ce.tSeq, sennum,
							rulecount);
		} else {
			throw new RuntimeException("Output mode can not be" + output_mode);
		}
	}

	public void transduce_t2s(CorpusEntry ce, int sennum) throws RuleLearnerException, RuleException, IOException, InterruptedException {
		// Sentence level delimitation
		// mywriter.grawriter.write("<sen id="+sennum+"</sen>");
		// mywriter.ptablewriter.write("<sen id="+sennum+"</sen>");
		// mywriter.lexiconwriter.write("<sen id="+sennum+"</sen>");

		if ((sennum % 1000) == 0) {
			System.out.println(sennum + " sens - " + rulecount + " rules");
		} else {
			// System.out.print(sennum+" ");
		}

		Alignment amap = new Alignment(ce.getAlignmentStr());

		ParseTreeNode tree = null;
		if (lexicalization_mode == 0) {
			tree = ParseTree.buildTree(ce.getSParse(), lexicalization_mode);
		} else {
			// Lexicalized Parse tree - head annotation
			tree = ParseTree.buildLexTree(ce.getSParse(), lexicalization_mode);
		}

		// Compute more contextual info for the trees
		if (context_features == 1) {
			ParseTree.annotateLeftContext(tree);
			ParseTree.annotateRightContext(tree);
		}

		String checkStr = ParseTree.getString(tree);
		int parsecount = MyUtils.wordCount(checkStr);

		if (parsecount != ce.sSeq.size()) {
			System.out.println(ce.sSeq.size() + ":" + parsecount
					+ " Mismatch of Tokenization between Parse and Sentence");
			System.out.println("S:" + ce.getS());
			System.out.println("P:" + checkStr);
			return;
		}

		// Galley Style Rules
		AlignerT2SBitSet.calculateSpans(tree, ce.getS(), ce.getT(), amap);

		// String treeStr = ParseTree.treeString(tree);
		// System.out.println(treeStr);

		if (output_mode.equals("S2S")) {
			rulecount =
					RuleExtractorS2S.extractRules(tree, mywriter, ce.sSeq, ce.tSeq, sennum,
							rulecount, amap);
		} else if (output_mode.equals("T2S")) {
			// Only for Taru style rule extraction
			rulecount =
					RuleExtractorT2S.extractRules(tree, mywriter, ce.sSeq, ce.tSeq, sennum,
							rulecount);
		} else {
			System.out.println("Output mode can not be" + output_mode);
			System.exit(0);
		}
	}

	public void transduce_t2t(CorpusEntry ce, int sennum) throws RuleLearnerException, RuleException, IOException, InterruptedException {
		// Sentence level delimitation
		// mywriter.grawriter.write("<sen id="+sennum+"</sen>");
		// mywriter.grawriter.write("\n");

		// mywriter.ptablewriter.write("<sen id="+sennum+"</sen>");
		// mywriter.ptablewriter.write("\n");

		// mywriter.lexiconwriter.write("<sen id="+sennum+"</sen>");
		// mywriter.lexiconwriter.write("\n");

		if ((sennum % 10000) == 0) {
			System.out.println(sennum + " sens - " + rulecount + " rules");
		}
		/*
		 * else{ System.out.print(sennum+" "); }
		 */
		Alignment amap = new Alignment(ce.getAlignmentStr());

		ParseTreeNode stree = null;
		if (lexicalization_mode == 0) {
			stree = ParseTree.buildTree(ce.getSParse(), lexicalization_mode);
		} else {
			// Lexicalized Parse tree - head annotation
			stree = ParseTree.buildLexTree(ce.getSParse(), lexicalization_mode);
		}

		ParseTreeNode ttree = ParseTree.buildTree(ce.getTParse(), lexicalization_mode);

		if (stree.sEnd != ce.sSeq.size() || ttree.sEnd != ce.tSeq.size()) {
			System.err.println(sennum + ": Mismatch in parse and sentence. Skipping !");
			return;
		}

		// Align Tree to Trees
		AlignerT2TBitset.calculateSpans(stree, ttree, ce.getS(), ce.getT(), amap);
		// System.out.println(ParseTree.treeString(stree));
		// Still Extract only Tree to String rules
		if (output_mode.equals("S2S")) {
			rulecount =
					RuleExtractorS2S.extractRules(stree, mywriter, ce.sSeq, ce.tSeq, sennum,
							rulecount, amap);
		} else if (output_mode.equals("T2S")) {
			rulecount =
					RuleExtractorT2S.extractRules(stree, mywriter, ce.sSeq, ce.tSeq, sennum,
							rulecount);
		} else if (output_mode.equals("T2T")) {
			// This format is the TIBURON and for TARU
			rulecount =
					RuleExtractorT2TAbhaya.extractRules(stree, mywriter, ce.sSeq, ce.tSeq, sennum,
							rulecount);
			// Let us keep around the parse trees in the tiburon format
			StringBuilder streeBuilder = new StringBuilder(50);
			StringBuilder ttreeBuilder = new StringBuilder(50);
			ParseTree.tiburonString(stree, streeBuilder);
			ParseTree.tiburonString(ttree, ttreeBuilder);
			String srcParse = streeBuilder.toString();
			String tgtParse = ttreeBuilder.toString();
			addCorpusParseTree(srcParse, tgtParse);
		} else {
			System.out.println("Output mode can not be" + output_mode);
			System.exit(0);
		}
	}

	// Union - Restructure both sides parse trees and extract grammars - March
	// 24/2009

	public void transduce_ts2ts(CorpusEntry ce, int sennum) throws RuleLearnerException, RuleException, IOException, InterruptedException {
		if (sennum < startfrom) {
			return;
		} else if (sennum == stopat) {
			System.err.println("Stopping here after " + stopat);
			System.err.println("Rule count " + rulecount);
			System.err.println("Unique count " + rulecount_union);
			mywriter.close();
			System.exit(0);
		}

		if ((sennum % 10000) == 0) {
			System.out.println(sennum + " sens - " + rulecount + " rules");
		}
		Alignment amap = new Alignment(ce.getAlignmentStr());
		Alignment ramap = new Alignment(ce.getReverseAlignmentStr());
		// System.err.println(amap.toConciseString());
		// System.err.println(ramap.toConciseString());

		ParseTreeNode stree = null, stree2 = null;
		if (lexicalization_mode == 0) {
			stree = ParseTree.buildTree(ce.getSParse(), lexicalization_mode);
			stree2 = ParseTree.buildTree(ce.getSParse(), lexicalization_mode);
		} else {
			// Lexicalized Parse tree - head annotation
			stree = ParseTree.buildLexTree(ce.getSParse(), lexicalization_mode);
			stree2 = ParseTree.buildLexTree(ce.getSParse(), lexicalization_mode);
		}

		// Compute more contextual info for the trees
		if (context_features == 1) {
			ParseTree.annotateLeftContext(stree);
			ParseTree.annotateRightContext(stree);
		}
		// Parse tree - no annotation
		ParseTreeNode ttree = ParseTree.buildTree(ce.getTParse(), 0);
		ParseTreeNode ttree2 = ParseTree.buildTree(ce.getTParse(), 0);

		if (stree.sEnd != ce.sSeq.size() || ttree.sEnd != ce.tSeq.size()) {
			String sp = ParseTree.getString(stree);
			String tp = ParseTree.getString(ttree);
			Vector<String> spSeq = new Vector<String>();
			Vector<String> tpSeq = new Vector<String>();

			StringTokenizer st = new StringTokenizer(sp);
			while (st.hasMoreTokens()) {
				spSeq.add(st.nextToken());
			}
			st = new StringTokenizer(tp);
			while (st.hasMoreTokens()) {
				tpSeq.add(st.nextToken());
			}
			System.out.println("S:" + ce.sSeq + ":" + ce.sSeq.size());
			System.out.println("SP:" + spSeq + ":" + spSeq.size() + "-" + stree.sEnd);
			System.out.println("T:" + ce.tSeq + ":" + ce.tSeq.size());
			System.out.println("TP:" + tpSeq + ":" + tpSeq.size() + "-" + ttree.sEnd);

			// String stp = ParseTree.treeString(stree);
			// System.out.println("STP:"+stp);
			System.err.println(sennum + ": Mismatch in parse and sentence. Skipping !");
			return;
		}
		if (amap.maxX > stree.sEnd || amap.maxY > ttree.sEnd) {
			System.err.println(sennum
					+ ": Mismatch in alignment info and sentence lengths. Skipping !");
			return;
		}

		// Align Tree to Trees
		AlignerT2TSBitset.calculateSpans(stree, ttree, ce.getS(), ce.getT(), amap);
		AlignerT2TSBitset.calculateSpans(ttree2, stree2, ce.getT(), ce.getS(), ramap);

		// Maintain hashtables so as not to DOUBLE count in the UNION
		Hashtable<String, Rule> gra = new Hashtable<String, Rule>();
		Hashtable<String, Rule> phr = new Hashtable<String, Rule>();
		Hashtable<String, Rule> lex = new Hashtable<String, Rule>();

		if (output_mode.equals("S2S")) {
			boolean REVERSE_FLAG = false; // Source to Target rules in the right
											// direction
			int rc1 =
					RuleExtractorS2S_Union.extractRules(stree, mywriter, ce.sSeq, ce.tSeq, sennum,
							0, amap, gra, phr, lex, REVERSE_FLAG);
			rulecount_dir1 += rc1;
			// Target to Source rules, so reverse them
			REVERSE_FLAG = true;
			int rc2 =
					RuleExtractorS2S_Union.extractRules(ttree2, mywriter, ce.tSeq, ce.sSeq, sennum,
							0, ramap, gra, phr, lex, REVERSE_FLAG);
			rulecount_dir2 += rc2;

			rulecount += rc1;
			rulecount += rc2;

			rulecount_union += RuleExtractorS2S_Union.flushRules(mywriter, gra, phr, lex);
		} else if (output_mode.equals("T2S")) {
			// TODO: Make sure you do not double count it
			rulecount =
					RuleExtractorT2S.extractRules(stree, mywriter, ce.sSeq, ce.tSeq, sennum,
							rulecount);
			rulecount =
					RuleExtractorT2S.extractRules(ttree, mywriter, ce.tSeq, ce.sSeq, sennum,
							rulecount);
		} else {
			System.out.println("Output mode can not be" + output_mode);
			System.exit(0);
		}
	}

}
