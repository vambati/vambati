package Scoring.mle.features.suffstat;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.mle.MLESuffStat;
import Scoring.mle.SuffStatMapper;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Utils.MyUtils;

public class SharedCategSourceTargetCount extends MLESuffStat {

	public SharedCategSourceTargetCount(FeatureManager iman, NonterminalTransformer filt) {
		super(iman, filt);
	}

	public boolean shouldCount(ScoreableRule rule) {
		return ScoreableRule.isSyntactic(rule) || ScoreableRule.syntacticLabelExists(rule, fman) == false;
	}

	public boolean shouldAffect(ScoreableRule rule) {
		return true;
	}

	public String getKey(ScoreableRule rule) throws RuleException {
		return MyUtils.untokenize(SuffStatMapper.DELIM,
				filt.transformConsequentNonterm(rule.getConsequent()),
				filt.transformAntecedents(rule.srcAntecedents),
				filt.transformAntecedents(rule.tgtAntecedents));
	}
}
