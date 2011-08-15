package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.CategSourceTargetCount;
import Scoring.mle.features.suffstat.SourceCount;

public class OldCTGS extends MLEFeature {

	public OldCTGS(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman,
				new CategSourceTargetCount(fman, trans),
				new SourceCount(fman, trans),
				smoothCount, trans);
	}
	
	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		return true;
	}
}
