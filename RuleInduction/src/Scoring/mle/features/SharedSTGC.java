package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.SharedCategCount;
import Scoring.mle.features.suffstat.SharedCategSourceTargetCount;

public class SharedSTGC extends MLEFeature {

	public SharedSTGC(FeatureManager fman, NonterminalTransformer filt, float smoothCount) {

		super(fman, new SharedCategSourceTargetCount(fman, filt),
				new SharedCategCount(fman, filt),
				smoothCount,
				filt);
	}

	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		// TODO
		throw new Error("Unimplemented");
	}
}
