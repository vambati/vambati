package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.SyntacticSourceTargetCount;
import Scoring.mle.features.suffstat.SyntacticTargetCount;

public class SyntacticSGT extends MLEFeature {

	public SyntacticSGT(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman, new SyntacticSourceTargetCount(fman, trans),
			  new SyntacticTargetCount(fman, trans),
			  smoothCount,
			  trans);
	}
       
        @Override
        public boolean shouldAffect(ScoreableRule rule) {
                return ScoreableRule.isSyntactic(rule);
        }
}
