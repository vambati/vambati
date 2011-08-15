package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.SyntacticCategTargetCount;
import Scoring.mle.features.suffstat.SyntacticTargetCount;

public class SyntacticCGT extends MLEFeature {

	public SyntacticCGT(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman, new SyntacticCategTargetCount(fman, trans),
				new SyntacticTargetCount(fman, trans),
				smoothCount,
				trans);
	}
	
	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		return ScoreableRule.isSyntactic(rule);
	}
}
