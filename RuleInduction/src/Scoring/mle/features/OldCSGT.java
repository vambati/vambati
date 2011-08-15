package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.CategSourceTargetCount;
import Scoring.mle.features.suffstat.TargetCount;

public class OldCSGT extends MLEFeature {

	public OldCSGT(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {
		super(fman,
				new CategSourceTargetCount(fman, trans),
				new TargetCount(fman, trans),
				smoothCount, trans);
	}
	
	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		return true;
	}
}
