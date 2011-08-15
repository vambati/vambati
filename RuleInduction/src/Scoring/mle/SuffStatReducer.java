package Scoring.mle;

import java.io.IOException;

import org.apache.hadoop.io.Text;

import Scoring.RuleException;
import Scoring.ScoreableRule;

public class SuffStatReducer extends SuffStatBase {

	@Override
	public void yield(String key, Context context, ScoreableRule outputRule) throws IOException,
			InterruptedException, RuleException {
		
		context.write(new Text(outputRule.toHadoopRecordString()), new Text(""));
	}
}
