package drivers;

import taruDecoder.features.LexicalWeightFeatureFunction;

/** 
 * First param: ARPA format language model file
 *  */

public class LexProbDriver
{
	public static void main(String[] args) throws Exception
	{
		//String lmFile = args[0];
		
		String sgtlex = "/chicago/usr0/ghannema/alignments/align12/fe/108-03-11.023855.ghannema.actual.ti.final";
		String tgslex = "/chicago/usr0/ghannema/alignments/align12/ef/108-03-11.023729.ghannema.actual.ti.final";
		
		String s1 = "NULL My colleague Mrs Armonie Bordes cuttingly pointed out yesterday that only a minority benefit from deploying community capital and pension premiums for entrepreneurial risks"; 
		String s2 = "NULL The Charter will, consequently, be used more to enshrine the current practices, including the reactionary practices, of the national States than to represent genuine progress";
		String t = "caisse";
		// TransSGT: -6.51302, TransTGS: -6.59351
		
		System.out.println("Loading lexicon...");
		//TranslationLexicon sgt = new TranslationLexicon(sgtlex);
		//double sprob = sgt.getPhraseProbability(s,t);
		//System.out.println("SgT log prob is: "+sprob);
		
		System.out.println("Loading second");
		LexicalWeightFeatureFunction.sgtlexfile = sgtlex;
		LexicalWeightFeatureFunction.tgslexfile = tgslex;
		LexicalWeightFeatureFunction.loadLex();
		
		double tprob = LexicalWeightFeatureFunction.getPhraseProbability_TGS(s1,t,false);
		System.out.println("TGS : t - s1: "+tprob);
		double tprob2 = LexicalWeightFeatureFunction.getPhraseProbability_TGS(s2,t,false);
		System.out.println("TGS: t - s2: "+tprob2);
		
		tprob = LexicalWeightFeatureFunction.getPhraseProbability_SGT(s1,t,false);
		System.out.println("SGT : t - s1: "+tprob);
		tprob2 = LexicalWeightFeatureFunction.getPhraseProbability_SGT(s2,t,false);
		System.out.println("SGT: t - s2: "+tprob2);
	}
}
