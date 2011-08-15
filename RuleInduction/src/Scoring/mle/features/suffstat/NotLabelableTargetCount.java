package Scoring.mle.features.suffstat;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.mle.MLESuffStat;
import Scoring.mle.SuffStatMapper;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Utils.MyUtils;

public class NotLabelableTargetCount extends MLESuffStat {

	public NotLabelableTargetCount(FeatureManager iman, NonterminalTransformer filt) {
		super(iman, filt);
	}

	public boolean shouldCount(ScoreableRule rule) {
		return ScoreableRule.syntacticLabelExists(rule, fman) == false;
	}

	public boolean shouldAffect(ScoreableRule rule) {
		return true;
	}

	public String getKey(ScoreableRule rule) throws RuleException {
		return MyUtils.untokenize(SuffStatMapper.DELIM,
				filt.transformAntecedentNonterm(rule.tgtAntecedents));
	}
}
