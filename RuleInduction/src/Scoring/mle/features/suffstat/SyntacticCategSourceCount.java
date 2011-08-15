package Scoring.mle.features.suffstat;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.mle.MLESuffStat;
import Scoring.mle.SuffStatMapper;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Utils.MyUtils;

public class SyntacticCategSourceCount extends MLESuffStat {

	public SyntacticCategSourceCount(FeatureManager iman, NonterminalTransformer filt) {
		super(iman, filt);
	}

	public boolean shouldCount(ScoreableRule rule) {
		return ScoreableRule.isSyntactic(rule);
	}

	public boolean shouldAffect(ScoreableRule rule) {
		return ScoreableRule.isSyntactic(rule);
	}

	public String getKey(ScoreableRule rule) throws RuleException {
		return MyUtils.untokenize(SuffStatMapper.DELIM,
				filt.transformConsequentNonterm(rule.consequent),
				filt.transformAntecedents(rule.srcAntecedents));
	}
}
