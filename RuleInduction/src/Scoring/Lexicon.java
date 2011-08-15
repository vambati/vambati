package Scoring;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import Utils.MyUtils;

public class Lexicon {

	private final float floor;
	private static final float NOT_FOUND = -1.0f;
	// private LexiconTable lexiconTable;
	Map<String, Map<String, Float>> tgs = new HashMap<String, Map<String, Float>>();
	Map<String, Map<String, Float>> sgt = new HashMap<String, Map<String, Float>>();

	public Lexicon(FileInputStream lexPtgs, FileInputStream lexPsgt, float floor)
			throws NumberFormatException, IOException {

		this.floor = floor;
		checkSanity(floor);

		// System.err.print("Reading lexF2N f e P(e|f) file " + f2nPath +
		// " ... ");
		BufferedReader dictIn = new BufferedReader(new InputStreamReader(lexPtgs));
		loadLexicon(dictIn, tgs);
		dictIn.close();
		checkSanity(tgs, "TGS");
		System.err.println("Done.");

		// System.err.print("Reading lexN2F e f P(f|e) file " + n2fPath +
		// " ... ");
		dictIn = new BufferedReader(new InputStreamReader(lexPsgt));
		loadLexicon(dictIn, sgt);
		dictIn.close();
		checkSanity(sgt, "SGT");
		System.err.println("Done.");

		// lexiconTable = ChaskiLib.readLexicon(n2f, f2n);
	}

	private void checkSanity(float floor) {
		if (floor <= 0.0 || floor >= 1.0) {
			throw new RuntimeException("Floor out of range (prob space): " + floor);
		}
	}

	public Lexicon(FileInputStream lexCombined, float floor) throws IOException {

		this.floor = floor;
		checkSanity(floor);

		BufferedReader in = new BufferedReader(new InputStreamReader(lexCombined));
		loadCombinedLexicon(in, tgs, sgt);
		in.close();
		System.err.println("Done.");
		checkSanity(sgt, "SGT");
		checkSanity(tgs, "TGS");
	}

	// sgt = p(f|e)
	public float getProb(String f, String e, boolean isSGT) throws LexiconException {
		double result;

		if (isSGT) {
			result = get(sgt, f, e);
		} else {
			// f and e should already be flipped upon calling this method!
			result = get(tgs, f, e);
		}

		// if (fToE) {
		// result = lexiconTable.getRevProb(e, f);
		// } else {
		// result = lexiconTable.getProb(e, f);
		// }
		if (result == NOT_FOUND) {
			if (f.equals("NULL") || e.equals("NULL")) {
				result = floor;
			} else {
				throw new LexiconException("ERROR!, corrupt lexicon (isSGT=" + isSGT + "): " + f
						+ " " + e);
			}
		}
		return (float) result;
	}

	private static float get(Map<String, Map<String, Float>> sgt, String s, String t) {
		Map<String, Float> map = sgt.get(t);
		if (map != null) {
			Float prob = map.get(s);
			if (prob != null) {
				return prob;
			}
		}
		return NOT_FOUND;
	}

	private static void put(Map<String, Map<String, Float>> sgt, String s, String t, float prob) {
		Map<String, Float> map = sgt.get(t);
		if (map == null) {
			map = new TreeMap<String, Float>();
			sgt.put(t, map);
		}
		map.put(s, prob);
	}

	public static void loadCombinedLexicon(BufferedReader combinedIn,
			Map<String, Map<String, Float>> tgs, Map<String, Map<String, Float>> sgt)
			throws IOException {

		String line;
		while ((line = combinedIn.readLine()) != null) {
			String[] columns = MyUtils.tokenize(line, "\t");
			String[] entries = MyUtils.tokenize(columns[1], " ");

			String sgtOrTgs = columns[0];
			String a = entries[0];
			String b = entries[1];
			float prob = Float.parseFloat(entries[2]);

			if (sgtOrTgs.equalsIgnoreCase("SGT")) {
				put(sgt, a, b, prob);
			} else if (sgtOrTgs.equalsIgnoreCase("TGS")) {
				put(tgs, a, b, prob);
			} else {
				throw new RuntimeException("Badly formatedd combined lexicon file: " + line);
			}
		}
	}

	public static void loadLexicon(BufferedReader dictIn, Map<String, Map<String, Float>> sgt)
			throws IOException {

		String line;
		while ((line = dictIn.readLine()) != null) {
			String[] toks = MyUtils.tokenize(line, " \t");
			String s = toks[0];
			String t = toks[1];
			float prob = Float.parseFloat(toks[2]);
			put(sgt, s, t, prob);
		}
	}

	private static void checkSanity(Map<String, Map<String, Float>> sgt, String direction) {

		System.err.println("Sanity checking direction " + direction + " for P(*|word) = 1.0");

		int MAX_ERRORS = 100;
		int MAX_ALLOWABLE_ERRORS = 5;
		int nErrors = 0;
		for (Entry<String, Map<String, Float>> entry : sgt.entrySet()) {

			float sum = 0.0f;
			Map<String, Float> allSourcesForTarget = entry.getValue();
			for (float prob : allSourcesForTarget.values()) {
				sum += prob;
			}

			String word = entry.getKey();
			if ((!word.equalsIgnoreCase("NULL") && notEquals(sum, 1.0f, 0.001f))
					|| (word.equalsIgnoreCase("NULL") && notEquals(sum, 1.0f, 0.5f))) {
				System.err.println("Corrupt lexicon for direction + " + direction
						+ ": Marginal of P(*|" + word + ") = " + sum + " != 1.0");
				if (allSourcesForTarget.size() < 20) {
					for (Entry<String, Float> badEntry : allSourcesForTarget.entrySet()) {
						System.err.println("READ LINE: P(" +  badEntry.getKey() + "|" + word + ") = " + badEntry.getValue());
					}
				}
				nErrors++;
				if (nErrors > MAX_ERRORS) {
					break;
				}
			}
		}

		if (nErrors > MAX_ALLOWABLE_ERRORS) {
			System.err.println("Encountered at least " + nErrors
					+ " errors when checking sanity of lexicon.");
			throw new RuntimeException("Corrupt lexicon.");
		}
	}

	private static boolean notEquals(float a, float b, float diff) {
		return (Math.abs(a - b) > diff);
	}
}
