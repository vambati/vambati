/**
 * 
 */
package Scoring.mle;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Utils.MyUtils;

public abstract class MLEFeature {
	public final String name;
	public final MLESuffStat numerator;
	public final MLESuffStat denominator;
	public final MLESuffStat classCounter;
	public final float smoothCount;
	public final int numCategories;

	public static final String FEATURE_NAME_DELIM = "_";

	public MLEFeature(FeatureManager fman, MLESuffStat numerator, MLESuffStat denominator,
			float smoothCount, NonterminalTransformer filt) {
		this(fman, numerator, denominator, null, -1, smoothCount, filt);
	}

	public MLEFeature(FeatureManager fman, MLESuffStat numerator, MLESuffStat denominator,
			int numCategories, float smoothCount, NonterminalTransformer filt) {
		this(fman, numerator, denominator, null, numCategories, smoothCount, filt);
	}

	private MLEFeature(FeatureManager fman, MLESuffStat numerator, MLESuffStat denominator,
			MLESuffStat classCounter, int numCategories, float smoothCount,
			NonterminalTransformer filt) {

		String nameSuffix = filt.name;
		this.name =
				MyUtils.untokenize(FEATURE_NAME_DELIM, this.getClass().getSimpleName(), nameSuffix,
						smoothCount + "");
		this.numerator = numerator;
		this.denominator = denominator;
		this.smoothCount = smoothCount;

		if (smoothCount > 0) {
			if (classCounter == null && numCategories == -1) {
				throw new RuntimeException(
						"Nonzero smoothing is not supported for this feature yet.");
			}
			this.classCounter = classCounter;
			this.numCategories = numCategories;
		} else {
			this.classCounter = null;
			this.numCategories = -1;
		}
		// this.id = fman.featIndexManager.get(name);
	}
	
	public float calculateLogFeature(ScoreableRule rule, FeatureManager fman) throws RuleException {
		
		if(classCounter != null) {
			throw new Error("Class counters are not yet supported.");
		}
		
		float numerator = rule.getSufficientStat(this.numerator.name, fman) + this.smoothCount;
		float denominator =
				rule.getSufficientStat(this.denominator.name, fman) + this.smoothCount * numCategories;
		float mle = numerator / denominator;
		float logMle = (float) Math.log10(mle);

		// and we want negative log probs (sign should always be
		// positive)
		if (logMle < 0) {
			logMle = -logMle;
		}

		if (Float.isInfinite(logMle) || Float.isNaN(logMle)) {
			throw new RuntimeException("NaN or infinite MLE for " + this.name + " = "
					+ this.getFormula() + " => " + numerator + "/" + denominator + "="
					+ mle + "; log(mle) = " + logMle + "\n"
					+ rule.toHadoopRecordString());
		}
		
		return logMle;
	}

	public String getFormula() {
		if (smoothCount > 0) {
			if (classCounter != null) {
				// determine number of categories from data
				return "(" + numerator.name + " + " + smoothCount + ")  / (" + denominator.name
						+ " + " + smoothCount + " * " + classCounter.name + ")";
			} else {
				// we already know the number of categories
				return "(" + numerator.name + " + " + smoothCount + ")  / (" + denominator.name
						+ " + " + smoothCount + " * " + numCategories + ")";
			}
		} else {
			return numerator.name + "  / " + denominator.name;
		}
	}

	public abstract boolean shouldAffect(ScoreableRule rule);
}
