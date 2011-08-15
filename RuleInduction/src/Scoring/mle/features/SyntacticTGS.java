package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.SyntacticSourceCount;
import Scoring.mle.features.suffstat.SyntacticSourceTargetCount;

public class SyntacticTGS extends MLEFeature {

	public SyntacticTGS(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman, new SyntacticSourceTargetCount(fman, trans),
			  new SyntacticSourceCount(fman, trans),
			  smoothCount,
			  trans);
	}
       
        @Override
        public boolean shouldAffect(ScoreableRule rule) {
                return ScoreableRule.isSyntactic(rule);
        }
}
