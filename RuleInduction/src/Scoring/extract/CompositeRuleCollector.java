package Scoring.extract;

import java.io.IOException;

import Scoring.RuleException;
import chaski.proc.extract.SentenceAlignment;

public class CompositeRuleCollector implements RuleProcessor {

	RuleProcessor[] emitters;
	
	public CompositeRuleCollector(RuleProcessor... emitters) {
		this.emitters = emitters;
	}
	
	@Override
	public void emitPhrase(SentenceAlignment sentence, int startE, int endE, int startF, int endF) {
		for(RuleProcessor r : emitters) {
			r.emitPhrase(sentence, startE, endE, startF, endF);
		}
	}

	@Override
	public void finishSentence() throws RuleException, IOException, InterruptedException {
		for(RuleProcessor r : emitters) {
			r.finishSentence();
		}
	}

}
