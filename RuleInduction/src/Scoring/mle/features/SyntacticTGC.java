package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.SyntacticCategCount;
import Scoring.mle.features.suffstat.SyntacticCategTargetCount;

public class SyntacticTGC extends MLEFeature {

	public SyntacticTGC(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman, new SyntacticCategTargetCount(fman, trans),
				new SyntacticCategCount(fman, trans),
				smoothCount,
				trans);
	}
	
	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		return ScoreableRule.isSyntactic(rule);
	}
}
