package Scoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.MyUtils;
import chaski.utils.Pair;

public class WordAlignment {
	
	private final Map<Integer, List<Pair<Integer, Float>>> alignmentMap;
	private final Map<Integer, List<Pair<Integer, Float>>> reverseAlignmentMap;
	private final List<Pair<Integer, Integer>> alignmentPairs = new ArrayList<Pair<Integer,Integer>>();
	
	private final String alignment;
	private static final List<Pair<Integer, Float>> EMPTY_LIST =
			new ArrayList<Pair<Integer, Float>>(0);

	public WordAlignment(String alignment) {

		this.alignment = alignment;

		alignmentMap = new HashMap<Integer, List<Pair<Integer, Float>>>();
		reverseAlignmentMap = new HashMap<Integer, List<Pair<Integer, Float>>>();

		String[] tokens = MyUtils.tokenize(alignment, " ");
		for (String tok : tokens) {

			String[] linkOccurrence = MyUtils.tokenize(tok, "/");
			String[] fe = MyUtils.tokenize(linkOccurrence[0], "-");

			int f = Integer.parseInt(fe[0]);
			int e = Integer.parseInt(fe[1]);
			float occurrences;
			if (linkOccurrence.length == 1) {
				occurrences = 1;
			} else {
				occurrences = Float.parseFloat(linkOccurrence[1]);
			}

			add(alignmentMap, f, e, occurrences);
			add(reverseAlignmentMap, e, f, occurrences);
			alignmentPairs.add(new Pair<Integer, Integer>(f, e));
		}
	}

	private static void add(Map<Integer, List<Pair<Integer, Float>>> alignmentMap, int f, int e,
			float occurrences) {

		List<Pair<Integer, Float>> list = alignmentMap.get(f);
		if (list == null) {
			list = new ArrayList<Pair<Integer, Float>>(2);
			alignmentMap.put(f, list);
		}
		list.add(new Pair<Integer, Float>(e, occurrences));
	}

	// indices are zero-based
	// if isFToE is true, i represents a foreign word
	public List<Pair<Integer, Float>> getWordsAlignedTo(int i, boolean isFtoE) {
		List<Pair<Integer, Float>> list;
		if (isFtoE) {
			list = alignmentMap.get(i);
		} else {
			list = reverseAlignmentMap.get(i);
		}

		if (list != null) {
			return list;
		} else {
			return EMPTY_LIST;
		}
	}

	public String toString() {
		return alignment;
	}

	public List<Pair<Integer, Integer>> getAlignmentPairs() {
		return alignmentPairs;
	}

}
