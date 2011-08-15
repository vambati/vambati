/*
 * Desc: Rule Learning using Version Spaces 
 *
 * Author: Vamshi Ambati 
 * Email: vamshi@cmu.edu 
 * Carnegie Mellon University 
 * Date: 27-Jan-2007
 */

package RuleLearner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import Options.Options;
import Rule.Rule;
import Scoring.RuleException;

public class Printer implements IPrinter {
	public PrintWriter ptablewriter = null;
	public PrintWriter lexiconwriter = null;
	public PrintWriter grawriter = null;

	// 1 - S2S
	// 2 - T2S
	public int mode = 1;

	// Output format variables
	// 0 - AVENUE , 1 - ONELINE
	public static int gra_out_mode = 0;
	public static int ptable_out_mode = 0;
	public static int lexicon_out_mode = 0;

	// 0 = TRUE, 1 = LOWER
	public static int output_case = 0;

	public static int FEATURES_FLAG = 0;

	public Printer(String graFile, String ptableFile, String lexFile, Options opts)
			throws IOException {
		grawriter = new PrintWriter(new BufferedWriter(new FileWriter(graFile)));
		ptablewriter = new PrintWriter(new BufferedWriter(new FileWriter(ptableFile)));
		lexiconwriter = new PrintWriter(new BufferedWriter(new FileWriter(lexFile)));

		if (opts.defined("CONTEXT_FEATURES")) {
			if (opts.get("CONTEXT_FEATURES").equals("TRUE"))
				FEATURES_FLAG = 1;
		}
		// output case
		if (opts.get("OUTPUT_CASE").equals("LOWER")) {
			output_case = 1;
		} else if (opts.get("OUTPUT_CASE").equals("UPPER")) {
			output_case = 2;
		} else {
			output_case = 0;
		}

		// Output mode options
		gra_out_mode = modeToInt(opts.get("GRA_FORMAT"));
		ptable_out_mode = modeToInt(opts.get("PTABLE_FORMAT"));
		lexicon_out_mode = modeToInt(opts.get("LEXICON_FORMAT"));
	}

	private static int modeToInt(String str) {
		final int mode;
		if (str.equals("JOSHUA")) {
			mode = 3;
		} else if (str.equals("HASH")) {
			mode = 2;
		} else if (str.equals("ONELINE")) {
			mode = 1;
		} else if (str.equals("AVENUE")) {
			mode = 0;
		} else {
			mode = -1;
		}
		return mode;
	}

	/*
	 * (non-Javadoc)
	 * @see RuleLearner.IPrinter#writeTaruPhrase(java.lang.String, int,
	 * java.lang.String, java.lang.String)
	 */
	public void writeTaruPhrase(String type, int id, String tree, String target) {
		// Modified to suit Taru output
		ptablewriter.println(type + "\t" + type + "\t" + tree + "\t" + target);

		// Taru scores temporary
		// grawriter.write("1 1\n");
	}

	/*
	 * (non-Javadoc)
	 * @see RuleLearner.IPrinter#writeTaruRule(java.lang.String, int,
	 * java.lang.String, java.lang.String)
	 */
	public void writeTaruRule(String type, int id, String tree, String target) {
		// Modified to suit Taru output
		grawriter.println(type + "\t" + type + "\t" + tree + "\t" + target);

		// Taru scores temporary
		// grawriter.write("1 1\n");
	}

	/*
	 * (non-Javadoc)
	 * @see RuleLearner.IPrinter#writeT2TRule(java.lang.String, int,
	 * java.lang.String, java.lang.String)
	 */
	public void writeT2TRule(String type, int id, String stree, String ttree) {
		grawriter.println("{" + type + "," + id + "}");
		grawriter.println(stree + " <-> " + ttree);
		grawriter.println();
	}

	/*
	 * (non-Javadoc)
	 * @see RuleLearner.IPrinter#writeRule(Rule.Rule)
	 */
	public void writeRule(Rule rl) throws RuleException {
		String str = "";
		if (gra_out_mode == 1) {
			// Concise format with/without features
			str = rl.toConciseString(FEATURES_FLAG);
		} else if (gra_out_mode == 0) {
			// Avenue format
			str = rl.toString();
		} else if (gra_out_mode == 2) {
			// Avenue format
			str = rl.toHashString();
		} else if (gra_out_mode == 3) {
			str = rl.toScoreableRule().toHadoopRecordString();
		} else {
			return;
		}

		if (output_case == 1) {
			str = str.toLowerCase();
		}
		grawriter.println(str);
	}

	/*
	 * (non-Javadoc)
	 * @see RuleLearner.IPrinter#writePhrase(Rule.Rule)
	 */
	public void writePhrase(Rule rl) throws RuleException {
		String str = "";
		// Put phrase limit as 12
		if (rl.slRuleSequence.size() < 15) {
			if (ptable_out_mode == 2) {
				// STTK HASH format
				str = rl.toHashString();
			} else if (ptable_out_mode == 1) {
				// SMT format
				str = rl.toPtableString();
			} else if (ptable_out_mode == 0) {
				// Avenue format
				str = rl.toLexString();
			} else if (ptable_out_mode == 3) {
				str = rl.toScoreableRule().toJoshuaString();
			} else {
				return;
			}

			if (output_case == 1) {
				str = str.toLowerCase();
			}
			ptablewriter.println(str);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see RuleLearner.IPrinter#writeLexicon(Rule.Rule)
	 */
	public void writeLexicon(Rule rl) throws RuleException {
		String str = "";
		if (lexicon_out_mode == 2) {
			// STTK Hash format
			str = rl.toHashString();
		} else if (lexicon_out_mode == 1) {
			// SMT format
			str = rl.toPtableString();
		} else if (lexicon_out_mode == 0) {
			str = rl.toLexString();
		} else if (lexicon_out_mode == 3) {
			str = rl.toScoreableRule().toJoshuaString();
		} else {
			return;
		}

		if (output_case == 1) {
			str = str.toLowerCase();
		}
		lexiconwriter.println(str);
	}

	/*
	 * (non-Javadoc)
	 * @see RuleLearner.IPrinter#writeTiburonTransducerFile(java.lang.String,
	 * java.util.HashMap)
	 */
	public void writeTiburonTransducerFile(String fileName, HashMap<String, Integer> ruleRepository) throws IOException {
		PrintWriter tiburonWriter = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
		tiburonWriter.println("q");
		for (String rule : ruleRepository.keySet()) {
			tiburonWriter.println(rule);
		}
		tiburonWriter.close();
	}

	/*
	 * (non-Javadoc)
	 * @see RuleLearner.IPrinter#writeTiburonTrainFile(java.lang.String,
	 * java.util.ArrayList)
	 */
	public void writeTiburonTrainFile(String fileName, ArrayList<String> parseTreeMap) throws IOException {
		PrintWriter tiburonWriter = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
		for (String parse : parseTreeMap) {
			tiburonWriter.println(parse);
		}
		tiburonWriter.close();
	}

	/*
	 * (non-Javadoc)
	 * @see RuleLearner.IPrinter#close()
	 */
	public void close() {
		grawriter.flush();
		ptablewriter.flush();
		lexiconwriter.flush();

		grawriter.close();
		ptablewriter.close();
		lexiconwriter.close();
	}
}
