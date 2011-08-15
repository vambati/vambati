/**
 * 
 */
package Scoring.extract;

import chaski.proc.extract.SentenceAlignment;

public class Phrase {
	public int startE;
	public int endE;
	public int startF;
	public int endF;

	public Phrase(int startE, int endE, int startF, int endF) {
		this.startE = startE;
		this.endE = endE;
		this.startF = startF;
		this.endF = endF;
	}

	public int length() {
		return endE - startE;
	}

	public String toString(SentenceAlignment sentence) {
		StringBuilder builder = new StringBuilder("\"");
		for (int i = startF; i <= endF; i++) {
			builder.append(sentence.foreign[i] + " ");
		}
		builder.append("\" ==> \"");
		for (int i = startE; i <= endE; i++) {
			builder.append(sentence.english[i] + " ");
		}
		builder.append("\"");
		return builder.toString();
	}
}
