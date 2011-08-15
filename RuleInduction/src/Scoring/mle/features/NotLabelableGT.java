package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.NonsyntacticTargetCount;
import Scoring.mle.features.suffstat.NotLabelableTargetCount;

public class NotLabelableGT extends MLEFeature {

	public NotLabelableGT(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman, new NotLabelableTargetCount(fman, trans),
				new NonsyntacticTargetCount(fman, trans),
				NotLabelableGS.NUM_CATEGORIES,
				smoothCount,
				trans);
		
		if(smoothCount == 0.0f) {
			throw new RuntimeException("This feature requires non-zero smoothing.");
		}
	}
	
	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		return rule.type == Type.PHRASE;
	}
}
