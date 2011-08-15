/**
 * 
 */
package Scoring.extract;

import java.io.IOException;

import Scoring.RuleException;
import Scoring.ScoreableRule;

public interface RuleWriter {
	void writeRule(ScoreableRule rule) throws RuleException, IOException, InterruptedException;
}