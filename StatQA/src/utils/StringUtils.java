
package utils;
import java.util.*;

public class StringUtils implements Cloneable
{
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
           return source.replaceAll("\\s+", " ");
    }

    /* remove all superfluous whitespaces in source string */
    public static String trim(String source) {
    	//source.replaceAll("\\n", " ");
    	//source.replaceAll("\\r", " ");
        return rtrim(ltrim(itrim(source)));
    }

    /* Count number of tokens in a String */
    public static int wordCount(String source) {
        source = rtrim(ltrim(itrim(source)));
        StringTokenizer st = new StringTokenizer(source);
        return st.countTokens();
    }
}

