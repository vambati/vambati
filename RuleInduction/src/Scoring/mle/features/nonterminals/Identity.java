package Scoring.mle.features.nonterminals;


public class Identity extends NonterminalTransformer {
	
	@Override
	public String transformAntecedentNonterm(String nonterm) {
		return nonterm;
	}

	@Override
	public String transformConsequentNonterm(String nonterm) {
		return nonterm;
	}
}
