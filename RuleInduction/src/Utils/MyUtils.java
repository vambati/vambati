/*
 * Desc: Rule Learning using Version Spaces 
 *
 * Author: Vamshi Ambati 
 * Email: vamshi@cmu.edu 
 * Carnegie Mellon University 
 * Date: 27-Jan-2007
 */

package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.StringTokenizer;

public class MyUtils implements Cloneable {
	/* remove leading whitespace */
	public static String ltrim(String source) {
		return source.replaceAll("^\\s+", "");
	}

	/* remove trailing whitespace */
	public static String rtrim(String source) {
		return source.replaceAll("\\s+$", "");
	}

	/* replace multiple whitespaces between words with single blank */
	public static String itrim(String source) {
		// return source.replaceAll("\\b\\s{2,}\\b", " ");
		return source.replaceAll("\\s+", " ");
	}

	/* remove all superfluous whitespaces in source string */
	public static String trim(String source) {
		return rtrim(ltrim(itrim(source)));
	}

	/* Count number of tokens in a String */
	public static int wordCount(String source) {
		source = rtrim(ltrim(itrim(source)));
		StringTokenizer st = new StringTokenizer(source);
		return st.countTokens();
	}

	public static int getHashCode(String str) {
		return (str.hashCode() & 1048575);
	}

	// Convert punct according to tiburon needs
	public static String convertNodeTypeForTiburon(String orig) {
		if (orig.equalsIgnoreCase(".") || orig.equalsIgnoreCase("$.")) {
			return "PUNCT-DOT";
		} else if (orig.equalsIgnoreCase(",") || orig.equalsIgnoreCase("$,")) {
			return "PUNCT-COMMA";
		} else if (orig.equalsIgnoreCase(":") || orig.equalsIgnoreCase("$:")) {
			return "PUNCT-COLON";
		} else if (orig.equalsIgnoreCase("$*LRB*")) {
			return "-LRB-";
		}
		return orig;
	}

	public static String convertLiteralForTiburon(String orig) {
		if (orig.equalsIgnoreCase(".")) {
			return "DOT";
		} else if (orig.equalsIgnoreCase(",")) {
			return "COMMA";
		} else if (orig.equalsIgnoreCase(":")) {
			return "COLON";
		} else if (orig.equalsIgnoreCase(";")) {
			return "SEMICOLON";
		} else if (orig.equalsIgnoreCase("?")) {
			return "QUESMARK";
		} else if (orig.equalsIgnoreCase("!")) {
			return "EXCLAIM";
		} else if (orig.equalsIgnoreCase("\"")) {
			return "''";
		}
		if (orig.contains(".")) {
			// System.out.println("Before . :"+orig);
			orig = orig.replaceAll("([^\\.]*)\\.([^\\.]*)", "$1-DOT-$2");
			// System.out.println("After . :" + orig);
		}
		if (orig.contains(",")) {
			// System.out.println("Before , :" + orig);
			orig = orig.replaceAll("([^,]*),([^,]*)", "$1-COMMA-$2");
			// System.out.println("After , :" + orig);
		}
		if (orig.contains("%")) {
			// System.out.println("Before % :" + orig);
			orig = orig.replaceAll("([^%]*)%([^%]*)", "$1-PERCENT-$2");
			// System.out.println("After % :" + orig);
		}
		return orig;
	}

	public static String[] tokenize(String str, String delims) {
		StringTokenizer tok = new StringTokenizer(str, delims);
		String[] toks = new String[tok.countTokens()];
		for (int i = 0; i < toks.length; i++) {
			toks[i] = tok.nextToken();
		}
		return toks;
	}

	public static float[] tokenizeFloats(String str, String delims) {
		StringTokenizer tok = new StringTokenizer(str, delims);
		float[] toks = new float[tok.countTokens()];
		for (int i = 0; i < toks.length; i++) {
			toks[i] = Float.parseFloat(tok.nextToken());
		}
		return toks;
	}

	public static int countOccurancesOfSingleDelim(final String searchable, final String substring) {
		int nOccurances = 0;
		int nBegin = searchable.indexOf(substring);
		while (nBegin != -1) {
			nOccurances++;
			final int nEnd = nBegin + substring.length();
			nBegin = searchable.indexOf(substring, nEnd);
		}
		return nOccurances;
	}

	public static String[] split(String str, String delim, int nMaxSplits) {

		final int nOccurances = Math.min(countOccurancesOfSingleDelim(str, delim) + 1, nMaxSplits);
		final String[] tokens = new String[nOccurances];

		// begin and end positions OF THE LAST DELIM FOUND
		int nBegin = str.indexOf(delim);
		int nEnd = 0;

		for (int i = 0; i < nOccurances - 1; i++) {
			tokens[i] = str.substring(nEnd, nBegin);
			nEnd = nBegin + delim.length();

			nBegin = str.indexOf(delim, nEnd);
		}

		if (nOccurances > 0) {
			tokens[tokens.length - 1] = str.substring(nEnd);
		}

		return tokens;
	}
	
	private static final DecimalFormat expFloatFormat = new DecimalFormat("####.#####E0");
	private static final DecimalFormat shortFloatFormat = new DecimalFormat("####.#####");
	public static String formatFloat(float f) {
		if(f < 0.01 || f > 9999) {
			return expFloatFormat.format(f);
		} else {
			return shortFloatFormat.format(f);
		}
	}

	public static String untokenize(String delim, float... args) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length - 1; i++) {
			builder.append(formatFloat(args[i]));
			builder.append(delim);
		}
		if (args.length > 0) {
			builder.append(formatFloat(args[args.length - 1]));
		}
		return builder.toString();
	}

	public static String untokenize(String delim, String... args) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length - 1; i++) {
			builder.append(args[i]);
			builder.append(delim);
		}
		if (args.length > 0) {
			builder.append(args[args.length - 1]);
		}
		return builder.toString();
	}

	public static String[] toArray(Collection<String> it) {
		String[] arr = new String[it.size()];
		int i = 0;
		for (String s : it) {
			arr[i] = s;
			i++;
		}
		return arr;
	}

	public static String readLine(File f) throws FileNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		String line = in.readLine();
		in.close();
		return line;
	}
}
