/**
 * 
 */
package Scoring.extract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;
import chaski.proc.extract.SentenceAlignment;

import com.google.common.base.Joiner;

public class HierarchicalPhraseEmitter implements RuleProcessor {

	private List<Phrase> phrases = new ArrayList<Phrase>(10000);
	private final RuleWriter emitter;
	private final int maxNonterms;
	private final int maxRuleSizeF;
	private SentenceAlignment sentence;
	private final boolean allowAdjacentSrcNonterms;
	private final boolean allowAbstractUnaryTargets;
	private final boolean allowUnalignedBoundariesForInitialPhrase;
	private final boolean requireOneWordAlignment;
	private static final int NONTERM = -1;

	// use phr to be compatible with non-syntactic phrases
	// using a unary rule clogs beams
	public static final String HIERO_LABEL = "PHR::PHR";

	public HierarchicalPhraseEmitter(RuleWriter emitter, int maxNonterms, int maxRuleSizeF,
			boolean allowAdjacentSrcNonterms, boolean allowAbstractUnaryTargets,
			boolean allowUnalignedBoundariesForInitialPhrase, boolean requireOneWordAlignment) {

		this.emitter = emitter;
		this.maxNonterms = maxNonterms;
		this.maxRuleSizeF = maxRuleSizeF;
		this.allowAdjacentSrcNonterms = allowAdjacentSrcNonterms;
		this.allowAbstractUnaryTargets = allowAbstractUnaryTargets;
		this.allowUnalignedBoundariesForInitialPhrase = allowUnalignedBoundariesForInitialPhrase;
		this.requireOneWordAlignment = requireOneWordAlignment;

		// From Chiang 2007:
		//		
		// 1. If there are multiple initial phrase pairs containing the same set
		// of
		// alignments, only the smallest is kept. That is, unaligned words are
		// not
		// allowed at the edges of phrases. (DONE)
		// 2. Initial phrases are limited to a length of 10 words on either
		// side. (DONE)
		// 3. Rules are limited to ﬁve nonterminals plus terminals on the French
		// side. (DONE)
		// 4. Rules can have at most two nonterminals, which simpliﬁes the
		// decoder
		// implementation. This also makes our grammar weakly equivalent to an
		// inversion transduction grammar (Wu 1997), although the conversion
		// would create a very large number of new nonterminal symbols. (DONE)
		// 5. It is prohibited for nonterminals to be adjacent on the French
		// side, a major
		// cause of spurious ambiguity. (DONE)
		// 6. A rule must have at least one pair of aligned words, so that
		// translation
		// decisions are always based on some lexical evidence. (DONE)

		System.err.println("maxNonterms = " + maxNonterms);
		System.err.println("allowAdjacentSrcNonterms = " + allowAdjacentSrcNonterms);
		System.err.println("allowAbstractUnaryTargets = " + allowAbstractUnaryTargets);
	}

	private final Comparator<Phrase> fComparator = new Comparator<Phrase>() {
		public int compare(Phrase p1, Phrase p2) {
			if (p1.startF != p2.startF) {
				return KoehnPhraseExtractor.compareInts(p1.startF, p2.startF);
			}
			if (p1.endF != p2.endF) {
				return KoehnPhraseExtractor.compareInts(p1.endF, p2.endF);
			}
			return 0;
		}
	};

	private final Comparator<Phrase> eComparator = new Comparator<Phrase>() {
		public int compare(Phrase p1, Phrase p2) {
			if (p1.startE != p2.startE) {
				return KoehnPhraseExtractor.compareInts(p1.startE, p2.startE);
			}
			if (p1.endE != p2.endE) {
				return KoehnPhraseExtractor.compareInts(p1.endE, p2.endE);
			}
			return 0;
		}
	};

	@Override
	public void emitPhrase(SentenceAlignment sentence, int startE, int endE, int startF, int endF) {
		// TODO: Assert sentence is the same
		this.sentence = sentence;

		// collect initial phrases
		Phrase phrase = new Phrase(startE, endE, startF, endF);
		if (areFirstConstraintsSatisfied(phrase)) {
			phrases.add(phrase);
		}
	}

	@Override
	public void finishSentence() throws RuleException, IOException, InterruptedException {
		findHierarchicalRules();
		this.sentence = null;
		phrases.clear();
	}

	public void findHierarchicalRules() throws RuleException, IOException, InterruptedException {

		// 2) do topological sort on foreign side
		Collections.sort(phrases, fComparator);

		// 3) find matching subphrases
		List<Phrase> children = new ArrayList<Phrase>(maxNonterms);
		for (int iParent = 0; iParent < phrases.size(); iParent++) {
			Phrase parent = phrases.get(iParent);
			findSubphrase(parent, iParent + 1, children);
		}
	}

	private void findSubphrase(Phrase parent, int firstChildToTry, List<Phrase> children)
			throws RuleException, IOException, InterruptedException {

		if (children.size() == maxNonterms) {
			return;
		}

		for (int iChild = firstChildToTry; iChild < phrases.size(); iChild++) {
			Phrase child = phrases.get(iChild);

			// stop when the child is out of bounds on F w.r.t. parent
			// phrase
			if (parent.startF <= child.startF && parent.endF >= child.endF) {

				// extract if in bounds on E, too
				if (parent.startE <= child.startE && parent.endE >= child.endE) {
					children.add(child);
					punchHole(sentence, parent, children);
					findSubphrase(parent, iChild + 1, children);
					children.remove(children.size() - 1);
				}
			} else {
				// check for early termination
				if (child.startE > parent.endE) {
					// all following children are guaranteed to fail, too
					break;
				}
			}
		}
	}

	private void punchHole(SentenceAlignment sentence, Phrase parent, List<Phrase> children)
			throws RuleException, IOException, InterruptedException {

		// System.out.println(parent.toString(sentence) + " --------- "
		// + children.get(0).toString(sentence));

		List<String> srcAnts = new ArrayList<String>(parent.endF - parent.startF);
		List<String> tgtAnts = new ArrayList<String>(parent.endE - parent.startE);
		List<Integer> srcIndices = new ArrayList<Integer>(parent.endF - parent.startF);
		List<Integer> tgtIndices = new ArrayList<Integer>(parent.endE - parent.startE);

		// don't matter how we order these
		Map<Phrase, String> phrase2nonterm =
				punchForeignHole(sentence, parent, srcAnts, srcIndices, children);

		punchEnglishHole(sentence, parent, tgtAnts, tgtIndices, phrase2nonterm, children);

		if (areSecondConstraintsSatisfied(srcIndices, tgtIndices)) {
			List<String> alignment = getWordAlignment(srcIndices, tgtIndices);
			if (areThirdConstraintsSatisfied(alignment)) {
				writeRule(srcAnts, tgtAnts, srcIndices, tgtIndices, alignment);
			}
		}
	}

	private boolean areFirstConstraintsSatisfied(Phrase phrase) {
		if (allowUnalignedBoundariesForInitialPhrase == false) {
			// do not allow unaligned boundary words on f side of initial
			// phrases
			if (sentence.alignedCountF[phrase.startF] == 0
					|| sentence.alignedCountF[phrase.endF] == 0) {
				return false;
			}
			// do not allow unaligned boundary words on e side of inital phrases
			if (sentence.alignedToE[phrase.startE].length == 0
					|| sentence.alignedToE[phrase.endE].length == 0) {
				return false;
			}
		}

		return true;
	}

	private boolean areSecondConstraintsSatisfied(List<Integer> srcIndices, List<Integer> tgtIndices) {

		// never allow unary abstract source sides since this imples a cycle
		if (srcIndices.size() == 1 && srcIndices.get(0) == NONTERM) {
			return false;
		}

		// don't allow unary abstract target sides since these make it difficult
		// to count the target accurately?
		if (allowAbstractUnaryTargets == false && tgtIndices.size() == 1
				&& tgtIndices.get(0) == NONTERM) {
			return false;
		}

		if (srcIndices.size() > maxRuleSizeF) {
			return false;
		}

		if (allowAdjacentSrcNonterms == false) {
			int prevIdx = 0;
			for (int i = 0; i < srcIndices.size(); i++) {
				int idx = srcIndices.get(i);
				if (prevIdx == NONTERM && idx == NONTERM) {
					return false;
				}
				prevIdx = idx;
			}
		}
		return true;
	}

	private boolean areThirdConstraintsSatisfied(List<String> alignment) {
		if (requireOneWordAlignment) {
			if(alignment.size() < 1) {
				return false;
			}
		}
		return true;
	}

	private void punchEnglishHole(SentenceAlignment sentence, Phrase parent, List<String> tgtAnts,
			List<Integer> tgtIndices, Map<Phrase, String> phrase2nonterm, List<Phrase> children) {

		// now reorder children by target side
		Collections.sort(children, eComparator);

		int prevStart = parent.startE;
		for (Phrase child : children) {
			// add the parent's terminals for spans not punched out by the
			// child
			for (int i = prevStart; i < child.startE; i++) {
				tgtAnts.add(sentence.english[i]);
				tgtIndices.add(i);
			}
			// insert a X nonterm for the span from startF to endF

			// TODO: How do we know its alignment
			String nonterm = phrase2nonterm.get(child);
			tgtAnts.add(nonterm);
			tgtIndices.add(NONTERM);
			prevStart = child.endE + 1;
		}

		// add end of english phrase
		for (int i = prevStart; i < parent.endE; i++) {
			tgtAnts.add(sentence.english[i]);
			tgtIndices.add(i);
		}
	}

	private Map<Phrase, String> punchForeignHole(SentenceAlignment sentence, Phrase parent,
			List<String> srcAnts, List<Integer> srcIndices, List<Phrase> children) {
		Map<Phrase, String> phrase2nonterm = new TreeMap<Phrase, String>(eComparator);

		// children are already guaranteed to be ordered on foreign side

		int iNonterm = 1;
		int prevStart = parent.startF;
		for (Phrase child : children) {

			// add the parent's terminals for spans not punched out by the
			// child
			for (int i = prevStart; i < child.startF; i++) {
				srcAnts.add(sentence.foreign[i]);
				srcIndices.add(i);
			}

			// insert a X nonterm for the span from startF to endF
			String nonterm = "[" + HIERO_LABEL + "," + iNonterm + "]";
			srcAnts.add(nonterm);
			srcIndices.add(NONTERM); // don't align nonterms
			phrase2nonterm.put(child, nonterm);
			iNonterm++;

			// skip over terminals covered by this non-term
			prevStart = child.endF + 1;
		}

		// add end of foreign phrase
		for (int i = prevStart; i <= parent.endF; i++) {
			srcAnts.add(sentence.foreign[i]);
			srcIndices.add(i);
		}
		return phrase2nonterm;
	}

	private void writeRule(List<String> srcAnts, List<String> tgtAnts, List<Integer> srcIndices,
			List<Integer> tgtIndices, List<String> alignment) throws RuleException, IOException,
			InterruptedException {

		String strSrc = Joiner.on(" ").join(srcAnts);
		String strTgt = Joiner.on(" ").join(tgtAnts);
		String strAlignment = Joiner.on(" ").join(alignment);

		ScoreableRule.allowBugs = true;
		ScoreableRule rule =
				new ScoreableRule(ScoreableRule.typeToString(Type.HIERO), "[" + HIERO_LABEL + "]",
						strSrc, strTgt, "", strAlignment, "");
		rule.setCount(1);
		emitter.writeRule(rule);
	}

	private List<String> getWordAlignment(List<Integer> srcIndices, List<Integer> tgtIndices) {
		List<String> alignment = new ArrayList<String>();
		for (int i = 0; i < srcIndices.size(); i++) {
			int srcIndex = srcIndices.get(i);
			if (srcIndex != NONTERM) {
				for (int j = 0; j < tgtIndices.size(); j++) {
					int tgtIndex = tgtIndices.get(j);
					if (tgtIndex != NONTERM && sentence.isAligned(srcIndex, tgtIndex)) {
						alignment.add(i + "-" + j);
					}
				}
			}
		}
		return alignment;
	}
}
