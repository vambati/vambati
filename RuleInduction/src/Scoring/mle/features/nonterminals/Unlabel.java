package Scoring.mle.features.nonterminals;

import Scoring.RuleException;
import Scoring.ScoreableRule;

public class Unlabel extends NonterminalTransformer  {

	@Override
	public String transformAntecedentNonterm(String antecedentNonterm) throws RuleException {
		return "[_X," + ScoreableRule.getLabelAlignment(antecedentNonterm) + "]";
	}

	@Override
	public String transformConsequentNonterm(String nonterm) {
		return "[_X]";
	}
}
