package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.SharedCategCount;
import Scoring.mle.features.suffstat.SharedCategTargetCount;

public class SharedTGC extends MLEFeature {

	public SharedTGC(FeatureManager fman, NonterminalTransformer filt, float smoothCount) {

		super(fman, new SharedCategTargetCount(fman, filt),
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
