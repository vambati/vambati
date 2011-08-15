package Scoring.extract;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import chaski.proc.lexicon.LexiconUtils;
import chaski.utils.Pair;
import chaski.utils.lexicon.InMemoryLogCompactLexicon;
import chaski.utils.lexicon.LexiconTable;
import chaski.utils.lexicon.PlainTextLexiconLoader;

public class ChaskiLib {
	
	public static final double PROB_FLOOR=1e-20;

	public static LexiconTable readLexicon(InputStream inp_e, InputStream inp_f) throws NumberFormatException, IOException{
		
		LexiconTable lex = new InMemoryLogCompactLexicon();
		
		PlainTextLexiconLoader ldr = new PlainTextLexiconLoader(inp_e
	    , inp_f,
	    lex);
		ldr.load();
		return lex;
		
	}

	public static Pair<Double, Double> getAlignmentScore(String alignment, String eng, String fre, LexiconTable lexicon){
			String[] engs = eng.trim().split("\\s+");
			String[] fres = fre.trim().split("\\s+");
			
			List<Pair<Integer,Integer>>[] alignToEng = new List[engs.length];
			List<Pair<Integer,Integer>>[] alignToFre = new List[fres.length];
			String[] als = alignment.replace('|',' ').trim().split("\\s+");
			for(String a : als){
				String[] alp = a.split("/");
				int evt = alp.length >1 ? Integer.parseInt(alp[1]) : 1;
				String[] ic = alp[0].split("-");
				int fid = Integer.parseInt(ic[1]);
				int eid = Integer.parseInt(ic[0]);
				
				if(alignToEng[eid]==null){
					alignToEng[eid] = new LinkedList<Pair<Integer,Integer>>();
					alignToEng[eid].add(new Pair<Integer,Integer>(fid,evt));
				}else{
					alignToEng[eid].add(new Pair<Integer,Integer>(fid,evt));
				}
				
				if(alignToFre[fid]==null){
					alignToFre[fid] = new LinkedList<Pair<Integer,Integer>>();
					alignToFre[fid].add(new Pair<Integer,Integer>(eid,evt));
				}else{
					alignToFre[fid].add(new Pair<Integer,Integer>(eid,evt));
				}
			}
			double fscore=1, escore=1;
			// Navigate and get the English score
			for(int i  = 0 ; i<engs.length ; i ++){
				double score = 0;  // score of a word
	//			Map<String,Float> ent = lexe.get(engs[i]);
	//			if(ent==null){
	//				LOG.fatal("ERROR!, corrupted lexicon");
	//			}
				if(alignToEng[i] == null){
					score = lexicon.getProb(engs[i], LexiconUtils.NULL);
					if(score == 0){
						throw new RuntimeException("ERROR!, corrupted lexicon (e2f)"+ engs[i]+" NULL");
					}				
				}else{
					Iterator<Pair<Integer, Integer>> it = alignToEng[i].iterator();
					int size = 0;
					while(it.hasNext()){
						Pair<Integer, Integer> v = it.next();
						String f = fres[v.getFirst().intValue()];
	//					Float prob = ent.get(f);
	//					if(prob==null){
	//						LOG.fatal("ERROR!, corrupted lexicon");
	//					}
						double prob = lexicon.getProb(engs[i], f);
						if(prob == 0){
							throw new RuntimeException("ERROR!, corrupted lexicon(e2f) "+ engs[i]+" " + f);
						}
	//					}else{
	//						LOG.info("See this prob: (e2f) " + engs[i]+" " + f + " " + prob );
	//					}
						double sscore = prob * v.getSecond().intValue();
						size += v.getSecond().intValue();
						score += sscore;
					}
					score /= size;
				}
				escore *= (score > PROB_FLOOR)? score : PROB_FLOOR;
			}
			
			for(int i  = 0 ; i<fres.length ; i ++){
				double score = 0;
	//			Map<String,Float> ent = lexf.get(fres[i]);
	//			if(ent==null){
	//				LOG.fatal("ERROR!, corrupted lexicon");
	//			}
				if(alignToFre[i] == null){
					score = lexicon.getRevProb(fres[i],LexiconUtils.NULL);
					if(score == 0){
						throw new RuntimeException("ERROR!, corrupted lexicon (f2e)"+ fres[i]+" NULL");
					}		
				}else{
					Iterator<Pair<Integer, Integer>> it = alignToFre[i].iterator();
					int size = 0;
					while(it.hasNext()){
						Pair<Integer, Integer> v = it.next();
						String e = engs[v.getFirst().intValue()];
	//					Float prob = ent.get(e);
	//					if(prob==null){
	//						LOG.fatal("ERROR!, corrupted lexicon");
	//					}
						double prob = lexicon.getRevProb(fres[i], e);
						if(prob == 0){
							throw new RuntimeException("ERROR!, corrupted lexicon (f2e)" + fres[i]+" " + e);
						}
						double sscore = prob * v.getSecond().intValue();
						size += v.getSecond().intValue();
						score += sscore;
					}
					score /= size;
				}
				fscore *= (score > PROB_FLOOR)? score : PROB_FLOOR;
			}
			
			return new Pair<Double,Double>(escore,fscore);
			
		}

}
