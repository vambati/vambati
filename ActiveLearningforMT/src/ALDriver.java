import model.smt.PhraseTable;
import query.SentenceSelection;

public class ALDriver {
	public static void main(String args[]){
	   	  	   
		String tag= args[0];
		
		String srcFile = "s.ssd."+tag;
		String hypFile = "s.ssd."+tag+".trans"; 
		String refFile = "e.ssd."+tag;
		
		String lex1 = tag+"/working-dir/model/lex.0-0.n2f";
		String lex2 = tag+"/working-dir/model/lex.0-0.f2n";
		String ptable = tag+"/working-dir/model/phrase-table.0-0.gz";
		
		PhraseTable pt = new PhraseTable(ptable);
		pt.computeEntropy();
		pt.sortModel(10000, 0); // Sort and show top 100 by ENTROPY 
		
		for(String s:pt.PTABLE.keySet()){
			System.out.println(pt.PTABLE.get(s).toString());
		}
 	}
}
