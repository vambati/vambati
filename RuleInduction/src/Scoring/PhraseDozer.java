package Scoring;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.TaskCompletionEvent;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import Hadoop.HadoopRuleLearningMapper;
import Scoring.extract.GhkmRuleExtractionMapper;
import Scoring.extract.MultiExtractorMapper;
import Scoring.grammar.FindNbestGrammarFreqMapper;
import Scoring.grammar.PruneGrammarByFreqMapper;
import Scoring.lexprobs.EstimateLexiconMapper;
import Scoring.lexprobs.EstimateLexiconReducer;
import Scoring.lexprobs.LexProbsMapper0;
import Scoring.lexprobs.LexProbsMapper1;
import Scoring.lexprobs.LexProbsMapper2;
import Scoring.misc.CombineCountsMapper;
import Scoring.misc.CombineCountsReducer;
import Scoring.misc.FilterMapper;
import Scoring.misc.IdentifySpecificContextRulesMapper;
import Scoring.misc.LabelSharingFilterMapper;
import Scoring.misc.LabelSharingMapper;
import Scoring.misc.LabelSharingReducer;
import Scoring.misc.PruneMapper;
import Scoring.misc.PruneReducer;
import Scoring.misc.UniqueReducer;
import Scoring.mle.EstimatorMapper;
import Scoring.mle.MLEFeature;
import Scoring.mle.MLESuffStat;
import Scoring.mle.SuffStatMapper;
import Scoring.mle.SuffStatReducer;
import Scoring.mle.features.MLEFeatureFactory;
import Utils.Beam;
import Utils.MyUtils;
import chaski.proc.extract.ExtractMapper;

public class PhraseDozer extends Configured implements Tool {

	public static final String PROGRAM_NAME = "PhraseDozer";
	public static final String VERSION = "V0.6.62";
	public static final String DATE = "June 7, 2010";

	private PhraseDozer() {
	}

	public static void requireOption(String str, Configuration configuration) {
		if (configuration.get(str) == null) {
			System.err.println("Required option -D " + str);
			System.exit(1);
		}
	}

	public int run(String[] args) throws Exception {

		PhraseDozerLocal.printHeader();

		if (args.length == 1 && args[0].equalsIgnoreCase("-v")) {
			System.exit(0);
		}

		if (args.length != 3 && args.length != 5 && args.length != 7) {
			System.err.println("Usage: BuildSyntaxPrioPhraseTable taskName inputDir outputDir [statsIn featsIn statsOut featsOut] " /*
																																	 * +
																																	 * "[-resume <step> <input_dir>]"
																																	 */);
			System.err.println("Available tasks:");
			System.err.println("mergeAlignmentFormat\toptions: -DsrcSents -DtgtSents (inputDir=localAlignmentFile)");
			System.err.println("extractPhrases\toptions: -Dmax-phrase-len");
			System.err.println("extractRules\toptions: -D INPUT_MODE -D OUTPUT_MODE -D MAX_RULE_SIZE [-D ALLOW_BUGS]");
			System.err.println("extractGhkm\toptions: -D maxCompositions -D maxSrc -D maxTgt");
			System.err.println("extractHiero\toptions: -D maxPhraseLength -D maxNonterms -D allowAdjacentNonterms -D writeKoehnPhrases");
			System.err.println("combineCounts\toptions: -DruleExtract (inputDir=phraseExtract)");
			System.err.println("identifyOverlapping");
			System.err.println("scoreMLE\toptions: -DfeaturesToAdd");
			System.err.println("filter\toptions: -DsrcPhrases");
			System.err.println("prune\toptions options: -DscalingFactors -DambiguityFactor -DminCount");
			System.err.println("pruneGrammar\toptions options: -Dnbest");
			System.err.println("identifySpecificContextRules\toptions options: -D stopwordsFile");
			System.err.println("filterOverlapping");
			System.err.println("estimateLexicons\toptions options: -DmergedAlignmentFormat");
			System.err.println("scoreLexProbs\toptions options: -DlexPtgs -DlexPsgt");
			// ToolRunner.printGenericCommandUsage(System.err);
			return 1;
		}

		String task = args[0];
		String strInDir = args[1];
		String strOutDir = args[2];

		FileSystem fs = FileSystem.get(getConf());

		if (task.equalsIgnoreCase("mergeAlignmentFormat")) {
			mergeAlignmentFormat(strInDir, strOutDir, fs);
		} else if (task.equalsIgnoreCase("extractPhrases")) {
			extractPhrases(strInDir, strOutDir, fs);
		} else if (task.equalsIgnoreCase("extractRules")) {
			extractRules(strInDir, strOutDir, fs);
		} else if (task.equalsIgnoreCase("extractGhkm")) {
			extractGhkm(strInDir, strOutDir, fs);
		} else if (task.equalsIgnoreCase("extractHiero")) {
			extractHiero(strInDir, strOutDir, fs);
		} else if (task.equalsIgnoreCase("filter")) {
			filter(strInDir, strOutDir, fs);
		} else if (task.equalsIgnoreCase("estimateLexicons")) {
			estimateLexicons(strInDir, strOutDir, fs);
		} else if (task.equalsIgnoreCase("scoreLexProbs")) {
			scoreLexProbs(strInDir, strOutDir, fs);
		} else if (task.equalsIgnoreCase("identifySpecificContextRules")) {
			identifySpecificContextRules(strInDir, strOutDir);

			// tasks that require feature information
		} else if (task.equalsIgnoreCase("combineCounts")) {
			if (args.length != 5) {
				System.err.println("Last 2 args for combineCounts should be statsOut and featsOut");
				System.exit(1);
			}
			FeatureManager fman = combineCounts(strInDir, strOutDir, fs);
			PhraseDozerLocal.writeFeatureInfo(args, fman, 3 - 2);
		} else if (task.equalsIgnoreCase("identifyOverlapping")) {
			FeatureManager fman = readFeatureInfo(args);
			identifyOverlapping(strInDir, strOutDir, fs, fman);
			writeFeatureInfo(args, fman);
		} else if (task.equalsIgnoreCase("scoremle")) {
			FeatureManager fman = readFeatureInfo(args);
			scoreMLE(args, strInDir, strOutDir, fs, fman);
			writeFeatureInfo(args, fman);
		} else if (task.equalsIgnoreCase("prune")) {
			FeatureManager fman = readFeatureInfo(args);
			prune(strInDir, strOutDir, fs, fman);
			writeFeatureInfo(args, fman);
		} else if (task.equalsIgnoreCase("pruneGrammar")) {
			FeatureManager fman = readFeatureInfo(args);
			pruneGrammar(strInDir, strOutDir, fs, fman);
			writeFeatureInfo(args, fman);
		} else if (task.equalsIgnoreCase("filterOverlapping")) {
			FeatureManager fman = readFeatureInfo(args);
			filterOverlapping(strInDir, strOutDir, fs, fman);
			writeFeatureInfo(args, fman);
		} else {
			System.err.println("Unrecognized task: " + task);
			System.exit(1);
		}

		return 0;
	}

	private void identifySpecificContextRules(String strInDir, String strOutDir)
			throws IOException, InterruptedException, ClassNotFoundException {

		requireOption("stopwordsFile", getConf());

		String jobName = "Identify Specific Context Rules";
		Job job = new Job(getConf(), jobName);
		System.out.println("Starting: " + jobName);

		FileInputFormat.setInputPaths(job, new Path(strInDir));
		FileOutputFormat.setOutputPath(job, new Path(strOutDir));

		String counterFilename = "identify.counters.txt";
		PrintWriter countersOut = new PrintWriter(counterFilename);

		// phase4.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setJarByClass(IdentifySpecificContextRulesMapper.class);
		job.setMapperClass(IdentifySpecificContextRulesMapper.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setNumReduceTasks(0);
		runJob(countersOut, "identify", job);
		System.out.println("Done: " + jobName);
		System.out.println("Wrote output to directory: " + strOutDir);

		countersOut.close();
	}

	private void identifyOverlapping(String strInDir, String strOutDir, FileSystem fs,
			FeatureManager fman) throws IOException, InterruptedException, ClassNotFoundException {

		fman.statIndexManager.add(SuffStatMapper.SYNTACTIC_LABEL_FOR_PAIR_EXISTS);
		addFeatureInfoToConf(getConf(), fman);
		requireOption("serializedStats", getConf());
		requireOption("serializedFeatures", getConf());

		Path in = new Path(strInDir);
		Path out = new Path(strOutDir);

		if (fs.exists(out)) {
			System.err.println("Path already exists: " + strOutDir);
			System.exit(1);
		}

		String counterFilename = "identifyOverlapping.counters.txt";
		PrintWriter countersOut = new PrintWriter(counterFilename);
		{
			String jobName = PROGRAM_NAME + " -- Identify overlapping nonsyntactic phrases";
			runFullJob(jobName, "identifyOverlapping", out, LabelSharingMapper.class,
					LabelSharingReducer.class, countersOut, in);
		}
		countersOut.close();
	}

	private void filterOverlapping(String strInDir, String strOutDir, FileSystem fs,
			FeatureManager fman) throws IOException, InterruptedException, ClassNotFoundException {

		addFeatureInfoToConf(getConf(), fman);
		requireOption("serializedStats", getConf());
		requireOption("serializedFeatures", getConf());

		Path in = new Path(strInDir);
		Path out = new Path(strOutDir);

		if (fs.exists(out)) {
			System.err.println("Path already exists: " + strOutDir);
			System.exit(1);
		}

		String counterFilename = "filterOverlapping.counters.txt";
		PrintWriter countersOut = new PrintWriter(counterFilename);
		{
			String jobName = PROGRAM_NAME + " -- Remove overlapping nonsyntactic phrases";
			runFullJob(jobName, "filterOverlapping", out, LabelSharingFilterMapper.class, null,
					countersOut, in);
		}
		countersOut.close();
	}

	private static FeatureManager readFeatureInfo(String[] args) throws FileNotFoundException,
			IOException {
		return PhraseDozerLocal.readFeatureInfo(args, 3, true);
	}

	private void writeFeatureInfo(String[] args, FeatureManager fman) throws FileNotFoundException {
		PhraseDozerLocal.writeFeatureInfo(args, fman, 3);
	}

	private void mergeAlignmentFormat(String alignmentFile, String hdfsOutDir, FileSystem fs)
			throws FileNotFoundException, IOException {

		requireOption("srcSents", getConf());
		requireOption("tgtSents", getConf());
		requireOption("numPieces", getConf());

		String srcSent = getConf().get("srcSents");
		String tgtSent = getConf().get("tgtSents");
		int numPieces = Integer.parseInt(getConf().get("numPieces"));

		System.out.println("Creating input file for non-syntactic phrase extraction...");
		System.out.println("Source sentences from: " + srcSent);
		System.out.println("Target sentences from: " + tgtSent);
		System.out.println("Alignments from: " + alignmentFile);

		if (fs.exists(new Path(hdfsOutDir))) {
			System.err.println("Path already exists: " + hdfsOutDir);
			System.exit(1);
		}

		BufferedReader localBufferedReader1 = new BufferedReader(new FileReader(srcSent));
		BufferedReader localBufferedReader2 = new BufferedReader(new FileReader(tgtSent));
		BufferedReader localBufferedReader3 = new BufferedReader(new FileReader(alignmentFile));
		PrintWriter[] localPrintWriter = new PrintWriter[numPieces];
		for (int i = 0; i < localPrintWriter.length; i++) {
			String hdfsPartPath = String.format("%s/part-%d5", hdfsOutDir, i);
			localPrintWriter[i] =
					new PrintWriter(new OutputStreamWriter(fs.create(new Path(hdfsPartPath))));
		}

		int k = 0;
		while (true) {
			String str1 = localBufferedReader1.readLine();
			String str2 = localBufferedReader2.readLine();
			String str3 = localBufferedReader3.readLine();
			if ((str1 == null) || (str2 == null) || (str3 == null))
				break;
			if (str1.indexOf(124) >= 0 || str2.indexOf(124) >= 0)
				continue;

			str1 = filterSpaces(str1);
			str2 = filterSpaces(str2);
			localPrintWriter[k].println(str1 + " {##} " + str2 + " {##} " + str3);

			k = (k + 1) % localPrintWriter.length;
		}

		for (int i = 0; i < localPrintWriter.length; i++) {
			localPrintWriter[i].close();
		}

	}

	private String filterSpaces(String str) {
		return MyUtils.untokenize(" ", MyUtils.tokenize(str, " \t"));
	}

	private void extractHiero(String strInDir, String strOutDir, FileSystem fs) throws IOException,
			InterruptedException, ClassNotFoundException {

		requireOption("maxPhraseLength", getConf());
		requireOption("maxNonterms", getConf());
		requireOption("maxRuleSizeF", getConf());
		requireOption("allowAdjacentNonterms", getConf());
		requireOption("allowAbstractUnaryTargets", getConf());
		requireOption("allowUnalignedBoundariesForInitialPhrase", getConf());
		requireOption("requireOneWordAlignment", getConf());
		requireOption("writeKoehnPhrases", getConf());

		Path inDir = new Path(strInDir);
		Path outDir = new Path(strOutDir);

		PrintWriter countersOut = new PrintWriter("hieroExtraction.counters.txt");
		runFullJob("Hiero Rule Extraction", "hieroExtraction", outDir, MultiExtractorMapper.class,
				null, countersOut, inDir);
		countersOut.close();
	}

	private void extractGhkm(String strInDir, String strOutDir, FileSystem fs) throws IOException,
			InterruptedException, ClassNotFoundException {

		requireOption("maxCompositions", getConf());
		requireOption("maxSrc", getConf());
		requireOption("maxTgt", getConf());

		Path inDir = new Path(strInDir);
		Path outDir = new Path(strOutDir);

		PrintWriter countersOut = new PrintWriter("ghkmExtraction.counters.txt");
		runFullJob("JavaGHKM Rule Extraction", "ghkmExtraction", outDir,
				GhkmRuleExtractionMapper.class, null, countersOut, inDir);
		countersOut.close();
	}

	private void extractRules(String strInDir, String strOutDir, FileSystem fs) throws IOException,
			InterruptedException, ClassNotFoundException {

		requireOption("INPUT_MODE", getConf());
		requireOption("OUTPUT_MODE", getConf());
		requireOption("MAX_RULE_SIZE", getConf());

		if (getConf().get("ALLOW_BUGS") == null) {
			getConf().set("ALLOW_BUGS", "false");
		}

		getConf().set("TOOL_MODE", "AVENUE");

		Path inDir = new Path(strInDir);
		Path outDir = new Path(strOutDir);

		PrintWriter countersOut = new PrintWriter("ruleExtraction.counters.txt");
		runFullJob("Avenue Rule Extraction", "ruleExtraction", outDir,
				HadoopRuleLearningMapper.class, null, countersOut, inDir);
		countersOut.close();
	}

	private void extractPhrases(String strInDir, String strOutDir, FileSystem fs)
			throws IOException, InterruptedException, ClassNotFoundException {

		Configuration conf = getConf();

		requireOption("max-phrase-len", getConf());
		// requireOption("max-unalign", getConf());
		// requireOption("Unalign-Percent", getConf());

		// defaults
		final int maxUnaligned = 999;
		final boolean balanceExtract = false;
		final int unalignedPercentThreashold = 100;

		conf.setInt("max-unalign", maxUnaligned);
		conf.setInt("Unalign-Percent", unalignedPercentThreashold);
		conf.setBoolean("remove-unbalanced", balanceExtract);

		Path in = new Path(strInDir);
		Path out = new Path(strOutDir);

		String counterFilename = "extract.counters.txt";
		PrintWriter countersOut = new PrintWriter(counterFilename);
		{
			String jobName = PROGRAM_NAME + " -- Get Non-Syntactic Phrases from Chaski";
			runFullJob(jobName, "extract", out, ExtractMapper.class, null, countersOut, in);
		}
		countersOut.close();
	}

	private void estimateLexicons(String strMergedAlignmentFormat, String strOutDir, FileSystem fs)
			throws IOException, InterruptedException, ClassNotFoundException {

		Path mergedAlignmentFormat = new Path(strMergedAlignmentFormat);
		Path lexCombined = new Path(strOutDir);

		if (fs.exists(lexCombined)) {
			System.err.println("Path already exists: " + strOutDir);
			System.exit(1);
		}

		String counterFilename = "estimateLexicons.counters.txt";
		PrintWriter countersOut = new PrintWriter(counterFilename);
		{
			String jobName = PROGRAM_NAME + " -- Estimate Unigram Lexical Translation Table";
			runFullJob(jobName, "estimateLexicon", lexCombined, EstimateLexiconMapper.class,
					EstimateLexiconReducer.class, countersOut, mergedAlignmentFormat);
		}
		countersOut.close();
	}

	private void scoreLexProbs(String strPhraseTableInDir, String strOutDir, FileSystem fs)
			throws IOException, InterruptedException, ClassNotFoundException {

		if (true)
			throw new Error("Needs updating");

		requireOption("lexF2N", getConf());
		requireOption("lexN2F", getConf());

		String strLexF2N = getConf().get("lexF2N");
		String strLexN2F = getConf().get("lexN2F");

		// inDir has
		Path phraseTableDir = new Path(strPhraseTableInDir);

		Path vocabDir = makeTempDir("BuildSyntaxPrioPhraseTable");
		Path lexF2N = new Path(strLexF2N);
		Path lexN2F = new Path(strLexN2F);
		Path filteredLexF2N = makeTempDir("BuildSyntaxPrioPhraseTable");
		Path filteredLexN2F = makeTempDir("BuildSyntaxPrioPhraseTable");
		Path phraseTableTemp = makeTempDir("BuildSyntaxPrioPhraseTable");
		Path phraseTableOut = new Path(strOutDir);

		if (fs.exists(phraseTableOut)) {
			System.err.println("Path already exists: " + phraseTableOut);
			System.exit(1);
		}

		{
			String jobName = PROGRAM_NAME + " -- Get Phrase Table Vocabulary (Phase 1/5)";
			Job phase2 = new Job(getConf(), jobName);
			System.out.println("Starting: " + jobName);

			FileInputFormat.addInputPath(phase2, phraseTableDir);
			FileOutputFormat.setOutputPath(phase2, vocabDir);

			// phase2.setInputFormatClass(KeyValueTextInputFormat.class);
			phase2.setJarByClass(LexProbsMapper0.class);
			phase2.setMapperClass(LexProbsMapper0.class);
			phase2.setCombinerClass(UniqueReducer.class);
			phase2.setReducerClass(UniqueReducer.class);
			phase2.setOutputKeyClass(Text.class);
			phase2.setOutputValueClass(Text.class);
			phase2.waitForCompletion(true);
			System.out.println("Done: " + jobName);
			System.out.println("Wrote phrase table vocabulary: " + vocabDir);

			getConf().set("vocabPath", vocabDir.toString());
		}

		{
			String jobName = PROGRAM_NAME + " -- Filter F2N Lex Probs (Phase 2/5)";
			Job phase3 = new Job(getConf(), jobName);
			System.out.println("Starting: " + jobName);

			FileInputFormat.setInputPaths(phase3, lexF2N);
			FileOutputFormat.setOutputPath(phase3, filteredLexF2N);

			// phase3.setInputFormatClass(KeyValueTextInputFormat.class);
			phase3.setJarByClass(LexProbsMapper1.class);
			phase3.setMapperClass(LexProbsMapper1.class);
			phase3.setNumReduceTasks(0);
			phase3.setOutputKeyClass(Text.class);
			phase3.setOutputValueClass(Text.class);
			phase3.waitForCompletion(true);
			System.out.println("Done: " + jobName);
			System.out.println("Wrote filtered Lex F2N directory: " + filteredLexF2N);

			getConf().set("filteredLexF2N", filteredLexF2N.toString());
		}

		{
			String jobName = PROGRAM_NAME + " -- Filter N2F Lex Probs (Phase 3/5)";
			Job phase4 = new Job(getConf(), jobName);
			System.out.println("Starting: " + jobName);

			FileInputFormat.setInputPaths(phase4, lexN2F);
			FileOutputFormat.setOutputPath(phase4, filteredLexN2F);

			// phase4.setInputFormatClass(KeyValueTextInputFormat.class);
			phase4.setJarByClass(LexProbsMapper1.class);
			phase4.setMapperClass(LexProbsMapper1.class);
			phase4.setNumReduceTasks(0);
			phase4.setOutputKeyClass(Text.class);
			phase4.setOutputValueClass(Text.class);
			phase4.waitForCompletion(true);
			System.out.println("Done: " + jobName);
			System.out.println("Wrote filtered Lex N2F directory: " + filteredLexN2F);

			getConf().set("filteredLexN2F", filteredLexN2F.toString());
		}

		{
			// getConf().set("mapred.job.map.memory.mb", "3072");
			getConf().set("direction", "F2N");
			String jobName = PROGRAM_NAME + " -- Add F2N Lexical Probabilities (Phase 4/5)";
			Job phase5 = new Job(getConf(), jobName);
			System.out.println("Starting: " + jobName);

			FileInputFormat.setInputPaths(phase5, phraseTableDir);
			FileOutputFormat.setOutputPath(phase5, phraseTableTemp);

			// phase5.setInputFormatClass(KeyValueTextInputFormat.class);
			phase5.setJarByClass(LexProbsMapper2.class);
			phase5.setMapperClass(LexProbsMapper2.class);
			phase5.setNumReduceTasks(0);
			phase5.setOutputKeyClass(Text.class);
			phase5.setOutputValueClass(Text.class);
			phase5.waitForCompletion(true);
			System.out.println("Done: " + jobName);
			System.out.println("Wrote phrase table to directory: " + phraseTableTemp);
		}

		{
			getConf().set("direction", "N2F");
			String jobName = PROGRAM_NAME + " -- Add N2F Lexical Probabilities (Phase 5/5)";
			Job phase6 = new Job(getConf(), jobName);
			System.out.println("Starting: " + jobName);

			FileInputFormat.setInputPaths(phase6, phraseTableTemp);
			FileOutputFormat.setOutputPath(phase6, phraseTableOut);

			// phase5.setInputFormatClass(KeyValueTextInputFormat.class);
			phase6.setJarByClass(LexProbsMapper2.class);
			phase6.setMapperClass(LexProbsMapper2.class);
			phase6.setNumReduceTasks(0);
			phase6.setOutputKeyClass(Text.class);
			phase6.setOutputValueClass(Text.class);
			phase6.waitForCompletion(true);
			System.out.println("Done: " + jobName);
			System.out.println("Wrote phrase table to directory: " + phraseTableOut);
		}
	}

	public static long getSize(Path path, FileSystem fs) throws IOException {
		long size = 0;
		FileStatus stat = fs.getFileStatus(path);
		if (stat.isDir()) {
			for (FileStatus child : fs.listStatus(path)) {
				size += getSize(child.getPath(), fs);
			}
		} else {
			size += stat.getLen();
		}
		return size;
	}

	private FeatureManager combineCounts(String strPhraseDir, String strOutDir, FileSystem fs)
			throws IOException, InterruptedException, ClassNotFoundException {

		// only suff stat coming from inputs can be count
		FeatureManager fman = new FeatureManager(ScoreableRule.COUNT, "");
		addFeatureInfoToConf(getConf(), fman);

		requireOption("ruleExtract", getConf());
		requireOption("serializedStats", getConf());
		requireOption("serializedFeatures", getConf());

		String jobName = PROGRAM_NAME + " -- Combine Counts";
		Job job = new Job(getConf(), jobName);
		System.out.println("Starting: " + jobName);

		String strRuleExtract = getConf().get("ruleExtract");

		Path phrasePath = new Path(strPhraseDir);
		Path syntaxPath = new Path(strRuleExtract);
		if (getSize(phrasePath, fs) > 0) {
			FileInputFormat.addInputPath(job, phrasePath);
		}
		if (getSize(syntaxPath, fs) > 0) {
			FileInputFormat.addInputPath(job, syntaxPath);
		}
		FileOutputFormat.setOutputPath(job, new Path(strOutDir));

		String counterFilename = "combineCounts.counters.txt";
		PrintWriter countersOut = new PrintWriter(counterFilename);

		job.setInputFormatClass(TextInputFormat.class); // different format than
		// usual -- fix when we
		// remove dependency on
		// Chaksi
		job.setJarByClass(CombineCountsMapper.class);
		job.setMapperClass(CombineCountsMapper.class);
		// job.setCombinerClass(CombineCountsCombiner.class); // does not work
		// with summing alignments (needs new reader)
		job.setReducerClass(CombineCountsReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		runJob(countersOut, "combineCounts", job);
		System.out.println("Done: " + jobName);
		System.out.println("Wrote combined counts to: " + strOutDir);

		countersOut.close();

		return fman;
	}

	private void addFeatureInfoToConf(Configuration conf, FeatureManager fman) {
		String stats = fman.statIndexManager.toString();
		String feats = fman.featIndexManager.toString();
		// HADOOP HACK since Hadoop doesn't properly send "" in config
		if (feats.equals("")) {
			feats = FeatureManager.NO_FEATURES;
		}
		conf.set("serializedStats", stats);
		conf.set("serializedFeatures", feats);
		System.err.println("serializedStats=" + stats);
		System.err.println("serializedFeats=" + feats);
	}

	private void scoreMLE(String[] args, String strInDir, String strOutDir, FileSystem fs,
			FeatureManager fman) throws IOException, InterruptedException, ClassNotFoundException {

		// requireOption("minCount", getConf());
		// requireOption("doSyntaxPrio", getConf());
		// requireOption("smoothCount", getConf());
		requireOption("featuresToAdd", getConf());

		// inDir has
		Path inDir = new Path(strInDir);

		// outDir has
		Path outDir = new Path(strOutDir);

		if (fs.exists(outDir)) {
			System.err.println("Path already exists: " + outDir);
			System.exit(1);
		}

		// float smoothCount = Float.parseFloat(getConf().get("smoothCount"));
		// // this comes from feature names now
		String[] featuresToAdd = MyUtils.tokenize(getConf().get("featuresToAdd"), " ");
		List<MLEFeature> features = new ArrayList<MLEFeature>();

		for (String featureToAdd : featuresToAdd) {
			MLEFeature feature = MLEFeatureFactory.get(featureToAdd, fman);
			features.add(feature);
		}
		// MLEFeature[] features =
		// new MLEFeature[] { new OldCSGTFeature(fman, smoothCount),
		// new OldCTGSFeature(fman, smoothCount) };

		// figure out which suff stats we need for these features
		Set<MLESuffStat> stats = new HashSet<MLESuffStat>();
		for (MLEFeature feat : features) {
			stats.add(feat.numerator);
			stats.add(feat.denominator);
		}
		// we're guaranteed to have the categ_src_tgt stat from the count
		// combiner
		// update: not anymore -- we've moved this to count
		// stats.remove(new CategSourceTargetCount());

		// inform the user what we've decided to do
		System.out.println("FEATURES:");
		for (MLEFeature feat : features) {
			fman.featIndexManager.add(feat.name);
			System.out.println(feat.name + " = " + feat.getFormula());
		}
		System.out.println();

		System.out.println("SUFFICIENT STATISTICS:");
		for (MLESuffStat stat : stats) {
			fman.statIndexManager.add(stat.name);
			System.out.println(stat.name);
		}
		System.out.println();

		addFeatureInfoToConf(getConf(), fman);
		requireOption("serializedStats", getConf());
		requireOption("serializedFeatures", getConf());

		String counterFilename = "scoreMLE.counters.txt";
		PrintWriter countersOut = new PrintWriter(counterFilename);

		// now obtain each of these sufficient stats through a series of MR
		int nTotalPhases = stats.size() + 1;
		int i = 0;
		Path prevDir = inDir;
		for (MLESuffStat stat : stats) {
			i++;

			String jobName =
					PROGRAM_NAME + " -- Get sufficient stat for " + stat.name + " (Phase " + i
							+ "/" + nTotalPhases + ")";
			Job job = new Job(getConf(), jobName);
			System.out.println("Starting: " + jobName);

			Path tempOutDir = makeTempDir("PhraseDozer");
			FileInputFormat.setInputPaths(job, prevDir);
			FileOutputFormat.setOutputPath(job, tempOutDir);
			prevDir = tempOutDir;

			// set job params
			Configuration conf = job.getConfiguration();
			conf.set("statClassName", stat.name);

			job.setJarByClass(SuffStatMapper.class);
			job.setMapperClass(SuffStatMapper.class);
			// job.setCombinerClass(SuffStatCombiner.class);
			job.setReducerClass(SuffStatReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);

			runJob(countersOut, "suffstat." + stat.name, job);

			System.out.println("Done: " + jobName);
			System.out.println("Wrote temporary directory: " + tempOutDir);
			System.out.println();
		}

		{
			String jobName =
					PROGRAM_NAME + " -- Estimate MLE Features (Phase " + nTotalPhases + "/"
							+ nTotalPhases + ")";
			Job job = new Job(getConf(), jobName);
			System.out.println("Starting: " + jobName);

			FileInputFormat.setInputPaths(job, prevDir);
			FileOutputFormat.setOutputPath(job, outDir);
			prevDir = outDir;

			// set job params
			Configuration conf = job.getConfiguration();
			// String[] featureClassNames = new String[features.length];
			// for (int j = 0; j < features.length; j++) {
			// String name = features[j].getClass().getName();
			// featureClassNames[j] = name;
			// conf.set(name + ".smoothCount", features[j].smoothCount + "");
			// }
			// conf.set("featureClassNames", MyUtils.untokenize(" ",
			// featureClassNames));
			conf.set("featureClassNames", MyUtils.untokenize(" ", featuresToAdd));

			job.setJarByClass(EstimatorMapper.class);
			job.setMapperClass(EstimatorMapper.class);
			job.setNumReduceTasks(0);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			runJob(countersOut, "estimation", job);
			System.out.println("Done: " + jobName);
			System.out.println("Wrote output to directory: " + outDir);
		}

		countersOut.close();

		// These files intentionally survive on failed exits
		// System.out.println("Deleting temporary directories");
		// fs.delete(tempDirB, true);
		// fs.delete(tempDirC, true);
		// fs.delete(tempDirD, true);
	}

	private void runJob(PrintWriter countersOut, String prefix, Job job) throws IOException,
			InterruptedException, ClassNotFoundException {

		long startTime = System.currentTimeMillis();
		boolean result = job.waitForCompletion(true);
		if (result == false) {
			throw new RuntimeException("Job failed.");
		}
		long finishTime = System.currentTimeMillis();

		Collection<String> groups = job.getCounters().getGroupNames();
		for (String group : groups) {
			CounterGroup counterGroup = job.getCounters().getGroup(group);
			for (Counter counter : counterGroup) {
				String key =
						prefix + ".counter." + counterGroup.getDisplayName().trim() + "."
								+ counter.getDisplayName().trim();
				key = key.replace(" ", "_");
				String value = counter.getValue() + "";
				countersOut.println(key + "\t" + value);
			}
		}

		// collect the amount of time that each of the tasks took in serial
		int minTaskSeconds = Integer.MAX_VALUE;
		int maxTaskSeconds = 0;
		int sumTaskSeconds = 0;
		int nTasks = 0;
		int nMaps = 0;
		int nReduces = 0;
		for (TaskCompletionEvent task : job.getTaskCompletionEvents(0)) {
			if (task.isMapTask()) {
				nMaps++;
			} else {
				nReduces++;
			}
			int taskSeconds = task.getTaskRunTime() / 1000;
			sumTaskSeconds += taskSeconds;
			minTaskSeconds = Math.min(minTaskSeconds, taskSeconds);
			maxTaskSeconds = Math.max(maxTaskSeconds, taskSeconds);
			nTasks += 1;
		}

		countersOut.println(prefix + ".NumMaps\t" + nMaps);
		countersOut.println(prefix + ".NumReduces\t" + nReduces);
		countersOut.println(prefix + ".Time.MinTaskSeconds\t" + minTaskSeconds);
		countersOut.println(prefix + ".Time.MaxTaskSeconds\t" + maxTaskSeconds);
		countersOut.println(prefix + ".Time.AvgTaskSeconds\t" + sumTaskSeconds / (float) nTasks);
		countersOut.println(prefix + ".Time.SumTaskSeconds\t" + sumTaskSeconds);

		long secondsElapsed = (finishTime - startTime) / 1000;
		countersOut.println(prefix + ".Time.StartTime\t" + (startTime / 1000));
		countersOut.println(prefix + ".Time.FinishTime\t" + (finishTime / 1000));
		countersOut.println(prefix + ".Time.SecondsElapsed\t" + secondsElapsed);

		System.err.println("Job took " + secondsElapsed + " seconds");
	}

	private void filter(String strInDir, String strOutDir, FileSystem fs) throws IOException,
			InterruptedException, ClassNotFoundException {

		requireOption("srcPhrases", getConf());

		String jobName = "Filter Syntactic Phrases and Grammar";
		Job job = new Job(getConf(), jobName);
		System.out.println("Starting: " + jobName);

		FileInputFormat.setInputPaths(job, new Path(strInDir));
		FileOutputFormat.setOutputPath(job, new Path(strOutDir));

		String counterFilename = "filter.counters.txt";
		PrintWriter countersOut = new PrintWriter(counterFilename);

		// phase4.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setJarByClass(FilterMapper.class);
		job.setMapperClass(FilterMapper.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setNumReduceTasks(0);
		runJob(countersOut, "filter", job);
		System.out.println("Done: " + jobName);
		System.out.println("Wrote output to directory: " + strOutDir);

		countersOut.close();
	}

	private void pruneGrammar(String strInDir, String strOutDir, FileSystem fs, FeatureManager fman)
			throws IOException, InterruptedException, ClassNotFoundException {

		addFeatureInfoToConf(getConf(), fman);

		requireOption("serializedStats", getConf());
		requireOption("serializedFeatures", getConf());
		requireOption("nbest", getConf());
		int nbest = Integer.parseInt(getConf().get("nbest"));

		Path beamDir = makeTempDir("PhraseDozer-FindCutoffFreq");

		String counterFilename = "pruneGrammar.counters.txt";
		PrintWriter countersOut = new PrintWriter(counterFilename);

		if (nbest == 0) {
			FSDataOutputStream os = FileSystem.get(getConf()).create(new Path(strOutDir));
			PrintWriter out = new PrintWriter(os);
			out.println();
			out.close();
		} else {

			{
				String jobName =
						"Prune Grammar to N-Best Rules (Phase 1/2 -- Determine cutoff frequency)";
				Job job = new Job(getConf(), jobName);
				System.out.println("Starting: " + jobName);

				FileInputFormat.setInputPaths(job, new Path(strInDir));
				FileOutputFormat.setOutputPath(job, beamDir);

				// phase5.setInputFormatClass(KeyValueTextInputFormat.class);
				job.setJarByClass(FindNbestGrammarFreqMapper.class);
				job.setMapperClass(FindNbestGrammarFreqMapper.class);
				job.setNumReduceTasks(0);
				job.setOutputKeyClass(Text.class);
				job.setOutputValueClass(Text.class);
				runJob(countersOut, "findCutoffFreq", job);
				System.out.println("Done: " + jobName);
				System.out.println("Wrote output to directory: " + beamDir);
			}

			System.out.println("Aggregating frequency beams...");

			// Open all beam files on single node, find cutoff, and store in var
			// for
			// next stage
			Beam<Float> beam = new Beam<Float>(nbest, true);
			for (FileStatus fileStatus : fs.listStatus(beamDir)) {
				BufferedReader in =
						new BufferedReader(new InputStreamReader(fs.open(fileStatus.getPath())));
				String line;
				while ((line = in.readLine()) != null) {
					float freq = Float.parseFloat(line);
					beam.add(freq);
				}
				in.close();
			}
			float minFreq = beam.getWorst();
			getConf().set("minFreq", minFreq + "");

			System.out.println("Final cutoff frequency: " + minFreq);

			{
				String jobName = "Prune Grammar to N-Best Rules (Phase 2/2 -- Prune by frequency)";
				runFullJob(jobName, "pruneGrammar", new Path(strOutDir),
						PruneGrammarByFreqMapper.class, null, countersOut, new Path(strInDir));
			}
		}
		countersOut.close();
	}

	private void runFullJob(String jobName, String shortName, Path outDir,
			Class<? extends Mapper<LongWritable, Text, Text, Text>> mapperClass,
			Class<? extends Reducer<Text, Text, Text, Text>> reducerClass, PrintWriter countersOut,
			Path... inDir)

	throws IOException, InterruptedException, ClassNotFoundException {

		Job job = new Job(getConf(), jobName);
		System.out.println("Starting: " + jobName);

		FileInputFormat.setInputPaths(job, inDir);
		FileOutputFormat.setOutputPath(job, outDir);

		// phase5.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setJarByClass(mapperClass);
		job.setMapperClass(mapperClass);
		if (reducerClass != null) {
			job.setReducerClass(reducerClass);
		} else {
			job.setNumReduceTasks(0);
		}
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		runJob(countersOut, shortName, job);
		System.out.println("Done: " + jobName);
		System.out.println("Wrote output to directory: " + outDir);
	}

	private void prune(String strInDir, String strOutDir, FileSystem fs, FeatureManager fman)
			throws IOException, InterruptedException, ClassNotFoundException {

		addFeatureInfoToConf(getConf(), fman);

		requireOption("minCount", getConf());
		requireOption("sortFeatureNames", getConf());
		requireOption("scalingFactors", getConf());
		requireOption("ambiguityFactor", getConf());
		requireOption("serializedStats", getConf());
		requireOption("serializedFeatures", getConf());

		String jobName = "Prune Phrases by Fan-out";
		Job job = new Job(getConf(), jobName);
		System.out.println("Starting: " + jobName);

		String counterFilename = "prune.counters.txt";
		PrintWriter countersOut = new PrintWriter(counterFilename);

		FileInputFormat.setInputPaths(job, new Path(strInDir));
		FileOutputFormat.setOutputPath(job, new Path(strOutDir));

		// phase5.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setJarByClass(PruneMapper.class);
		job.setMapperClass(PruneMapper.class);
		job.setReducerClass(PruneReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		runJob(countersOut, "prune", job);
		System.out.println("Done: " + jobName);
		System.out.println("Wrote output to directory: " + strOutDir);

		countersOut.close();
	}

	private Path makeTempDir(String appName) {
		Path tempDirB =
				new Path("tmp/" + appName + "-"
						+ Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));
		return tempDirB;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new PhraseDozer(), args);
		System.exit(res);
	}

}
