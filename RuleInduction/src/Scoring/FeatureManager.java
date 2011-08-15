package Scoring;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Scoring.ScoreableRule.Type;
import Scoring.extract.ChaskiLib;
import chaski.utils.Pair;
import chaski.utils.lexicon.LexiconTable;

public class FeatureManager {

	public static final float FLOOR = (float) Math.log(0.0000001);

	// public static final String TGS_PHRASE = "TGS_PHRASE";
	// public static final String SGT_PHRASE = "SGT_PHRASE";
	// public static final String NONSYNTACTIC_SGT_PHRASE =
	// "NONSYNTACTIC_SGT_PHRASE";
	// public static final String NONSYNTACTIC_TGS_PHRASE =
	// "NONSYNTACTIC_TGS_PHRASE";
	// public static final String SHARED_STGC_PHRASE = "SHARED_STGC_PHRASE";
	// public static final String SHARED_CTGS_PHRASE = "SHARED_CTGS_PHRASE";
	// public static final String SHARED_CGS = "SHARED_CGS";
	// public static final String SHARED_CGT = "SHARED_CGT";
	// public static final String SHARED_TGC = "SHARED_TGC";

	public static final String PHRASE_PENALTY = "PHRASE_PENALTY";
	public static final String FREQ = "FREQ";
	public static final String ARITY = "ARITY";
	public static final String PHRASE_BALANCE = "PHRASE_BALANCE";
	public static final String TGS_LEXICAL = "TGS_LEXICAL";
	public static final String SGT_LEXICAL = "SGT_LEXICAL";
	public static final String SOURCE_UNALIGNED = "SOURCE_UNALIGNED";
	public static final String TARGET_UNALIGNED = "TARGET_UNALIGNED";
	public static final String SOURCE_UNALIGNED_BOUNDARY = "SOURCE_UNALIGNED_BOUNDARY";
	public static final String TARGET_UNALIGNED_BOUNDARY = "TARGET_UNALIGNED_BOUNDARY";
	public static final String TYPE_SRC_ABSTRACT = "TYPE_SRC_ABSTRACT";
	public static final String TYPE_TGT_ABSTRACT = "TYPE_TGT_ABSTRACT";
	public static final String TYPE_PHRASE_PAIR = "TYPE_PHRASE_PAIR";
	public static final String TYPE_TGT_INSERTION = "TYPE_TGT_INSERTION";
	public static final String TYPE_SRC_DELETION = "TYPE_SRC_DELETION";
	public static final String TYPE_INTERLEAVED_RULE = "TYPE_INTERLEAVED_RULE";
	public static final String TYPE_LINGUISTIC = "TYPE_LINGUISTIC";
	public static final String GLUE_COUNT = "GLUE_COUNT";
	public static final String MONOTONE = "MONOTONE";
	public static final String BLANK = "BLANK";

	// static {
	// addFeature(TGS_PHRASE);
	// addFeature(SGT_PHRASE);
	// addFeature(PHRASE_PENALTY);
	// addFeature(FREQ);
	// addFeature(TGS_LEXICAL);
	// addFeature(SGT_LEXICAL);
	// addFeature(GLUE_COUNT);
	// }

	public final IndexManager statIndexManager;
	public final IndexManager featIndexManager;
	public static final String NO_FEATURES = "_";

	public FeatureManager(String serializedStats, String serializedFeats) {

		// HADOOP HACK: Hadoop does not send the empty string properly in config
		// files
		if (serializedFeats.equals(NO_FEATURES)) {
			serializedFeats = "";
		}

		statIndexManager = IndexManager.readLine(serializedStats);
		featIndexManager = IndexManager.readLine(serializedFeats);

		int countIndex = statIndexManager.get(ScoreableRule.COUNT);
		if (countIndex != 0) {
			throw new RuntimeException("COUNT must always be first sufficient statistic");
		}
	}

	public static boolean isAbstract(String[] antecedents) {
		for (String word : antecedents) {
			if (!ScoreableRule.isNonterminal(word)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isPhrase(ScoreableRule rule) {
		// for (String word : antecedents) {
		// if (ScoreableRule.isNonterminal(word)) {
		// return false;
		// }
		// }
		// return true;
		return rule.type == Type.PHRASE;
	}

	public void addLowCountFeatureNamesToManager(int[] startRanges, int[] endRanges) {
		for (int i = 0; i < startRanges.length; i++) {
			String name = getCountFeatureName(startRanges[i], endRanges[i]);
			this.featIndexManager.add(name);
		}
	}

	public void addArityFeatureNamesToManager(int[] startRanges, int[] endRanges) {
		for (int i = 0; i < startRanges.length; i++) {
			String name = getArityFeatureName(startRanges[i], endRanges[i]);
			this.featIndexManager.add(name);
		}
	}

	public void addArityFeature(ScoreableRule rule, int[] startRanges, int[] endRanges)
			throws RuleException {

		assert startRanges.length == endRanges.length;
		int arity = rule.getNonterminalCount();

		for (int i = 0; i < startRanges.length; i++) {
			String name = getArityFeatureName(startRanges[i], endRanges[i]);
			final float value;
			if (arity >= startRanges[i] && arity <= endRanges[i]) {
				value = 1.0f;
			} else {
				value = 0.0f;
			}

			rule.setFeature(name, value, this);
		}
	}

	private String getArityFeatureName(int start, int end) {
		if (start == end) {
			return ARITY + "_" + start;
		} else {
			return ARITY + "_" + start + "_" + end;
		}
	}

	public void addLowCountsFeature(ScoreableRule rule, int[] startRanges, int[] endRanges)
			throws RuleException {

		assert startRanges.length == endRanges.length;
		float count = rule.getCount(this);

		for (int i = 0; i < startRanges.length; i++) {
			String name = getCountFeatureName(startRanges[i], endRanges[i]);
			final float value;
			if (count >= startRanges[i] && count <= endRanges[i]) {
				value = 1.0f;
			} else {
				value = 0.0f;
			}

			rule.setFeature(name, value, this);
		}
	}

	private String getCountFeatureName(int start, int end) {
		if (start == end) {
			return FREQ + "_" + start;
		} else {
			return FREQ + "_" + start + "_" + end;
		}
	}

	public void addRuleTypeFeatures(ScoreableRule rule) throws RuleException {

		float srcAbstract = isAbstract(rule.getSourceAntecedents()) ? 1.0f : 0.0f;
		float tgtAbstract = isAbstract(rule.getTargetAntecedents()) ? 1.0f : 0.0f;
		float isPhrasePair = isPhrase(rule) ? 1.0f : 0.0f;
		float tgtInsertion = (srcAbstract == 1.0 && tgtAbstract == 0.0) ? 1.0f : 0.0f;
		float srcDeletion = (srcAbstract == 0.0 && tgtAbstract == 1.0 ? 1.0f : 0.0f);
		float interleavedRule =
				(srcAbstract == 0.0 && tgtAbstract == 0.0 && isPhrasePair == 0.0) ? 1.0f : 0.0f;
		float isLinguistic = ScoreableRule.isSyntactic(rule) ? 1.0f : 0.0f;

		rule.setFeature(TYPE_SRC_ABSTRACT, srcAbstract, this);
		rule.setFeature(TYPE_TGT_ABSTRACT, tgtAbstract, this);
		rule.setFeature(TYPE_PHRASE_PAIR, isPhrasePair, this);
		rule.setFeature(TYPE_TGT_INSERTION, tgtInsertion, this);
		rule.setFeature(TYPE_SRC_DELETION, srcDeletion, this);
		rule.setFeature(TYPE_INTERLEAVED_RULE, interleavedRule, this);
		rule.setFeature(TYPE_LINGUISTIC, isLinguistic, this);
	}

	public void addMonotoneFeature(ScoreableRule rule) throws RuleException {

		List<Integer> alignment = rule.getNonterminalAlignment();
		if (rule.type == Type.GRAMMAR) {

			int prev = alignment.get(0);
			boolean monotone = true;
			for (int i = 1; i < alignment.size(); i++) {
				int current = alignment.get(i);
				if (prev != current) {
					monotone = false;
					break;
				}
				prev = current;
			}

			float value = monotone ? 1.0f : 0.0f;
			rule.setFeature(MONOTONE, value, this);
		} else {
			rule.setFeature(MONOTONE, 0.0f, this);
		}
	}
	
	private static float oneOver(double val) {
		return (float) (1.0 / val);
	}

	public void addUnalignedWordsFeatures(ScoreableRule rule) throws RuleException {

		WordAlignment alignment = rule.getAlignment();

		int unalignedSrc = countUnalignedWords(rule.getSourceAntecedents().length, alignment, true);
		int unalignedTgt =
				countUnalignedWords(rule.getTargetAntecedents().length, alignment, false);
		int unalignedSrcBoundary =
				countUnalignedBoundaryWords(rule.getSourceAntecedents().length, alignment, true);
		int unalignedTgtBoundary =
				countUnalignedBoundaryWords(rule.getTargetAntecedents().length, alignment, false);
		
		rule.setFeature(SOURCE_UNALIGNED, oneOver(unalignedSrc), this);
		rule.setFeature(TARGET_UNALIGNED, oneOver(unalignedTgt), this);
		rule.setFeature(SOURCE_UNALIGNED_BOUNDARY, oneOver(unalignedSrcBoundary), this);
		rule.setFeature(TARGET_UNALIGNED_BOUNDARY, oneOver(unalignedTgtBoundary), this);
	}

	private int countUnalignedBoundaryWords(int nWords, WordAlignment alignment, boolean isFtoE) {
		List<Pair<Integer, Float>> alignedLeft = alignment.getWordsAlignedTo(0, isFtoE);
		List<Pair<Integer, Float>> alignedRight = alignment.getWordsAlignedTo(nWords - 1, isFtoE);
		int nUnalignedLeft = (alignedLeft.size() == 0) ? 1 : 0;
		int nUnalignedRight = (alignedRight.size() == 0) ? 1 : 0;
		return nUnalignedLeft + nUnalignedRight;
	}

	private int countUnalignedWords(int nWords, WordAlignment alignment, boolean isFtoE) {
		int nUnaligned = 0;
		for (int i = 0; i < nWords; i++) {
			List<Pair<Integer, Float>> aligned = alignment.getWordsAlignedTo(i, isFtoE);
			if (aligned.size() == 0) {
				nUnaligned++;
			}
		}
		return nUnaligned;
	}

	public void addChaskiLexicalProbabilities(ScoreableRule rule, LexiconTable lexicon)
			throws RuleException {

		Pair<Double, Double> lexProbs =
				ChaskiLib.getAlignmentScore(rule.alignment, rule.srcAntecedents,
						rule.tgtAntecedents, lexicon);

		double n2fScore = lexProbs.getFirst().doubleValue(); // P(f|e) = P(s|t)
		double f2nScore = lexProbs.getSecond().doubleValue(); // P(e|f) = P(t|s)

		float tgsLogProb = (float) -Math.log10(f2nScore);
		float sgtLogProb = (float) -Math.log10(n2fScore);

		rule.setFeature(TGS_LEXICAL, tgsLogProb, this);
		rule.setFeature(SGT_LEXICAL, sgtLogProb, this);
	}

	// if one direction is null, that direction will not be scored
	// <lexF2N> has lines \"f e p(e|f)\"; <lexN2F> has lines \"e f p(f|e)\".
	// lexF2N's outer hash is keyed on f, inner hash is keyed on e
	//
	// includeNull is only meaningful for useAlignments -- if true, add the sum
	// of X->NULL to each lex prob
	public void addLexicalProbabilities(ScoreableRule rule, Lexicon lex, boolean useAlignments)
			throws LexiconException, RuleException {

		String[] srcWords = rule.getSourceAntecedents();
		String[] tgtWords = rule.getTargetAntecedents();

		float sgtLex;
		float tgsLex;

		if (useAlignments) {

			WordAlignment alignment = rule.getAlignment();
			sgtLex = calcLogLexScore(srcWords, tgtWords, alignment, lex, true);
			tgsLex = calcLogLexScore(tgtWords, srcWords, alignment, lex, false);

		} else {

			throw new Error("Needs updating.");

			// // iterate over all src/tgt words PLUS NULL
			// float[] sgtSums = new float[srcWords.length + 1];
			// float[] tgsSums = new float[tgtWords.length + 1];
			//
			// // Look up and total the lexical translation probabilities;
			// // NULL is included on both sides, but not NULL--NULL alignments:
			// for (int i = 0; i < srcWords.length + 1; i++) {
			// String s = i < srcWords.length ? srcWords[i] : "NULL";
			//
			// for (int j = 0; j < tgtWords.length + 1; j++) {
			// String t = j < tgtWords.length ? tgtWords[j] : "NULL";
			//
			// if (s.equals("NULL") && t.equals("NULL")) {
			// continue;
			// }
			// updateLexProbSums(f2n, n2f, sgtSums, tgsSums, i, s, j, t);
			// }
			// }
			//
			// // Floor lexical probabilities in case nothing was found, but if
			// sgtLex = getLogProbCrossProductFromSums(sgtSums, includeNull);
			// tgsLex = getLogProbCrossProductFromSums(tgsSums, includeNull);
		}

		// it's a fully abstract grammar rule, give it 1 instead:
		if (isAbstract(rule.getSourceAntecedents())) {
			sgtLex = 0.0f;
		}
		if (isAbstract(rule.getTargetAntecedents())) {
			tgsLex = 0.0f;
		}

		rule.setFeature(TGS_LEXICAL, tgsLex, this);
		rule.setFeature(SGT_LEXICAL, sgtLex, this);
	}

	// c.p. line 249 in processPhrasePairs() in Philipp Koehn's phrase-score.cpp
	private static float calcLogLexScore(String[] srcWords, String[] tgtWords,
			WordAlignment alignment, Lexicon lex, boolean isSGT) throws LexiconException,
			RuleException {

		final String NULL = "NULL";

		// calculate p(s|t)
		double lexScore = 1.0;
		for (int i = 0; i < srcWords.length; i++) {
			String s = srcWords[i];

			if (ScoreableRule.isNonterminal(s) == false) {

				List<Pair<Integer, Float>> tWordsAlignedToS = alignment.getWordsAlignedTo(i, isSGT);

				if (tWordsAlignedToS.size() == 0) {
					// get p(s|null)
					lexScore *= lex.getProb(s, NULL, isSGT);
					ScoreableRule.checkProbSanity(lexScore);

				} else {
					float thisWordScore = 0.0f;
					float sumOccurrences = 0.0f;

					for (Pair<Integer, Float> entry : tWordsAlignedToS) {
						int j = entry.getFirst();
						try {
							String t = tgtWords[j];
							float occurrences = entry.getSecond();

							// get p(s|t)
							thisWordScore += lex.getProb(s, t, isSGT) * occurrences;
							sumOccurrences += occurrences;
						} catch (ArrayIndexOutOfBoundsException e) {
							throw new RuntimeException(Arrays.toString(srcWords) + " "
									+ Arrays.toString(tgtWords) + " " + alignment + " " + isSGT
									+ " " + i + "->" + j, e);
						}
					}
					lexScore *= thisWordScore / sumOccurrences;
					ScoreableRule.checkProbSanity(lexScore);
				}
			}

		}

		float result = (float) -Math.log10(lexScore);
		try {
			ScoreableRule.checkDoubleSanity(result);
		} catch (RuleException e) {
			throw new RuleException("Log of " + lexScore + " = " + result);
		}
		return result;
	}

	// <lexF2N> has lines \"f e p(e|f) = s t p(t|s)\"; <lexN2F> has lines \"e f
	// p(f|e) = t s p(s|t)\".
	// lexF2N's outer hash is keyed on f = s, inner hash is keyed on e = t
	private static void updateLexProbSums(Map<String, Map<String, Float>> f2n,
			Map<String, Map<String, Float>> n2f, float[] sgtSums, float[] tgsSums, int i, String s,
			int j, String t) {

		if (!ScoreableRule.isNonterminal(s) && !ScoreableRule.isNonterminal(t)) {

			if (n2f != null) {
				Map<String, Float> enDict = n2f.get(t);
				if (enDict != null) {
					Float fe = enDict.get(s);
					if (fe != null) {
						sgtSums[i] += fe;
					}
				}
			}

			if (f2n != null) {
				Map<String, Float> frDict = f2n.get(s);
				if (frDict != null) {
					Float ef = frDict.get(t);
					if (ef != null) {
						tgsSums[j] += ef;
					}
				}
			}
		}
	}

	private static float getLogProbAlignmentProductFromSums(float[] sums,
			Iterable<Integer> indicesToLogSum) {

		float lex = 0.0f;
		for (int i : indicesToLogSum) {
			if (sums[i] == 0.0) {
				lex += FLOOR;
			} else {
				lex += Math.log(sums[i]);
			}
		}
		return -lex;
	}

	private static float getLogProbCrossProductFromSums(float[] sums, boolean includeNull) {

		final int N = includeNull ? sums.length : sums.length - 1;

		float lex = 0.0f;
		for (int i = 0; i < N; i++) {
			if (sums[i] == 0.0) {
				lex += FLOOR;
			} else {
				lex += Math.log(sums[i]);
			}
		}
		return -lex;
	}

	public void addPhraseBalance(ScoreableRule rule, float expectedRatioTgtOverSrc,
			float maxDifference, int minCount) throws RuleException {

		int featValue = 0;
		
		int nSrc = rule.getSourceTerminalCount();
		int nTgt = rule.getTargetTerminalCount();
		if (nSrc >= minCount && nTgt >= minCount ) {
			float actualRatio = (float) nTgt / (float) nSrc;
			if (Math.abs(actualRatio - expectedRatioTgtOverSrc) > maxDifference) {
				featValue = 1;
			}
		}
		
		rule.setFeature(PHRASE_BALANCE, featValue, this);
	}

	public void addPhraseCount(ScoreableRule rule) throws RuleException {
		if (isPhrase(rule)) {
			rule.setFeature(PHRASE_PENALTY, 0.4343f, this);
		} else {
			rule.setFeature(PHRASE_PENALTY, 0.0f, this);
		}
	}

	public void addGlueCount(ScoreableRule rule) throws RuleException {
		rule.setFeature(GLUE_COUNT, 0.0f, this);
	}
}
