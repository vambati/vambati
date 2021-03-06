package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.NonsyntacticSourceCount;
import Scoring.mle.features.suffstat.NonsyntacticSourceTargetCount;

public class NonsyntacticTGS extends MLEFeature {

	public NonsyntacticTGS(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman,
				new NonsyntacticSourceTargetCount(fman, trans),
				new NonsyntacticSourceCount(fman, trans),
				smoothCount,
				trans);
	}
	
	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		return rule.type == Type.PHRASE || rule.type == Type.HIERO;
	}
}
