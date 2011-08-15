package Scoring.mle.features;

import Scoring.mle.MLESuffStat;

public abstract class TMFeature {
	public final MLESuffStat[] suffStats;
	
	public TMFeature(MLESuffStat[] suffStats) {
		this.suffStats = suffStats;
	}
	
	public abstract float calculate();
	
	public abstract String getFormula();
}
