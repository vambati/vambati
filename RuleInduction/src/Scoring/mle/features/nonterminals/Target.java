package Scoring.mle.features.nonterminals;

import Scoring.RuleException;
import Scoring.ScoreableRule;

public class Target extends NonterminalTransformer {

	@Override
	public String transformAntecedentNonterm(String nonterm) throws RuleException {
		return "[" + ScoreableRule.getTargetLabel(nonterm) + ","
				+ ScoreableRule.getLabelAlignment(nonterm) + "]";
	}

	@Override
	public String transformConsequentNonterm(String nonterm) throws RuleException {
		return "[" + ScoreableRule.getTargetLabel(nonterm) + "]";
	}
}
