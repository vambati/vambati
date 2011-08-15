import java.io.BufferedReader;
import java.io.FileReader;

/*
 * using the Google Translation API downloaded from - 
 * http://code.google.com/p/google-api-translate-java/
 */
import com.google.api.translate.Language;
import com.google.api.translate.Translate;

public class GoogleTranslate {
  public static void main(String[] args) throws Exception {
    // Set the HTTP referrer to your website address.
    Translate.setHttpReferrer("LTI CMU");

 	   if(args.length!=1) 
       { 
           System.err.println("Usage: java GoogleTranslate <inputFile>");
           System.err.println("SrcLang: Spanish, TgtLang:English");
           System.exit(0); 
       }       

	  String inputFile = args[0];
	  
	System.err.println("Translating:"+inputFile);
	  try {
			BufferedReader rbr = new BufferedReader(new FileReader(inputFile));
			String line = "";
			while((line = rbr.readLine()) != null){
				String[] toks = line.split("\\t");
				if(toks.length==2){
					String src = toks[1];
					System.err.println("Spanish:"+src);
				    String translatedText = Translate.execute(src, Language.SPANISH, Language.ENGLISH);
				    // String translatedText = Translate.execute(src, Language.ENGLISH, Language.SPANISH);
				    System.err.println("English:"+translatedText);
				    System.out.println(translatedText);
				}else{
					System.out.println(line);
				}
			}
		rbr.close();
	  }catch(Exception e){}
  }
}
