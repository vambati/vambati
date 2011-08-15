package RuleLearner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import Rule.Rule;
import Scoring.RuleException;

public interface IPrinter {

	public void writeTaruPhrase(String type, int id, String tree, String target);

	public void writeTaruRule(String type, int id, String tree, String target);

	public void writeT2TRule(String type, int id, String stree, String ttree);

	public void writeRule(Rule rl) throws RuleException, IOException, InterruptedException;

	public void writePhrase(Rule rl) throws RuleException, IOException, InterruptedException;

	public void writeLexicon(Rule rl) throws RuleException, IOException, InterruptedException;

	public void writeTiburonTransducerFile(String fileName, HashMap<String, Integer> ruleRepository) throws IOException;

	public void writeTiburonTrainFile(String fileName, ArrayList<String> parseTreeMap) throws IOException;

	public void close();

}
