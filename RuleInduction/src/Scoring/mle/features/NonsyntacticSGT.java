package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.NonsyntacticSourceTargetCount;
import Scoring.mle.features.suffstat.NonsyntacticTargetCount;

public class NonsyntacticSGT extends MLEFeature {

	public NonsyntacticSGT(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman,
				new NonsyntacticSourceTargetCount(fman, trans),
				new NonsyntacticTargetCount(fman, trans),
				smoothCount,
				trans);
	}

	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		return rule.type == Type.PHRASE || rule.type == Type.HIERO;
	}
}
