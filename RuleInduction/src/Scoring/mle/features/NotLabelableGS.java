package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.NonsyntacticSourceCount;
import Scoring.mle.features.suffstat.NotLabelableSourceCount;

public class NotLabelableGS extends MLEFeature {
	
	public static final int NUM_CATEGORIES = 2;

	public NotLabelableGS(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman, new NotLabelableSourceCount(fman, trans),
				new NonsyntacticSourceCount(fman, trans),
				NUM_CATEGORIES,
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
