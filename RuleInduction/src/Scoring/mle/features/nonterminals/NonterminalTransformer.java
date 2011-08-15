package Scoring.mle.features.nonterminals;

import Scoring.RuleException;
import Scoring.ScoreableRule;
import Utils.MyUtils;

public abstract class NonterminalTransformer {
	
	public final String name;
	
	public NonterminalTransformer() {
		name = this.getClass().getSimpleName();
		assert name != null;
	}
	
	public String transformAntecedents(String ants) throws RuleException {
		
		String[] result = MyUtils.tokenize(ants, " ");
		for (int i = 0; i < result.length; i++) {
			if (ScoreableRule.isNonterminal(result[i])) {
				result[i] = transformAntecedentNonterm(result[i]);
			}
		}
		String transformed = MyUtils.untokenize(" ", result);
		return transformed;
	}

	public abstract String transformAntecedentNonterm(String nonterm) throws RuleException;
	
	public abstract String transformConsequentNonterm(String nonterm) throws RuleException;
}
