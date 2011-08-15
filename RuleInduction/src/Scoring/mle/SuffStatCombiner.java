package Scoring.mle;

import java.io.IOException;

import org.apache.hadoop.io.Text;

import Scoring.RuleException;
import Scoring.ScoreableRule;

public class SuffStatCombiner extends SuffStatBase {

	@Override
	public void yield(String key, Context context, ScoreableRule outputRule) throws IOException,
			InterruptedException, RuleException {
		
		context.write(new Text(key), new Text(outputRule.toHadoopRecordString()));
	}
}
