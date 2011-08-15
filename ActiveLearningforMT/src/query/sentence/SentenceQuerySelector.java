package query.sentence;
import data.*;

public interface SentenceQuerySelector {

	public double computeScore(TranslationEntry e);
}
