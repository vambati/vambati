package qual;

import java.io.*;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;

public class GoogleTranslation {
  public static void main(String[] args) throws Exception {
 	   if(args.length!=1) 
       { 
           System.err.println("Usage: java GoogleTranslate <inputFile.BBN>");
           System.err.println("SrcLang: Spanish, TgtLang:English");
           System.exit(0); 
       }
 	   translateFile(args[0]);
  }


 public static void translateFile(String inputFile) { 
	// Set the HTTP referrer to your website address.
	Translate.setHttpReferrer("LTI Carnegie Mellon University");
	    
	System.err.println("Translating:"+inputFile);
	  try {
			BufferedReader rbr = new BufferedReader(new FileReader(inputFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile+".google")); 
			
			String line = "";
			while((line = rbr.readLine()) != null){
				String[] toks = line.split("\\t");
				if(toks.length==2){
					String id = toks[0];
					String src = toks[1];
					System.err.println("Urdu:"+src);
					
				    String translatedText = "";
				    try { 
				    	translatedText = Translate.execute(src, Language.URDU, Language.ENGLISH);
				    }catch(Exception te){
				    	System.err.println(te.toString());
				    	translatedText = "NO_TRANSLATION_AVAILABLE";
				    }
				    
				    System.err.println("English:"+translatedText);
				    bw.write(id+"\t"+translatedText+"\n");
					bw.flush();
				}else{
					System.out.println(line);
				}
			}
		rbr.close();
		bw.close(); 
	  }catch(Exception e){
		  System.err.println(e.toString());
		  System.exit(0);
	  }
  }
}
