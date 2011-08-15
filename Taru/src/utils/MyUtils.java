/*
* Desc: Rule Learning using Version Spaces 
*
* Author: Vamshi Ambati 
* Email: vamshi@cmu.edu 
* Carnegie Mellon University 
* Date: 27-Jan-2007
*/

package utils;
import java.util.*;

public class MyUtils implements Cloneable
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
     //   return source.replaceAll("\\b\\s{2,}\\b", " ");
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
    
    public static int getHashCode(String str){
    	return (str.hashCode() & 1048575);
    }
    
    // Convert punct according to tiburon needs
    public static String convertNodeTypeForTiburon(String orig){
//    	if(orig.equalsIgnoreCase(".") || orig.equalsIgnoreCase("$.")){
//    		return "PUNCT-DOT";
//    	}
//    	else if(orig.equalsIgnoreCase(",") || orig.equalsIgnoreCase("$,")){
//    		return "PUNCT-COMMA";
//    	}
//    	else 
    	if(orig.equalsIgnoreCase(":") || orig.equalsIgnoreCase("$:")){
    		return "PUNCT-COLON";
    	}
//    	else if(orig.equalsIgnoreCase("$*LRB*")){
//    		return "-LRB-";
//    	}
    	return orig;
    }
    
    public static String convertLiteralForTiburon(String orig){
//    	if(orig.equalsIgnoreCase(".")){
//    		return "DOT";
//    	}
//    	else if(orig.equalsIgnoreCase(",")){
//    		return "COMMA";
//    	}
//    	else 
    	if(orig.equalsIgnoreCase(":")){
    		return "COLON";
    	}
//    	else if(orig.equalsIgnoreCase(";")){
//    		return "SEMICOLON";
//    	}
//    	else if(orig.equalsIgnoreCase("?")){
//    		return "QUESMARK";
//    	}
//    	else if(orig.equalsIgnoreCase("!")){
//    		return "EXCLAIM";
//    	}
//    	else if(orig.equalsIgnoreCase("\"")){
//    		return "''";
//    	}
//    	if(orig.contains(".")){
////    		System.out.println("Before . :"+orig);
//    		orig = orig.replaceAll("([^\\.]*)\\.([^\\.]*)","$1-DOT-$2");
////    		System.out.println("After . :" + orig);
//    	}
//    	if(orig.contains(",")){
////    		System.out.println("Before , :" + orig);
//    		orig = orig.replaceAll("([^,]*),([^,]*)","$1-COMMA-$2");
////    		System.out.println("After , :" + orig);
//    	}
//    	if(orig.contains("%")){
////    		System.out.println("Before % :" + orig);
//    		orig = orig.replaceAll("([^%]*)%([^%]*)","$1-PERCENT-$2");
////    		System.out.println("After % :" + orig);
//    	}
    	//return orig.toLowerCase();
    	return orig;
    }
}
