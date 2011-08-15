package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.SyntacticCategSourceCount;
import Scoring.mle.features.suffstat.SyntacticSourceCount;

public class SyntacticCGS extends MLEFeature {

	public SyntacticCGS(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman, new SyntacticCategSourceCount(fman, trans),
				new SyntacticSourceCount(fman, trans),
				smoothCount,
				trans);
	}
	
	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		return ScoreableRule.isSyntactic(rule);
	}
}
