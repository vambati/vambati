/**
 * 
 */
package Scoring.extract;

import java.io.IOException;

import Scoring.RuleException;
import chaski.proc.extract.SentenceAlignment;

public interface RuleProcessor {
	void emitPhrase(SentenceAlignment sentence, int startE, int endE, int startF, int endF);
	void finishSentence() throws RuleException, IOException, InterruptedException;
}