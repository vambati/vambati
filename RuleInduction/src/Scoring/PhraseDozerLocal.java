package Scoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import Scoring.ScoreableRule.Type;
import Scoring.cycles.UnaryCycleRemover;
import Scoring.extract.ChaskiLib;
import Scoring.stupidrules.ExtractFrequentWordsFromLM;
import Scoring.stupidrules.ExtractFrequentWordsFromLM.ScoredUnigram;
import Utils.MyUtils;
import chaski.utils.lexicon.LexiconTable;

public class PhraseDozerLocal {

	private static final float PROB_SPACE_FLOOR = 10E-10f;

	public static void main(String[] args) throws Exception {

		PhraseDozerLocal.printHeader();

		if (args.length == 1 && args[0].equalsIgnoreCase("-v")) {
			System.exit(0);
		}

		if (args.length < 1) {
			System.err.println("Usage: program <task>");
			System.err.println("Tasks: ");
			System.err.println("addInstanceFeatures\t[phraseCount] [glueCount] [typeFeatures] [freqFeatures]");
			System.err.println("addChaskiLexProbs\tlexF2N lexN2F");
			System.err.println("addLexProbs\tlexPtgs lexPsgt | addLexProbs lexCombined");
			System.err.println("removeCycles\tgrammarIn grammarOut cyclesOut");
			System.err.println("removeStupidRules\tgrammarIn grammarOut stupidOut lmFile nMostFrequentWords");
			System.err.println("findNbestUnigramsInLM\tlmIn n unigramsOut");
			System.err.println("formatForJoshua\tgrammarIn grammarOut");
			System.err.println("formatForMoses\ttmIn tmOut");
			System.err.println("formatForCdec\tgrammarIn suffStatsIn featureNamesIn tmOut glueOut");
			System.exit(1);
		}

		String task = args[0];
		if (task.equalsIgnoreCase("addinstancefeatures")) {
			addInstanceFeatures(args);
		} else if (task.equalsIgnoreCase("addchaskilexprobs")) {
			addChaskiLexProbs(args);
		} else if (task.equalsIgnoreCase("addlexprobs")) {
			addLexProbs(args);
		} else if (task.equalsIgnoreCase("removeCycles")) {
			removeCycles(args);
		} else if (task.equalsIgnoreCase("removeStupidRules")) {
			removeStupidRules(args);
		} else if (task.equalsIgnoreCase("findNbestUnigramsInLM")) {
			findNbestUnigramsInLM(args);
		} else if (task.equalsIgnoreCase("formatForJoshua")) {
			formatForJoshua(args);
		} else if (task.equalsIgnoreCase("formatForMoses")) {
			formatForMoses(args);
		} else if (task.equalsIgnoreCase("formatForCdec")) {
			formatForCdec(args);
		} else {
			System.err.println("Unrecognized task: " + task);
			System.exit(1);
		}
	}

	private static void findNbestUnigramsInLM(String[] args) throws FileNotFoundException,
			IOException {

		if (args.length != 4) {
			System.err.println("Usage: program findNbestUnigramsInLM lmIn n stopwordsOut");
			System.exit(1);
		}

		System.err.println("Finding n-best unigrams in LM...");

		String lmFile = args[1];
		int nMostFrequentWords = Integer.parseInt(args[2]);
		String outFile = args[3];

		Iterable<ScoredUnigram> mostFrequentWords =
				ExtractFrequentWordsFromLM.extract(new FileInputStream(lmFile), nMostFrequentWords);
		PrintWriter out = new PrintWriter(outFile);
		for (ScoredUnigram u : mostFrequentWords) {
			out.println(u.word);
		}
		out.close();
	}

	private static void formatForJoshua(String[] args) throws IOException, RuleException {
		if (args.length != 3) {
			System.err.println("Usage: program formatForJoshua grammarIn grammarOut");
			System.exit(1);
		}

		System.err.println("Formatting for Joshua...");

		String grammarIn = args[1];
		String grammarOut = args[2];

		BufferedReader in = new BufferedReader(new FileReader(grammarIn));
		PrintWriter out = new PrintWriter(grammarOut);
		String line;
		while ((line = in.readLine()) != null) {
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(line);
			out.println(rule.toJoshuaString());
		}
		out.close();
		in.close();
	}

	private static void formatForCdec(String[] args) throws IOException, RuleException {
		if (args.length != 6) {
			System.err.println("Usage: program formatForCdec grammarIn suffStatsIn featureNamesIn tmOut glueOut");
			System.exit(1);
		}

		System.err.println("Formatting for Cdec...");

		String grammarIn = args[1];
		// String suffStatsIn = args[2];
		// String featureNamesIn = args[3];
		String grammarOut = args[4];
		String glueOut = args[5];

		FeatureManager fman = readFeatureInfo(args, 2, false);
		
		if(!fman.featIndexManager.indices.contains(FeatureManager.GLUE_COUNT)) {
			throw new RuntimeException("GLUE_COUNT feature not found in featureNames");
		}

		Set<String> glueables = new HashSet<String>();

		BufferedReader in = new BufferedReader(new FileReader(grammarIn));
		PrintWriter out = new PrintWriter(grammarOut);
		String line;
		while ((line = in.readLine()) != null) {
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(line);
			glueables.add(rule.consequent);
			out.println(rule.toCdecString(fman));
		}
		out.close();
		in.close();

		String suffStats = "";
		
		String glueFeatures = makeGlueFeatureString(fman, true);
		String allZeroFeatures = makeGlueFeatureString(fman, false);

		// generate glue grammar
		out = new PrintWriter(glueOut);
		for (String glueable : glueables) {
			
			String label = ScoreableRule.getLabelfromConsequent(glueable);
			
			ScoreableRule promotionRule =
					new ScoreableRule("G", "[S]", "[" + label + ",1]", "["
							+ label + ",1]", allZeroFeatures, "0-0", suffStats);
			out.println(promotionRule.toCdecString(fman));

			ScoreableRule glueRule =
					new ScoreableRule("G", "[S]", "[S,1] [" + label + ",2]", "[S,1] ["
							+ label + ",2]", glueFeatures, "0-0 1-1", suffStats);
			out.println(glueRule.toCdecString(fman));
		}
		out.close();
	}

	private static String makeGlueFeatureString(FeatureManager fman, boolean setGlueIndicator) {
		StringBuilder featureBuilder = new StringBuilder();
		for(String name : fman.featIndexManager.indices) {
			if(setGlueIndicator && name.equals(FeatureManager.GLUE_COUNT)) {
				featureBuilder.append("1 ");
			} else {
				featureBuilder.append("0 ");
			}
		}
		String features = featureBuilder.toString().trim();
		return features;
	}

	private static void formatForMoses(String[] args) throws IOException, RuleException {
		if (args.length != 3) {
			System.err.println("Usage: program formatForMoses tmIn tmOut");
			System.exit(1);
		}

		System.err.println("Formatting for Moses...");

		String grammarIn = args[1];
		String grammarOut = args[2];

		BufferedReader in = new BufferedReader(new FileReader(grammarIn));
		PrintWriter out = new PrintWriter(grammarOut);
		String line;
		while ((line = in.readLine()) != null) {
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(line);
			if (rule.type == Type.PHRASE) {
				// toMosesString handles conversion from log space into prob
				// space
				out.println(rule.toMosesString());
			}
		}
		out.close();
		in.close();
	}

	private static void removeStupidRules(String[] args) throws IOException, RuleException {
		if (args.length != 6) {
			System.err.println("Usage: program removeStupidRules grammarIn grammarOut stupidOut lmFile nMostFrequentWords");
			System.exit(1);
		}

		String grammarIn = args[1];
		String grammarOut = args[2];
		String stupidOut = args[3];
		String lmFile = args[4];
		int nMostFrequentWords = Integer.parseInt(args[5]);

		// first, construct graph of unary rules
		System.err.println("Extracting n most frequent words from LM unigrams...");
		Iterable<ScoredUnigram> mostFrequentWords =
				ExtractFrequentWordsFromLM.extract(new FileInputStream(lmFile), nMostFrequentWords);
		Set<String> wordsAllowedToHallucinate = new HashSet<String>();
		for (ScoredUnigram unigram : mostFrequentWords) {
			wordsAllowedToHallucinate.add(unigram.word);
			System.out.println(unigram.word + " " + unigram.score);
		}

		System.err.println("Filtering stupid rules from grammar...");

		BufferedReader in = new BufferedReader(new FileReader(grammarIn));
		PrintWriter out = new PrintWriter(grammarOut);
		PrintWriter badOut = new PrintWriter(stupidOut);
		int nRemoved = 0;
		String line;
		while ((line = in.readLine()) != null) {
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(line);
			boolean stupid = false;
			if (FeatureManager.isAbstract(rule.getSourceAntecedents())) {
				for (String tgt : rule.getTargetAntecedents()) {
					if (ScoreableRule.isNonterminal(tgt) == false
							&& wordsAllowedToHallucinate.contains(tgt) == false) {
						stupid = true;
						break;
					}
				}
			}
			if (stupid) {
				nRemoved++;
				badOut.println("removed." + nRemoved + "\t" + rule.toHadoopRecordString());
			} else {
				out.println(rule.toHadoopRecordString());
			}
		}
		badOut.println("CountRemoved\t" + nRemoved);
		out.close();
		badOut.close();
		in.close();
	}

	private static void removeCycles(String[] args) throws IOException, RuleException {

		if (args.length != 8) {
			System.err.println("Usage: program removeCycles grammarIn grammarOut cyclesOut statsIn featsIn statsOut featsOut");
			System.exit(1);
		}

		String grammarIn = args[1];
		String grammarOut = args[2];
		String cyclesOut = args[3];

		final int firstIndex = 4;
		FeatureManager fman = PhraseDozerLocal.readFeatureInfo(args, firstIndex);

		UnaryCycleRemover cycleRemover = new UnaryCycleRemover(fman);

		// first, construct graph of unary rules
		System.err.println("Building directed graph of unary rules...");
		BufferedReader in = new BufferedReader(new FileReader(grammarIn));
		String line;
		while ((line = in.readLine()) != null) {
			if (line.trim().equals("")) {
				continue;
			}
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(line);
			try {
				if (rule.getSourceAntecedents().length == 1
						&& ScoreableRule.isNonterminal(rule.srcAntecedents)) {
					cycleRemover.addUnaryRule(rule);
				}
			} catch (RuleException e) {
				System.err.println("Error for line: " + rule.toHadoopRecordString());
				throw e;
			}
		}
		in.close();

		// determine which rules to remove
		System.err.println("Finding rules causing unary cycles...");
		Set<ScoreableRule> rulesToRemove = cycleRemover.getRulesForRemoval();

		// now remove rules
		System.err.println("Writing cycle-free grammar...");
		in = new BufferedReader(new FileReader(grammarIn));
		PrintWriter out = new PrintWriter(grammarOut);
		PrintWriter badOut = new PrintWriter(cyclesOut);
		int nRemoved = 0;
		while ((line = in.readLine()) != null) {
			if (line.trim().equals("")) {
				continue;
			}
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(line);
			if (rulesToRemove.contains(rule)) {
				nRemoved++;
				badOut.println("removed." + nRemoved + "\t" + rule.toHadoopRecordString());
			} else {
				out.println(rule.toHadoopRecordString());
			}
		}
		badOut.println("CountRemoved\t" + nRemoved);
		badOut.close();
		out.close();
		in.close();

		PhraseDozerLocal.writeFeatureInfo(args, fman, firstIndex);
	}

	public static void addLexProbs(String[] args) throws NumberFormatException,
			FileNotFoundException, IOException, RuleException {

		final Lexicon lexicon;
		final FeatureManager fman;
		final int firstIndex;
		if (args.length == 7) {
			System.err.println("Reading lexicons...");
			String ptgs = args[1];
			String psgt = args[2];
			lexicon =
					new Lexicon(new FileInputStream(ptgs), new FileInputStream(psgt),
							PROB_SPACE_FLOOR);

			firstIndex = 3;
			fman = PhraseDozerLocal.readFeatureInfo(args, firstIndex);

		} else if (args.length == 6) {
			System.err.println("Reading lexicons...");
			String lexCombined = args[1];
			lexicon = new Lexicon(new FileInputStream(lexCombined), PROB_SPACE_FLOOR);

			firstIndex = 2;
			fman = PhraseDozerLocal.readFeatureInfo(args, firstIndex);

		} else {
			System.err.println("Usage: program addLexProbs (lexPtgs lexPsgt | addLexProbs) lexCombined suffStatNamesIn featureNamesIn suffStatNamesOut featureNamesOut");
			System.exit(1);
			throw new Error();
		}

		System.err.println("Adding features...");
		fman.featIndexManager.add(FeatureManager.TGS_LEXICAL);
		fman.featIndexManager.add(FeatureManager.SGT_LEXICAL);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		int i = 1;
		while ((line = in.readLine()) != null) {
			if (i % 100000 == 0) {
				System.err.println("Read " + i + " lines so far...");
			}
			if (line.trim().equals("")) {
				continue;
			}
			try {
				ScoreableRule rule = ScoreableRule.parseHadoopRecord(line);
				fman.addLexicalProbabilities(rule, lexicon, true);
				System.out.println(rule.toHadoopRecordString());
				i++;
			} catch (Exception e) {
				System.err.println("Error for line: " + line);
				throw new RuntimeException(e);
			}
		}
		in.close();

		PhraseDozerLocal.writeFeatureInfo(args, fman, firstIndex);
	}

	public static void addChaskiLexProbs(String[] args) throws NumberFormatException,
			FileNotFoundException, IOException, RuleException {

		if (args.length != 3) {
			System.err.println("Usage: program addChaskiLexProbs lexF2N lexN2F");
			System.exit(1);
		}

		String f2n = args[1];
		String n2f = args[2];

		final int firstIndex = 3;
		FeatureManager fman = PhraseDozerLocal.readFeatureInfo(args, firstIndex);

		System.err.println("Reading lexicons...");
		LexiconTable lexicon =
				ChaskiLib.readLexicon(new FileInputStream(f2n), new FileInputStream(n2f));

		System.err.println("Adding features...");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		int i = 1;
		while ((line = in.readLine()) != null) {
			if (i % 100000 == 0) {
				System.err.println("Read " + i + " lines so far...");
			}
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(line);
			fman.addChaskiLexicalProbabilities(rule, lexicon);
			System.out.println(rule.toHadoopRecordString());
			i++;
		}
		in.close();

		PhraseDozerLocal.writeFeatureInfo(args, fman, firstIndex);
	}

	private static void addInstanceFeatures(String[] args) throws IOException, RuleException {

		boolean blank = true;
		boolean phraseCount = false;
		boolean glueCount = false;
		boolean typeFeatures = false;
		boolean freqFeatures = false;
		boolean unalignedFeatures = false;
		boolean arityFeatures = false;
		boolean phraseBalance = false;

		float expectedRatio = 0;
		float maxRatioDiff = 0;
		int minCount = 0;

		final int firstIndex = 1;
		FeatureManager fman = PhraseDozerLocal.readFeatureInfo(args, firstIndex);

		System.err.println("Adding instance features...");

		for (int i = 5; i < args.length; i++) {
			String arg = args[i];
			if (arg.equalsIgnoreCase("blank")) {
				blank = true;
			} else if (arg.equalsIgnoreCase("phrasecount")) {
				phraseCount = true;
			} else if (arg.equalsIgnoreCase("gluecount")) {
				glueCount = true;
			} else if (arg.equalsIgnoreCase("typefeatures")) {
				typeFeatures = true;
			} else if (arg.equalsIgnoreCase("freqfeatures")) {
				freqFeatures = true;
			} else if (arg.equalsIgnoreCase("unaligned")) {
				unalignedFeatures = true;
			} else if (arg.equalsIgnoreCase("arity")) {
				arityFeatures = true;
			} else if (arg.toLowerCase().startsWith("phrasebalance")) {
				phraseBalance = true;
				// phrasebalance_tgtOverSrc=1.0_maxDiff=2.0_minCount=2
				String[] balanceArgs = arg.split("_");
				if (balanceArgs.length != 4) {
					printPhraseBalanceUsageAndExit();
				}
				String[] ratio = balanceArgs[1].split("=");
				String[] diff = balanceArgs[2].split("=");
				String[] count = balanceArgs[3].split("=");
				if (!ratio[0].equals("tgtOverSrc") || !diff[0].equals("maxDiff")
						|| !count[0].equals("minCount")) {
					printPhraseBalanceUsageAndExit();
				}

				expectedRatio = Float.parseFloat(ratio[1]);
				maxRatioDiff = Float.parseFloat(diff[1]);
				minCount = Integer.parseInt(count[1]);

			} else {
				System.err.println("Unrecognized feature: " + arg);
				System.exit(1);
			}
		}

		if (blank) {
			fman.featIndexManager.add(FeatureManager.BLANK);
		}
		if (phraseCount) {
			fman.featIndexManager.add(FeatureManager.PHRASE_PENALTY);
		}
		if (typeFeatures) {
			fman.featIndexManager.add(FeatureManager.TYPE_SRC_ABSTRACT);
			fman.featIndexManager.add(FeatureManager.TYPE_TGT_ABSTRACT);
			fman.featIndexManager.add(FeatureManager.TYPE_PHRASE_PAIR);
			fman.featIndexManager.add(FeatureManager.TYPE_TGT_INSERTION);
			fman.featIndexManager.add(FeatureManager.TYPE_SRC_DELETION);
			fman.featIndexManager.add(FeatureManager.TYPE_INTERLEAVED_RULE);
			fman.featIndexManager.add(FeatureManager.TYPE_LINGUISTIC);
		}
		final int[] COUNTS = new int[] { 1, 2, 3 };
		if (freqFeatures) {
			fman.addLowCountFeatureNamesToManager(COUNTS, COUNTS);
		}
		// skip arity zero since this is covered by the isPhrasePair feature
		final int[] ARITIES = new int[] { 1, 2, 3, 4 };
		if (arityFeatures) {
			fman.addArityFeatureNamesToManager(ARITIES, ARITIES);
		}
		if (unalignedFeatures) {
			fman.featIndexManager.add(FeatureManager.SOURCE_UNALIGNED);
			fman.featIndexManager.add(FeatureManager.TARGET_UNALIGNED);
			fman.featIndexManager.add(FeatureManager.SOURCE_UNALIGNED_BOUNDARY);
			fman.featIndexManager.add(FeatureManager.TARGET_UNALIGNED_BOUNDARY);
		}
		if (phraseBalance) {
			// TODO: Produce non-overlapping name using params
			fman.featIndexManager.add(FeatureManager.PHRASE_BALANCE);
		}
		if (glueCount) {
			fman.featIndexManager.add(FeatureManager.GLUE_COUNT);
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = in.readLine()) != null) {
			if (line.trim().equals("")) {
				continue;
			}

			// TODO: add() must now be called in feature manager -- for lex
			// probs too!
			ScoreableRule rule = ScoreableRule.parseHadoopRecord(line);
			if (phraseCount) {
				fman.addPhraseCount(rule);
			}
			if (typeFeatures) {
				fman.addRuleTypeFeatures(rule);
			}
			if (freqFeatures) {
				fman.addLowCountsFeature(rule, COUNTS, COUNTS);
			}
			if (unalignedFeatures) {
				fman.addUnalignedWordsFeatures(rule);
			}
			if (arityFeatures) {
				fman.addArityFeature(rule, ARITIES, ARITIES);
			}
			if (phraseBalance) {
				fman.addPhraseBalance(rule, expectedRatio, maxRatioDiff, minCount);
			}
			if (glueCount) {
				fman.addGlueCount(rule);
			}
			System.out.println(rule.toHadoopRecordString());
		}
		in.close();

		PhraseDozerLocal.writeFeatureInfo(args, fman, firstIndex);
	}

	private static void printPhraseBalanceUsageAndExit() {
		System.err.println("Format of phrasebalance is phrasebalance_tgtOverSrc=X_maxDiff=Y_minCount=Z");
		System.exit(1);
	}

	public static void printHeader() {
		System.err.println(PhraseDozer.PROGRAM_NAME + ": Syntactic Phrase Table and Grammar Tools");
		System.err.println("by Jonathan Clark");
		System.err.println(PhraseDozer.VERSION);
		System.err.println(PhraseDozer.DATE);
		System.err.println("Carnegie Mellon University -- Language Technologies Institute");
		System.err.println();
	}

	public static FeatureManager readFeatureInfo(String[] args, int firstIndex)
			throws FileNotFoundException, IOException {
		return readFeatureInfo(args, firstIndex, true);
	}

	public static FeatureManager readFeatureInfo(String[] args, int firstIndex, boolean requireOuts)
			throws FileNotFoundException, IOException {

		if (requireOuts) {
			if (args.length < firstIndex + 4) {
				System.err.println("Task requires arguments at indices " + firstIndex + "-"
						+ (firstIndex + 4) + "to be: statsIn featsIn statsOut featsOut");
				System.exit(1);
			}
		} else {
			if (args.length < firstIndex + 2) {
				System.err.println("Task requires arguments at indices " + firstIndex + "-"
						+ (firstIndex + 2) + "to be: statsIn featsIn");
				System.exit(1);
			}
		}

		String serializedStatsIn = args[firstIndex + 0];
		String serializedFeatsIn = args[firstIndex + 1];

		String serializedStats = MyUtils.readLine(new File(serializedStatsIn));
		String serializedFeats = MyUtils.readLine(new File(serializedFeatsIn));

		if (serializedStats == null) {
			throw new RuntimeException("Blank serializedStats file");
		}
		if (serializedFeats == null) {
			throw new RuntimeException("Blank serializedFeats file");
		}

		FeatureManager fman = new FeatureManager(serializedStats, serializedFeats);
		return fman;
	}

	public static void writeFeatureInfo(String[] args, FeatureManager fman, int firstIndex)
			throws FileNotFoundException {
		String serializedStatsOut = args[firstIndex + 2];
		String serializedFeatsOut = args[firstIndex + 3];
		fman.statIndexManager.writeFile(new File(serializedStatsOut));
		fman.featIndexManager.writeFile(new File(serializedFeatsOut));
	}
}
