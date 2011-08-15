package Scoring.extract;

import java.io.IOException;

import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;
import chaski.proc.extract.SentenceAlignment;

/**
 * Extract code taken from Chaski by Qin Gao under the LGPL license. Refactored
 * and modified by Jonathan Clark.
 * 
 * @author jon
 */
public class KoehnPhraseExtractor {

	private int maxPhraseLength;
	private RuleWriter phraseWriter;
	private RuleProcessor phraseProcessor;

	public KoehnPhraseExtractor(int maxPhraseLength, RuleWriter emitter, RuleProcessor processor) {
		this.maxPhraseLength = maxPhraseLength;
		this.phraseWriter = emitter;
		this.phraseProcessor = processor;
	}

	static int compareInts(int i, int j) {
		return ((i == j) ? 0 : (i < j) ? -1 : 1);
	}

	public void extract(SentenceAlignment sentence) throws RuleException, IOException,
			InterruptedException {

		int countE = sentence.english.length;
		int countF = sentence.foreign.length;

		for (int startE = 0; startE < countE; startE++) {
			for (int endE = startE; (endE < countE && endE < startE + maxPhraseLength); endE++) {
				int minF = Integer.MAX_VALUE;
				int maxF = -1;
				int[] usedF = new int[sentence.alignedCountF.length];
				System.arraycopy(sentence.alignedCountF, 0, usedF, 0, usedF.length);
				for (int ei = startE; ei <= endE; ei++) {
					for (int i = 0; i < sentence.alignedToE[ei].length; i++) {
						int fi = sentence.alignedToE[ei][i];
						minF = Math.min(fi, minF);
						maxF = Math.max(fi, maxF);
						usedF[fi]--;
					}
				}

				if (isAlignedToForeignWords(maxF)
						&& isForeignPhraseWithinLimits(maxPhraseLength, minF, maxF)
						&& !areForeignWordsAlignedToOutOfBoundEnglishWords(minF, maxF, usedF)) {

					for (int startF = minF; (isAlignedToForeignWords(startF)
							&& startF > maxF - maxPhraseLength && (startF == minF || sentence.alignedCountF[startF] == 0)); // unaligned
					startF--) {
						// end point of foreign phrase may advance over
						// unaligned
						for (int endF = maxF; (endF < countF && endF < startF + maxPhraseLength && // within
						// length
						// limit
						(endF == maxF || sentence.alignedCountF[endF] == 0)); // unaligned
						endF++) {

							emit(sentence, startE, endE, startF, endF);
						}
					}
				}
			}
		}

		phraseProcessor.finishSentence();
	}

	private void emit(SentenceAlignment sentence, int startE, int endE, int startF, int endF)
			throws RuleException, IOException, InterruptedException {

		String src = KoehnPhraseExtractor.getForeignPhrase(sentence, startF, endF);
		String tgt = KoehnPhraseExtractor.getEnglishPhrase(sentence, startE, endE);
		String align = KoehnPhraseExtractor.getAlignmentInfo(sentence, startE, endE, startF);
		String order = KoehnPhraseExtractor.getOrderingInfo(sentence, startE, endE, startF, endF);

		ScoreableRule rule =
				new ScoreableRule(ScoreableRule.typeToString(Type.PHRASE), "[PHR::PHR]", src, tgt,
						"", align, "");
		phraseWriter.writeRule(rule);

		phraseProcessor.emitPhrase(sentence, startE, endE, startF, endF);
	}

	static String getEnglishPhrase(SentenceAlignment sentence, int startE, int endE) {

		StringBuilder bdf = new StringBuilder();
		int ei;
		for (ei = startE; ei < endE; ei++) {// Actuall,
			// fi=startF
			// to fi ==
			// endF
			bdf.append(sentence.english[ei]);
			bdf.append(" ");
		}
		bdf.append(sentence.english[ei]);
		return bdf.toString();
	}

	static String getForeignPhrase(SentenceAlignment sentence, int startF, int endF) {

		StringBuilder bf = new StringBuilder();
		int fi;
		for (fi = startF; fi < endF; fi++) {// Actuall,
			// fi=startF
			// to fi ==
			// endF
			bf.append(sentence.foreign[fi]);
			bf.append(" ");
		}
		bf.append(sentence.foreign[fi]);
		return bf.toString();
	}

	private boolean isForeignPhraseWithinLimits(int maxPhraseLength, int minF, int maxF) {
		return maxF - minF < maxPhraseLength;
	}

	private boolean isAlignedToForeignWords(int maxF) {
		return maxF >= 0;
	}

	private static boolean areForeignWordsAlignedToOutOfBoundEnglishWords(int minF, int maxF,
			int[] usedF) {
		boolean out_of_bounds = false;
		for (int fi = minF; fi <= maxF && !out_of_bounds; fi++) {
			if (usedF[fi] > 0) {
				out_of_bounds = true;
			}
		}
		return out_of_bounds;
	}

	static String getAlignmentInfo(SentenceAlignment sentence, int startE, int endE, int startF) {

		StringBuilder bf = new StringBuilder();
		for (int ei = startE; ei <= endE; ei++) {
			for (int i = 0; i < sentence.alignedToE[ei].length; i++) {
				int ffi = sentence.alignedToE[ei][i];
				bf.append(ffi - startF);
				bf.append("-");
				bf.append(ei - startE);
				bf.append(" ");
			}
		}
		return bf.toString();
	}

	static String getOrderingInfo(SentenceAlignment sentence, int startE, int endE, int startF,
			int endF) {

		StringBuilder bf = new StringBuilder();

		boolean connectedLeftTop = sentence.isAligned(startF - 1, startE - 1);
		boolean connectedRightTop = sentence.isAligned(endF + 1, startE - 1);
		if (connectedLeftTop && !connectedRightTop)
			bf.append("m");
		else if (!connectedLeftTop && connectedRightTop)
			bf.append("s");
		else
			bf.append("o");

		// orientation to following E
		boolean connectedLeftBottom = sentence.isAligned(startF - 1, endE + 1);
		boolean connectedRightBottom = sentence.isAligned(endF + 1, endE + 1);
		if (connectedLeftBottom && !connectedRightBottom)
			bf.append(" t");
		else if (!connectedLeftBottom && connectedRightBottom)
			bf.append(" n");
		else
			bf.append(" p");

		return bf.toString();
	}
}
