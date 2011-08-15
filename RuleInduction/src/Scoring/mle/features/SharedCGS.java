package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.SharedCategSourceCount;
import Scoring.mle.features.suffstat.SharedSourceCount;

public class SharedCGS extends MLEFeature {

	public SharedCGS(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman, new SharedCategSourceCount(fman, trans),
				new SharedSourceCount(fman, trans),
				smoothCount,
				trans);
	}

	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		// TODO
		throw new Error("Unimplemented");
	}
}
