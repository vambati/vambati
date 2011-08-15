package Scoring.stupidrules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import Utils.Beam;
import Utils.MyUtils;

public class ExtractFrequentWordsFromLM {

	public static class ScoredUnigram implements Comparable<ScoredUnigram> {
		public String word;
		public float score;

		public ScoredUnigram(String tok, float prob) {
			this.word = tok;
			this.score = prob;
		}

		public int compareTo(ScoredUnigram other) {
			return Float.compare(score, other.score);
		}
	}

	public static Iterable<ScoredUnigram> extract(InputStream lmFile, int nWords) throws IOException {
		Beam<ScoredUnigram> beam = new Beam<ScoredUnigram>(nWords, true);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(lmFile));
		String line;
		boolean inUnigrams = false;
		
		while ((line = in.readLine()) != null) {
			if(line.trim().equals("")) {
				continue;
			}
			if(line.startsWith("\\2-grams:" )) {
				break;
			}
			if(inUnigrams) {
				String[] columns = MyUtils.tokenize(line, "\t ");
				float prob = Float.parseFloat(columns[0]);
				String tok = columns[1];
				beam.add(new ScoredUnigram(tok, prob));
			}
			if(line.startsWith("\\1-grams:")) {
				inUnigrams = true;
			}
		}
		in.close();
		return beam;
	}

}
