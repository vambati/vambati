/**
 * 
 */
package Scoring.mle;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Utils.MyUtils;

public abstract class MLESuffStat {

	public final String name;
	public final FeatureManager fman;
	public final NonterminalTransformer filt;

	public MLESuffStat(FeatureManager iman, NonterminalTransformer trans) {
		fman = iman;
		this.filt = trans;
		name =
				MyUtils.untokenize(MLEFeature.FEATURE_NAME_DELIM, this.getClass().getSimpleName(),
						trans.name);
	}

	public boolean equals(Object obj) {
		if (obj instanceof MLESuffStat) {
			MLESuffStat other = (MLESuffStat) obj;
			return name.equals(other.name);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return name.hashCode();
	}

	public abstract boolean shouldCount(ScoreableRule rule);

	public abstract boolean shouldAffect(ScoreableRule rule);

	public abstract String getKey(ScoreableRule rule) throws RuleException;
}
