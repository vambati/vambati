package Scoring.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import Scoring.RuleException;
import Scoring.ScoreableRule;
import Scoring.ScoreableRule.Type;
import chaski.proc.extract.SentenceAlignment;

public class MultiExtractorMapper extends Mapper<LongWritable, Text, Text, Text> {

	private KoehnPhraseExtractor moses;

	private Counter sentenceCount;
	private Counter hieroCount;
	private Counter koehnPhraseCount;

	public static class StdoutWriter implements RuleWriter {
		public int nPhrases = 0;
		public int nHiero = 0;

		@Override
		public void writeRule(ScoreableRule rule) throws RuleException {
			System.out.println(rule.toHadoopRecordString());
			if (rule.type == Type.PHRASE) {
				nPhrases++;
			} else {
				nHiero++;
			}
		}
	};
	public final StdoutWriter STDOUT_WRITER = new StdoutWriter();

	public class HadoopRuleWriter implements RuleWriter {

		private final Counter counter;
		private final Context context;
		private final boolean doWrite;

		public HadoopRuleWriter(Context context, Counter koehnPhraseCount, boolean doWrite) {
			this.context = context;
			this.counter = koehnPhraseCount;
			this.doWrite = doWrite;
		}

		@Override
		public void writeRule(ScoreableRule rule) throws RuleException, IOException,
				InterruptedException {

			counter.increment(1);
			if (doWrite) {
				context.write(new Text(rule.toHadoopRecordString()), new Text());
			}
		}
	};

	public MultiExtractorMapper(int maxPhraseLength) {
		HierarchicalPhraseEmitter hiero =
				new HierarchicalPhraseEmitter(STDOUT_WRITER, 2, 5, false, false, false, true);
		moses = new KoehnPhraseExtractor(maxPhraseLength, STDOUT_WRITER, hiero);
	}

	public MultiExtractorMapper() {
		System.err.println("Initializing for Hadoop...");
	}

	@Override
	public void setup(final Context context) {

		StrictHadoopConfiguration conf = new StrictHadoopConfiguration(context.getConfiguration());

		int maxPhraseLength = conf.getInt("maxPhraseLength");
		int maxNonterms = conf.getInt("maxNonterms");
		int maxRuleSizeF = conf.getInt("maxRuleSizeF"); //
		boolean allowAdjacentNonterms = conf.getBoolean("allowAdjacentNonterms");
		boolean allowAbstractUnaryTargets = conf.getBoolean("allowAbstractUnaryTargets");
		boolean allowUnalignedBoundariesForInitialPhrase = conf.getBoolean("allowUnalignedBoundariesForInitialPhrase"); //
		boolean requireOneWordAlignment = conf.getBoolean("requireOneWordAlignment"); //
		boolean writeKoehnPhrases = conf.getBoolean("writeKoehnPhrases");

		sentenceCount = context.getCounter("COUNT", "Sentences");
		hieroCount = context.getCounter("COUNT", "Hiero Rules");

		final RuleWriter hieroWriter = new HadoopRuleWriter(context, hieroCount, true);
		final RuleWriter koehnWriter;
		if (writeKoehnPhrases) {
			koehnPhraseCount = context.getCounter("COUNT", "Koehn Phrases");
			koehnWriter = new HadoopRuleWriter(context, koehnPhraseCount, true);
		} else {
			koehnPhraseCount = context.getCounter("COUNT", "Koehn Phrases Ignored");
			koehnWriter = new HadoopRuleWriter(context, koehnPhraseCount, false);
		}

		HierarchicalPhraseEmitter hiero =
				new HierarchicalPhraseEmitter(hieroWriter, maxNonterms, maxRuleSizeF,
						allowAdjacentNonterms, allowAbstractUnaryTargets,
						allowUnalignedBoundariesForInitialPhrase, requireOneWordAlignment);
		moses = new KoehnPhraseExtractor(maxPhraseLength, koehnWriter, hiero);
	}

	@Override
	public void map(LongWritable dummy, Text value, final Context context) throws IOException,
			InterruptedException {

		try {
			final String strValue = value.toString().trim();
			sentenceCount.increment(1);
			processLine(strValue);
		} catch (RuleException e) {
			throw new RuntimeException(e);
		}
	}

	private void processLine(String line) throws IOException, RuleException, InterruptedException {

		System.err.println("Processing record: " + line);
		String[] fields = line.split(" \\|\\|\\| ");

		if (fields.length < 4) {
			throw new RuntimeException("Less than 4 fields in entry: " + line.toString().trim());
		}

		String eLine = fields[0].trim();
		String fLine = fields[1].trim();
		// String eTreeLine = fields[2].trim();
		String align = fields[3].trim();
		// String fTreeLine = fields[4].trim();

		align = GhkmRuleExtractionMapper.convertAlignment(align, true);

		SentenceAlignment sentence = new SentenceAlignment(eLine, fLine, align);
		moses.extract(sentence);
	}

	public static void main(String[] args) throws Exception {

		MultiExtractorMapper multi = new MultiExtractorMapper(7);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = in.readLine()) != null) {
			multi.processLine(line);
		}
		System.err.println("Extracted " + multi.STDOUT_WRITER.nPhrases + " phrases and "
				+ multi.STDOUT_WRITER.nHiero + " hierarchical rules");
		in.close();
	}
}
