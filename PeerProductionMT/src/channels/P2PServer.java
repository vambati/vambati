package channels; 

import java.io.*;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

public class P2PServer {
	protected static String xsdFile = "/afs/cs/user/vamshi/workspace-eclipse/PeerProductionMT/data/xml/translations.xsd";
	
	// ######## GLOBAL #############
	public static String SOURCE_LANGUAGE = "Spanish";
	public static String TARGET_LANGUAGE = "English";
	
	public static String GOLD_FILE = "/mnt/1tb/usr6/vamshi/CROWD_DATA/PEER_PRODUCTION/generic/gold-standard.ur";
	
	// Turkers blocked 
	public static String blockFile = "/mnt/1tb/usr6/vamshi/CROWD_DATA/PEER_PRODUCTION/ur-en/turkers.block";
	
	//########### MTurk Parameters ######################
	public static String propertiesFile = "/afs/cs/user/vamshi/mturk/vamshi.properties";
	static String templateXmlFile =   "/afs/cs/user/vamshi/workspace-eclipse/PeerProductionMT/data/mturk/translation_external.xml";
	public static String hitPropertiesFile = "/afs/cs/user/vamshi/workspace-eclipse/PeerProductionMT/data/mturk/translation.properties";

	// External URL parameters for MTURK posting 
	// public static  String externalURL = "http://tera-3.ul.cs.cmu.edu/cgi-bin/crowdeagle/translate.cgi?"; // Spanish
	// public static String externalURL = "http://tera-3.ul.cs.cmu.edu/cgi-bin/crowdeagle/translate-ur.cgi?"; // English to Urdu (with transliteration)
	//   public static String externalURL = "http://tera-3.ul.cs.cmu.edu/cgi-bin/crowdeagle/translate-ur2.cgi?"; // Urdu
	// public static String externalURL = "http://tera-3.ul.cs.cmu.edu/cgi-bin/crowdeagle/translate-all.cgi?"; // No country restriction
	
	public static String externalURL = "http://tera-3.ul.cs.cmu.edu/cgi-bin/crowdeagle/translate-eval.cgi?"; // Translation and Evaluation together	   
	//###################################

	//############# Directory Structure ###########
	// Spanish-English
	// public static String dirPath = "/mnt/1tb/usr6/vamshi/PEER_PRODUCTION/";
	// public static String imagesDirPath = "/afs/cs.cmu.edu/user/vamshi/www/p2p/"; 
	// public static String CURRENT_LOG = "/mnt/1tb/usr6/vamshi/PEER_PRODUCTION/CURRENT_LOG";
	
	// Urdu-English 
	public static String dirPath = "/mnt/1tb/usr6/vamshi/CROWD_DATA/PEER_PRODUCTION/ur-en";
	// public static String imagesDirPath = "/afs/cs.cmu.edu/user/vamshi/www/p2p/ur/";
	public static String imagesDirPath = "/mnt/1tb/usr6/vamshi/CROWD_DATA/PEER_PRODUCTION/images/ur/"; // On Chicago
	public static String CURRENT_LOG = "/mnt/1tb/usr6/vamshi/CROWD_DATA/PEER_PRODUCTION/ur-en/CURRENT_LOG";

	//#####################################
	// Protocol strings (Depricated by email channel!)
	protected static int PORT_NUMBER = 6789; 
	public static String start = "BBN-CMU-START";
	public static String end = "BBN-CMU-END";
	
	protected SchemaFactory factory = null;
	protected static Validator validator = null; 
	
	public P2PServer(){
		try { 
	        // 1. Lookup a factory for the W3C XML Schema language
	         factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
	        
	        // 2. Compile the schema. 
	        // Here the schema is loaded from a java.io.File, but you could use 
	        // a java.net.URL or a javax.xml.transform.Source instead.
	        File schemaLocation = new File(xsdFile);
	        Schema schema = factory.newSchema(schemaLocation);
	         validator = schema.newValidator();
		}
		catch(Exception e){
		}
	}
	    
    public static boolean isValid(String xmlFile){
        // 4. Parse the document you want to check.
        Source source = new StreamSource(xmlFile);
        
        // 5. Check the document
        try {
            validator.validate(source);
            System.err.println("---"+ xmlFile + " is valid.");
        }
        catch (Exception ex) {
            System.err.println(xmlFile + " is not valid because ");
            System.err.println(ex.toString());
            return false;
        }     	
        return true;
    }
    
    public static void main(String args[]){
    	P2PServer s = new P2PServer();
    	s.isValid(args[0]);
    }
    
	public static synchronized void createImages(String imagesDirPath,String bbnFile) {
		System.err.println("Creating images using Pango-View ");
  		
		try {
		  BufferedReader br = new BufferedReader( new FileReader(bbnFile));	      
	      int i=0;
	      String hitLine = ""; 
	      hitLine = br.readLine(); // Skip HEADER LINE 
	      while( (hitLine = br.readLine()) != null)
	      {
	    	  String[] toks = hitLine.split("\\t");
		        String id = toks[0]; 
		        String sen = toks[1]; 
		        String imgFile = imagesDirPath+"/"+id+".png";
		    
		        try
		        {
		        	int width = sen.length() * 20;
		        	int height = 90; 
		        		
			  // String[] senarr = {"convert" ,"-font", "/usr/share/fonts/bitstream-vera/Vera.ttf","-encoding","utf8", "-pointsize", "24", "label:"+sen,imgFile};
//		  String[] senarr = {"convert" ,"-font", "/afs/cs.cmu.edu/user/vamshi/nn.ttf","-encoding","utf8", "-pointsize", "20","-size",width+"x"+height, "label:"+sen,imgFile};
//String[] senarr = {"/shared/code/render-text-to-image", "--font=NONE", "--size=14", "--text='" + sen + "'", "--output=" + imgFile};
String[] senarr = {"/shared/code/render-text-to-image", "--font=NONE", "--size=20", "--text=" + sen, "--output=" + imgFile};
			        
		            // Create image from temp file
		            Runtime rt = Runtime.getRuntime(); 
			        System.err.println("Creating image for "+imgFile);			        
			        //System.err.println(sen);
			        Process proc = rt.exec(senarr);
			        
			        proc.waitFor();
			        int exitVal = proc.exitValue();
		            System.err.println("Process exitValue: " + exitVal);
		        } catch (Throwable t)
		          {
		            t.printStackTrace();
		            System.exit(0);
		          }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
