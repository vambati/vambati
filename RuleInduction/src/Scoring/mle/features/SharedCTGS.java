package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.SharedCategSourceTargetCount;
import Scoring.mle.features.suffstat.SharedSourceCount;

public class SharedCTGS extends MLEFeature {

	public SharedCTGS(FeatureManager fman, NonterminalTransformer filt, float smoothCount) {

		super(fman, new SharedCategSourceTargetCount(fman, filt),
				new SharedSourceCount(fman, filt),
				smoothCount,
				filt);
	}

	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		// TODO
		throw new Error("Unimplemented");
	}
}
