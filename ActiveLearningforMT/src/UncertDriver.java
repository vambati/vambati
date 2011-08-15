
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import crowdsource.validate.TranslationValidator;
import data.CorpusData;
import data.UDS;
import model.smt.Dictionary;
import model.smt.PhraseTable;
import model.smt.TranslationLEXICON;
import query.sentence.uncert.*;
import utils.MyNLP;
import utils.StringUtils;

public class UncertDriver {

	public static int MAX_LENGTH = 3; 
	public String ngFile = "/chicago/usr6/vamshi/ActiveLearning/Sp-En/BTEC.sp.new.counts.sorted";
	public String ptableFile = "/chicago/usr6/vamshi/ActiveLearning/Sp-En/expts/rand/3/working-dir/model/phrase-table.0-0.gz";
	//public String ngFile = "/chicago/usr6/vamshi/ActiveLearning/Sp-En/1.gram.txt";
	
	public static void main(String args[]){
	
		String file = args[0]; // CorpusData File 
		String round = args[1]; // Round number
		String dev = args[2]; // Held-out devset
		
		String lex1 = round+"/working-dir/model/lex.e2f";
		String lex2 = round+"/working-dir/model/lex.f2e";
		String ptable = round+"/working-dir/model/phrase-table.gz";
//		
//		lex1 = round+"/working-dir/model/lex.0-0.n2f";
//		lex2 = round+"/working-dir/model/lex.0-0.f2n";
//		ptable = round+"/working-dir/model/phrase-table.0-0.gz";
		
// Data 
//		CorpusData ch = new CorpusData("corpusFile");
//		UDS ul = new UDS(ch, 0);
		
		PhraseTable pt = new PhraseTable(ptable);
		// Phrase Table
		// 1. Type/ Token Ratio (src,tgt) 
		// 2. Entropy held-out dataset (src,tgt)
		// 3. Perplexity training dataset
		// 4. Mutual Information (significance or chi-square test)
		
		HashMap<String,Integer> dlog = StringUtils.filePhrases(dev, MAX_LENGTH);
		double ttr = pt.typetok(dev);
		double pFE = pt.computeFileEntropy(dlog);
		double pE = pt.computeEntropy();
		double ppl = pt.computePerplexity();
		
		// Lexicon
		// 1. Type/ Token Ratio (src,tgt) 
		// 2. Entropy held-out dataset (src,tgt)
		// 3. Perplexity training dataset
		// 4. Mutual Information (Moore style, significance or chi-square test)
		
		TranslationLEXICON lt = new TranslationLEXICON(lex1,lex2);
		double fE1 = lt.computeFileEntropy_SGT(dlog);
		double fE2 = lt.computeFileEntropy_TGS(dlog);
		// double E = lt.computeEntropy();

		System.err.println(ttr+"\t"+pFE+"\t"+fE1+"\t"+fE2+"\t"+ppl);
		
//		int k =Integer.parseInt(args[1]);
//		int type = Integer.parseInt(args[2]);		
		//PhraseIG pig = new PhraseIG(ptable); 			// Phrasal InformationGain Entropy 
//		pig.MODEL.sortModel(k,type);
		
	}
	
	public void hypEval(String srcFile, String hypFile,String refFile) { 
		
		TranslationValidator v = new TranslationValidator();
		try {
			BufferedReader or = new BufferedReader(new FileReader(hypFile));
			BufferedReader gr = new BufferedReader(new FileReader(refFile));
			BufferedReader sr = new BufferedReader(new FileReader(srcFile));
			int i=0,j=0; 
			String src = ""; String trans=""; String tgt = "";
			
			while((trans = or.readLine()) != null){
				// trans is from MOSES nbest fromat n=1
				String transArr[] = trans.split(" \\|\\|\\| ");
				String hyp="",featscore=""; 
				double hypscore = -1.0,pscore1=0,pscore2=0,lscore1 = 0,lscore2=0,lmscore=0;
				double length = 0; 
				
				if(transArr.length==4){
					hyp = transArr[1];
					featscore =transArr[2]; 
					hypscore = Double.parseDouble(transArr[3]);
					String[] scoreArr = featscore.split("(\\w)+: ");
					String d = scoreArr[1];
					
					String lm = scoreArr[2];
					lmscore = Double.parseDouble(lm);
					
					String tm = scoreArr[3];
					String[] tmArr = tm.split("\\s+");
					// TM Scores individual 
					pscore1 = Double.parseDouble(tmArr[0]);
					pscore2 = Double.parseDouble(tmArr[2]);
					lscore1 = Double.parseDouble(tmArr[1]);
					lscore2 = Double.parseDouble(tmArr[3]);
					
					String len = scoreArr[4];
					length = Double.parseDouble(len);
					length = length *-1;
				}else{
					System.err.println("ERROR:"+trans);
					System.exit(0);
				}
				tgt = gr.readLine();
				src = sr.readLine();
								
				hyp = MyNLP.removePunctuation(hyp);
				src = MyNLP.removePunctuation(src);
				tgt = MyNLP.removePunctuation(tgt);
				
				double score = v.score(hyp,tgt);
				// double ptable_score = lt.getPhraseProbability_SGT(src, tgt, true) + lt.getPhraseProbability_TGS(src, tgt, true);
				 
				// Normalize all 
				lscore1/=length; lscore2/=length;
				pscore1/=length; pscore2/=length;
				lmscore/=length; hypscore/=length;
				
				System.out.println(score+"\t"+lscore1+"\t"+lscore2+"\t"+pscore1+"\t"+pscore2+"\t"+lmscore+"\t"+hypscore);
				
				double ptable_score = (pscore1 + pscore2 )/(2*length);
				double final_score = hypscore / length;
				
				//if(score>=0.7){
				//if((ptable_score>=-0.1) && (ptable_score!=0)){
				if(final_score>=-15){
					// System.out.println(hyp);
					// System.out.println(src+"\n"+hyp+"\n"+tgt+"\n"+score+"\n"+featscore+"\t"+lscore1+"\t"+lscore2+"\t"+ptable_score+"\t"+hypscore+"\n------------\n");
					i++;
				}else{
					// System.out.println(tgt);
					j++;
				}
			}
			// Print All 
			//System.err.println("Yes:"+i+"\tNo:"+j+"\t"+E+"\t"+fE+"\t"+pE+"\t"+pfE);
			System.err.println("Yes:"+i+"\tNo:"+j);
			
			or.close(); gr.close();
		}catch(Exception e){
			System.err.println(e.toString());
		}
	}
}
