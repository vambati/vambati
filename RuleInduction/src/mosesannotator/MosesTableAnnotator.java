package mosesannotator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import Rule.Alignment;
import RuleLearner.CorpusEntry;
import RuleLearner.RuleLearner;
import RuleLearner.aligner.AlignerT2SBitSet;
import RuleLearner.extractor.RuleExtractorS2S;
import RuleLearner.extractor.RuleExtractorT2S;
import TreeParser.*;
import Utils.MyUtils;
import Utils.ParseTree;

public class MosesTableAnnotator {
	public static int rulecount = 0; 
	
     public static void main(String[] argv) throws IOException {

    	 if(argv.length!=2){
    		 System.err.println("usage: java Annotator extract-log parse-file");
    		 System.exit(0);	 
    	}
        BufferedReader corpusReader = null ;
        BufferedReader sparseReader = null ;
     	try {		
     		corpusReader= new BufferedReader(new InputStreamReader(new FileInputStream(argv[0])));
     		sparseReader= new BufferedReader(new InputStreamReader(new FileInputStream(argv[1])));
     	}catch(IOException ioe){}

     	String str = "";
     	String sl ="",tl="",type="",align = "",sparsetree="";
     	int i=0;
     	int pcounter=0;
     	Hashtable<Integer,Phraselink> phrases = null;
     	while( (str = corpusReader.readLine())!=null)
     	{
     		if(str.startsWith("LOG: SRC:"))	{
     		     str = str.replaceAll("^LOG: SRC:", ""); 
     		     sl = str;
     		     sl = MyUtils.trim(sl);
     		     i++;
     		}
     		else if(str.startsWith("LOG: TGT:"))	{
     			str = str.replaceAll("^LOG: TGT:", ""); 
     		     tl = str;
     		     tl = MyUtils.trim(tl);
     		}
     		// New line needs to be present at the end in order to ADD
     		else if(str.startsWith("LOG: PHRASES_BEGIN:"))
     		{
     				phrases = new  Hashtable<Integer, Phraselink>();
     		}
     		else if(str.startsWith("LOG: PHRASES_END:")){
     			if((sparsetree = sparseReader.readLine())!=null)
     			{
     				if((sparsetree.equals("NULL")) || (sparsetree.length() == 0))
     				{
     					System.out.println("SParse Error at -"+ i);
     					continue;
     				}
     				else { 
     					annotatePhrases(sl,tl,sparsetree,phrases,i);
     					sl= tl=type=sparsetree="";
     					phrases = null;
     					pcounter=0;
     				}
     			}
     		}
     		else if(str.startsWith("LOG")){
     			// Alignment
     		}
     		else // 0 1 2 3 
     		{
     			String[] nums = str.split("\\s+");
     			Phraselink x = new Phraselink(nums[0],nums[1],nums[2],nums[3]);
     			pcounter++;
     			phrases.put(pcounter,x);
     		}
     	}
     }
     public static void annotatePhrases(String sl,String tl,String sparse,Hashtable<Integer,Phraselink> phrases,int sennum){
    	 //System.out.println("Extracting from:"+sparse);
 		if((sennum % 1000)==0){
			System.err.println(sennum+" sens - "+rulecount+" rules");
		}
			ParseTreeNode tree = null;  
			tree = ParseTree.buildTree(sparse,0); 			
			String checkStr = ParseTree.getString(tree);
			int parsecount = MyUtils.wordCount(checkStr);
			
			String[] sarr = sl.split("\\s+");
			String[] tarr = tl.split("\\s+");
			int tgtlen = tarr.length;
			int srclen = sarr.length;
			
			if(parsecount!=tgtlen) {
				System.out.println("S:"+tl);
				System.out.println("P:"+checkStr);
				System.out.println(tgtlen+":"+parsecount+" Mismatch of Tokenization between Parse and Sentence");
				return;
			}
			
			Hashtable<String,String> nodeSpans = new Hashtable<String, String>();
			ParseTree.getSpans(tree,nodeSpans,0); 
			for(Integer key: phrases.keySet()) {
				Phraselink phr = phrases.get(key);
				String type = "PHR"; // default 
				String srcStr=""; String tgtStr="";  
				for(int i=phr.srcx;i<=phr.srcy;i++) {
					srcStr+=sarr[i]+" ";
				}
				for(int j=phr.tgtx;j<=phr.tgty;j++) {
					tgtStr+=tarr[j]+" ";
				}
				srcStr = MyUtils.trim(srcStr);
				tgtStr = MyUtils.trim(tgtStr);
			
				String spankey = phr.tgtx+"_"+phr.tgty;
				if(nodeSpans.containsKey(spankey)){
					type = nodeSpans.get(spankey);
				}
				System.out.println(type+"\t"+type+"\t"+srcStr+"\t"+tgtStr);
				rulecount++;
			}
     }
}