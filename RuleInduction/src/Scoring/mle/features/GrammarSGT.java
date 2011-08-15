package Scoring.mle.features;

import Scoring.FeatureManager;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.suffstat.SyntacticSourceTargetCount;
import Scoring.mle.features.suffstat.SyntacticTargetCount;

public class GrammarSGT extends MLEFeature {

	public GrammarSGT(FeatureManager fman, NonterminalTransformer trans, float smoothCount) {

		super(fman, new SyntacticSourceTargetCount(fman, trans),
				new SyntacticTargetCount(fman, trans),
				smoothCount,
				trans);
	}
	
	@Override
	public boolean shouldAffect(ScoreableRule rule) {
		return rule.type == Type.GRAMMAR;
	}
}
