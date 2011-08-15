package Scoring.mle.features.nonterminals;

import Scoring.RuleException;
import Scoring.ScoreableRule;

public class Source extends NonterminalTransformer {

	@Override
	public String transformAntecedentNonterm(String nonterm) throws RuleException {
		return "[" + ScoreableRule.getSourceLabel(nonterm) + ","
				+ ScoreableRule.getLabelAlignment(nonterm) + "]";
	}

	@Override
	public String transformConsequentNonterm(String nonterm) throws RuleException {
		return "[" + ScoreableRule.getSourceLabel(nonterm) + "]";
	}
}
